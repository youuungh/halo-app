package com.ninezero.halo.core.ui.components.comment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.core.ui.components.media.MediaDisplayPolicy
import com.ninezero.halo.core.ui.components.media.clampedAspectRatio
import com.ninezero.halo.core.ui.components.media.preferredAspectRatio
import com.ninezero.halo.core.ui.components.media.singleMediaHeightForWidth
import com.ninezero.halo.core.ui.components.modifiers.ImageThumbnailInfoStore
import com.ninezero.halo.core.ui.components.modifiers.LocalActiveViewerMediaKey
import com.ninezero.halo.core.ui.components.modifiers.LocalMediaSharedTransitionScope
import com.ninezero.halo.core.ui.components.modifiers.blockHorizontalScrollLeak
import com.ninezero.halo.core.ui.components.modifiers.mediaSharedElement
import com.ninezero.halo.core.ui.components.video_player.VideoPlayer
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType

/** 댓글 미디어 표시 */
@Composable
internal fun CommentMedia(  // 단일은 종횡비, 다중은 가로 스크롤 타일
    mediaAttachments: List<MediaItem>,
    startPadding: Dp,
    endPadding: Dp,
    modifier: Modifier = Modifier,
    showLine: Boolean = false,
    linePadStart: Dp = 0.dp,
    lineWidth: Dp = 1.5.dp,
    lineColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    onMediaClick: (index: Int) -> Unit = {},
    isVideoPlaying: Boolean = false,
    isUploading: Boolean = false,
    // shared element owner 키, null이면 URL만
    mediaOwnerKey: String? = null
) {
    if (mediaAttachments.isEmpty()) return

    // 영상 뷰어 열리면 인라인 정지
    val overlayOpen = LocalViewerOverlayHostState.current?.suppressInlineVideo ?: false
    val effectiveVideoPlaying = isVideoPlaying && !overlayOpen
    // 썸네일 morph용, scope null이면 일반 표시
    val mediaSharedScope = LocalMediaSharedTransitionScope.current
    val activeViewerKey = LocalActiveViewerMediaKey.current

    val isSingleMedia = mediaAttachments.size == 1
    val multiMediaHeight = MediaDisplayPolicy.COMMENT_MULTI_ITEM_HEIGHT
    val imagePadding = 4.dp
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant

    if (isSingleMedia) {
        val media = mediaAttachments.first()
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
        ) {
            val availableWidth = maxWidth - startPadding - endPadding
            val singleMinRatio = if (media.type == MediaType.VIDEO) {
                MediaDisplayPolicy.COMMENT_SINGLE_VIDEO_MIN_RATIO
            } else {
                MediaDisplayPolicy.COMMENT_SINGLE_MIN_RATIO
            }
            val singleMediaHeight = singleMediaHeightForWidth(
                availableWidth = availableWidth,
                media = media,
                minRatio = singleMinRatio,
                maxRatio = MediaDisplayPolicy.COMMENT_SINGLE_MAX_RATIO
            )
            val clampedRatio = clampedAspectRatio(
                aspectRatio = media.preferredAspectRatio(),
                minRatio = singleMinRatio,
                maxRatio = MediaDisplayPolicy.COMMENT_SINGLE_MAX_RATIO
            )
            val effectiveHeight = if (media.type == MediaType.VIDEO) {
                singleMediaHeight.coerceAtMost(
                    availableWidth * MediaDisplayPolicy.COMMENT_SINGLE_VIDEO_MAX_HEIGHT_RATIO
                )
            } else {
                singleMediaHeight
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(effectiveHeight + imagePadding * 2)
            ) {
                // 선, 전체 높이
                if (showLine) {
                    Box(
                        modifier = Modifier
                            .padding(start = linePadStart)
                            .width(lineWidth)
                            .fillMaxHeight()
                            .background(lineColor)
                    )
                }

                // 미디어
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = imagePadding)
                        .padding(start = startPadding, end = endPadding)
                ) {
                    if (media.type == MediaType.VIDEO) {
                        VideoPlayer(
                            videoUrl = media.url,
                            thumbnailUrl = media.thumbnailUrl,
                            isPlaying = effectiveVideoPlaying && !isUploading,
                            onClick = { onMediaClick(0) },
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                            fixedHeight = effectiveHeight,
                            maxWidth = availableWidth,
                            preferredAspectRatio = clampedRatio,
                            minAspectRatio = singleMinRatio,
                            maxAspectRatio = MediaDisplayPolicy.COMMENT_SINGLE_MAX_RATIO,
                            durationMs = media.durationMs
                        )
                    } else {
                        AsyncImage(
                            // 목록은 썸네일, 키는 원본
                            model = media.thumbnailUrl ?: media.url,
                            contentDescription = "댓글 이미지",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(singleMediaHeight)
                                .mediaSharedElement(mediaSharedScope, activeViewerKey, media.url, mediaOwnerKey)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onMediaClick(0) }
                                ),
                            contentScale = ContentScale.Crop,
                            placeholder = ColorPainter(placeholderColor),
                            error = ColorPainter(placeholderColor),
                            // 뷰어 morph용 캐시 키·비율
                            onSuccess = ImageThumbnailInfoStore.onSuccessCapture(media.url)
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(multiMediaHeight + imagePadding * 2)
        ) {
            // 선
            if (showLine) {
                Box(
                    modifier = Modifier
                        .padding(start = linePadStart)
                        .width(lineWidth)
                        .fillMaxHeight()
                        .background(lineColor)
                )
            }

            // 미디어
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = imagePadding)
            ) {
                val maxItemWidth = (maxWidth - startPadding - endPadding).coerceAtLeast(1.dp)
                val maxVisibleRatio = (maxItemWidth / multiMediaHeight).coerceIn(
                    MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
                    MediaDisplayPolicy.COMMENT_MULTI_MAX_RATIO
                )
                // 가로줄에서 중앙에 가장 가까운 영상 1개만 재생
                val rowState = rememberLazyListState()
                val activeMediaId by remember {
                    derivedStateOf {
                        val info = rowState.layoutInfo
                        val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
                        info.visibleItemsInfo
                            .minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }
                            ?.key as? Int
                    }
                }
                val videoCount = remember(mediaAttachments) { mediaAttachments.count { it.type == MediaType.VIDEO } }

                LazyRow(
                    state = rowState,
                    modifier = Modifier
                        .fillMaxSize()
                        .blockHorizontalScrollLeak(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(start = startPadding, end = endPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(
                        count = mediaAttachments.size,
                        key = { mediaAttachments[it].id }
                    ) { index ->
                        val media = mediaAttachments[index]
                        if (media.type == MediaType.VIDEO) {
                            VideoPlayer(
                                videoUrl = media.url,
                                thumbnailUrl = media.thumbnailUrl,
                                isPlaying = effectiveVideoPlaying && !isUploading && (videoCount <= 1 || activeMediaId == media.id),
                                onClick = { onMediaClick(index) },
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                                fixedHeight = multiMediaHeight,
                                maxWidth = maxItemWidth,
                                preferredAspectRatio = clampedAspectRatio(
                                    aspectRatio = media.preferredAspectRatio(),
                                    minRatio = MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
                                    maxRatio = maxVisibleRatio
                                ),
                                minAspectRatio = MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
                                maxAspectRatio = maxVisibleRatio,
                                durationMs = media.durationMs
                            )
                        } else {
                            val itemRatio = clampedAspectRatio(
                                aspectRatio = media.preferredAspectRatio(),
                                minRatio = MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
                                maxRatio = maxVisibleRatio
                            )
                            AsyncImage(
                                // 목록은 썸네일, 키는 원본
                                model = media.thumbnailUrl ?: media.url,
                                contentDescription = "댓글 이미지",
                                modifier = Modifier
                                    .width(multiMediaHeight * itemRatio)
                                    .height(multiMediaHeight)
                                    .mediaSharedElement(mediaSharedScope, activeViewerKey, media.url, mediaOwnerKey)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onMediaClick(index) }
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
}
