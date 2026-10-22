package com.ninezero.halo.feature.chat.domain.model

import com.ninezero.halo.core.model.UserSummary

data class Chat(
    val id: Int,
    val otherUser: UserSummary,
    val lastMessage: LastMessageInfo?,
    val unreadCount: Int,
    val isArchived: Boolean,
    val isBlockedByMe: Boolean = false,
    val isBlockedByOther: Boolean = false,
    val createdAt: Long
)

data class LastMessageInfo(
    val preview: String,
    val timestamp: Long
)
