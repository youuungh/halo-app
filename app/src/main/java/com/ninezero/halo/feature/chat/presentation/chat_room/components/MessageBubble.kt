package com.ninezero.halo.feature.chat.presentation.chat_room.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninezero.halo.R
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.SearchMatchYellow
import com.ninezero.halo.core.ui.theme.Warning
import com.ninezero.halo.core.util.toMessageTime
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.Message
import com.ninezero.halo.feature.chat.domain.model.MessageStatus
import com.ninezero.halo.feature.chat.domain.model.PostMessageInfo
import com.ninezero.halo.feature.chat.domain.model.ProductMessageInfo

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier,
    showTime: Boolean = true,
    searchQuery: String = "",
    isCurrentSearchMatch: Boolean = false,
    isDownloading: Boolean = false,
    downloadProgress: Float = 0f,
    onLongClick: () -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onVideoClick: (String, String?) -> Unit = { _, _ -> },
    onFileClick: (String, String) -> Unit = { _, _ -> },
    onPostClick: (Int, Boolean) -> Unit = { _, _ -> },
    onProductClick: (Int, Boolean) -> Unit = { _, _ -> },
    onLockedInfo: (String) -> Unit = {},
    onRetryFailed: () -> Unit = {},
    onCancelFailed: () -> Unit = {},
    onFileDownloadCancel: () -> Unit
) {
    val isFromMe = message.isFromMe

    val needsBubble = message.isDeleted ||
            message.chatMessageType == ChatMessageType.TEXT ||
            // 사진 없는 사진 메시지는 글 말풍선으로
            (message.chatMessageType == ChatMessageType.IMAGE && message.mediaAttachments.isNullOrEmpty()) ||
            message.chatMessageType !in listOf(
        ChatMessageType.TEXT,
        ChatMessageType.IMAGE,
        ChatMessageType.VIDEO,
        ChatMessageType.FILE,
        ChatMessageType.POST_LINK,
        ChatMessageType.PRODUCT_LINK
    )

    // 미디어 + 캡션이면 시각·안읽음을 캡션 옆에
    val caption = message.content
    val showCaption = !needsBubble && !caption.isNullOrBlank() &&
            message.chatMessageType in listOf(
        ChatMessageType.IMAGE,
        ChatMessageType.VIDEO,
        ChatMessageType.FILE
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // 내가 보낸 SENDING 텍스트만 좌측에 로딩 인디케이터
        if (isFromMe && message.status == MessageStatus.SENDING &&
            message.chatMessageType == ChatMessageType.TEXT
        ) {
            LoadingIndicator(
                size = LoadingIndicatorSize.Small,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        // 보낸 실패 메시지는 왼쪽에 재시도·취소
        if (isFromMe && message.status == MessageStatus.FAILED) {
            FailedActionButton(
                iconRes = R.drawable.ic_rotate_cw,
                contentDescription = "재시도",
                onClick = onRetryFailed
            )
            Spacer(modifier = Modifier.width(6.dp))
            FailedActionButton(
                iconRes = R.drawable.ic_close,
                contentDescription = "취소",
                onClick = onCancelFailed
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        // SENT에만 우측 unread + 시간
        if (isFromMe && showTime && message.status == MessageStatus.SENT && !showCaption) {
            Column(horizontalAlignment = Alignment.End) {
                if (!message.isRead && !message.isDeleted) UnreadIndicator()  // 삭제 메시지는 읽음 처리 대상에서 제외
                MessageTime(timestamp = message.createdAt)
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        if (needsBubble) {
            val bubbleShape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            )

            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(bubbleShape)
                    .background(
                        if (isFromMe) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { if (isFromMe && !message.isDeleted) onLongClick() }
                    )
            ) {
                if (message.isDeleted) {
                    DeletedMessageContent(isFromMe = isFromMe)
                } else {
                    TextMessageContent(
                        content = message.content ?: "[지원하지 않는 메시지]",
                        isFromMe = isFromMe,
                        searchQuery = searchQuery,
                        isCurrentSearchMatch = isCurrentSearchMatch
                    )
                }
            }
        } else {
            // 미디어 + 텍스트면 아래에 캡션 말풍선
            Column(
                horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { if (isFromMe && !message.isDeleted) onLongClick() }
                        )
                ) {
                    when (message.chatMessageType) {
                        ChatMessageType.IMAGE -> {
                            Box {
                                val originals = message.mediaAttachments ?: emptyList()
                                ImageMessageContent(
                                    // 말풍선은 썸네일, 풀스크린은 원본
                                    displayUrls = originals.mapIndexed { i, url ->
                                        message.mediaThumbnails?.getOrNull(
                                            i
                                        ) ?: url
                                    },
                                    mediaWidth = message.mediaWidth,
                                    mediaHeight = message.mediaHeight,
                                    onClick = { index -> onImageClick(originals, index) },
                                    onLongClick = { if (isFromMe && !message.isDeleted) onLongClick() }
                                )
                                MediaUploadOverlay(message = message)
                            }
                        }

                        ChatMessageType.VIDEO -> {
                            Box {
                                VideoMessageContent(
                                    thumbnailUrl = message.thumbnailUrl,
                                    duration = message.duration ?: 0L,
                                    width = message.mediaWidth,
                                    height = message.mediaHeight,
                                    // 업로드 완료 전엔 재생 배지 숨김
                                    showPlayButton = message.status == MessageStatus.SENT,
                                    onClick = {
                                        onVideoClick(
                                            message.mediaAttachments?.firstOrNull() ?: "",
                                            message.thumbnailUrl
                                        )
                                    },
                                    onLongClick = { if (isFromMe && !message.isDeleted) onLongClick() }
                                )
                                MediaUploadOverlay(message = message)
                            }
                        }

                        ChatMessageType.FILE -> {
                            val fileUrl = message.mediaAttachments?.firstOrNull() ?: ""
                            val fileName = message.fileName ?: fileUrl.substringAfterLast("/")
                            Box {
                                FileMessageContent(
                                    fileName = fileName,
                                    fileSize = message.fileSize ?: 0L,
                                    isDownloading = isDownloading,
                                    downloadProgress = downloadProgress,
                                    // 업로드 전 파일은 아직 서버에 없음
                                    enabled = message.status == MessageStatus.SENT,
                                    onDownload = { onFileClick(fileUrl, fileName) },
                                    onCancel = onFileDownloadCancel,
                                    onLongClick = { if (isFromMe && !message.isDeleted) onLongClick() }
                                )
                                MediaUploadOverlay(message = message)
                            }
                        }

                        ChatMessageType.POST_LINK -> {
                            PostMessageContent(
                                postInfo = message.postInfo,
                                onClick = {
                                    message.postInfo?.let {
                                        onPostClick(
                                            it.postId,
                                            it.canAccess
                                        )
                                    }
                                },
                                onLockedInfo = onLockedInfo
                            )
                        }

                        ChatMessageType.PRODUCT_LINK -> {
                            ProductMessageContent(
                                productInfo = message.productInfo,
                                onClick = {
                                    message.productInfo?.let {
                                        onProductClick(
                                            it.productId,
                                            it.canAccess
                                        )
                                    }
                                },
                                onLockedInfo = onLockedInfo
                            )
                        }
                    }
                }

                if (showCaption) {
                    val captionShape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromMe) 16.dp else 4.dp,
                        bottomEnd = if (isFromMe) 4.dp else 16.dp
                    )
                    // 시각·안읽음 캡션
                    Row(verticalAlignment = Alignment.Bottom) {
                        if (isFromMe && showTime && message.status == MessageStatus.SENT) {
                            Column(horizontalAlignment = Alignment.End) {
                                if (!message.isRead && !message.isDeleted) UnreadIndicator()
                                MessageTime(timestamp = message.createdAt)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(captionShape)
                                .background(
                                    if (isFromMe) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { if (isFromMe && !message.isDeleted) onLongClick() }
                                )
                        ) {
                            TextMessageContent(
                                content = caption,
                                isFromMe = isFromMe,
                                searchQuery = searchQuery,
                                isCurrentSearchMatch = isCurrentSearchMatch
                            )
                        }
                        if (!isFromMe && showTime) {
                            Spacer(modifier = Modifier.width(4.dp))
                            MessageTime(timestamp = message.createdAt)
                        }
                    }
                }
            }
        }

        if (!isFromMe && showTime && !showCaption) {
            Spacer(modifier = Modifier.width(4.dp))
            MessageTime(timestamp = message.createdAt)
        }

        if (isFromMe && message.status == MessageStatus.FAILED) {
            Spacer(modifier = Modifier.width(4.dp))
            FailedBadge()
        }
    }
}

@Composable
private fun TextMessageContent(
    content: String,
    isFromMe: Boolean,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    isCurrentSearchMatch: Boolean = false
) {
    val textColor = if (isFromMe) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface
    // 일치는 모두 노랑, 지금 보고 있는 결과만 주황
    val highlightColor = if (isCurrentSearchMatch) Warning else SearchMatchYellow

    if (searchQuery.isNotBlank() && content.contains(searchQuery, ignoreCase = true)) {
        val annotatedString = buildAnnotatedString {
            var currentIndex = 0
            val lowerContent = content.lowercase()
            val lowerQuery = searchQuery.lowercase()

            while (currentIndex < content.length) {
                val matchIndex = lowerContent.indexOf(lowerQuery, currentIndex)
                if (matchIndex == -1) {
                    append(content.substring(currentIndex))
                    break
                } else {
                    if (matchIndex > currentIndex) {
                        append(content.substring(currentIndex, matchIndex))
                    }
                    withStyle(SpanStyle(background = highlightColor, color = Color.Black)) {
                        append(content.substring(matchIndex, matchIndex + searchQuery.length))
                    }
                    currentIndex = matchIndex + searchQuery.length
                }
            }
        }

        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    } else {
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun MessageTime(
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    Text(
        text = timestamp.toMessageTime(),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    )
}

@Composable
private fun UnreadIndicator(modifier: Modifier = Modifier) {
    Text(
        text = "1",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun FailedActionButton(
    @androidx.annotation.DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 전송 실패 표시, 말풍선 오른쪽 아래 */
@Composable
private fun FailedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error)
            .semantics { contentDescription = "전송 실패" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "!",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
private fun DeletedMessageContent(
    isFromMe: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor = if (isFromMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Text(
        text = "삭제된 메시지입니다",
        style = MaterialTheme.typography.bodyMedium,
        color = textColor,
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@HaloLightDarkPreview
@Composable
private fun MessageBubblePreview() {
    HaloTheme {
        Column {
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 1, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.TEXT,
                    content = "안녕하세요!",
                    mediaAttachments = null, productInfo = null,
                    isRead = false, readAt = null,
                    createdAt = System.currentTimeMillis(),
                    isFromMe = true
                )
            )
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 2, roomId = 1,
                    sender = UserSummary(
                        id = 2,
                        username = "user1",
                        displayName = "김민수",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.TEXT,
                    content = "네, 안녕하세요! 반갑습니다.",
                    mediaAttachments = null, productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 60000,
                    isFromMe = false
                )
            )
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 3, roomId = 1,
                    sender = UserSummary(
                        id = 2,
                        username = "user1",
                        displayName = "김민수",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.PRODUCT_LINK,
                    content = null, mediaAttachments = null,
                    productInfo = ProductMessageInfo(
                        productId = 1,
                        name = "프리미엄 일러스트 에셋 팩",
                        price = 20300L,
                        originalPrice = 29000L,
                        discountRate = 30,
                        imageUrl = null
                    ),
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 120000,
                    isFromMe = false
                )
            )
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 4, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.POST_LINK,
                    content = null, mediaAttachments = null,
                    productInfo = null,
                    postInfo = PostMessageInfo(
                        postId = 7,
                        content = "오늘 작업한 스케치 일부 공유해요. 피드백 부탁드립니다!",
                        authorName = "나",
                        authorAvatarUrl = null,
                        mediaAttachments = listOf(
                            "https://example.com/sketch1.jpg",
                            "https://example.com/sketch2.jpg"
                        ),
                        mediaType = "IMAGE",
                        likeCount = 12,
                        commentCount = 5,
                        shareCount = 3
                    ),
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 180000,
                    isFromMe = true
                )
            )
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 5, roomId = 1,
                    sender = UserSummary(
                        id = 2,
                        username = "user1",
                        displayName = "김민수",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.POST_LINK,
                    content = null, mediaAttachments = null,
                    productInfo = null,
                    postInfo = PostMessageInfo(
                        postId = 9,
                        content = "새로운 브이로그 올렸어요! 한번 봐주세요 :)",
                        authorName = "김민수",
                        authorAvatarUrl = null,
                        mediaAttachments = listOf("https://example.com/vlog.mp4"),
                        mediaType = "VIDEO",
                        thumbnailUrl = "https://example.com/vlog_thumb.jpg",
                        likeCount = 34,
                        commentCount = 7,
                        shareCount = 11
                    ),
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 240000,
                    isFromMe = false
                )
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageBubbleTextStatusPreview() {
    HaloTheme {
        Column {
            // SENDING: 전송 중 라벨
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = -1, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.TEXT,
                    content = "방금 보낸 메시지 (전송 중)",
                    mediaAttachments = null, productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis(),
                    isFromMe = true,
                    status = MessageStatus.SENDING,
                    clientTempId = "preview-sending-1"
                )
            )
            // FAILED: 좌측 재시도·취소 아이콘 + 전송 실패 라벨
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = -2, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.TEXT,
                    content = "전송에 실패한 메시지입니다",
                    mediaAttachments = null, productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 30000,
                    isFromMe = true,
                    status = MessageStatus.FAILED,
                    clientTempId = "preview-failed-1"
                )
            )
            // SENT 대조군
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 100, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.TEXT,
                    content = "정상 전송된 메시지",
                    mediaAttachments = null, productInfo = null,
                    isRead = false, readAt = null,
                    createdAt = System.currentTimeMillis() - 60000,
                    isFromMe = true
                )
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SharedLinkRestrictedPreview() {
    HaloTheme {
        Column {
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 5, roomId = 1,
                    sender = UserSummary(
                        id = 2,
                        username = "user1",
                        displayName = "김민수",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.PRODUCT_LINK,
                    content = null, mediaAttachments = null,
                    productInfo = ProductMessageInfo(
                        productId = 2,
                        name = "TIER2 한정 팬미팅 VOD",
                        price = 15000L,
                        imageUrl = null,
                        canAccess = false
                    ),
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 240000,
                    isFromMe = false
                )
            )
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 6, roomId = 1,
                    sender = UserSummary(
                        id = 2,
                        username = "user1",
                        displayName = "김민수",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.POST_LINK,
                    content = null, mediaAttachments = null,
                    productInfo = null,
                    postInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 300000,
                    isFromMe = false
                )
            )
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 7, roomId = 1,
                    sender = UserSummary(
                        id = 2,
                        username = "user1",
                        displayName = "김민수",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.POST_LINK,
                    content = null, mediaAttachments = null,
                    productInfo = null,
                    postInfo = PostMessageInfo(
                        postId = 8,
                        content = "멤버십 전용 포스트 미리보기",
                        authorName = "김민수",
                        authorAvatarUrl = null,
                        mediaAttachments = listOf("https://example.com/exclusive.jpg"),
                        mediaType = "IMAGE",
                        likeCount = 42,
                        commentCount = 8,
                        shareCount = 5,
                        canAccess = false
                    ),
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 360000,
                    isFromMe = false
                )
            )
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 8, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.PRODUCT_LINK,
                    content = null, mediaAttachments = null,
                    productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 420000,
                    isFromMe = true
                )
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageBubbleSearchHighlightPreview() {
    HaloTheme {
        Column {
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 1, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.TEXT,
                    content = "안녕하세요! 오늘 날씨가 좋네요.",
                    mediaAttachments = null, productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis(),
                    isFromMe = true
                ),
                searchQuery = "날씨"
            )
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 2, roomId = 1,
                    sender = UserSummary(
                        id = 2,
                        username = "user1",
                        displayName = "김민수",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.TEXT,
                    content = "네, 정말 날씨가 좋아요! 날씨 덕분에 기분이 좋네요.",
                    mediaAttachments = null, productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 60000,
                    isFromMe = false
                ),
                searchQuery = "날씨"
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageBubbleDeletedPreview() {
    HaloTheme {
        Column {
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 1, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.TEXT,
                    content = "원래 내용",
                    mediaAttachments = null, productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis(),
                    isFromMe = true, isDeleted = true
                )
            )
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 2, roomId = 1,
                    sender = UserSummary(
                        id = 2,
                        username = "user1",
                        displayName = "김민수",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.TEXT,
                    content = "원래 내용",
                    mediaAttachments = null, productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 60000,
                    isFromMe = false, isDeleted = true
                )
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageBubbleImageStatusPreview() {
    HaloTheme {
        Column {
            // SENDING - 진행률 30%
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = -1, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.IMAGE,
                    content = null,
                    mediaAttachments = listOf("https://example.com/image1.jpg"),
                    productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis(),
                    isFromMe = true,
                    status = MessageStatus.SENDING,
                    clientTempId = "preview-image-sending",
                    uploadProgress = 0.3f
                )
            )
            // SENDING 진행률 0%
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = -2, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.IMAGE,
                    content = null,
                    mediaAttachments = listOf("https://example.com/image2.jpg"),
                    productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 10000,
                    isFromMe = true,
                    status = MessageStatus.SENDING,
                    clientTempId = "preview-image-sending-2",
                    uploadProgress = 0f
                )
            )
            // 실패: 왼쪽 재시도·취소
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = -3, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.IMAGE,
                    content = null,
                    mediaAttachments = listOf("https://example.com/image3.jpg"),
                    productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 30000,
                    isFromMe = true,
                    status = MessageStatus.FAILED,
                    clientTempId = "preview-image-failed"
                )
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageBubbleImageSinglePreview() {
    HaloTheme {
        MessageBubble(
            onFileDownloadCancel = {},
            message = Message(
                id = 1, roomId = 1,
                sender = UserSummary(id = 1, username = "me", displayName = "나", avatarUrl = ""),
                chatMessageType = ChatMessageType.IMAGE,
                content = null,
                mediaAttachments = listOf("https://example.com/image1.jpg"),
                productInfo = null,
                isRead = true, readAt = null,
                createdAt = System.currentTimeMillis(),
                isFromMe = true
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageBubbleImageTwoPreview() {
    HaloTheme {
        MessageBubble(
            onFileDownloadCancel = {},
            message = Message(
                id = 1, roomId = 1,
                sender = UserSummary(
                    id = 2,
                    username = "user",
                    displayName = "상대방",
                    avatarUrl = ""
                ),
                chatMessageType = ChatMessageType.IMAGE,
                content = null,
                mediaAttachments = listOf(
                    "https://example.com/image1.jpg",
                    "https://example.com/image2.jpg"
                ),
                productInfo = null,
                isRead = true, readAt = null,
                createdAt = System.currentTimeMillis(),
                isFromMe = false
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageBubbleImageThreePreview() {
    HaloTheme {
        MessageBubble(
            onFileDownloadCancel = {},
            message = Message(
                id = 1, roomId = 1,
                sender = UserSummary(id = 1, username = "me", displayName = "나", avatarUrl = ""),
                chatMessageType = ChatMessageType.IMAGE,
                content = null,
                mediaAttachments = listOf(
                    "https://example.com/image1.jpg",
                    "https://example.com/image2.jpg",
                    "https://example.com/image3.jpg"
                ),
                productInfo = null,
                isRead = true, readAt = null,
                createdAt = System.currentTimeMillis(),
                isFromMe = true
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageBubbleImageGridPreview() {
    HaloTheme {
        MessageBubble(
            onFileDownloadCancel = {},
            message = Message(
                id = 1, roomId = 1,
                sender = UserSummary(id = 1, username = "me", displayName = "나", avatarUrl = ""),
                chatMessageType = ChatMessageType.IMAGE,
                content = null,
                mediaAttachments = listOf(
                    "https://example.com/image1.jpg",
                    "https://example.com/image2.jpg",
                    "https://example.com/image3.jpg",
                    "https://example.com/image4.jpg",
                    "https://example.com/image5.jpg",
                    "https://example.com/image6.jpg"
                ),
                productInfo = null,
                isRead = true, readAt = null,
                createdAt = System.currentTimeMillis(),
                isFromMe = true
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageBubbleVideoPreview() {
    HaloTheme {
        Column {
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 1, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.VIDEO,
                    content = null,
                    mediaAttachments = listOf("https://example.com/video.mp4"),
                    duration = 125000L,
                    productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis(),
                    isFromMe = true
                )
            )
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 2, roomId = 1,
                    sender = UserSummary(
                        id = 2,
                        username = "user",
                        displayName = "상대방",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.VIDEO,
                    content = null,
                    mediaAttachments = listOf("https://example.com/video2.mp4"),
                    productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 60000,
                    isFromMe = false
                )
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageBubbleFilePreview() {
    HaloTheme {
        Column {
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 1, roomId = 1,
                    sender = UserSummary(
                        id = 1,
                        username = "me",
                        displayName = "나",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.FILE,
                    content = null,
                    mediaAttachments = listOf("https://example.com/document.pdf"),
                    fileName = "document.pdf",
                    fileSize = 1024 * 500,
                    productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis(),
                    isFromMe = true
                )
            )
            MessageBubble(
                onFileDownloadCancel = {},
                message = Message(
                    id = 2, roomId = 1,
                    sender = UserSummary(
                        id = 2,
                        username = "user",
                        displayName = "상대방",
                        avatarUrl = ""
                    ),
                    chatMessageType = ChatMessageType.FILE,
                    content = null,
                    mediaAttachments = listOf("https://example.com/report.xlsx"),
                    fileName = "report.xlsx",
                    fileSize = 1024 * 1024 * 2,
                    productInfo = null,
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 60000,
                    isFromMe = false
                )
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun FileDownloadingPreview() {
    HaloTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FileMessageContent(
                fileName = "presentation.pptx",
                fileSize = 1024 * 1024 * 5,
                isDownloading = true,
                downloadProgress = 0.45f,
                onDownload = {},
                onCancel = {},
                onLongClick = {}
            )
            FileMessageContent(
                fileName = "report.pdf",
                fileSize = 1024 * 1024 * 2,
                isDownloading = true,
                downloadProgress = 0.8f,
                onDownload = {},
                onCancel = {},
                onLongClick = {}
            )
            FileMessageContent(
                fileName = "document.pdf",
                fileSize = 1024 * 500,
                isDownloading = false,
                onDownload = {},
                onCancel = {},
                onLongClick = {}
            )
            FileMessageContent(
                fileName = "회원목록_2026.csv",
                fileSize = 1024 * 1024 * 14,
                onDownload = {},
                onCancel = {},
                onLongClick = {}
            )
        }
    }
}
