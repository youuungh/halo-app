package com.ninezero.halo.feature.chat.presentation.chat_list

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.chat.domain.model.Chat
import com.ninezero.halo.feature.chat.domain.model.LastMessageInfo

internal fun previewChatListState(): ChatListState {
    return ChatListState(
        chatRooms = previewChatRooms
    )
}

internal fun previewChatListLoadingState(): ChatListState {
    return ChatListState(isLoading = true)
}

internal fun previewChatListEmptyState(): ChatListState {
    return ChatListState()
}

internal fun previewChatListErrorState(): ChatListState {
    return ChatListState(error = "네트워크 연결을 확인해주세요")
}

private val previewChatRooms = listOf(
    previewChatRoom(
        id = 1,
        userId = 1,
        username = "user1",
        displayName = "김민수",
        avatarUrl = "avatarUrl",
        lastPreview = "안녕하세요! 오늘 시간 되시나요?",
        lastMessageOffset = 3_600_000,
        unreadCount = 3
    ),
    previewChatRoom(
        id = 2,
        userId = 2,
        username = "user2",
        displayName = "이지은",
        avatarUrl = "avatarUrl",
        isFollowing = true,
        lastPreview = "네, 감사합니다!",
        lastMessageOffset = 86_400_000
    )
)

private fun previewChatRoom(
    id: Int,
    userId: Int,
    username: String,
    displayName: String,
    avatarUrl: String?,
    lastPreview: String,
    lastMessageOffset: Long,
    isFollowing: Boolean = false,
    unreadCount: Int = 0,
    now: Long = System.currentTimeMillis()
): Chat {
    return Chat(
        id = id,
        otherUser = previewUserSummary(
            id = userId,
            username = username,
            displayName = displayName,
            avatarUrl = avatarUrl,
            isFollowing = isFollowing
        ),
        lastMessage = LastMessageInfo(
            preview = lastPreview,
            timestamp = now - lastMessageOffset
        ),
        unreadCount = unreadCount,
        isArchived = false,
        createdAt = now
    )
}

@HaloLightDarkPreview
@Composable
private fun ChatListScreenPreview() {
    HaloScreenPreview {
        ChatListScreenContent(
            state = previewChatListState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ChatListScreenLoadingPreview() {
    HaloScreenPreview {
        ChatListScreenContent(
            state = previewChatListLoadingState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ChatListScreenEmptyPreview() {
    HaloScreenPreview {
        ChatListScreenContent(
            state = previewChatListEmptyState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ChatListScreenErrorPreview() {
    HaloScreenPreview {
        ChatListScreenContent(
            state = previewChatListErrorState(),
            onIntent = {}
        )
    }
}
