package com.ninezero.halo.feature.chat.presentation.chat_room

import android.graphics.Bitmap
import android.net.Uri
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.chat.domain.model.Message

data class AttachmentState(
    val uri: Uri,
    val fileName: String = "",
    val fileSize: Long = 0L,
    val thumbnail: Bitmap? = null,
    val duration: Long = 0L         // 비디오 길이 ms
)

enum class AttachmentUploadStatus {
    PENDING,      // 업로드 대기
    UPLOADING,    // 업로드 중
    SUCCESS,      // 업로드 완료
    FAILED        // 업로드 실패
}

data class ChatRoomState(
    val roomId: Int = 0,  // draft 모드면 0
    val otherUser: UserSummary? = null,
    val otherUserLoaded: Boolean = false,  // 헤더 아바타 재요청 키
    val messages: List<Message> = emptyList(),
    val unreadStartMessageId: Int? = null,
    val entryUnreadCount: Int = 0,
    val messageInput: String = "",
    val isLoading: Boolean = false,
    val messagesResolved: Boolean = false,  // 확정 전엔 로딩 표시 보류
    val error: String? = null,  // 첫 로드 실패, 메시지가 없을 때 오류+재시도
    val isLoadingMore: Boolean = false,
    val isSending: Boolean = false,
    val hasMore: Boolean = true,
    val snackbarState: SnackbarState? = null,

    // 검색
    val isSearchMode: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Message> = emptyList(),
    val currentSearchIndex: Int = 0,
    val isSearching: Boolean = false,
    val searchPage: Int = 1,
    val searchHasMore: Boolean = false,
    val searchTotalCount: Int = 0,

    // 삭제 확인
    val messageToDelete: Message? = null,
    val showDeleteDialog: Boolean = false,

    // 차단
    val isBlocked: Boolean = false,  // 차단 해제 버튼 오버레이
    val isBlockedByOther: Boolean = false,  // 안내 문구 오버레이
    val showProfileOptionsSheet: Boolean = false,
    val showBlockConfirmDialog: Boolean = false,
    val showUnblockConfirmDialog: Boolean = false,

    // 첨부
    val isAttachmentExpanded: Boolean = false,
    val selectedImages: List<AttachmentState> = emptyList(),
    val selectedVideo: AttachmentState? = null,
    val selectedFile: AttachmentState? = null,
    val downloadingFiles: Map<String, Float> = emptyMap()  // fileUrl별 진행률
) {
    val hasAttachment: Boolean
        get() = selectedImages.isNotEmpty() || selectedVideo != null || selectedFile != null

    val canSend: Boolean
        get() = (messageInput.isNotBlank() || hasAttachment) && !isSending
}

sealed interface ChatRoomIntent {
    data object ScreenResumed : ChatRoomIntent
    data object LoadMoreMessages : ChatRoomIntent
    data object Retry : ChatRoomIntent
    data class MessageInputChanged(val text: String) : ChatRoomIntent
    data object SendMessage : ChatRoomIntent
    data class RetryFailedMessage(val clientTempId: String) : ChatRoomIntent
    data class CancelFailedMessage(val clientTempId: String) : ChatRoomIntent
    data class ShowDeleteDialog(val message: Message) : ChatRoomIntent
    data object DismissDeleteDialog : ChatRoomIntent
    data object ConfirmDeleteMessage : ChatRoomIntent
    data object NavigateBack : ChatRoomIntent
    data class PostLinkClicked(val postId: Int, val canAccess: Boolean) : ChatRoomIntent
    data class ProductLinkClicked(val productId: Int, val canAccess: Boolean) : ChatRoomIntent

    // 검색
    data object ToggleSearchMode : ChatRoomIntent
    data class SearchQueryChanged(val query: String) : ChatRoomIntent
    data object SearchMessages : ChatRoomIntent
    data object ClearSearch : ChatRoomIntent
    data object NavigateToPreviousResult : ChatRoomIntent
    data object NavigateToNextResult : ChatRoomIntent

    // 차단
    data object ShowProfileOptions : ChatRoomIntent
    data object DismissProfileOptions : ChatRoomIntent
    data object ShowBlockConfirmDialog : ChatRoomIntent
    data object DismissBlockConfirmDialog : ChatRoomIntent
    data object ShowUnblockConfirmDialog : ChatRoomIntent
    data object DismissUnblockConfirmDialog : ChatRoomIntent
    data object ConfirmBlockUser : ChatRoomIntent
    data object ConfirmUnblockUser : ChatRoomIntent
    data class ShowLockedInfo(val message: String) : ChatRoomIntent
    data object SnackbarDismissed : ChatRoomIntent

    // 첨부
    data object ToggleAttachmentExpanded : ChatRoomIntent
    data class AddImages(val uris: List<Uri>) : ChatRoomIntent
    data class RemoveImage(val index: Int) : ChatRoomIntent
    data class SetVideo(val uri: Uri) : ChatRoomIntent
    data object RemoveVideo : ChatRoomIntent
    data class SetFile(val uri: Uri) : ChatRoomIntent
    data object RemoveFile : ChatRoomIntent
    data class DownloadFile(val fileUrl: String, val fileName: String) : ChatRoomIntent
}

sealed interface ChatRoomSideEffect {
    data object NavigateBack : ChatRoomSideEffect
    data object ScrollToBottom : ChatRoomSideEffect
    data object HideKeyboard : ChatRoomSideEffect
    data object FocusSearchField : ChatRoomSideEffect
    data class ScrollToMessage(val messageId: Int) : ChatRoomSideEffect
    data class ScrollToUnread(val lastReadMessageId: Int) : ChatRoomSideEffect
    data class NavigateToPostDetail(val postId: Int) : ChatRoomSideEffect
    data class NavigateToProductDetail(val productId: Int) : ChatRoomSideEffect
    data class ShowCenterToast(val message: String) : ChatRoomSideEffect
    data class ShowToast(val message: String) : ChatRoomSideEffect
}
