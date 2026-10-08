package com.ninezero.halo.feature.feed.presentation.reply

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.Constants.Content.MAX_COMMENT_CONTENT_LENGTH
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.feature.feed.domain.delegate.PostMutationDelegate
import com.ninezero.halo.feature.feed.domain.repository.PendingCommentRepository
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import com.ninezero.halo.feature.feed.domain.usecase.GetCachedCommentUseCase
import com.ninezero.halo.feature.feed.domain.usecase.GetCachedPostUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetCachedMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.ObserveMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ReplyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository,
    private val postMutationDelegate: PostMutationDelegate,
    private val pendingCommentRepository: PendingCommentRepository,
    private val getCachedPostUseCase: GetCachedPostUseCase,
    private val getCachedCommentUseCase: GetCachedCommentUseCase,
    private val getCachedMyProfileUseCase: GetCachedMyProfileUseCase,
    private val observeMyProfileUseCase: ObserveMyProfileUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    @param:ApplicationContext private val context: Context
) : ViewModel(), OrbitContainerHost<ReplyState, ReplyState, ReplySideEffect> {

    private val postId: Int = savedStateHandle.get<Int>("postId") ?: 0
    private val parentCommentId: Int? = savedStateHandle.get<Int>("parentCommentId")

    override val container: OrbitContainer<ReplyState, ReplyState, ReplySideEffect> = orbitContainer(
        ReplyState(
            post = if (parentCommentId == null) getCachedPostUseCase(postId) else null,
            parentComment = parentCommentId?.let { getCachedCommentUseCase(it) },
            myProfile = getCachedMyProfileUseCase()
        )
    )

    init {
        loadPost()
        observeMyProfile()
    }

    // 열 때 프로필이 비어 있어도 조회되면 채움
    private fun observeMyProfile() {
        observeMyProfileUseCase()
            .filterNotNull()
            .onEach { profile ->
                intent { reduce { state.copy(myProfile = profile) } }
            }
            .launchIn(viewModelScope)
        if (container.stateFlow.value.myProfile == null) intent { getMyProfileUseCase() }
    }

    fun onIntent(intent: ReplyIntent) {
        when (intent) {
            is ReplyIntent.UpdateReplyContent -> updateReplyContent(intent.content)
            is ReplyIntent.AddMedia -> addMedia(intent.uris)
            is ReplyIntent.RemoveMedia -> removeMedia(intent.uri)
            is ReplyIntent.ClearAllMedia -> clearAllMedia()
            is ReplyIntent.SubmitReply -> submitReply()
            is ReplyIntent.NavigateBack -> navigateBack()
            is ReplyIntent.SnackbarDismissed -> dismissSnackbar()
            is ReplyIntent.ToggleAuthorFollow -> toggleAuthorFollow(intent.authorId, intent.shouldFollow)
        }
    }

    private fun toggleAuthorFollow(authorId: Int, shouldFollow: Boolean) = intent {
        reduce {
            state.copy(
                post = state.post?.takeIf { it.author.id == authorId }
                    ?.let { it.copy(author = it.author.copy(isFollowing = shouldFollow)) } ?: state.post,
                parentComment = state.parentComment?.takeIf { it.author.id == authorId }
                    ?.let { it.copy(author = it.author.copy(isFollowing = shouldFollow)) } ?: state.parentComment
            )
        }
        postMutationDelegate.toggleAuthorFollow(authorId, shouldFollow)
            ?.onFailure {
                // 롤백
                reduce {
                    state.copy(
                        post = state.post?.takeIf { it.author.id == authorId }
                            ?.let { it.copy(author = it.author.copy(isFollowing = !shouldFollow)) } ?: state.post,
                        parentComment = state.parentComment?.takeIf { it.author.id == authorId }
                            ?.let { it.copy(author = it.author.copy(isFollowing = !shouldFollow)) } ?: state.parentComment,
                        snackbarState = SnackbarState(if (shouldFollow) "팔로우 실패" else "팔로우 취소 실패")
                    )
                }
            }
    }

    private fun loadPost() = intent {
        postRepository.getPostById(postId)
            .onSuccess { post ->
                reduce { state.copy(post = post) }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        snackbarState = SnackbarState(error.message ?: "포스트를 불러올 수 없습니다")
                    )
                }
            }
    }

    private fun updateReplyContent(content: String) = blockingIntent {
        reduce { state.copy(replyContent = content) }
    }

    private fun addMedia(uris: List<Uri>) = intent {
        val currentUris = state.selectedMediaUris
        val availableSlots = MAX_COMMENT_MEDIA - currentUris.size

        if (availableSlots <= 0) {
            reduce {
                state.copy(snackbarState = SnackbarState("미디어는 최대 ${MAX_COMMENT_MEDIA}개까지 첨부 가능합니다"))
            }
            return@intent
        }

        // 용량 검증
        val validUris = uris.take(availableSlots).filter { uri ->
            val fileInfo = MediaUtils.getFileInfo(context, uri) ?: return@filter false

            if (fileInfo.fileSize > MAX_COMMENT_MEDIA_SIZE) {
                reduce {
                    state.copy(snackbarState = SnackbarState("파일은 최대 10MB까지 업로드 가능합니다"))
                }
                return@filter false
            }
            // 영상은 서버가 받는 형식만
            val mimeType = MediaUtils.resolveMimeType(context, uri)
            if (mimeType.startsWith("video/") && mimeType !in MediaUtils.ALLOWED_VIDEO_TYPES) {
                reduce {
                    state.copy(snackbarState = SnackbarState("지원하지 않는 비디오 형식입니다"))
                }
                return@filter false
            }
            true
        }

        if (validUris.isEmpty()) return@intent

        // 검사하는 사이 지운 항목이 되살아나지 않게 최신 목록에 붙임
        reduce {
            val newUris = (state.selectedMediaUris + validUris).distinct()
            if (newUris.size > MAX_COMMENT_MEDIA) {
                state.copy(
                    selectedMediaUris = newUris.take(MAX_COMMENT_MEDIA),
                    snackbarState = SnackbarState("미디어는 최대 ${MAX_COMMENT_MEDIA}개까지 첨부 가능합니다")
                )
            } else {
                state.copy(selectedMediaUris = newUris)
            }
        }
    }

    companion object {
        private const val MAX_COMMENT_MEDIA = 10
        private const val MAX_COMMENT_MEDIA_SIZE = 10 * 1024 * 1024L  // 10MB
    }

    private fun removeMedia(uri: Uri) = intent {
        val updatedUris = state.selectedMediaUris.filter { it != uri }
        reduce { state.copy(selectedMediaUris = updatedUris) }
    }

    private fun clearAllMedia() = intent {
        reduce { state.copy(selectedMediaUris = emptyList()) }
    }

    private fun submitReply() = intent {
        if (state.isSubmitting) return@intent

        val content = state.replyContent.trim()
        if (content.isBlank()) return@intent

        if (content.length > MAX_COMMENT_CONTENT_LENGTH) {
            reduce {
                state.copy(snackbarState = SnackbarState("내용은 ${MAX_COMMENT_CONTENT_LENGTH}자를 초과할 수 없습니다"))
            }
            return@intent
        }

        val myProfile = state.myProfile ?: getMyProfileUseCase().getOrNull() ?: run {
            reduce { state.copy(snackbarState = SnackbarState("프로필을 불러올 수 없습니다")) }
            return@intent
        }

        // 연타/중복 가드
        reduce { state.copy(isSubmitting = true) }

        val mediaUris = state.selectedMediaUris.map { it.toString() }.ifEmpty { null }

        // 대기 댓글로 저장, 업로드는 워커
        try {
            pendingCommentRepository.addPendingComment(
                postId = postId,
                content = content,
                parentCommentId = parentCommentId,
                authorId = myProfile.userId,
                authorUsername = myProfile.username,
                authorDisplayName = myProfile.displayName,
                authorAvatarUrl = myProfile.avatarUrl,
                mediaUris = mediaUris
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 미디어 사본 실패는 화면에 남아 안내
            val message = if (e is IOException) "첨부한 사진·영상을 저장하지 못했습니다" else "게시 실패"
            reduce { state.copy(isSubmitting = false, snackbarState = SnackbarState(message)) }
            return@intent
        }

        // 즉시 뒤로가기
        postSideEffect(ReplySideEffect.ReplySubmitted)
    }

    private fun navigateBack() = intent {
        postSideEffect(ReplySideEffect.NavigateBack)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
