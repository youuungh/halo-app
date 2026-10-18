package com.ninezero.halo.feature.chat.presentation.chat_room

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.Message

internal fun previewChatRoomState(): ChatRoomState {
    val otherUser = previewUserSummary(id = 2, username = "minsoo_art", displayName = "김민수", avatarUrl = "avatarUrl", isFollowing = true)
    val me = previewUserSummary(id = 1, username = "me", displayName = "나", avatarUrl = "avatarUrl")
    return ChatRoomState(
        roomId = 1,
        otherUser = otherUser,
        hasMore = false,
        messages = listOf(
            previewChatMessage(16, me, "감사합니다! 기대할게요", 0, isFromMe = true, isRead = false),
            previewChatMessage(15, otherUser, "네! 작업 완료되면 바로 알려드릴게요~", 30_000, isFromMe = false),
            previewChatMessage(14, me, "좋아요! 그럼 진행 부탁드려요", 60_000, isFromMe = true),
            previewChatMessage(13, otherUser, "보통 2주 정도 소요되고, 가격은 사이즈에 따라 달라요", 90_000, isFromMe = false),
            previewChatMessage(12, me, "작업 기간이랑 가격은 어떻게 되나요?", 120_000, isFromMe = true),
            previewChatMessage(11, otherUser, "네 가능해요! 어떤 스타일로 원하세요?", 150_000, isFromMe = false),
            previewChatMessage(10, me, "혹시 커미션 가능하신가요?", 180_000, isFromMe = true),
            previewChatMessage(9, otherUser, "감사합니다! 열심히 그리고 있어요 ㅎㅎ", 210_000, isFromMe = false, isDeleted = true),
            previewChatMessage(8, me, "최근에 올리신 일러스트 너무 좋더라구요", 240_000, isFromMe = true),
            previewChatMessage(7, me, "작가님 작품 정말 예쁘네요!", 270_000, isFromMe = true),
            previewChatMessage(6, otherUser, "네, 안녕하세요! 반갑습니다", 300_000, isFromMe = false),
            previewChatMessage(5, me, "안녕하세요! 작가님 팬이에요", 330_000, isFromMe = true),
            previewChatMessage(4, otherUser, "팔로우 감사합니다!", 360_000, isFromMe = false),
            previewChatMessage(3, me, "작가님 팔로우했어요!", 390_000, isFromMe = true),
            previewChatMessage(2, otherUser, "안녕하세요~", 420_000, isFromMe = false)
        )
    )
}

internal fun previewChatRoomManyMessagesState(): ChatRoomState {
    val otherUser = previewUserSummary(id = 2, username = "artist_kim", displayName = "김아트", avatarUrl = "avatarUrl", isFollowing = true)
    val me = previewUserSummary(id = 1, username = "me", displayName = "나", avatarUrl = "avatarUrl")
    return ChatRoomState(
        roomId = 1,
        otherUser = otherUser,
        hasMore = false,
        isBlocked = true,
        messages = listOf(
            previewChatMessage(20, me, "네 알겠습니다! 감사해요", 0, isFromMe = true, isRead = false),
            previewChatMessage(19, otherUser, "작업 기간은 약 2주 정도 예상하시면 됩니다", 10_000, isFromMe = false),
            previewChatMessage(18, me, "기간은 얼마나 걸릴까요?", 20_000, isFromMe = true, isRead = false),
            previewChatMessage(17, otherUser, "반신 일러스트 기준 15만원부터 시작합니다", 30_000, isFromMe = false),
            previewChatMessage(16, me, "가격은 어떻게 되나요?", 40_000, isFromMe = true),
            previewChatMessage(15, otherUser, "네 가능합니다! 어떤 스타일로 원하시나요?", 50_000, isFromMe = false),
            previewChatMessage(14, me, "혹시 커미션 가능하신가요?", 60_000, isFromMe = true),
            previewChatMessage(13, otherUser, "감사합니다 ㅎㅎ 열심히 그리고 있어요!", 70_000, isFromMe = false),
            previewChatMessage(12, me, "작가님 그림체 정말 예뻐요!", 80_000, isFromMe = true),
            previewChatMessage(11, otherUser, "안녕하세요! 반갑습니다", 90_000, isFromMe = false),
            previewChatMessage(10, me, "안녕하세요!", 100_000, isFromMe = true),
            previewChatMessage(9, otherUser, "문의 주셔서 감사합니다", 200_000, isFromMe = false),
            previewChatMessage(8, me, "작품 구경하다 왔어요", 300_000, isFromMe = true),
            previewChatMessage(7, otherUser, "어떤 작품이 마음에 드셨나요?", 400_000, isFromMe = false),
            previewChatMessage(6, me, "최근에 올리신 판타지 일러스트요!", 500_000, isFromMe = true),
            previewChatMessage(5, otherUser, "아 그 작품이요! 좋아해주셔서 감사해요", 600_000, isFromMe = false),
            previewChatMessage(4, me, "색감이 정말 좋더라구요", 700_000, isFromMe = true),
            previewChatMessage(3, otherUser, "파스텔톤을 좋아해서요 ㅎㅎ", 800_000, isFromMe = false),
            previewChatMessage(2, me, "저도 파스텔톤 좋아해요!", 900_000, isFromMe = true),
            previewChatMessage(1, otherUser, "취향이 비슷하시네요", 1_000_000, isFromMe = false)
        )
    )
}

internal fun previewChatRoomSearchState(): ChatRoomState {
    val otherUser = previewUserSummary(id = 2, username = "artist_kim", displayName = "김작가", avatarUrl = "avatarUrl")
    val me = previewUserSummary(id = 1, username = "me", displayName = "나", avatarUrl = "avatarUrl")
    val messages = listOf(
        previewChatMessage(20, me, "일러스트 작업 의뢰 가능할까요?", 10_000, isFromMe = true),
        previewChatMessage(19, otherUser, "네! 일러스트 의뢰 받고 있어요", 20_000, isFromMe = false),
        previewChatMessage(18, me, "어떤 스타일의 일러스트가 가능한가요?", 30_000, isFromMe = true),
        previewChatMessage(17, otherUser, "판타지, 캐릭터, 배경 일러스트 다 가능해요!", 40_000, isFromMe = false),
        previewChatMessage(16, me, "캐릭터 일러스트로 부탁드릴게요", 50_000, isFromMe = true),
        previewChatMessage(15, otherUser, "좋아요! 어떤 캐릭터인가요?", 60_000, isFromMe = false),
        previewChatMessage(14, me, "제가 쓰고 있는 소설 주인공이에요", 70_000, isFromMe = true),
        previewChatMessage(13, otherUser, "오 소설이요? 재밌겠네요!", 80_000, isFromMe = false),
        previewChatMessage(12, me, "네 판타지 소설이에요", 90_000, isFromMe = true),
        previewChatMessage(11, otherUser, "판타지 일러스트 그리는 거 좋아해서 기대돼요", 100_000, isFromMe = false),
        previewChatMessage(10, me, "작가님 포트폴리오 봤는데 정말 멋지더라구요", 200_000, isFromMe = true),
        previewChatMessage(9, otherUser, "감사합니다! 열심히 그렸어요", 300_000, isFromMe = false),
        previewChatMessage(8, me, "일러스트 가격은 어떻게 되나요?", 400_000, isFromMe = true),
        previewChatMessage(7, otherUser, "캐릭터 일러스트는 5만원부터 시작해요", 500_000, isFromMe = false),
        previewChatMessage(6, me, "합리적이네요!", 600_000, isFromMe = true),
        previewChatMessage(5, otherUser, "감사해요 ㅎㅎ", 700_000, isFromMe = false),
        previewChatMessage(4, me, "그럼 진행 부탁드려요!", 800_000, isFromMe = true),
        previewChatMessage(3, otherUser, "네! 캐릭터 설정 보내주시면 바로 시작할게요", 900_000, isFromMe = false),
        previewChatMessage(2, me, "좋아요 지금 보낼게요", 1_000_000, isFromMe = true),
        previewChatMessage(1, otherUser, "기다리고 있을게요!", 1_100_000, isFromMe = false)
    )
    return ChatRoomState(
        roomId = 1,
        otherUser = otherUser,
        hasMore = false,
        messages = messages,
        isSearchMode = true,
        searchQuery = "일러스트",
        searchResults = messages.filter { it.content?.contains("일러스트", ignoreCase = true) == true },
        currentSearchIndex = 2
    )
}

internal fun previewChatRoomUnreadSeparatorState(): ChatRoomState {
    val otherUser = previewUserSummary(id = 2, username = "minsoo_art", displayName = "김민수", avatarUrl = "avatarUrl", isFollowing = true)
    val me = previewUserSummary(id = 1, username = "me", displayName = "나", avatarUrl = "avatarUrl")
    return ChatRoomState(
        roomId = 1,
        otherUser = otherUser,
        hasMore = false,
        messages = listOf(
            previewChatMessage(15, otherUser, "시간 되실 때 확인 부탁드려요!", 5_000, isFromMe = false, isRead = false),
            previewChatMessage(14, otherUser, "스케치 완성했어요! 한번 봐주세요", 30_000, isFromMe = false, isRead = false),
            previewChatMessage(13, otherUser, "안녕하세요! 작업 진행 상황 알려드립니다", 60_000, isFromMe = false, isRead = false),
            previewChatMessage(12, otherUser, "네! 캐릭터 설정 받았습니다. 작업 시작할게요", 300_000, isFromMe = false),
            previewChatMessage(11, me, "캐릭터 설정 보내드렸어요!", 350_000, isFromMe = true),
            previewChatMessage(10, me, "좋아요! 그럼 진행 부탁드려요", 400_000, isFromMe = true),
            previewChatMessage(9, otherUser, "반신 일러스트 기준 15만원부터 시작합니다", 500_000, isFromMe = false),
            previewChatMessage(8, me, "가격은 어떻게 되나요?", 600_000, isFromMe = true),
            previewChatMessage(7, otherUser, "네 가능해요! 어떤 스타일로 원하세요?", 700_000, isFromMe = false),
            previewChatMessage(6, me, "혹시 커미션 가능하신가요?", 800_000, isFromMe = true),
            previewChatMessage(5, otherUser, "안녕하세요! 반갑습니다", 900_000, isFromMe = false),
            previewChatMessage(4, me, "안녕하세요! 작가님 팬이에요", 1_000_000, isFromMe = true)
        ),
        unreadStartMessageId = 12,
        entryUnreadCount = 3
    )
}

internal fun previewChatRoomNewMessageBarState(): ChatRoomState {
    val otherUser = previewUserSummary(id = 2, username = "minsoo_art", displayName = "김민수", avatarUrl = "avatarUrl", isFollowing = true)
    val me = previewUserSummary(id = 1, username = "me", displayName = "나", avatarUrl = "avatarUrl")
    return ChatRoomState(
        roomId = 1,
        otherUser = otherUser,
        hasMore = false,
        messages = listOf(
            previewChatMessage(18, otherUser, "컬러 작업도 거의 끝나가요!", 0, isFromMe = false, isRead = false),
            previewChatMessage(17, otherUser, null, 5_000, isFromMe = false, isRead = false, type = ChatMessageType.IMAGE, mediaAttachments = listOf("https://example.com/sketch.jpg")),
            previewChatMessage(16, otherUser, "스케치 완성했어요!", 30_000, isFromMe = false, isRead = false),
            previewChatMessage(15, me, "좋아요! 기대할게요", 300_000, isFromMe = true),
            previewChatMessage(14, otherUser, "네! 작업 시작할게요", 350_000, isFromMe = false),
            previewChatMessage(13, me, "캐릭터 설정 보내드렸어요!", 400_000, isFromMe = true),
            previewChatMessage(12, me, "좋아요! 그럼 진행 부탁드려요", 500_000, isFromMe = true),
            previewChatMessage(11, otherUser, "반신 일러스트 기준 15만원부터 시작합니다", 600_000, isFromMe = false),
            previewChatMessage(10, me, "가격은 어떻게 되나요?", 700_000, isFromMe = true),
            previewChatMessage(9, otherUser, "네 가능해요! 어떤 스타일로 원하세요?", 800_000, isFromMe = false),
            previewChatMessage(8, me, "혹시 커미션 가능하신가요?", 900_000, isFromMe = true),
            previewChatMessage(7, otherUser, "안녕하세요! 반갑습니다 🙌", 1_000_000, isFromMe = false)
        ),
        unreadStartMessageId = 14,
        entryUnreadCount = 3
    )
}

internal fun previewChatRoomLoadingState(): ChatRoomState {
    return ChatRoomState(
        roomId = 1,
        otherUser = previewUserSummary(id = 2, username = "minsoo_art", displayName = "김민수"),
        entryPhase = EntryPhase.LOADING
    )
}

private fun previewChatMessage(
    id: Int,
    sender: UserSummary,
    content: String?,
    createdAtOffset: Long,
    isFromMe: Boolean,
    isRead: Boolean = true,
    type: ChatMessageType = ChatMessageType.TEXT,
    mediaAttachments: List<String>? = null,
    isDeleted: Boolean = false,
    now: Long = System.currentTimeMillis()
): Message {
    return Message(
        id = id,
        roomId = 1,
        sender = sender,
        chatMessageType = type,
        content = content,
        mediaAttachments = mediaAttachments,
        productInfo = null,
        isRead = isRead,
        readAt = null,
        createdAt = now - createdAtOffset,
        isFromMe = isFromMe,
        isDeleted = isDeleted
    )
}

@HaloLightDarkPreview
@Composable
private fun ChatRoomScreenPreview() {
    HaloScreenPreview {
        ChatRoomScreenContent(
            state = previewChatRoomState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ChatRoomScreenManyMessagesPreview() {
    HaloScreenPreview {
        ChatRoomScreenContent(
            state = previewChatRoomManyMessagesState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ChatRoomScreenSearchPreview() {
    HaloScreenPreview {
        ChatRoomScreenContent(
            state = previewChatRoomSearchState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ChatRoomScreenUnreadSeparatorPreview() {
    HaloScreenPreview {
        ChatRoomScreenContent(
            state = previewChatRoomUnreadSeparatorState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ChatRoomScreenNewMessageBarPreview() {
    HaloScreenPreview {
        ChatRoomScreenContent(
            state = previewChatRoomNewMessageBarState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ChatRoomScreenLoadingPreview() {
    HaloScreenPreview {
        ChatRoomScreenContent(
            state = previewChatRoomLoadingState(),
            onIntent = {}
        )
    }
}
