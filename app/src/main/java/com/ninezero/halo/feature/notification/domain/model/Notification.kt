package com.ninezero.halo.feature.notification.domain.model

data class Notification(
    val id: Int,
    val senderId: Int?,
    val senderUsername: String?,
    val senderAvatarUrl: String?,
    val type: String,
    val message: String,
    val targetType: String?,
    val targetId: Int?,
    val imageUrl: String?,
    val deepLink: String?,
    val isRead: Boolean,
    val createdAt: Long
)

data class NotificationListResult(
    val notifications: List<Notification>,
    val unreadCount: Int,
    val nextCursor: String?,
)
