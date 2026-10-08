package com.ninezero.halo.feature.chat.data.mapper

import com.ninezero.halo.core.database.entity.CachedMessageEntity
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.Message
import com.ninezero.halo.feature.chat.domain.model.MessageStatus
import com.ninezero.halo.feature.chat.domain.model.PostMessageInfo
import com.ninezero.halo.feature.chat.domain.model.ProductMessageInfo
import kotlinx.serialization.json.Json

object MessageCacheMapper {

    private val json = Json { ignoreUnknownKeys = true }

    fun Message.toEntity(): CachedMessageEntity {
        return CachedMessageEntity(
            id = id,
            roomId = roomId,
            senderId = sender.id,
            senderUsername = sender.username,
            senderDisplayName = sender.displayName,
            senderAvatarUrl = sender.avatarUrl,
            chatMessageType = chatMessageType.name,
            content = content,
            mediaAttachments = mediaAttachments?.let { json.encodeToString(it) },
            mediaThumbnails = mediaThumbnails?.let { json.encodeToString(it) },
            thumbnailUrl = thumbnailUrl,
            duration = duration,
            mediaWidth = mediaWidth,
            mediaHeight = mediaHeight,
            fileSize = fileSize,
            fileName = fileName,
            productId = productInfo?.productId,
            productName = productInfo?.name,
            productPrice = productInfo?.price,
            productOriginalPrice = productInfo?.originalPrice,
            productDiscountRate = productInfo?.discountRate,
            productImageUrl = productInfo?.imageUrl,
            productCanAccess = productInfo?.canAccess,
            productCreatorId = productInfo?.creatorId,
            productCreatorName = productInfo?.creatorName,
            productRequiredTier = productInfo?.requiredTier,
            postId = postInfo?.postId,
            postContent = postInfo?.content,
            postAuthorName = postInfo?.authorName,
            postAuthorAvatarUrl = postInfo?.authorAvatarUrl,
            postCanAccess = postInfo?.canAccess,
            postMediaAttachments = postInfo?.mediaAttachments?.let { json.encodeToString(it) },
            postMediaType = postInfo?.mediaType,
            postThumbnailUrl = postInfo?.thumbnailUrl,
            postMediaThumbnails = postInfo?.mediaThumbnails?.let { json.encodeToString(it) },
            postMediaTypes = postInfo?.mediaTypes?.let { json.encodeToString(it) },
            postLikeCount = postInfo?.likeCount,
            postCommentCount = postInfo?.commentCount,
            postShareCount = postInfo?.shareCount,
            postCreatorId = postInfo?.creatorId,
            postRequiredTier = postInfo?.requiredTier,
            postIsHidden = postInfo?.isHidden,
            isRead = isRead,
            readAt = readAt,
            createdAt = createdAt,
            isFromMe = isFromMe,
            isDeleted = isDeleted,
            status = status.name,
            clientTempId = clientTempId,
            uploadProgress = uploadProgress
        )
    }

    fun CachedMessageEntity.toDomain(): Message {
        return Message(
            id = id,
            roomId = roomId,
            sender = UserSummary(
                id = senderId,
                username = senderUsername,
                displayName = senderDisplayName,
                avatarUrl = senderAvatarUrl
            ),
            chatMessageType = ChatMessageType.from(chatMessageType),
            content = content,
            mediaAttachments = mediaAttachments?.let {
                try {
                    json.decodeFromString<List<String>>(it)
                } catch (e: Exception) {
                    null
                }
            },
            mediaThumbnails = mediaThumbnails?.let {
                try {
                    json.decodeFromString<List<String>>(it)
                } catch (e: Exception) {
                    null
                }
            },
            thumbnailUrl = thumbnailUrl,
            duration = duration,
            mediaWidth = mediaWidth,
            mediaHeight = mediaHeight,
            fileSize = fileSize,
            fileName = fileName,
            productInfo = if (productId != null && productName != null && productPrice != null) {
                ProductMessageInfo(
                    productId = productId,
                    name = productName,
                    price = productPrice,
                    originalPrice = productOriginalPrice,
                    discountRate = productDiscountRate,
                    imageUrl = productImageUrl,
                    canAccess = productCanAccess ?: true,
                    creatorId = productCreatorId,
                    creatorName = productCreatorName,
                    requiredTier = productRequiredTier
                )
            } else null,
            postInfo = if (postId != null && postContent != null && postAuthorName != null) {
                PostMessageInfo(
                    postId = postId,
                    content = postContent,
                    authorName = postAuthorName,
                    authorAvatarUrl = postAuthorAvatarUrl,
                    mediaAttachments = postMediaAttachments?.let {
                        try {
                            json.decodeFromString<List<String>>(it)
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } ?: emptyList(),
                    mediaType = postMediaType,
                    thumbnailUrl = postThumbnailUrl,
                    mediaThumbnails = postMediaThumbnails?.let {
                        try {
                            json.decodeFromString<List<String>>(it)
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } ?: emptyList(),
                    mediaTypes = postMediaTypes?.let {
                        try {
                            json.decodeFromString<List<String>>(it)
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } ?: emptyList(),
                    likeCount = postLikeCount ?: 0,
                    commentCount = postCommentCount ?: 0,
                    shareCount = postShareCount ?: 0,
                    canAccess = postCanAccess ?: true,
                    creatorId = postCreatorId,
                    requiredTier = postRequiredTier,
                    isHidden = postIsHidden ?: false
                )
            } else null,
            isRead = isRead,
            readAt = readAt,
            createdAt = createdAt,
            isFromMe = isFromMe,
            isDeleted = isDeleted,
            status = MessageStatus.from(status),
            clientTempId = clientTempId,
            uploadProgress = uploadProgress
        )
    }

    fun List<Message>.toEntities(): List<CachedMessageEntity> = map { it.toEntity() }
}
