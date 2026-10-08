package com.ninezero.halo.feature.feed.presentation.blocked_profiles

import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

data class BlockedProfilesState(
    val blockedUsers: List<UserSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = true,
    val nextCursor: String? = null,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,
    val showUnblockDialog: Boolean = false,
    val selectedUser: UserSummary? = null
)

sealed interface BlockedProfilesIntent {
    data object LoadBlockedUsers : BlockedProfilesIntent
    data object RefreshBlockedUsers : BlockedProfilesIntent
    data object LoadMoreBlockedUsers : BlockedProfilesIntent
    data class NavigateToUserProfile(val userId: Int) : BlockedProfilesIntent
    data class UnblockClick(val user: UserSummary) : BlockedProfilesIntent
    data object ConfirmUnblock : BlockedProfilesIntent
    data object DismissUnblockDialog : BlockedProfilesIntent
    data object SnackbarDismissed : BlockedProfilesIntent
}

sealed interface BlockedProfilesSideEffect {
    data class NavigateToUserProfile(val userId: Int) : BlockedProfilesSideEffect
}
