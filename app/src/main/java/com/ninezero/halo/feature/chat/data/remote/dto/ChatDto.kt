package com.ninezero.halo.feature.chat.data.remote.dto

import com.ninezero.halo.core.network.PagedDto
import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.core.network.dto.toDomain
import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.chat.domain.model.Chat
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.LastMessageInfo
import com.ninezero.halo.feature.chat.domain.model.Message
import com.ninezero.halo.feature.chat.domain.model.PostMessageInfo
import com.ninezero.halo.feature.chat.domain.model.ProductMessageInfo
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val otherUserId: Int
)

@Serializable
data class ChatDto(
    val id: Int,
    val otherUser: UserSummaryDto,
    val lastMessage: LastMessageInfoDto? = null,
    val unreadCount: Int = 0,
    val isArchived: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class LastMessageInfoDto(
    val preview: String,
    val timestamp: String
)

@Serializable
data class SendMessageRequest(
    val content: String? = null,
    val chatMessageType: String = ChatMessageType.TEXT.name,
    val mediaAttachments: List<String>? = null,
    val mediaThumbnails: List<String>? = null,
    val productId: Int? = null,
    val thumbnailUrl: String? = null,
    val duration: Long? = null,
    val mediaWidth: Int? = null,
    val mediaHeight: Int? = null,
    val fileSize: Long? = null,
    val fileName: String? = null,
    // 재전송 중복 방지용 클라이언트 생성 식별자
    val clientTempId: String? = null,
    // 직접 업로드한 영상·파일
    val uploadId: Int? = null
)

@Serializable
data class ChatUploadPrepareRequest(
    val messageType: String,
    val mimeType: String,
    val size: Long,
    val fileName: String? = null
)

@Serializable
data class MessageDto(
    val id: Int,
    val roomId: Int,
    val sender: UserSummaryDto,
    val chatMessageType: String,
    val content: String? = null,
    val mediaAttachments: List<String>? = null,
    val mediaThumbnails: List<String>? = null,
    val thumbnailUrl: String? = null,
    val duration: Long? = null,
    val mediaWidth: Int? = null,
    val mediaHeight: Int? = null,
    val fileSize: Long? = null,
    val fileName: String? = null,
    val postInfo: PostMessageInfoDto? = null,
    val productInfo: ProductMessageInfoDto? = null,
    val isRead: Boolean = false,
    val readAt: String? = null,
    val createdAt: String,
    val isFromMe: Boolean = false,
    val isDeleted: Boolean = false
)

@Serializable
data class ProductMessageInfoDto(
    val productId: Int,
    val name: String,
    val price: String,
    val originalPrice: String? = null,
    val discountRate: Int? = null,
    val imageUrl: String? = null,
    val canAccess: Boolean = true,
    val creatorId: Int? = null,
    val creatorName: String? = null,
    val requiredTier: String? = null
)

@Serializable
data class PostMessageInfoDto(
    val postId: Int,
    val content: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val imageUrl: String? = null,
    val mediaAttachments: List<String> = emptyList(),
    val mediaType: String? = null,
    val thumbnailUrl: String? = null,
    val mediaThumbnails: List<String> = emptyList(),
    val mediaTypes: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val canAccess: Boolean = true,
    val creatorId: Int? = null,
    val requiredTier: String? = null,
    val isHidden: Boolean = false  // 크리에이터 해제로 숨겨진 글, 삭제와 구분
)

@Serializable
data class MessageListResponseDto(
    val messages: List<MessageDto>,
    val lastMessageId: Int? = null,
    val lastReadMessageId: Int? = null,
    val hasNext: Boolean = false
)

@Serializable
data class MarkMessagesReadResponseDto(
    val lastReadMessageId: Int? = null
)

typealias PaginatedMessageResponseDto = PagedDto<MessageDto>

/** 상대 읽음 이벤트 */
@Serializable
data class MessageReadEventDto(
    val roomId: Int,
    val messageIds: List<Int>,
    val readerId: Int,
    val readAt: String
)

/** 상대 삭제 이벤트 */
@Serializable
data class MessageDeletedEventDto(
    val roomId: Int,
    val messageId: Int
)

typealias ChatRoomListResponseDto = PagedDto<ChatDto>

fun ChatDto.toDomain() = Chat(
    id = id,
    otherUser = otherUser.toDomain(),
    lastMessage = lastMessage?.toDomain(),
    unreadCount = unreadCount,
    isArchived = isArchived,
    createdAt = createdAt.parseTimestamp()
)

fun LastMessageInfoDto.toDomain() = LastMessageInfo(
    preview = preview,
    timestamp = timestamp.parseTimestamp()
)

fun MessageDto.toDomain() = Message(
    id = id,
    roomId = roomId,
    sender = sender.toDomain(),
    chatMessageType = ChatMessageType.from(chatMessageType),
    content = content,
    mediaAttachments = mediaAttachments,
    mediaThumbnails = mediaThumbnails,
    thumbnailUrl = thumbnailUrl,
    duration = duration,
    mediaWidth = mediaWidth,
    mediaHeight = mediaHeight,
    fileSize = fileSize,
    fileName = fileName,
    productInfo = productInfo?.toDomain(),
    postInfo = postInfo?.toDomain(),
    isRead = isRead,
    readAt = readAt?.parseTimestamp(),
    createdAt = createdAt.parseTimestamp(),
    isFromMe = isFromMe,
    isDeleted = isDeleted
)

fun ProductMessageInfoDto.toDomain() = ProductMessageInfo(
    productId = productId,
    name = name,
    price = price,
    originalPrice = originalPrice,
    discountRate = discountRate,
    imageUrl = imageUrl,
    canAccess = canAccess,
    creatorId = creatorId,
    creatorName = creatorName,
    requiredTier = requiredTier
)

fun PostMessageInfoDto.toDomain() = PostMessageInfo(
    postId = postId,
    content = content,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    mediaAttachments = mediaAttachments,
    mediaType = mediaType,
    thumbnailUrl = thumbnailUrl,
    mediaThumbnails = mediaThumbnails,
    mediaTypes = mediaTypes,
    likeCount = likeCount,
    commentCount = commentCount,
    shareCount = shareCount,
    canAccess = canAccess,
    creatorId = creatorId,
    requiredTier = requiredTier,
    isHidden = isHidden
)
