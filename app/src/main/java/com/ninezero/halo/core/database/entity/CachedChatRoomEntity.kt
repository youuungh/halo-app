package com.ninezero.halo.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_chat_rooms",
    // 목록 정렬 키 인덱스
    indices = [
        Index(value = ["isArchived", "lastMessageTimestamp"])
    ]
)
data class CachedChatRoomEntity(
    @PrimaryKey
    val id: Int,
    // 상대 정보
    val otherUserId: Int,
    val otherUserUsername: String,
    val otherUserDisplayName: String?,
    val otherUserAvatarUrl: String?,
    val otherUserAvatarThumbUrl: String?,
    val otherUserRole: String,
    val otherUserFollowerCount: Int,
    val otherUserFollowingCount: Int,
    val otherUserPostCount: Int,
    val otherUserIsFollowing: Boolean,
    // 마지막 메시지 정보
    val lastMessagePreview: String?,
    val lastMessageTimestamp: Long?,
    // 카운트·플래그
    val unreadCount: Int,
    val isArchived: Boolean,
    val isBlockedByMe: Boolean,
    val isBlockedByOther: Boolean,
    val createdAt: Long,
    val cachedAt: Long = System.currentTimeMillis(),
    val isHistoryComplete: Boolean = false
)
