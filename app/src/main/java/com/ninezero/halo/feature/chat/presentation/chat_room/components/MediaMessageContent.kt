package com.ninezero.halo.feature.chat.presentation.chat_room.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.media.MediaUtils
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.util.toReadableFileSize
import com.ninezero.halo.core.util.toVideoDuration
import com.ninezero.halo.feature.chat.domain.model.Message
import com.ninezero.halo.feature.chat.domain.model.MessageStatus
import com.ninezero.halo.feature.chat.presentation.chat_room.AttachmentUploadStatus

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

@Composable
internal fun ImageMessageContent(
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
        if (displayCount == 1) {
            SingleImageMessageItem(
                url = displayUrls[0],
                onClick = { onClick(0) },
                width = mediaWidth,
                height = mediaHeight
            )
            return@Box
        }

        // 칸 사이 2dp, 모서리는 묶음 바깥만
        Box(modifier = Modifier.clip(MaterialTheme.shapes.medium)) {
            when (displayCount) {
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
                    // 왼쪽 큰 1장 + 오른쪽 작은 2장
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 2f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        ImageGridItem(
                            url = displayUrls[0],
                            onClick = { onClick(0) },
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxHeight()
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            for (index in 1..2) {
                                ImageGridItem(
                                    url = displayUrls[index],
                                    onClick = { onClick(index) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                )
                            }
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
    // 알게 된 비율은 화면 밖에 나갔다 와도 유지
    var aspectRatio by rememberSaveable(url) { mutableFloatStateOf(initialRatio) }

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
                .clip(MaterialTheme.shapes.medium)
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
internal fun VideoMessageContent(
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
    // 알게 된 비율은 화면 밖에 나갔다 와도 유지
    var aspectRatio by rememberSaveable(thumbnailUrl) { mutableFloatStateOf(initialRatio) }

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
                .clip(MaterialTheme.shapes.medium)
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
                            shape = MaterialTheme.shapes.extraSmall
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
internal fun FileMessageContent(
    fileName: String,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fileSize: Long = 0L,
    isDownloading: Boolean = false,
    downloadProgress: Float = 0f
) {
    val extension = MediaUtils.fileExtension(fileName)
    val shape = MaterialTheme.shapes.medium

    Row(
        modifier = modifier
            .width(200.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .cardBorder(shape)
            // 카드를 눌러도 받기, 받을 때마다 새로 저장
            .clickable(enabled = enabled && !isDownloading) { onDownload() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (extension.isNotEmpty()) {
                Text(
                    text = extension.take(4),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_file),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                text = when {
                    isDownloading && fileSize > 0 ->
                        "${(fileSize * downloadProgress).toLong().toReadableFileSize()} / ${fileSize.toReadableFileSize()}"
                    isDownloading -> "${(downloadProgress * 100).toInt()}%"
                    fileSize > 0 -> fileSize.toReadableFileSize()
                    else -> extension
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .bounceClick(enabled = enabled)
                .size(36.dp)
                .clip(CircleShape)
                // 받는 중엔 바탕 대신 둘레 링이 진행을 보여줌
                .background(if (isDownloading) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = enabled) { if (isDownloading) onCancel() else onDownload() },
            contentAlignment = Alignment.Center
        ) {
            if (isDownloading) {
                CircularProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.matchParentSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.5.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp
                )
            }
            Icon(
                painter = painterResource(if (isDownloading) R.drawable.ic_close else R.drawable.ic_arrow_down_to_line),
                contentDescription = if (isDownloading) "다운로드 취소" else "다운로드",
                modifier = Modifier.size(if (isDownloading) 16.dp else 18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun BoxScope.MediaUploadOverlay(message: Message) {
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
private fun UploadOverlay(
    uploadStatus: AttachmentUploadStatus,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val isFailed = uploadStatus == AttachmentUploadStatus.FAILED
    // 실패는 옅게만 덮고 표시는 오른쪽 ! 가 맡음
    Box(
        modifier = modifier.background(Color.Black.copy(alpha = if (isFailed) 0.25f else 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        if (!isFailed) content()
    }
}
