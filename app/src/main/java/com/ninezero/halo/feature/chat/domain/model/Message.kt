package com.ninezero.halo.feature.chat.domain.model

import com.ninezero.halo.core.model.UserSummary

data class Message(
    val id: Int,
    val roomId: Int,
    val sender: UserSummary,
    val chatMessageType: ChatMessageType,
    val content: String?,
    val mediaAttachments: List<String>?,
    val mediaThumbnails: List<String>? = null,  // 목록 표시용
    val thumbnailUrl: String? = null,
    val duration: Long? = null,
    val mediaWidth: Int? = null,
    val mediaHeight: Int? = null,
    val fileSize: Long? = null,
    val fileName: String? = null,
    val productInfo: ProductMessageInfo?,
    val postInfo: PostMessageInfo? = null,
    val isRead: Boolean,
    val readAt: Long?,
    val createdAt: Long,
    val isFromMe: Boolean,
    val isDeleted: Boolean = false,
    val status: MessageStatus = MessageStatus.SENT,
    val clientTempId: String? = null,  // 임시 id, null이면 서버 메시지
    val uploadProgress: Float = 1f  // 0~1, 텍스트는 항상 1
)

data class ProductMessageInfo(
    val productId: Int,
    val name: String,
    val price: String,
    val originalPrice: String? = null,
    val discountRate: Int? = null,
    val imageUrl: String?,
    val canAccess: Boolean = true,
    val creatorId: Int? = null,
    val creatorName: String? = null,
    val requiredTier: String? = null
)

data class PostMessageInfo(
    val postId: Int,
    val content: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val mediaAttachments: List<String> = emptyList(),
    val mediaType: String? = null,
    val thumbnailUrl: String? = null,
    val mediaThumbnails: List<String> = emptyList(),  // 타일별 썸네일
    val mediaTypes: List<String> = emptyList(),  // IMAGE·VIDEO
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val canAccess: Boolean = true,
    val creatorId: Int? = null,
    val requiredTier: String? = null,
    val isHidden: Boolean = false
)

data class MessageListResult(
    val messages: List<Message>,
    val lastReadMessageId: Int?,
    val hasNext: Boolean
)
