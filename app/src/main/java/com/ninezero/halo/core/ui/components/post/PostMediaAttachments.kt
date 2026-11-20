package com.ninezero.halo.core.ui.components.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.core.ui.components.media.ImageThumbnailInfoStore
import com.ninezero.halo.core.ui.components.media.MediaDisplayPolicy
import com.ninezero.halo.core.ui.components.media.VideoPlayer
import com.ninezero.halo.core.ui.components.media.clampedAspectRatio
import com.ninezero.halo.core.ui.components.media.preferredAspectRatio
import com.ninezero.halo.core.ui.components.media.rememberAnimatedImage
import com.ninezero.halo.core.ui.components.media.singleMediaHeightForWidth
import com.ninezero.halo.core.ui.components.modifiers.LocalActiveViewerMediaKey
import com.ninezero.halo.core.ui.components.modifiers.LocalMediaSharedTransitionScope
import com.ninezero.halo.core.ui.components.modifiers.blockHorizontalScrollLeak
import com.ninezero.halo.core.ui.components.modifiers.mediaSharedElement
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview
import com.ninezero.halo.core.ui.preview.previewMediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType

/** 글 미디어, 하나면 크게 여럿이면 가로 줄 */
@Composable
internal fun PostMediaAttachments(
    attachments: List<MediaItem>,
    startPadding: Dp,
    endPadding: Dp = 8.dp,
    isUploading: Boolean = false,
    isVideoPlaying: Boolean = false,
    onMediaClick: (index: Int) -> Unit = {}
) {
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    val multiMediaHeight = MediaDisplayPolicy.FEED_MULTI_ITEM_HEIGHT
    // 썸네일 morph용, scope null이면 일반 표시
    val mediaSharedScope = LocalMediaSharedTransitionScope.current
    val activeViewerKey = LocalActiveViewerMediaKey.current

    if (attachments.size == 1) {
        val media = attachments.first()
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = startPadding, end = endPadding)
        ) {
            val availableWidth = maxWidth
            // 세로 영상은 긴 비율 허용, 이미지 4:5
            val singleMinRatio = if (media.type == MediaType.VIDEO) {
                MediaDisplayPolicy.FEED_SINGLE_VIDEO_MIN_RATIO
            } else {
                MediaDisplayPolicy.FEED_SINGLE_MIN_RATIO
            }
            val singleMediaHeight = singleMediaHeightForWidth(
                availableWidth = availableWidth,
                media = media,
                minRatio = singleMinRatio,
                maxRatio = MediaDisplayPolicy.FEED_SINGLE_MAX_RATIO
            )
            val clampedRatio = clampedAspectRatio(
                aspectRatio = media.preferredAspectRatio(),
                minRatio = singleMinRatio,
                maxRatio = MediaDisplayPolicy.FEED_SINGLE_MAX_RATIO
            )

            if (media.type == MediaType.VIDEO) {
                // 세로 영상 비율은 유지하되 높이만 캡
                val videoHeight = singleMediaHeight.coerceAtMost(
                    availableWidth * MediaDisplayPolicy.FEED_SINGLE_VIDEO_MAX_HEIGHT_RATIO
                )
                VideoPlayer(
                    videoUrl = media.url,
                    thumbnailUrl = media.thumbnailUrl,
                    isPlaying = isVideoPlaying && !isUploading,
                    onClick = { if (!isUploading) onMediaClick(0) },
                    modifier = Modifier.clip(MaterialTheme.shapes.medium),
                    fixedHeight = videoHeight,
                    maxWidth = availableWidth,
                    preferredAspectRatio = clampedRatio,
                    minAspectRatio = singleMinRatio,
                    maxAspectRatio = MediaDisplayPolicy.FEED_SINGLE_MAX_RATIO,
                    durationMs = media.durationMs
                )
            } else {
                AsyncImage(
                    // 목록은 썸네일, 키는 원본
                    model = rememberAnimatedImage(media.thumbnailUrl ?: media.url),
                    contentDescription = "첨부 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(singleMediaHeight)
                        .mediaSharedElement(mediaSharedScope, activeViewerKey, media.url)
                        .clip(MaterialTheme.shapes.medium)
                        .then(
                            if (isUploading) Modifier else Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onMediaClick(0) }
                        ),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(placeholderColor),
                    error = ColorPainter(placeholderColor),
                    // 뷰어 morph용 캐시 키/비율 보관
                    onSuccess = ImageThumbnailInfoStore.onSuccessCapture(media.url)
                )
            }
        }
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val maxItemWidth = (maxWidth - startPadding - endPadding).coerceAtLeast(1.dp)
            // peek 여백만큼 줄인 폭으로 비율 캡
            val peekTargetWidth = (maxItemWidth * MediaDisplayPolicy.MULTI_PEEK_FRACTION).coerceAtLeast(1.dp)
            val maxVisibleRatio = (peekTargetWidth / multiMediaHeight).coerceIn(
                MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
                MediaDisplayPolicy.FEED_MULTI_MAX_RATIO
            )
            val rowState = rememberLazyListState()
            // 중앙 미디어 1개만 재생
            val activeMediaId by remember {
                derivedStateOf {
                    val info = rowState.layoutInfo
                    val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
                    info.visibleItemsInfo
                        .minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }
                        ?.key as? Int
                }
            }
            // 영상 1개면 중앙 매칭 생략, 여럿이면 중앙만 재생
            val videoCount = remember(attachments) { attachments.count { it.type == MediaType.VIDEO } }

            LazyRow(
                state = rowState,
                modifier = Modifier
                    .fillMaxWidth()
                    .blockHorizontalScrollLeak(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = startPadding, end = endPadding)
            ) {
                itemsIndexed(
                    items = attachments,
                    key = { _, media -> media.id }
                ) { index, media ->
                    if (media.type == MediaType.VIDEO) {
                        VideoPlayer(
                            videoUrl = media.url,
                            thumbnailUrl = media.thumbnailUrl,
                            isPlaying = isVideoPlaying && !isUploading && (videoCount <= 1 || activeMediaId == media.id),
                            onClick = { if (!isUploading) onMediaClick(index) },
                            modifier = Modifier.clip(MaterialTheme.shapes.medium),
                            fixedHeight = multiMediaHeight,
                            maxWidth = maxItemWidth,
                            preferredAspectRatio = clampedAspectRatio(
                                aspectRatio = media.preferredAspectRatio(),
                                minRatio = MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
                                maxRatio = maxVisibleRatio
                            ),
                            minAspectRatio = MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
                            maxAspectRatio = maxVisibleRatio,
                            durationMs = media.durationMs
                        )
                    } else {
                        val itemRatio = clampedAspectRatio(
                            aspectRatio = media.preferredAspectRatio(),
                            minRatio = MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
                            maxRatio = maxVisibleRatio
                        )
                        AsyncImage(
                            // 목록은 썸네일, 키는 원본
                            model = rememberAnimatedImage(media.thumbnailUrl ?: media.url),
                            contentDescription = "첨부 이미지",
                            modifier = Modifier
                                .width(multiMediaHeight * itemRatio)
                                .height(multiMediaHeight)
                                .mediaSharedElement(mediaSharedScope, activeViewerKey, media.url)
                                .clip(MaterialTheme.shapes.medium)
                                .then(
                                    if (isUploading) Modifier else Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onMediaClick(index) }
                                ),
                            contentScale = ContentScale.Crop,
                            placeholder = ColorPainter(placeholderColor),
                            error = ColorPainter(placeholderColor),
                            onSuccess = ImageThumbnailInfoStore.onSuccessCapture(media.url)
                        )
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostMediaAttachmentsPreview() {
    HaloSurfacePreview {
        Column(modifier = Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PostMediaAttachments(attachments = listOf(previewMediaItem()), startPadding = 16.dp)
            PostMediaAttachments(attachments = List(3) { previewMediaItem(id = it) }, startPadding = 16.dp)
        }
    }
}
