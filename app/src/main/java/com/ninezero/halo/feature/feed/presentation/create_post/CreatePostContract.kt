package com.ninezero.halo.feature.feed.presentation.create_post

import android.net.Uri
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.PostDraft
import com.ninezero.halo.feature.profile.domain.model.UserProfile
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.tag.domain.model.Tag

data class CreatePostState(
    val myProfile: UserProfile? = null,
    val content: String = "",
    val selectedMediaUris: List<Uri> = emptyList(),
    val isSubmitting: Boolean = false,
    val snackbarState: SnackbarState? = null,

    // PostDraft 관련
    val drafts: List<PostDraft> = emptyList(),
    val isDraftLoading: Boolean = false,
    val showDraftBottomSheet: Boolean = false,
    val showDraftDeletedSnackbar: Boolean = false,

    // 수정 모드 관련
    val isEditMode: Boolean = false,
    val editPostId: Int? = null,
    val originalMediaItems: List<MediaItem> = emptyList(),
    val originalContent: String = "",  // 나가기 확인은 원본과 달라졌을 때만

    // 커뮤니티 글 작성 모드 관련
    val isCommunityMode: Boolean = false,
    val communityCreatorId: Int? = null,

    // 태그 관련
    val showTagBottomSheet: Boolean = false,
    val tags: List<String> = emptyList(),                       // 일반 태그 최대 5개
    val pendingTags: List<String> = emptyList(),                // 바텀시트에서 편집 중인 임시 태그
    val sectionTags: List<Tag> = emptyList(),                   // 섹션 태그 목록
    val selectedSectionTagId: Int? = null,
    val tempSectionTagId: Int? = null,                          // 바텀시트에서 선택 중인 임시 섹션 태그 ID
    val isLoadingSectionTags: Boolean = false,
    val tagSnackbarMessage: String? = null,

    // 상세설정 관련
    val showSettingsBottomSheet: Boolean = false,
    val isSecret: Boolean = false,                              // 비밀글, 커뮤니티 전용
    val requiredTier: String = SubscriptionPlanTier.FREE.name,  // 멤버십 티어, 크리에이터 전용
    val planTiers: Set<SubscriptionPlanTier> = emptySet()      // 활성 플랜이 있는 등급
)

sealed interface CreatePostIntent {
    data class UpdateContent(val content: String) : CreatePostIntent
    data class AddMedia(val uris: List<Uri>) : CreatePostIntent
    data class RemoveMedia(val uri: Uri) : CreatePostIntent
    data object ClearAllMedia : CreatePostIntent
    data object SubmitPost : CreatePostIntent
    data object NavigateBack : CreatePostIntent
    data object SnackbarDismissed : CreatePostIntent

    // PostDraft 관련
    data object OpenDraftBottomSheet : CreatePostIntent
    data object CloseDraftBottomSheet : CreatePostIntent
    // 두 옵션 모두 저장이 끝난 뒤 실행, 실패면 남음
    data class SaveDraft(val exitAfterSave: Boolean = false, val openDraftsAfterSave: Boolean = false) : CreatePostIntent
    data class LoadDraft(val draft: PostDraft) : CreatePostIntent
    data class DeleteDraft(val draft: PostDraft) : CreatePostIntent
    data object DismissDraftDeletedSnackbar : CreatePostIntent

    // 태그 관련
    data object OpenTagBottomSheet : CreatePostIntent
    data object CloseTagBottomSheet : CreatePostIntent
    data object ConfirmTagChanges : CreatePostIntent  // 태그 변경사항 적용
    data class AddTag(val tag: String) : CreatePostIntent
    data class RemoveTag(val tag: String) : CreatePostIntent
    data class SelectSectionTag(val tagId: Int?) : CreatePostIntent
    data class CreateSectionTag(val name: String) : CreatePostIntent
    data class UpdateSectionTag(val tagId: Int, val name: String) : CreatePostIntent
    data class DeleteSectionTag(val tagId: Int) : CreatePostIntent
    data object DismissTagSnackbar : CreatePostIntent

    // 상세설정 관련
    data object OpenSettingsBottomSheet : CreatePostIntent
    data object CloseSettingsBottomSheet : CreatePostIntent
    data class SetIsSecret(val isSecret: Boolean) : CreatePostIntent
    data class SetRequiredTier(val tier: String) : CreatePostIntent
}

sealed interface CreatePostSideEffect {
    data object NavigateBack : CreatePostSideEffect
    data object PostSubmitted : CreatePostSideEffect  // 업로드 큐 추가됨
    data object PostUpdated : CreatePostSideEffect    // 수정 완료
    data class ShowToast(val message: String) : CreatePostSideEffect
}
