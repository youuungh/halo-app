package com.ninezero.halo.feature.feed.presentation.comment_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.feed.domain.delegate.PostMutationDelegate
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.CommentUploadEvent
import com.ninezero.halo.feature.feed.domain.repository.CommentUploadEventRepository
import com.ninezero.halo.feature.feed.domain.repository.PendingCommentRepository
import com.ninezero.halo.feature.feed.domain.usecase.DeleteCommentUseCase
import com.ninezero.halo.feature.feed.domain.usecase.GetCommentRepliesUseCase
import com.ninezero.halo.feature.feed.domain.usecase.GetCommentThreadUseCase
import com.ninezero.halo.feature.feed.domain.usecase.GetParentChainUseCase
import com.ninezero.halo.feature.feed.domain.usecase.GetPostByIdUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ObserveCachedCommentsUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ObservePostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.RemoveCachedPostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.TogglePinPostUseCase
import com.ninezero.halo.feature.feed.presentation.BlockTargetUser
import com.ninezero.halo.feature.feed.presentation.CommentSortType
import com.ninezero.halo.feature.feed.presentation.toUnavailablePostReason
import com.ninezero.halo.feature.profile.domain.usecase.GetCachedMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.ObserveMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class CommentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getParentChainUseCase: GetParentChainUseCase,
    private val getCommentThreadUseCase: GetCommentThreadUseCase,
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val observePostUseCase: ObservePostUseCase,
    private val removeCachedPostUseCase: RemoveCachedPostUseCase,
    private val observeCachedCommentsUseCase: ObserveCachedCommentsUseCase,
    private val getCommentRepliesUseCase: GetCommentRepliesUseCase,
    private val postMutationDelegate: PostMutationDelegate,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val togglePinPostUseCase: TogglePinPostUseCase,
    private val pendingCommentRepository: PendingCommentRepository,
    private val commentUploadEventRepository: CommentUploadEventRepository,
    private val observeMyProfileUseCase: ObserveMyProfileUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    getCachedMyProfileUseCase: GetCachedMyProfileUseCase
) : ViewModel(), OrbitContainerHost<CommentDetailState, CommentDetailState, CommentDetailSideEffect> {

    private val commentId: Int = savedStateHandle.get<Int>("commentId") ?: 0
    private val highlightAnchor: Boolean = savedStateHandle.get<Boolean>("highlightAnchor") ?: false
    private val highlightReplyId: Int? = savedStateHandle.get<Int>("highlightReplyId")
    private var highlightReplyEnsured = false
    private var isBlockingParentAuthor = false  // 차단 완료 다이얼로그를 닫을 때 나감
    private var isBlockingPostAuthor = false  // 원글 작성자 차단 중엔 원글을 가림으로 둠
    private val myProfile = getCachedMyProfileUseCase()
    private val myUserId: Int? = myProfile?.userId
    private val isCurrentUserCreator: Boolean = myProfile?.isCreator == true

    override val container: OrbitContainer<CommentDetailState, CommentDetailState, CommentDetailSideEffect> =
        orbitContainer(
            CommentDetailState(
                myUserId = myUserId,
                isCurrentUserCreator = isCurrentUserCreator,
                highlightAnchor = highlightAnchor,
                highlightReplyId = highlightReplyId
            )
        )

    init {
        observeMyProfile()
        if (commentId > 0) {
            // 이 댓글의 답글 스낵바는 이 화면 몫
            commentUploadEventRepository.registerActiveCommentDetail(commentId)
            observeCachedComments()
            loadCommentDetail()
            observePendingReplies()
            observeUploadResults()
        }
    }

    override fun onCleared() {
        if (commentId > 0) {
            commentUploadEventRepository.unregisterActiveCommentDetail(commentId)
        }
    }

    private fun observeCachedComments() {
        observeCachedCommentsUseCase()
            .onEach { cachedComments ->
                intent {
                    // 부모 작성자 차단 중엔 화면 유지
                    if (isBlockingParentAuthor) return@intent
                    val currentParent = state.parentComment
                    if (currentParent != null && cachedComments[currentParent.id] == null) {
                        postSideEffect(CommentDetailSideEffect.NavigateBack)
                        return@intent
                    }
                    // 반영은 reduce 안에서, 동시 갱신 유실 방지
                    reduce {
                        state.copy(
                            parentChain = state.parentChain.mapNotNull { comment ->
                                cachedComments[comment.id]
                            },
                            parentComment = state.parentComment?.let { parent ->
                                cachedComments[parent.id]
                            },
                            replies = state.replies.mapNotNull { reply ->
                                cachedComments[reply.id]
                            },
                            selectedComment = state.selectedComment?.let { selected ->
                                cachedComments[selected.id]
                            }
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observePendingReplies() {
        pendingCommentRepository.getPendingCommentsByParentId(commentId)
            .onEach { pendingReplies ->
                intent {
                    reduce { state.copy(pendingReplies = pendingReplies) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeUploadResults() {
        commentUploadEventRepository.uploadEvents
            .onEach { result ->
                when (result) {
                    is CommentUploadEvent.Success -> {
                        if (result.parentCommentId == commentId) {
                            intent {
                                // 새 답글은 나갈 때까지 맨 위 고정
                                reduce {
                                    state.copy(
                                        replies = (listOf(result.comment) + state.replies).distinctBy { it.id },
                                        snackbarState = SnackbarState("게시됨")
                                    )
                                }
                            }
                        }
                    }
                    is CommentUploadEvent.Failure -> {
                        if (result.parentCommentId == commentId) {
                            intent {
                                reduce {
                                    // 서버 메시지 우선
                                    state.copy(snackbarState = SnackbarState(result.errorMessage.ifBlank { "게시 실패" }))
                                }
                            }
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // 내 프로필이 늦게 오면 내 글 판정 다시
    private fun observeMyProfile() {
        observeMyProfileUseCase()
            .filterNotNull()
            .onEach { profile ->
                intent { reduce { state.copy(myUserId = profile.userId, isCurrentUserCreator = profile.isCreator) } }
            }
            .launchIn(viewModelScope)
        if (myProfile == null) intent { getMyProfileUseCase() }
    }

    fun onIntent(intent: CommentDetailIntent) {
        when (intent) {
            is CommentDetailIntent.RefreshReplies -> refreshReplies()
            is CommentDetailIntent.NavigateBack -> navigateBack()
            is CommentDetailIntent.NavigateToReply -> navigateToReply()

            // 포스트 상호작용
            is CommentDetailIntent.LikePost -> likePost()
            is CommentDetailIntent.UnlikePost -> unlikePost()

            // 부모 댓글 상호작용
            is CommentDetailIntent.LikeParentComment -> likeParentComment()
            is CommentDetailIntent.UnlikeParentComment -> unlikeParentComment()

            // 답글 관련
            is CommentDetailIntent.LoadMoreReplies -> loadMoreReplies()
            is CommentDetailIntent.ChangeSortType -> changeSortType(intent.sortType)

            // 답글 상호작용
            is CommentDetailIntent.LikeReply -> likeReply(intent.commentId)
            is CommentDetailIntent.UnlikeReply -> unlikeReply(intent.commentId)
            is CommentDetailIntent.FollowAuthor -> followAuthor(intent.authorId)
            is CommentDetailIntent.UnfollowAuthor -> unfollowAuthor(intent.authorId)

            // 댓글 옵션
            is CommentDetailIntent.ShowCommentOptions -> showCommentOptions(intent.comment)
            is CommentDetailIntent.DismissCommentOptions -> dismissCommentOptions()
            is CommentDetailIntent.DeleteComment -> deleteComment()
            is CommentDetailIntent.BookmarkComment -> bookmarkComment()

            // 포스트 옵션
            is CommentDetailIntent.ShowPostOptions -> showPostOptions()
            is CommentDetailIntent.DismissPostOptions -> dismissPostOptions()
            is CommentDetailIntent.EditPost -> editPost()
            is CommentDetailIntent.PinPost -> pinPost()
            is CommentDetailIntent.BookmarkPost -> bookmarkPost()

            // 차단 다이얼로그
            is CommentDetailIntent.BlockCommentAuthor -> blockCommentAuthor()
            is CommentDetailIntent.BlockPostAuthor -> blockPostAuthor()
            is CommentDetailIntent.DismissBlockConfirmDialog -> dismissBlockConfirmDialog()
            is CommentDetailIntent.ConfirmBlockUser -> confirmBlockUser()
            is CommentDetailIntent.DismissBlockCompletedDialog -> dismissBlockCompletedDialog()

            is CommentDetailIntent.NavigateToUserProfile -> navigateToUserProfile(intent.userId)
            is CommentDetailIntent.NavigateToCommentDetail -> navigateToCommentDetail(intent.commentId)
            is CommentDetailIntent.NavigateToPost -> navigateToPost()
            is CommentDetailIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    // 데이터 로딩

    private fun loadCommentDetail(id: Int = commentId) = intent {
        // 조상이 캐시에 없으면 서버에서 받음
        val fullChain = getParentChainUseCase(id)

        if (fullChain.isNotEmpty() && fullChain.first().parentCommentId == null) {
            val tappedComment = fullChain.last()
            // 조상 체인
            val parents = fullChain.dropLast(1)

            reduce {
                state.copy(
                    parentChain = parents,
                    parentComment = tappedComment,
                    isParentLoading = false
                )
            }
            loadPost(tappedComment.postId)
            loadReplies(id)
            refreshParentChain(id)
        } else {
            // 캐시에 없거나 조상이 잘린 경우
            reduce { state.copy(isParentLoading = true) }
            loadCommentFromServer(id)
        }
    }

    // 캐시 체인은 그 뒤 블라인드·삭제를 모름, 서버 것으로 교체
    private fun refreshParentChain(id: Int) = intent {
        getCommentThreadUseCase(id).onSuccess { chain ->
            val tappedComment = chain.lastOrNull() ?: return@onSuccess
            reduce {
                state.copy(
                    parentChain = chain.dropLast(1),
                    parentComment = tappedComment
                )
            }
        }
    }

    private fun loadCommentFromServer(id: Int) = intent {
        // 댓글과 조상 체인 단일 요청
        getCommentThreadUseCase(id)
            .onSuccess { chain ->
                val tappedComment = chain.lastOrNull()
                if (tappedComment == null) {
                    reduce { state.copy(isParentLoading = false) }
                    return@onSuccess
                }
                val parents = chain.dropLast(1)

                reduce {
                    state.copy(
                        parentChain = parents,
                        parentComment = tappedComment,
                        isParentLoading = false
                    )
                }

                loadPost(tappedComment.postId)
                loadReplies(id)
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isParentLoading = false,
                        snackbarState = SnackbarState(error.message ?: "댓글을 불러올 수 없습니다")
                    )
                }
            }
    }

    private fun loadPost(postId: Int) = intent {
        reduce { state.copy(isPostLoading = true) }

        observePost(postId)

        getPostByIdUseCase(postId)
            .onSuccess { post ->
                reduce {
                    state.copy(
                        post = post,
                        isPostLoading = false,
                        unavailablePost = null
                    )
                }
            }
            .onFailure { error ->
                val reason = error.toUnavailablePostReason()
                reduce { state.copy(isPostLoading = false, unavailablePost = reason) }
                // 못 보는 글이면 캐시에서도 뺌
                if (reason != null) removeCachedPostUseCase(postId)
            }
    }

    private fun observePost(postId: Int) {
        observePostUseCase(postId)
            .onEach { post ->
                intent {
                    if (post == null && isBlockingPostAuthor) return@intent
                    reduce {
                        state.copy(post = post)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadReplies(id: Int = commentId) = intent {
        reduce {
            state.copy(
                isRepliesLoading = true,
                repliesError = null,
                repliesPage = 1
            )
        }

        val sortBy = state.repliesSortBy
        getCommentRepliesUseCase(id, 1, 20, sortBy.apiValue)
            .onSuccess { replies ->
                if (state.repliesSortBy != sortBy) return@intent
                reduce {
                    state.copy(
                        replies = replies,
                        isRepliesLoading = false,
                        isChangingSort = false,
                        isRefreshing = false,
                        repliesError = null,
                        repliesHasMore = replies.size >= 20
                    )
                }
                ensureHighlightReplyLoaded()
            }
            .onFailure { e ->
                if (state.repliesSortBy != sortBy) return@intent
                val errorMessage = e.message ?: "답글을 불러오는데 실패했습니다"
                reduce {
                    state.copy(
                        isRepliesLoading = false,
                        isChangingSort = false,
                        isRefreshing = false,
                        repliesError = errorMessage,
                        snackbarState = SnackbarState(errorMessage)
                    )
                }
            }
    }

    private fun ensureHighlightReplyLoaded() = intent {
        val targetId = state.highlightReplyId ?: return@intent
        if (highlightReplyEnsured) return@intent
        highlightReplyEnsured = true

        var attempts = 0
        while (state.replies.none { it.id == targetId } && state.repliesHasMore && attempts < 20) {
            val nextPage = state.repliesPage + 1
            val replies = getCommentRepliesUseCase(commentId, nextPage, 20, state.repliesSortBy.apiValue)
                .getOrNull() ?: break
            reduce {
                state.copy(
                    replies = (state.replies + replies).distinctBy { it.id },
                    repliesPage = nextPage,
                    repliesHasMore = replies.size >= 20
                )
            }
            attempts++
        }
    }

    private fun loadMoreReplies() = intent {
        if (state.isRepliesLoading || !state.repliesHasMore) return@intent

        val nextPage = state.repliesPage + 1
        val sortBy = state.repliesSortBy
        reduce { state.copy(isRepliesLoading = true) }

        getCommentRepliesUseCase(commentId, nextPage, 20, sortBy.apiValue)
            .onSuccess { replies ->
                if (state.repliesSortBy != sortBy) return@intent
                reduce {
                    state.copy(
                        replies = (state.replies + replies).distinctBy { it.id },
                        isRepliesLoading = false,
                        repliesPage = nextPage,
                        repliesHasMore = replies.size >= 20
                    )
                }
            }
            .onFailure { error ->
                reduce { state.copy(isRepliesLoading = false, snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")) }
            }
    }

    private fun refreshReplies() = intent {
        // 댓글을 못 받았으면 답글만 받지 않고 처음부터
        if (state.parentComment == null) {
            loadCommentDetail()
            return@intent
        }
        // 목록 유지한 채 스피너만
        reduce { state.copy(isRefreshing = true) }
        loadReplies()
    }

    private fun changeSortType(sortType: CommentSortType) = intent {
        if (state.repliesSortBy == sortType) return@intent

        reduce {
            state.copy(
                repliesSortBy = sortType,
                isChangingSort = true,
                replies = emptyList(),
                isRepliesLoading = true,
                repliesPage = 1,
                repliesHasMore = true
            )
        }
        loadReplies()
    }

    // 포스트 상호작용

    private fun likePost() = intent {
        val post = state.post ?: return@intent
        postMutationDelegate.togglePostLike(post.id, shouldLike = true)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("좋아요 실패")) }
            }
    }

    private fun unlikePost() = intent {
        val post = state.post ?: return@intent
        postMutationDelegate.togglePostLike(post.id, shouldLike = false)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("좋아요 취소 실패")) }
            }
    }

    // 부모 댓글 상호작용

    private fun likeParentComment() = toggleParentCommentLike(shouldLike = true)

    private fun unlikeParentComment() = toggleParentCommentLike(shouldLike = false)

    private fun toggleParentCommentLike(shouldLike: Boolean) = intent {
        val comment = state.parentComment ?: return@intent
        postMutationDelegate.toggleCommentLike(comment.id, shouldLike)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("좋아요 처리 실패")) }
            }
    }

    // 답글 상호작용

    private fun likeReply(replyId: Int) = toggleReplyLike(replyId, shouldLike = true)

    private fun unlikeReply(replyId: Int) = toggleReplyLike(replyId, shouldLike = false)

    private fun toggleReplyLike(replyId: Int, shouldLike: Boolean) = intent {
        postMutationDelegate.toggleCommentLike(replyId, shouldLike)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("좋아요 처리 실패")) }
            }
    }

    private fun followAuthor(authorId: Int) = intent {
        postMutationDelegate.toggleAuthorFollow(authorId, shouldFollow = true)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("팔로우 실패")) }
            }
    }

    private fun unfollowAuthor(authorId: Int) = intent {
        postMutationDelegate.toggleAuthorFollow(authorId, shouldFollow = false)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("팔로우 취소 실패")) }
            }
    }

    // 댓글 옵션

    private fun showCommentOptions(comment: Comment) = intent {
        reduce { state.copy(showCommentOptionsSheet = true, selectedComment = comment) }
    }

    private fun dismissCommentOptions() = intent {
        reduce { state.copy(showCommentOptionsSheet = false, selectedComment = null) }
    }

    private fun deleteComment() = intent {
        val selected = state.selectedComment ?: return@intent
        dismissCommentOptions()

        // 목록 반영과 화면 종료는 캐시 구독 몫
        deleteCommentUseCase(selected.id)
            .onSuccess {
                reduce {
                    state.copy(
                        post = state.post?.copy(
                            commentCount = maxOf(0, (state.post?.commentCount ?: 0) - 1)
                        ),
                        snackbarState = SnackbarState("댓글 삭제됨")
                    )
                }
            }
            .onFailure { error ->
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "삭제 실패")) }
            }
    }

    private fun bookmarkComment() = intent {
        val comment = state.selectedComment ?: return@intent
        val shouldBookmark = !comment.isBookmarked
        dismissCommentOptions()

        reduce {
            state.copy(
                snackbarState = SnackbarState(if (shouldBookmark) "저장됨" else "저장 취소됨")
            )
        }

        postMutationDelegate.toggleCommentBookmark(comment.id, shouldBookmark)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("저장 실패")) }
            }
    }

    private fun blockCommentAuthor() = intent {
        val author = state.selectedComment?.author ?: return@intent
        dismissCommentOptions()

        reduce {
            state.copy(
                showBlockConfirmDialog = true,
                blockTargetUser = BlockTargetUser(
                    id = author.id,
                    displayName = author.displayName ?: author.username,
                    avatarUrl = author.avatarUrl
                )
            )
        }
    }

    // 포스트 옵션

    private fun showPostOptions() = intent {
        reduce { state.copy(showPostOptionsSheet = true) }
    }

    private fun dismissPostOptions() = intent {
        reduce { state.copy(showPostOptionsSheet = false) }
    }

    private fun bookmarkPost() = intent {
        val post = state.post ?: return@intent
        val shouldBookmark = !post.isBookmarked
        dismissPostOptions()

        reduce {
            state.copy(
                snackbarState = SnackbarState(if (shouldBookmark) "저장됨" else "저장 취소됨")
            )
        }

        postMutationDelegate.togglePostBookmark(post.id, shouldBookmark)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("저장 실패")) }
            }
    }

    private fun editPost() = intent {
        val postId = state.post?.id ?: return@intent
        dismissPostOptions()
        postSideEffect(CommentDetailSideEffect.NavigateToEditPost(postId))
    }

    private fun pinPost() = intent {
        val post = state.post ?: return@intent
        val shouldPin = !post.isPinned

        reduce {
            state.copy(
                showPostOptionsSheet = false,
                snackbarState = SnackbarState(if (shouldPin) "고정됨" else "고정 해제됨")
            )
        }

        togglePinPostUseCase(post.id, shouldPin)
            .onFailure { error ->
                reduce {
                    state.copy(
                        snackbarState = SnackbarState(error.message ?: "고정 처리 실패")
                    )
                }
            }
    }

    private fun blockPostAuthor() = intent {
        val author = state.post?.author ?: return@intent
        dismissPostOptions()

        reduce {
            state.copy(
                showBlockConfirmDialog = true,
                blockTargetUser = BlockTargetUser(
                    id = author.id,
                    displayName = author.displayName ?: author.username,
                    avatarUrl = author.avatarUrl
                )
            )
        }
    }

    // 네비게이션

    private fun navigateToUserProfile(userId: Int) = intent {
        postSideEffect(CommentDetailSideEffect.NavigateToUserProfile(userId))
    }

    private fun navigateToCommentDetail(commentId: Int) = intent {
        postSideEffect(CommentDetailSideEffect.NavigateToCommentDetail(commentId))
    }

    private fun navigateToPost() = intent {
        val postId = state.post?.id ?: state.parentComment?.postId ?: return@intent
        postSideEffect(CommentDetailSideEffect.NavigateToPost(postId))
    }

    private fun navigateBack() = intent {
        postSideEffect(CommentDetailSideEffect.NavigateBack)
    }

    private fun navigateToReply() = intent {
        val postId = state.parentComment?.postId ?: return@intent
        postSideEffect(CommentDetailSideEffect.NavigateToReply(postId, commentId))
    }

    // 차단

    private fun dismissBlockConfirmDialog() = intent {
        reduce {
            state.copy(
                showBlockConfirmDialog = false,
                blockTargetUser = null
            )
        }
    }

    private fun confirmBlockUser() = intent {
        val targetUser = state.blockTargetUser ?: return@intent
        val isParentAuthor = state.parentComment?.author?.id == targetUser.id

        reduce { state.copy(showBlockConfirmDialog = false) }

        isBlockingParentAuthor = isParentAuthor
        isBlockingPostAuthor = state.post?.author?.id == targetUser.id
        postMutationDelegate.blockUser(targetUser.id)
            .onFailure {
                isBlockingParentAuthor = false
                isBlockingPostAuthor = false
            }
            .onSuccess {
                if (isParentAuthor) {
                    reduce {
                        state.copy(
                            showBlockCompletedDialog = true
                        )
                    }
                } else {
                    reduce {
                        state.copy(
                            replies = state.replies
                                .filter { it.author.id != targetUser.id }
                                .map { reply ->
                                    if (reply.firstReply?.author?.id == targetUser.id) {
                                        reply.copy(firstReply = null)
                                    } else reply
                                },
                            post = if (state.post?.author?.id == targetUser.id) {
                                state.post?.copy(isBlocked = true)
                            } else state.post,
                            showBlockCompletedDialog = true
                        )
                    }
                }
            }
    }

    private fun dismissBlockCompletedDialog() = intent {
        val targetUser = state.blockTargetUser
        val isParentAuthor = state.parentComment?.author?.id == targetUser?.id

        reduce {
            state.copy(
                showBlockCompletedDialog = false,
                blockTargetUser = null
            )
        }

        if (isParentAuthor) {
            // 부모 댓글 작성자를 차단하면 화면 종료
            postSideEffect(CommentDetailSideEffect.NavigateBack)
        }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
