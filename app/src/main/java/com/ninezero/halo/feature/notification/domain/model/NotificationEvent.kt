package com.ninezero.halo.feature.notification.domain.model

sealed class NotificationEvent {
    data object Connected : NotificationEvent()
    data class NewNotification(val notification: Notification) : NotificationEvent()
    data object RefreshRequired : NotificationEvent()
}
