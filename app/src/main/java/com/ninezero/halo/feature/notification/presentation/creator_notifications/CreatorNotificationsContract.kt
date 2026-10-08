package com.ninezero.halo.feature.notification.presentation.creator_notifications

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.profile.domain.model.FollowedCreator

data class CreatorNotificationsState(
    val creators: List<FollowedCreator> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val isLoadingMore: Boolean = false,
    val snackbarState: SnackbarState? = null
)

sealed interface CreatorNotificationsIntent {
    data object LoadMore : CreatorNotificationsIntent
    data object Retry : CreatorNotificationsIntent
    data class ToggleNotify(val creator: FollowedCreator) : CreatorNotificationsIntent
    data object NavigateBack : CreatorNotificationsIntent
    data object SnackbarDismissed : CreatorNotificationsIntent
}

sealed interface CreatorNotificationsSideEffect {
    data object NavigateBack : CreatorNotificationsSideEffect
}
