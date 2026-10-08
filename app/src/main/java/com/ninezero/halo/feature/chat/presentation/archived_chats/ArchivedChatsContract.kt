package com.ninezero.halo.feature.chat.presentation.archived_chats

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.chat.domain.model.Chat

data class ArchivedChatsState(
    val archivedChats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 1,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,
    // 보관 해제 시트
    val selectedChat: Chat? = null,
    val showUnarchiveToast: Boolean = false,
    val unarchiveToastMessage: String = "",
    // 삭제 다이얼로그
    val showDeleteDialog: Boolean = false
)

sealed interface ArchivedChatsIntent {
    data object LoadArchivedChats : ArchivedChatsIntent
    data object RefreshArchivedChats : ArchivedChatsIntent
    data object LoadMoreArchivedChats : ArchivedChatsIntent
    data class NavigateToChatRoom(val roomId: Int) : ArchivedChatsIntent
    data object SnackbarDismissed : ArchivedChatsIntent
    // 보관 해제 시트
    data class SelectChatForUnarchive(val chat: Chat) : ArchivedChatsIntent
    data object DismissUnarchiveSheet : ArchivedChatsIntent
    data object UnarchiveSelectedChat : ArchivedChatsIntent
    data object DismissUnarchiveToast : ArchivedChatsIntent
    // 삭제 다이얼로그
    data object OpenDeleteDialog : ArchivedChatsIntent
    data object DismissDeleteDialog : ArchivedChatsIntent
    data object DeleteSelectedChat : ArchivedChatsIntent
}

sealed interface ArchivedChatsSideEffect {
    data class NavigateToChatRoom(val roomId: Int) : ArchivedChatsSideEffect
}
