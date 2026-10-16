package com.ninezero.halo.feature.feed.presentation.create_post

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.Constants.Content.MAX_POST_CONTENT_LENGTH
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.feature.feed.domain.model.NewMediaItem
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.feed.domain.model.PostDraft
import com.ninezero.halo.feature.feed.domain.model.PostType
import com.ninezero.halo.feature.feed.domain.repository.PendingPostRepository
import com.ninezero.halo.feature.feed.domain.repository.PostDraftRepository
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import com.ninezero.halo.feature.feed.domain.usecase.GetCachedPostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.UpdatePostUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetCachedMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.ObserveMyProfileUseCase
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.subscription.domain.usecase.GetMyPlanTiersUseCase
import com.ninezero.halo.feature.tag.domain.model.TagTargetType
import com.ninezero.halo.feature.tag.domain.usecase.CreateTagUseCase
import com.ninezero.halo.feature.tag.domain.usecase.DeleteTagUseCase
import com.ninezero.halo.feature.tag.domain.usecase.GetCreatorTagsUseCase
import com.ninezero.halo.feature.tag.domain.usecase.UpdateTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.MultipartBody
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pendingPostRepository: PendingPostRepository,
    private val postDraftRepository: PostDraftRepository,
    private val postRepository: PostRepository,
    private val getCachedMyProfileUseCase: GetCachedMyProfileUseCase,
    private val observeMyProfileUseCase: ObserveMyProfileUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getCachedPostUseCase: GetCachedPostUseCase,
    private val updatePostUseCase: UpdatePostUseCase,
    private val getCreatorTagsUseCase: GetCreatorTagsUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    @param:ApplicationContext private val context: Context,
    private val getMyPlanTiersUseCase: GetMyPlanTiersUseCase
) : ViewModel(), OrbitContainerHost<CreatePostState, CreatePostState, CreatePostSideEffect> {

    // postId가 있으면 수정 모드
    private val editPostId: Int? = savedStateHandle.get<Int>("postId")?.takeIf { it > 0 }
    // creatorId가 있으면 커뮤니티 글
    private val communityCreatorId: Int? = savedStateHandle.get<Int>("creatorId")?.takeIf { it > 0 }
    // 섹션 태그는 한 번만 로드
    private var sectionTagsRequested = false

    override val container: OrbitContainer<CreatePostState, CreatePostState, CreatePostSideEffect> = orbitContainer(
        CreatePostState(
            myProfile = getCachedMyProfileUseCase(),
            isEditMode = editPostId != null,
            editPostId = editPostId,
            isCommunityMode = communityCreatorId != null,
            communityCreatorId = communityCreatorId
        )
    )

    init {
        if (editPostId != null) {
            loadPostForEdit(editPostId)
        } else {
            observeDrafts()
        }
        val myProfile = getCachedMyProfileUseCase()
        if (myProfile?.isCreator == true) {
            loadSectionTags()
            loadPlanTiers()
        }
        observeMyProfile()
    }

    // 프로필이 늦게 와도 채움, 크리에이터면 섹션 태그도
    private fun observeMyProfile() {
        observeMyProfileUseCase()
            .filterNotNull()
            .onEach { profile ->
                intent {
                    reduce { state.copy(myProfile = profile) }
                    if (profile.isCreator) {
                        loadSectionTags()
                        loadPlanTiers()
                    }
                }
            }
            .launchIn(viewModelScope)
        if (container.stateFlow.value.myProfile == null) intent { getMyProfileUseCase() }
    }

    private fun loadPostForEdit(postId: Int) = intent {
        val post = getCachedPostUseCase(postId)
        if (post != null) {
            val originalImageUrls = post.mediaAttachments.map { it.url }
            reduce {
                state.copy(
                    content = post.content,
                    originalContent = post.content,
                    selectedMediaUris = originalImageUrls.map { it.toUri() },
                    originalMediaItems = post.mediaAttachments,
                    // 글 소속 기준, 커뮤니티는 이미지만
                    isCommunityMode = post.contextType == PostContextType.COMMUNITY
                )
            }
        } else {
            reduce { state.copy(snackbarState = SnackbarState("포스트를 불러올 수 없습니다")) }
            postSideEffect(CreatePostSideEffect.NavigateBack)
        }
    }

    private fun observeDrafts() {
        postDraftRepository.getAllDrafts()
            .onEach { drafts ->
                intent { reduce { state.copy(drafts = drafts) } }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: CreatePostIntent) {
        when (intent) {
            is CreatePostIntent.UpdateContent -> updateContent(intent.content)
            is CreatePostIntent.AddMedia -> addMedia(intent.uris)
            is CreatePostIntent.RemoveMedia -> removeMedia(intent.uri)
            is CreatePostIntent.ClearAllMedia -> clearAllMedia()
            is CreatePostIntent.SubmitPost -> submitPost()
            is CreatePostIntent.NavigateBack -> navigateBack()
            is CreatePostIntent.SnackbarDismissed -> dismissSnackbar()
            // PostDraft 관련
            is CreatePostIntent.OpenDraftBottomSheet -> openDraftBottomSheet()
            is CreatePostIntent.CloseDraftBottomSheet -> closeDraftBottomSheet()
            is CreatePostIntent.SaveDraft -> saveDraft(intent.exitAfterSave, intent.openDraftsAfterSave)
            is CreatePostIntent.LoadDraft -> loadDraft(intent.draft)
            is CreatePostIntent.DeleteDraft -> deleteDraft(intent.draft)
            is CreatePostIntent.DismissDraftDeletedSnackbar -> dismissDraftDeletedSnackbar()
            // 태그 관련
            is CreatePostIntent.OpenTagBottomSheet -> openTagBottomSheet()
            is CreatePostIntent.CloseTagBottomSheet -> closeTagBottomSheet()
            is CreatePostIntent.ConfirmTagChanges -> confirmTagChanges()
            is CreatePostIntent.AddTag -> addTag(intent.tag)
            is CreatePostIntent.RemoveTag -> removeTag(intent.tag)
            is CreatePostIntent.SelectSectionTag -> selectSectionTag(intent.tagId)
            is CreatePostIntent.CreateSectionTag -> createSectionTag(intent.name)
            is CreatePostIntent.UpdateSectionTag -> updateSectionTag(intent.tagId, intent.name)
            is CreatePostIntent.DeleteSectionTag -> deleteSectionTag(intent.tagId)
            is CreatePostIntent.DismissTagSnackbar -> dismissTagSnackbar()
            // 상세설정 관련
            is CreatePostIntent.OpenSettingsBottomSheet -> openSettingsBottomSheet()
            is CreatePostIntent.CloseSettingsBottomSheet -> closeSettingsBottomSheet()
            is CreatePostIntent.SetIsSecret -> setIsSecret(intent.isSecret)
            is CreatePostIntent.SetRequiredTier -> setRequiredTier(intent.tier)
        }
    }

    private fun updateContent(content: String) = blockingIntent {
        reduce { state.copy(content = content) }
    }

    private fun addMedia(uris: List<Uri>) = intent {
        val currentUris = state.selectedMediaUris
        val availableSlots = MAX_POST_MEDIA - currentUris.size

        if (availableSlots <= 0) {
            reduce {
                state.copy(snackbarState = SnackbarState("미디어는 최대 ${MAX_POST_MEDIA}개까지 첨부 가능합니다"))
            }
            return@intent
        }

        val validUris = uris.take(availableSlots).filter { uri ->
            val fileInfo = MediaUtils.getFileInfo(context, uri) ?: return@filter false

            if (fileInfo.fileSize > MAX_POST_MEDIA_SIZE) {
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
            if (newUris.size > MAX_POST_MEDIA) {
                state.copy(
                    selectedMediaUris = newUris.take(MAX_POST_MEDIA),
                    snackbarState = SnackbarState("미디어는 최대 ${MAX_POST_MEDIA}개까지 첨부 가능합니다")
                )
            } else {
                state.copy(selectedMediaUris = newUris)
            }
        }
    }

    private fun removeMedia(uri: Uri) = intent {
        reduce {
            state.copy(selectedMediaUris = state.selectedMediaUris - uri)
        }
    }

    private fun clearAllMedia() = intent {
        reduce { state.copy(selectedMediaUris = emptyList()) }
    }

    private fun submitPost() = intent {
        if (state.isSubmitting) return@intent  // 연타/중복 제출 가드

        val content = state.content.trim()
        if (content.isBlank()) return@intent

        val contentWithTags = if (state.tags.isNotEmpty()) {
            val hashTags = state.tags.joinToString(" ") { "#$it" }
            "$content\n\n$hashTags"
        } else {
            content
        }

        if (contentWithTags.length > MAX_POST_CONTENT_LENGTH) {
            reduce {
                state.copy(
                    snackbarState = SnackbarState("내용은 ${MAX_POST_CONTENT_LENGTH}자를 초과할 수 없습니다")
                )
            }
            return@intent
        }

        reduce { state.copy(isSubmitting = true) }

        if (state.isEditMode) {
            updatePost(contentWithTags)
        } else {
            createNewPost(contentWithTags)
        }
    }

    private fun createNewPost(content: String) = intent {
        val myProfile = state.myProfile ?: getMyProfileUseCase().getOrNull() ?: run {
            reduce { state.copy(isSubmitting = false, snackbarState = SnackbarState("프로필을 불러올 수 없습니다")) }
            return@intent
        }

        val creatorId = if (state.isCommunityMode) state.communityCreatorId else null
        if (state.isCommunityMode && creatorId == null) {
            reduce {
                state.copy(
                    isSubmitting = false,
                    snackbarState = SnackbarState("커뮤니티 정보를 찾을 수 없습니다")
                )
            }
            return@intent
        }

        val mediaUris = state.selectedMediaUris
        val postType = if (mediaUris.isEmpty()) PostType.TEXT else PostType.MEDIA

        val tagIds = state.selectedSectionTagId?.let { listOf(it) }

        // 대기 글로 저장, 업로드는 워커
        try {
            pendingPostRepository.addPendingPost(
                content = content,
                postType = postType,
                mediaUris = mediaUris.ifEmpty { null },
                authorId = myProfile.userId,
                authorUsername = myProfile.username,
                authorDisplayName = myProfile.displayName,
                authorAvatarUrl = myProfile.avatarUrl,
                tagIds = tagIds,
                sectionTagId = state.selectedSectionTagId,
                requiredTier = state.requiredTier,
                creatorId = creatorId,
                isSecret = state.isCommunityMode && state.isSecret
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 미디어 사본 실패는 화면에 남아 안내
            val message = if (e is IOException) "첨부한 사진·영상을 저장하지 못했습니다" else "게시 실패"
            reduce { state.copy(isSubmitting = false, snackbarState = SnackbarState(message)) }
            return@intent
        }

        reduce { state.copy(isSubmitting = false) }
        postSideEffect(CreatePostSideEffect.PostSubmitted)
    }

    private fun updatePost(content: String) = intent {
        // 조기 종료 시 isSubmitting 해제
        val postId = state.editPostId ?: run {
            reduce { state.copy(isSubmitting = false) }
            return@intent
        }

        val currentUris = state.selectedMediaUris
        val originalMediaByUrl = state.originalMediaItems.associateBy { it.url }

        // 선택 순서를 유지한 keepMediaIds/reorder 구성
        val keepMediaIds = currentUris
            .map { it.toString() }
            .filter { uri -> uri.startsWith("http://") || uri.startsWith("https://") }
            .mapNotNull { url -> originalMediaByUrl[url]?.id }

        val newMediaUris = currentUris.filterNot { uri ->
            val value = uri.toString()
            value.startsWith("http://") || value.startsWith("https://")
        }
        // 사진은 파일 파트, 새 영상은 먼저 업로드
        val newMediaParts = mutableListOf<MultipartBody.Part>()
        val newItems = mutableListOf<NewMediaItem>()
        for (uri in newMediaUris) {
            if (MediaUtils.resolveMimeType(context, uri).startsWith("video/")) {
                val uploadId = postRepository.uploadPostVideo(postId, uri.toString()).getOrElse { error ->
                    reduce {
                        state.copy(
                            isSubmitting = false,
                            snackbarState = SnackbarState(error.message ?: "수정 실패")
                        )
                    }
                    return@intent
                }
                newItems += NewMediaItem.DirectUpload(uploadId)
            } else {
                val part = MediaUtils.createUploadPart(context, uri, "mediaFiles", "image_${newMediaParts.size}") ?: continue
                newItems += NewMediaItem.FilePart(newMediaParts.size)
                newMediaParts += part
            }
        }
        // 커뮤니티 글도 일반 수정 API
        val result = updatePostUseCase(
            postId = postId,
            content = content,
            keepMediaIds = keepMediaIds,
            newFiles = newMediaParts,
            reorder = keepMediaIds,
            items = newItems
        )

        result.onSuccess { updatedPost ->
            postRepository.cachePost(updatedPost)
            reduce { state.copy(isSubmitting = false) }
            postSideEffect(CreatePostSideEffect.PostUpdated)
        }.onFailure { error ->
            reduce {
                state.copy(
                    isSubmitting = false,
                    snackbarState = SnackbarState(error.message ?: "수정 실패")
                )
            }
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(CreatePostSideEffect.NavigateBack)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    // PostDraft 관련
    private fun openDraftBottomSheet() = intent {
        reduce { state.copy(showDraftBottomSheet = true, isDraftLoading = false) }
    }

    private fun closeDraftBottomSheet() = intent {
        reduce { state.copy(showDraftBottomSheet = false) }
    }

    @Volatile private var draftSaveInFlight = false
    @Volatile private var exitWhenDraftSaved = false  // 저장 중에 누른 나가며 저장
    @Volatile private var openDraftsWhenSaved = false

    // 나가며 저장은 저장이 끝난 뒤 나감, 실패면 안내하고 남음
    private fun saveDraft(exitAfterSave: Boolean, openDraftsAfterSave: Boolean) = intent {
        if (state.isSubmitting) {  // 저장·게시 중 다시 저장하면 초안이 둘
            // 진행 중인 저장이 끝나면 나감·목록
            if (draftSaveInFlight) {
                if (exitAfterSave) exitWhenDraftSaved = true
                if (openDraftsAfterSave) openDraftsWhenSaved = true
            }
            return@intent
        }
        val content = state.content.trim()
        val mediaUris = state.selectedMediaUris

        if (content.isBlank() && mediaUris.isEmpty()) {  // 내용이 없으면 저장하지 않음
            if (exitAfterSave) postSideEffect(CreatePostSideEffect.NavigateBack)
            if (openDraftsAfterSave) reduce { state.copy(showDraftBottomSheet = true, isDraftLoading = false) }
            return@intent
        }

        // 미디어 사본 복사까지 기다리는 동안 게시·다시 저장을 막음
        draftSaveInFlight = true
        reduce { state.copy(isSubmitting = true) }
        val myProfile = state.myProfile ?: getMyProfileUseCase().getOrNull() ?: run {
            draftSaveInFlight = false
            exitWhenDraftSaved = false
            openDraftsWhenSaved = false
            reduce { state.copy(isSubmitting = false, snackbarState = SnackbarState("프로필을 불러올 수 없습니다")) }
            return@intent
        }

        try {
            postDraftRepository.saveDraft(
                content = content,
                mediaUris = mediaUris,
                authorId = myProfile.userId,
                authorUsername = myProfile.username,
                authorDisplayName = myProfile.displayName,
                authorAvatarUrl = myProfile.avatarUrl
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            draftSaveInFlight = false
            exitWhenDraftSaved = false
            openDraftsWhenSaved = false
            // 미디어 사본 실패는 따로 안내
            val message = if (e is IOException) "첨부한 사진·영상을 저장하지 못했습니다" else "임시 저장 실패"
            reduce { state.copy(isSubmitting = false, snackbarState = SnackbarState(message)) }
            return@intent
        }
        val exit = exitAfterSave || exitWhenDraftSaved
        val openDrafts = openDraftsAfterSave || openDraftsWhenSaved
        draftSaveInFlight = false
        exitWhenDraftSaved = false
        openDraftsWhenSaved = false

        reduce {
            state.copy(
                isSubmitting = false,
                content = "",
                selectedMediaUris = emptyList(),
                tags = emptyList(),
                selectedSectionTagId = null,
                isSecret = false,
                requiredTier = SubscriptionPlanTier.FREE.name,
                // 방금 저장한 초안까지 든 목록으로 열기
                showDraftBottomSheet = if (openDrafts && !exit) true else state.showDraftBottomSheet
            )
        }
        postSideEffect(CreatePostSideEffect.ShowToast("임시 저장됨"))
        if (exit) postSideEffect(CreatePostSideEffect.NavigateBack)
    }

    private fun loadDraft(draft: PostDraft) = intent {
        if (state.isSubmitting) {  // 저장이 끝나며 입력을 비워 불러온 초안이 날아감
            postSideEffect(CreatePostSideEffect.ShowToast("저장·게시가 끝난 뒤 불러올 수 있습니다"))
            return@intent
        }
        reduce {
            state.copy(
                content = draft.content,
                selectedMediaUris = draft.mediaUris,
                showDraftBottomSheet = false
            )
        }

        // 기록만 삭제, 사본은 다음 저장 때 이동
        postDraftRepository.deleteDraftRecordOnly(draft.id)
    }

    private fun deleteDraft(draft: PostDraft) = intent {
        postDraftRepository.deleteDraft(draft.id)
        reduce { state.copy(showDraftDeletedSnackbar = true) }
    }

    private fun dismissDraftDeletedSnackbar() = intent {
        reduce { state.copy(showDraftDeletedSnackbar = false) }
    }

    // 태그 관련
    private fun openTagBottomSheet() = intent {
        reduce {
            state.copy(
                showTagBottomSheet = true,
                pendingTags = state.tags,
                tempSectionTagId = state.selectedSectionTagId
            )
        }
    }

    private fun closeTagBottomSheet() = intent {
        reduce {
            state.copy(
                showTagBottomSheet = false,
                pendingTags = emptyList(),
                tempSectionTagId = null
            )
        }
    }

    private fun confirmTagChanges() = intent {
        reduce {
            state.copy(
                showTagBottomSheet = false,
                tags = state.pendingTags,
                selectedSectionTagId = state.tempSectionTagId,
                pendingTags = emptyList(),
                tempSectionTagId = null
            )
        }
    }

    private fun addTag(tag: String) = intent {
        val trimmedTag = tag.trim()
        if (trimmedTag.isBlank()) return@intent

        if (state.pendingTags.size >= MAX_TAGS) {
            reduce {
                state.copy(
                    tagSnackbarMessage = "태그는 최대 ${MAX_TAGS}개까지 추가할 수 있습니다"
                )
            }
            return@intent
        }

        if (state.pendingTags.any { it.equals(trimmedTag, ignoreCase = true) }) {
            reduce {
                state.copy(
                    tagSnackbarMessage = "이미 추가된 태그입니다"
                )
            }
            return@intent
        }

        reduce {
            state.copy(pendingTags = state.pendingTags + trimmedTag)
        }
    }

    private fun removeTag(tag: String) = intent {
        reduce {
            state.copy(pendingTags = state.pendingTags - tag)
        }
    }

    private fun selectSectionTag(tagId: Int?) = intent {
        reduce {
            state.copy(tempSectionTagId = tagId)
        }
    }

    private fun createSectionTag(name: String) = intent {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return@intent

        val myProfile = state.myProfile ?: return@intent
        if (!myProfile.isCreator) return@intent
        // 로드·편집 중엔 막음, 늦은 로드가 새 태그를 덮음
        if (state.isLoadingSectionTags) return@intent

        if (state.sectionTags.size >= MAX_SECTION_TAGS) {
            reduce {
                state.copy(
                    tagSnackbarMessage = "섹션 태그는 최대 ${MAX_SECTION_TAGS}개까지 추가할 수 있습니다"
                )
            }
            return@intent
        }

        reduce { state.copy(isLoadingSectionTags = true) }

        createTagUseCase(
            name = trimmedName,
            targetType = TagTargetType.POST,
            isSectionEnabled = true
        ).onSuccess { newTag ->
            reduce {
                state.copy(
                    sectionTags = state.sectionTags + newTag,
                    isLoadingSectionTags = false,
                    tagSnackbarMessage = "섹션 태그가 추가되었습니다"
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isLoadingSectionTags = false,
                    tagSnackbarMessage = error.message ?: "섹션 태그 추가 실패"
                )
            }
        }
    }

    private fun updateSectionTag(tagId: Int, name: String) = intent {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return@intent
        if (state.isLoadingSectionTags) return@intent

        reduce { state.copy(isLoadingSectionTags = true) }

        updateTagUseCase(
            tagId = tagId,
            name = trimmedName
        ).onSuccess { updatedTag ->
            reduce {
                state.copy(
                    sectionTags = state.sectionTags.map { if (it.id == tagId) updatedTag else it },
                    isLoadingSectionTags = false,
                    tagSnackbarMessage = "섹션 태그가 수정되었습니다"
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isLoadingSectionTags = false,
                    tagSnackbarMessage = error.message ?: "섹션 태그 수정 실패"
                )
            }
        }
    }

    private fun deleteSectionTag(tagId: Int) = intent {
        if (state.isLoadingSectionTags) return@intent
        reduce { state.copy(isLoadingSectionTags = true) }

        deleteTagUseCase(tagId).onSuccess {
            reduce {
                state.copy(
                    sectionTags = state.sectionTags.filter { it.id != tagId },
                    selectedSectionTagId = if (state.selectedSectionTagId == tagId) null else state.selectedSectionTagId,
                    // 시트에서 고른 채 지운 태그로 게시되는 것 방지
                    tempSectionTagId = if (state.tempSectionTagId == tagId) null else state.tempSectionTagId,
                    isLoadingSectionTags = false,
                    tagSnackbarMessage = "섹션 태그가 삭제되었습니다"
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isLoadingSectionTags = false,
                    tagSnackbarMessage = error.message ?: "섹션 태그 삭제 실패"
                )
            }
        }
    }

    private fun dismissTagSnackbar() = intent {
        reduce { state.copy(tagSnackbarMessage = null) }
    }

    // 플랜 없는 등급은 서버가 게시를 거절해 선택지에서 뺌
    private fun loadPlanTiers() = intent {
        getMyPlanTiersUseCase().onSuccess { tiers -> reduce { state.copy(planTiers = tiers) } }
    }

    private fun loadSectionTags() {
        if (sectionTagsRequested) return
        sectionTagsRequested = true
        fetchSectionTags()
    }

    private fun fetchSectionTags() = intent {
        val myProfile = state.myProfile ?: return@intent
        if (!myProfile.isCreator) return@intent

        reduce { state.copy(isLoadingSectionTags = true) }

        getCreatorTagsUseCase(
            creatorId = myProfile.userId,
            targetType = TagTargetType.POST
        ).onSuccess { tags ->
            reduce {
                state.copy(
                    sectionTags = tags.filter { it.isSectionEnabled },
                    isLoadingSectionTags = false
                )
            }
        }.onFailure {
            reduce { state.copy(isLoadingSectionTags = false) }
        }
    }

    // 상세설정 관련 함수들
    private fun openSettingsBottomSheet() = intent {
        reduce { state.copy(showSettingsBottomSheet = true) }
    }

    private fun closeSettingsBottomSheet() = intent {
        reduce { state.copy(showSettingsBottomSheet = false) }
    }

    private fun setIsSecret(isSecret: Boolean) = intent {
        reduce { state.copy(isSecret = isSecret) }
    }

    private fun setRequiredTier(tier: String) = intent {
        reduce { state.copy(requiredTier = tier) }
    }

    companion object {
        private const val MAX_POST_MEDIA = 10
        private const val MAX_POST_MEDIA_SIZE = 10 * 1024 * 1024L  // 10MB
        private const val MAX_TAGS = 5
        private const val MAX_SECTION_TAGS = 3
    }
}
