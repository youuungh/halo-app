package com.ninezero.halo.feature.chat.presentation.archived_chats

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.chat.domain.model.Chat
import com.ninezero.halo.feature.chat.domain.model.LastMessageInfo

internal fun previewArchivedChatsState(): ArchivedChatsState {
    return ArchivedChatsState(
        archivedChats = previewArchivedChats
    )
}

internal fun previewArchivedChatsEmptyState(): ArchivedChatsState {
    return ArchivedChatsState()
}

internal fun previewArchivedChatsLoadingState(): ArchivedChatsState {
    return ArchivedChatsState(isLoading = true)
}

internal fun previewArchivedChatsErrorState(): ArchivedChatsState {
    return ArchivedChatsState(error = "네트워크 연결을 확인해주세요")
}

private val previewArchivedChats = listOf(
    previewArchivedChat(
        id = 1, userId = 1, username = "user1", displayName = "김민수",
        lastPreview = "보관된 메시지입니다", lastMessageOffset = 3_600_000
    ),
    previewArchivedChat(
        id = 2, userId = 2, username = "user2", displayName = "이지은",
        isFollowing = true, lastPreview = "네, 감사합니다!",
        lastMessageOffset = 86_400_000
    ),
    previewArchivedChat(
        id = 3, userId = 3, username = "user3", displayName = "박서준",
        lastPreview = "다음에 또 연락할게요", lastMessageOffset = 172_800_000, unreadCount = 2
    ),
    previewArchivedChat(
        id = 4, userId = 4, username = "user4", displayName = "최유나",
        isFollowing = true, lastPreview = "사진 보내줘서 고마워요",
        lastMessageOffset = 259_200_000
    ),
    previewArchivedChat(
        id = 5, userId = 5, username = "user5", displayName = "정해인",
        lastPreview = null, lastMessageOffset = 0
    )
)

private fun previewArchivedChat(
    id: Int,
    userId: Int,
    username: String,
    displayName: String,
    isFollowing: Boolean = false,
    lastPreview: String?,
    lastMessageOffset: Long,
    unreadCount: Int = 0,
    now: Long = System.currentTimeMillis()
): Chat {
    return Chat(
        id = id,
        otherUser = previewUserSummary(
            id = userId,
            username = username,
            displayName = displayName,
            isFollowing = isFollowing
        ),
        lastMessage = lastPreview?.let {
            LastMessageInfo(
                preview = it,
                timestamp = now - lastMessageOffset
            )
        },
        unreadCount = unreadCount,
        isArchived = true,
        createdAt = now
    )
}

@HaloLightDarkPreview
@Composable
private fun ArchivedChatsScreenPreview() {
    HaloScreenPreview {
        ArchivedChatsScreenContent(
            state = previewArchivedChatsState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ArchivedChatsScreenLoadingPreview() {
    HaloScreenPreview {
        ArchivedChatsScreenContent(
            state = previewArchivedChatsLoadingState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ArchivedChatsScreenEmptyPreview() {
    HaloScreenPreview {
        ArchivedChatsScreenContent(
            state = previewArchivedChatsEmptyState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ArchivedChatsScreenErrorPreview() {
    HaloScreenPreview {
        ArchivedChatsScreenContent(
            state = previewArchivedChatsErrorState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}
