package com.ninezero.halo.feature.notification.data.remote.dto

import com.ninezero.halo.core.network.CursorDto
import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.notification.domain.model.Notification
import com.ninezero.halo.feature.notification.domain.model.normalizeTargetType
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: Int,
    val recipientId: Int,
    val senderId: Int? = null,
    val senderUsername: String? = null,
    val senderAvatarUrl: String? = null,
    val type: String,
    val category: String,
    val title: String,
    val message: String,
    val targetType: String? = null,
    val targetId: Int? = null,
    val imageUrl: String? = null,
    val deepLink: String? = null,
    val isRead: Boolean = false,
    val readAt: String? = null,
    val createdAt: String
)

typealias PaginatedNotificationsDto = CursorDto<NotificationDto>

@Serializable
data class NotificationListResponseDto(
    val notifications: PaginatedNotificationsDto,
    val unreadCount: Int
)

@Serializable
data class UnreadCountResponseDto(
    val count: Int
)

fun NotificationDto.toDomain() = Notification(
    id = id,
    senderId = senderId,
    senderUsername = senderUsername,
    senderAvatarUrl = senderAvatarUrl,
    type = type,
    message = message,
    targetType = normalizeTargetType(targetType),
    targetId = targetId,
    imageUrl = imageUrl,
    deepLink = deepLink,
    isRead = isRead,
    createdAt = createdAt.parseTimestamp()
)
