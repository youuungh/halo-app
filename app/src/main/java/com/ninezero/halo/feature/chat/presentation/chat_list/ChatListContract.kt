package com.ninezero.halo.feature.chat.presentation.chat_list

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.chat.domain.model.Chat

data class ChatListState(
    val chatRooms: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = false,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,
    // 보관 시트
    val selectedChat: Chat? = null,
    val showArchiveToast: Boolean = false,
    val archiveToastMessage: String = "",
    // 삭제 다이얼로그
    val showDeleteDialog: Boolean = false
)

sealed interface ChatListIntent {
    data object LoadChatRooms : ChatListIntent
    data object RefreshChatRooms : ChatListIntent
    data object LoadMoreChatRooms : ChatListIntent
    data class NavigateToChatRoom(val roomId: Int) : ChatListIntent
    data object SnackbarDismissed : ChatListIntent
    // 보관 시트
    data class SelectChatForArchive(val chat: Chat) : ChatListIntent
    data object DismissArchiveSheet : ChatListIntent
    data object ArchiveSelectedChat : ChatListIntent
    data object DismissArchiveToast : ChatListIntent
    // 삭제 다이얼로그
    data object OpenDeleteDialog : ChatListIntent
    data object DismissDeleteDialog : ChatListIntent
    data object DeleteSelectedChat : ChatListIntent
}

sealed interface ChatListSideEffect {
    data class NavigateToChatRoom(val roomId: Int) : ChatListSideEffect
}
