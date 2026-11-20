package com.ninezero.halo.core.ui.components.media

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.ninezero.halo.core.media.MediaUtils
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

/** 고른 미디어 미리보기 크기, 글과 댓글이 다름 */
class MediaPreviewSizes(
    val singleMinRatio: Float,
    val singleMaxRatio: Float,
    val multiItemHeight: Dp,
    val multiMinRatio: Float,
    val multiMaxRatio: Float
) {
    companion object {
        val Post = MediaPreviewSizes(
            singleMinRatio = MediaDisplayPolicy.FEED_SINGLE_MIN_RATIO,
            singleMaxRatio = MediaDisplayPolicy.FEED_SINGLE_MAX_RATIO,
            multiItemHeight = MediaDisplayPolicy.FEED_MULTI_ITEM_HEIGHT,
            multiMinRatio = MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
            multiMaxRatio = MediaDisplayPolicy.FEED_MULTI_MAX_RATIO
        )
        val Comment = MediaPreviewSizes(
            singleMinRatio = MediaDisplayPolicy.COMMENT_SINGLE_MIN_RATIO,
            singleMaxRatio = MediaDisplayPolicy.COMMENT_SINGLE_MAX_RATIO,
            multiItemHeight = MediaDisplayPolicy.COMMENT_MULTI_ITEM_HEIGHT,
            multiMinRatio = MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
            multiMaxRatio = MediaDisplayPolicy.COMMENT_MULTI_MAX_RATIO
        )
    }
}

/** 고른 사진 · 영상 미리보기, 하나면 크게 여럿이면 가로 줄 */
@Composable
fun SelectedMediaRow(
    uris: List<Uri>,
    startPadding: Dp,
    endPadding: Dp,
    placeholderColor: Color,
    sizes: MediaPreviewSizes,
    onRemove: (Uri) -> Unit
) {
    val context = LocalContext.current
    Spacer(modifier = Modifier.height(12.dp))
    val isSingleSelection = uris.size == 1
    val multiSelectionHeight = sizes.multiItemHeight
    val cornerShape = MaterialTheme.shapes.medium

    if (isSingleSelection) {
        val singleUri = uris.first()
        val isVideo = remember(singleUri) {
            MediaUtils.resolveMimeType(context, singleUri).startsWith("video/")
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = startPadding, end = endPadding)
        ) {
            // 동영상은 실제 비율, 로드 전 16:9
            val videoRatio by produceState(16f / 9f, singleUri, isVideo) {
                if (isVideo) {
                    val (w, h) = MediaUtils.getVideoDimensions(context, singleUri)
                    if (w != null && h != null && w > 0 && h > 0) value = w.toFloat() / h
                }
            }
            val preferredRatio = if (isVideo) videoRatio else 1f
            val clampedRatio = preferredRatio.coerceIn(
                sizes.singleMinRatio,
                sizes.singleMaxRatio
            )
            val mediaHeight = maxWidth / clampedRatio

            SelectedMediaPreviewItem(
                uri = singleUri,
                isVideo = isVideo,
                placeholderColor = placeholderColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mediaHeight)
                    .clip(cornerShape),
                onRemove = { onRemove(singleUri) }
            )
        }
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val maxItemWidth = (maxWidth - startPadding - endPadding).coerceAtLeast(1.dp)
            val maxVisibleRatio = (maxItemWidth / multiSelectionHeight).coerceIn(
                sizes.multiMinRatio,
                sizes.multiMaxRatio
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = startPadding, end = endPadding),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = uris,
                    key = { it.toString() }
                ) { uri ->
                    val isVideo = remember(uri) {
                        MediaUtils.resolveMimeType(context, uri).startsWith("video/")
                    }
                    val videoRatio by produceState(16f / 9f, uri, isVideo) {
                        if (isVideo) {
                            val (w, h) = MediaUtils.getVideoDimensions(context, uri)
                            if (w != null && h != null && w > 0 && h > 0) value = w.toFloat() / h
                        }
                    }
                    val preferredRatio = if (isVideo) videoRatio else 1f
                    val itemRatio = preferredRatio.coerceIn(
                        sizes.multiMinRatio,
                        maxVisibleRatio
                    )

                    SelectedMediaPreviewItem(
                        uri = uri,
                        isVideo = isVideo,
                        placeholderColor = placeholderColor,
                        modifier = Modifier
                            .width(multiSelectionHeight * itemRatio)
                            .height(multiSelectionHeight)
                            .clip(cornerShape),
                        onRemove = { onRemove(uri) }
                    )
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SelectedMediaRowPreview() {
    HaloSurfacePreview {
        SelectedMediaRow(
            uris = List(3) { "content://preview/$it".toUri() },
            startPadding = 16.dp,
            endPadding = 16.dp,
            placeholderColor = MaterialTheme.colorScheme.surfaceVariant,
            sizes = MediaPreviewSizes.Post,
            onRemove = {}
        )
    }
}
