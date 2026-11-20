package com.ninezero.halo.core.ui.components.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.compose.LocalPlatformContext
import coil3.gif.AnimatedImageDecoder
import coil3.request.ImageRequest

/** GIF도 움직이는 이미지 요청, 포스트 · 댓글 · 뷰어 전용 */
@Composable
fun rememberAnimatedImage(url: String?): ImageRequest {
    val context = LocalPlatformContext.current
    return remember(url) {
        ImageRequest.Builder(context)
            .data(url)
            .decoderFactory(AnimatedImageDecoder.Factory())
            .build()
    }
}
