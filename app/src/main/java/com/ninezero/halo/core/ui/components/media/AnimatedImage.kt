package com.ninezero.halo.core.ui.components.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.compose.LocalPlatformContext
import coil3.gif.AnimatedImageDecoder
import coil3.memory.MemoryCache
import coil3.request.ImageRequest

/** GIF 애니메이션까지 재생하는 이미지 요청 */
@Composable
fun rememberAnimatedImage(url: String?, placeholderKey: MemoryCache.Key? = null): ImageRequest {
    val context = LocalPlatformContext.current
    return remember(url) {
        ImageRequest.Builder(context)
            .data(url)
            .placeholderMemoryCacheKey(placeholderKey)
            .decoderFactory(AnimatedImageDecoder.Factory())
            .build()
    }
}
