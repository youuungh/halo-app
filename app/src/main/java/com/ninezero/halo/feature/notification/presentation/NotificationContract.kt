package com.ninezero.halo.feature.notification.presentation

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.notification.domain.model.Notification

data class NotificationState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val hasMore: Boolean = false,
    val nextCursor: String? = null,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,
    val deletedNotification: Notification? = null,
    val toastMessage: String? = null
)

sealed interface NotificationIntent {
    data object LoadNotifications : NotificationIntent
    data object RefreshNotifications : NotificationIntent
    data object RefreshOnResume : NotificationIntent
    data object LoadMore : NotificationIntent
    data class DeleteNotification(val notificationId: Int) : NotificationIntent
    data object UndoDelete : NotificationIntent
    data object SnackbarDismissed : NotificationIntent
    data object DismissToast : NotificationIntent
    data class NavigateToTarget(val notification: Notification) : NotificationIntent
    data object NavigateBack : NotificationIntent
}

sealed interface NotificationSideEffect {
    data object NavigateBack : NotificationSideEffect
    data class NavigateToPost(val postId: Int, val highlightCommentId: Int? = null) : NotificationSideEffect  // null이면 일반 진입
    data class NavigateToComment(val commentId: Int, val highlightReplyId: Int? = null) : NotificationSideEffect  // null이면 앵커만
    data class NavigateToUser(val userId: Int) : NotificationSideEffect
    data class NavigateToOrder(val orderId: Int) : NotificationSideEffect
    data class NavigateToChatRoom(val roomId: Int) : NotificationSideEffect
    data class NavigateToProduct(val productId: Int) : NotificationSideEffect
    data object NavigateToPoint : NotificationSideEffect
    data object NavigateToCreatorApplication : NotificationSideEffect
    data object NavigateToMySubscriptions : NotificationSideEffect
    data object NavigateToSalesManage : NotificationSideEffect
    data class NavigateToReviewList(val productId: Int, val reviewId: Int?) : NotificationSideEffect
}
