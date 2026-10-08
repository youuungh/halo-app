package com.ninezero.halo.feature.chat.data.mapper

import com.ninezero.halo.core.database.entity.CachedChatRoomEntity
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.feature.chat.domain.model.Chat
import com.ninezero.halo.feature.chat.domain.model.LastMessageInfo

object ChatRoomCacheMapper {

    fun Chat.toEntity(): CachedChatRoomEntity {
        return CachedChatRoomEntity(
            id = id,
            otherUserId = otherUser.id,
            otherUserUsername = otherUser.username,
            otherUserDisplayName = otherUser.displayName,
            otherUserAvatarUrl = otherUser.avatarUrl,
            otherUserRole = otherUser.role,
            otherUserFollowerCount = otherUser.followerCount,
            otherUserFollowingCount = otherUser.followingCount,
            otherUserPostCount = otherUser.postCount,
            otherUserIsFollowing = otherUser.isFollowing,
            lastMessagePreview = lastMessage?.preview,
            lastMessageTimestamp = lastMessage?.timestamp,
            unreadCount = unreadCount,
            isArchived = isArchived,
            createdAt = createdAt
        )
    }

    fun CachedChatRoomEntity.toDomain(): Chat {
        return Chat(
            id = id,
            otherUser = UserSummary(
                id = otherUserId,
                username = otherUserUsername,
                displayName = otherUserDisplayName,
                avatarUrl = otherUserAvatarUrl,
                role = otherUserRole,
                followerCount = otherUserFollowerCount,
                followingCount = otherUserFollowingCount,
                postCount = otherUserPostCount,
                isFollowing = otherUserIsFollowing
            ),
            lastMessage = if (lastMessagePreview != null && lastMessageTimestamp != null) {
                LastMessageInfo(
                    preview = lastMessagePreview,
                    timestamp = lastMessageTimestamp
                )
            } else null,
            unreadCount = unreadCount,
            isArchived = isArchived,
            createdAt = createdAt
        )
    }

    fun List<Chat>.toEntities(): List<CachedChatRoomEntity> = map { it.toEntity() }

    fun List<CachedChatRoomEntity>.toDomains(): List<Chat> = map { it.toDomain() }
}
