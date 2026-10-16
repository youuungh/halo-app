package com.ninezero.halo.feature.chat.presentation.chat_room.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.post.LocalSubscribeRequester
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.core.util.toMessageTime
import com.ninezero.halo.core.util.toReadableFileSize
import com.ninezero.halo.core.util.toVideoDuration
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.Message
import com.ninezero.halo.feature.chat.domain.model.MessageStatus
import com.ninezero.halo.feature.chat.domain.model.PostMessageInfo
import com.ninezero.halo.feature.chat.domain.model.ProductMessageInfo
import com.ninezero.halo.feature.chat.presentation.chat_room.AttachmentUploadStatus
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

private const val CHAT_SINGLE_MEDIA_MIN_RATIO = 3f / 4f
private const val CHAT_SINGLE_MEDIA_MAX_RATIO = 16f / 9f
private const val CHAT_SINGLE_IMAGE_DEFAULT_RATIO = 1f
private const val CHAT_SINGLE_VIDEO_DEFAULT_RATIO = 16f / 9f
private val CHAT_SINGLE_MEDIA_MAX_WIDTH = 240.dp
private val CHAT_SINGLE_MEDIA_MAX_HEIGHT = 220.dp

private fun clampChatSingleRatio(ratio: Float): Float =
    ratio.coerceIn(CHAT_SINGLE_MEDIA_MIN_RATIO, CHAT_SINGLE_MEDIA_MAX_RATIO)

private fun calcChatSingleMediaSize(
    availableWidth: Dp,
    ratio: Float
): Pair<Dp, Dp> {
    val clampedRatio = clampChatSingleRatio(ratio)
    val safeWidth = availableWidth.coerceAtLeast(1.dp)
    val rawHeight = safeWidth / clampedRatio
    val targetHeight = rawHeight.coerceAtMost(CHAT_SINGLE_MEDIA_MAX_HEIGHT)
    val targetWidth = (targetHeight * clampedRatio).coerceAtMost(safeWidth)
    return targetWidth to targetHeight
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier,
    showTime: Boolean = true,
    searchQuery: String = "",
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
    onCancelFailed: () -> Unit = {}
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

    // 미디어+캡션이면 시각·안읽음을 캡션 옆에
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
                if (!message.isRead) UnreadIndicator()
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
                        searchQuery = searchQuery
                    )
                }
            }
        } else {
            // 미디어+텍스트면 아래에 캡션 말풍선
            Column(
                horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                                    onClick = { index -> onImageClick(originals, index) }
                                )
                                if (message.status != MessageStatus.SENT) {
                                    MediaUploadOverlay(message = message)
                                }
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
                                    }
                                )
                                if (message.status != MessageStatus.SENT) {
                                    MediaUploadOverlay(message = message)
                                }
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
                                    onDownload = { onFileClick(fileUrl, fileName) }
                                )
                                if (message.status != MessageStatus.SENT) {
                                    MediaUploadOverlay(message = message)
                                }
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
                    // 시각/안읽음 캡션
                    Row(verticalAlignment = Alignment.Bottom) {
                        if (isFromMe && showTime && message.status == MessageStatus.SENT) {
                            Column(horizontalAlignment = Alignment.End) {
                                if (!message.isRead) UnreadIndicator()
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
                                searchQuery = searchQuery
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
    }
}

@Composable
private fun TextMessageContent(
    content: String,
    isFromMe: Boolean,
    modifier: Modifier = Modifier,
    searchQuery: String = ""
) {
    val textColor = if (isFromMe) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface
    val highlightColor = Color(0xFFFFEB3B)

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
private fun ImageMessageContent(
    displayUrls: List<String>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    mediaWidth: Int? = null,
    mediaHeight: Int? = null
) {
    val imageCount = displayUrls.size
    val displayCount = minOf(imageCount, 4)
    val extraCount = imageCount - 4

    Box(modifier = modifier.widthIn(max = 240.dp)) {
        when (displayCount) {
            1 -> {
                SingleImageMessageItem(
                    url = displayUrls[0],
                    onClick = { onClick(0) },
                    width = mediaWidth,
                    height = mediaHeight
                )
            }

            2 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    displayUrls.take(2).forEachIndexed { index, url ->
                        ImageGridItem(
                            url = url,
                            onClick = { onClick(index) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }

            3 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    displayUrls.take(3).forEachIndexed { index, url ->
                        ImageGridItem(
                            url = url,
                            onClick = { onClick(index) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        displayUrls.take(2).forEachIndexed { index, url ->
                            ImageGridItem(
                                url = url,
                                onClick = { onClick(index) },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        ImageGridItem(
                            url = displayUrls[2],
                            onClick = { onClick(2) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                        Box(modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)) {
                            ImageGridItem(
                                url = displayUrls[3],
                                onClick = { onClick(3) },
                                modifier = Modifier.fillMaxSize()
                            )
                            if (extraCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .clickable { onClick(3) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+$extraCount",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleImageMessageItem(
    url: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Int? = null,
    height: Int? = null
) {
    // 서버가 준 크기가 있으면 로드 전부터 정확한 비율로 자리 예약
    val initialRatio = if (width != null && height != null && width > 0 && height > 0) {
        clampChatSingleRatio(width.toFloat() / height.toFloat())
    } else CHAT_SINGLE_IMAGE_DEFAULT_RATIO
    var aspectRatio by remember(url) { mutableFloatStateOf(initialRatio) }

    BoxWithConstraints(
        modifier = modifier.widthIn(max = CHAT_SINGLE_MEDIA_MAX_WIDTH)
    ) {
        val availableWidth = maxWidth.coerceAtMost(CHAT_SINGLE_MEDIA_MAX_WIDTH)
        val (targetWidth, targetHeight) = calcChatSingleMediaSize(
            availableWidth = availableWidth,
            ratio = aspectRatio
        )

        Box(
            modifier = Modifier
                .width(targetWidth)
                .height(targetHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = url,
                contentDescription = "이미지",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onSuccess = { success ->
                    val image = success.result.image
                    val width = image.width
                    val height = image.height
                    if (width > 0 && height > 0) {
                        aspectRatio = clampChatSingleRatio(width.toFloat() / height.toFloat())
                    }
                }
            )

            if (url.isBlank() || url.startsWith("https://example.com")) {
                Icon(
                    painter = painterResource(R.drawable.ic_image),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ImageGridItem(
    url: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = url,
            contentDescription = "이미지",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (url.isBlank() || url.startsWith("https://example.com")) {
            Icon(
                painter = painterResource(R.drawable.ic_image),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VideoMessageContent(
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null,
    duration: Long = 0L,
    width: Int? = null,
    height: Int? = null,
    showPlayButton: Boolean = true,
    onClick: () -> Unit
) {
    // 서버가 준 크기가 있으면 로드 전부터 정확한 비율로 자리 예약
    val initialRatio = if (width != null && height != null && width > 0 && height > 0) {
        clampChatSingleRatio(width.toFloat() / height.toFloat())
    } else CHAT_SINGLE_VIDEO_DEFAULT_RATIO
    var aspectRatio by remember(thumbnailUrl) { mutableFloatStateOf(initialRatio) }

    BoxWithConstraints(
        modifier = modifier.widthIn(max = CHAT_SINGLE_MEDIA_MAX_WIDTH)
    ) {
        val availableWidth = maxWidth.coerceAtMost(CHAT_SINGLE_MEDIA_MAX_WIDTH)
        val (targetWidth, targetHeight) = calcChatSingleMediaSize(
            availableWidth = availableWidth,
            ratio = aspectRatio
        )

        Box(
            modifier = Modifier
                .width(targetWidth)
                .height(targetHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onClick() }
        ) {
            if (thumbnailUrl != null) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = "비디오 썸네일",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onSuccess = { success ->
                        val image = success.result.image
                        val width = image.width
                        val height = image.height
                        if (width > 0 && height > 0) {
                            aspectRatio = clampChatSingleRatio(width.toFloat() / height.toFloat())
                        }
                    }
                )
            }

            // 재생 버튼, 업로드 완료 전 숨김
            if (showPlayButton) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play),
                        contentDescription = "재생",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
            }

            // 재생 시간
            if (duration > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = duration.toVideoDuration(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FileMessageContent(
    fileName: String,
    modifier: Modifier = Modifier,
    fileSize: Long = 0L,
    isDownloading: Boolean = false,
    downloadProgress: Float = 0f,
    onDownload: () -> Unit
) {
    val extension = MediaUtils.fileExtension(fileName)

    Row(
        modifier = modifier
            .widthIn(max = 200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_file),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (fileSize > 0) "$extension · ${fileSize.toReadableFileSize()}" else extension,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isDownloading) Color.Transparent
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .then(
                    if (!isDownloading) Modifier
                        .bounceClick()
                        .clickable { onDownload() }
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDownloading) {
                CircularProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                Text(
                    text = "${(downloadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_down_to_line),
                    contentDescription = "다운로드",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PostMessageContent(
    postInfo: PostMessageInfo?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLockedInfo: (String) -> Unit = {}
) {
    val subscribeRequester = LocalSubscribeRequester.current
    val isDeleted = postInfo == null
    // 크리에이터 해제로 숨겨진 글, 삭제·잠금과 구분
    val isHidden = postInfo?.isHidden == true
    val isRestricted = postInfo?.canAccess == false && !isHidden
    val isOverlayVisible = isDeleted || isHidden || isRestricted
    val isClickable = postInfo != null && postInfo.canAccess
    val hazeState = remember { HazeState() }
    val mediaAttachments = postInfo?.mediaAttachments ?: emptyList()
    val hasMedia = mediaAttachments.isNotEmpty()
    val isVideo = postInfo?.mediaType == "VIDEO"

    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .widthIn(max = 220.dp)
            .then(
                if (isOverlayVisible) Modifier
                    .widthIn(min = 200.dp)
                    .heightIn(min = 120.dp) else Modifier
            )
            .shadow(
                elevation = 3.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 0.25.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
            .then(
                when {
                    isClickable -> Modifier.clickable { onClick() }
                    // TIER 잠금만 구독 시트, FREE·null 잠금은 안내만
                    isRestricted -> Modifier.clickable {
                        if (postInfo.requiredTier != null && postInfo.requiredTier != "FREE" && postInfo.creatorId != null) {
                            subscribeRequester(postInfo.creatorId, postInfo.authorName)
                        } else {
                            onLockedInfo(
                                if (postInfo.creatorId != null) "${postInfo.authorName} 팔로워 전용 포스트예요"
                                else "차단한 유저의 포스트예요"
                            )
                        }
                    }

                    else -> Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier
                .then(if (isOverlayVisible) Modifier.hazeSource(state = hazeState) else Modifier)
        ) {
            Row(
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = postInfo?.authorAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_profile_placeholder),
                        error = painterResource(R.drawable.ic_profile_placeholder),
                        fallback = painterResource(R.drawable.ic_profile_placeholder)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = postInfo?.authorName ?: "알 수 없음",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!postInfo?.content.isNullOrBlank()) {
                Text(
                    text = postInfo.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (hasMedia) {
                if (mediaAttachments.size == 1) {
                    // 공유 카드의 미디어는 프리뷰
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = if (isVideo) postInfo.thumbnailUrl
                            else postInfo?.mediaThumbnails?.firstOrNull()
                                ?: mediaAttachments.first(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (isVideo) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_play),
                                    contentDescription = "재생",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // 여러 미디어는 가로로, 4개부터 +N
                    val displayCount = minOf(mediaAttachments.size, 3)
                    val remainingCount = mediaAttachments.size - 3

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0 until displayCount) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = postInfo?.mediaThumbnails?.getOrNull(i)
                                        ?: mediaAttachments[i],
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // 동영상 타일 재생 배지
                                if (postInfo?.mediaTypes?.getOrNull(i) == "VIDEO" && !(i == 2 && remainingCount > 0)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_play),
                                            contentDescription = "재생",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                                // 3번째 이미지에 +N 오버레이
                                if (i == 2 && remainingCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+$remainingCount",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 액션 영역
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_heart),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${postInfo?.likeCount ?: 0}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_comment),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${postInfo?.commentCount ?: 0}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${postInfo?.shareCount ?: 0}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (isOverlayVisible) {
            val tier = postInfo?.requiredTier
            LinkStatusOverlay(
                // TIER1·2는 멤버십 잠금, FREE·null은 차단 안내
                message = when {
                    isDeleted -> "삭제된 포스트"
                    isHidden -> "유효하지 않은 포스트"
                    tier != null && tier != "FREE" -> "$tier 이상 멤버십 전용"
                    postInfo.creatorId != null -> "팔로워 전용 포스트"
                    else -> "차단한 유저의 포스트"
                },
                creatorName = if (!isHidden && (tier != null && tier != "FREE" || postInfo?.creatorId != null)) postInfo.authorName else null,
                iconRes = when {
                    isDeleted -> R.drawable.ic_delete
                    isHidden -> R.drawable.ic_eye_off
                    else -> R.drawable.ic_lock
                },
                hazeState = hazeState,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
private fun ProductMessageContent(
    productInfo: ProductMessageInfo?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLockedInfo: (String) -> Unit = {}
) {
    val subscribeRequester = LocalSubscribeRequester.current
    val isDeleted = productInfo == null
    val isRestricted = productInfo?.canAccess == false
    val isOverlayVisible = isDeleted || isRestricted
    val isClickable = productInfo != null && productInfo.canAccess
    val hazeState = remember { HazeState() }
    val hasImage = !productInfo?.imageUrl.isNullOrBlank()

    val numberFormat = rememberKoreanNumberFormat()
    val hasDiscount = productInfo?.originalPrice != null
            && productInfo.discountRate != null
            && productInfo.discountRate > 0
    val formattedPrice = productInfo?.price?.toBigDecimalOrNull()
        ?.let { numberFormat.format(it) } ?: productInfo?.price

    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .width(248.dp)
            .height(128.dp)
            .shadow(
                elevation = 3.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 0.25.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
            .then(
                when {
                    isClickable -> Modifier.clickable { onClick() }
                    isRestricted -> Modifier.clickable {
                        if (productInfo.requiredTier != null && productInfo.requiredTier != "FREE" && productInfo.creatorId != null) {
                            subscribeRequester(productInfo.creatorId, productInfo.creatorName ?: "")
                        } else {
                            onLockedInfo("${productInfo.creatorName ?: "크리에이터"} 팔로워 전용 상품이에요")
                        }
                    }

                    else -> Modifier
                }
            )
    ) {
        // 상품 이미지
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .hazeSource(state = hazeState),
            contentAlignment = Alignment.Center
        ) {
            // 플레이스홀더
            Icon(
                painter = painterResource(R.drawable.ic_image),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            if (hasImage) {
                AsyncImage(
                    model = productInfo.imageUrl,
                    contentDescription = productInfo.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (isOverlayVisible) {
            // 잠금/삭제
            val tier = productInfo?.requiredTier
            LinkStatusOverlay(
                // FREE·null 잠금은 팔로워 전용 안내
                message = when {
                    isDeleted -> "삭제된 상품"
                    tier != null && tier != "FREE" -> "$tier 이상 멤버십 전용"
                    else -> "팔로워 전용 상품"
                },
                creatorName = if (isDeleted) null else productInfo.creatorName ?: "크리에이터",
                iconRes = if (isDeleted) R.drawable.ic_delete else R.drawable.ic_lock,
                hazeState = hazeState,
                modifier = Modifier.matchParentSize()
            )
        } else {
            val isPreview = LocalInspectionMode.current
            val variantColor = MaterialTheme.colorScheme.surfaceVariant
            val barGlass = if (isPreview) {
                Modifier.background(Color.Black.copy(alpha = 0.45f))
            } else {
                Modifier.haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = variantColor,
                        tint = HaloBlurTint(Color.Black.copy(alpha = 0.35f)),
                        blurRadius = 20.dp,
                        noiseFactor = 0f
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(46.dp)
                    .then(barGlass)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 상품명
                Text(
                    text = productInfo.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // 가격
                if (hasDiscount) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "${productInfo.discountRate}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alignByBaseline()
                        )
                        Text(
                            text = "${formattedPrice}원",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                } else {
                    Text(
                        text = "${formattedPrice ?: "가격 정보 없음"}원",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkStatusOverlay(
    message: String,
    iconRes: Int,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    creatorName: String? = null
) {
    val isPreview = LocalInspectionMode.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val overlayModifier = if (isPreview) {
        Modifier.background(Color.Black.copy(alpha = 0.55f))
    } else {
        Modifier.haloBlur(state = hazeState) {
            style = HaloBlurStyle(
                backgroundColor = surfaceColor,
                tint = HaloBlurTint(Color.Black.copy(alpha = 0.55f)),
                blurRadius = 14.dp,
                noiseFactor = 0f
            )
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(overlayModifier),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (creatorName != null) {
                Text(
                    text = creatorName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
private fun BoxScope.MediaUploadOverlay(message: Message) {
    UploadOverlay(
        uploadStatus = if (message.status == MessageStatus.FAILED) {
            AttachmentUploadStatus.FAILED
        } else {
            AttachmentUploadStatus.UPLOADING
        },
        modifier = Modifier.matchParentSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LoadingIndicator(
                size = LoadingIndicatorSize.Medium,
                color = Color.White
            )
            if (message.uploadProgress > 0f) {
                Text(
                    text = "${(message.uploadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
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
            .clip(RoundedCornerShape(12.dp))
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

@Composable
private fun UploadOverlay(
    uploadStatus: AttachmentUploadStatus,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier.background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        if (uploadStatus == AttachmentUploadStatus.FAILED) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_warning),
                    contentDescription = "전송 실패",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
                Text(
                    text = "전송 실패",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            content()
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageBubblePreview() {
    HaloTheme {
        Column {
            MessageBubble(
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
                        price = "20300",
                        originalPrice = "29000",
                        discountRate = 30,
                        imageUrl = null
                    ),
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 120000,
                    isFromMe = false
                )
            )
            MessageBubble(
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
            // FAILED: 좌측 재시도/취소 아이콘 + 전송 실패 라벨
            MessageBubble(
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
                        price = "15,000원",
                        imageUrl = null,
                        canAccess = false
                    ),
                    isRead = true, readAt = null,
                    createdAt = System.currentTimeMillis() - 240000,
                    isFromMe = false
                )
            )
            MessageBubble(
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
                onDownload = {}
            )
            FileMessageContent(
                fileName = "report.pdf",
                fileSize = 1024 * 1024 * 2,
                isDownloading = true,
                downloadProgress = 0.8f,
                onDownload = {}
            )
            FileMessageContent(
                fileName = "document.pdf",
                fileSize = 1024 * 500,
                isDownloading = false,
                onDownload = {}
            )
        }
    }
}

