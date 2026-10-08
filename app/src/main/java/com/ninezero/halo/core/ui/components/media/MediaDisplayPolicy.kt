package com.ninezero.halo.core.ui.components.media

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType

object MediaDisplayPolicy {
    const val FEED_SINGLE_MIN_RATIO = 4f / 5f
    const val FEED_SINGLE_MAX_RATIO = 16f / 9f
    const val FEED_SINGLE_VIDEO_MIN_RATIO = 9f / 16f
    const val FEED_SINGLE_VIDEO_MAX_HEIGHT_RATIO = 1.4f
    const val COMMENT_SINGLE_MIN_RATIO = 4f / 5f
    const val COMMENT_SINGLE_MAX_RATIO = 16f / 9f
    const val COMMENT_SINGLE_VIDEO_MIN_RATIO = 9f / 16f
    const val COMMENT_SINGLE_VIDEO_MAX_HEIGHT_RATIO = 1.4f

    // 여러 장은 높이 고정, 비율 clamp
    const val MULTI_PEEK_FRACTION = 0.9f
    const val FEED_MULTI_MIN_RATIO = 4f / 5f
    const val FEED_MULTI_MAX_RATIO = 16f / 9f
    const val COMMENT_MULTI_MIN_RATIO = 4f / 5f
    const val COMMENT_MULTI_MAX_RATIO = 4f / 3f
    val FEED_MULTI_ITEM_HEIGHT = 230.dp
    val COMMENT_MULTI_ITEM_HEIGHT = 180.dp
}

/** 미디어 표시 종횡비 */
fun MediaItem.preferredAspectRatio(defaultVideoRatio: Float = 16f / 9f): Float {
    val metadataRatio = if (width != null && height != null && width > 0 && height > 0) {
        width.toFloat() / height.toFloat()
    } else {
        null
    }

    return metadataRatio ?: when (type) {
        MediaType.VIDEO -> defaultVideoRatio
        else -> 1f
    }
}

/** 종횡비 범위 제한 */
fun clampedAspectRatio(aspectRatio: Float, minRatio: Float, maxRatio: Float): Float {
    return aspectRatio.coerceIn(minRatio, maxRatio)
}

/** 단일 미디어 표시 높이 */
fun singleMediaHeightForWidth(
    availableWidth: Dp,
    media: MediaItem,
    minRatio: Float,
    maxRatio: Float
): Dp {
    val ratio = clampedAspectRatio(
        aspectRatio = media.preferredAspectRatio(),
        minRatio = minRatio,
        maxRatio = maxRatio
    )
    return availableWidth / ratio
}
