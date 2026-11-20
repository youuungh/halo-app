package com.ninezero.halo.feature.profile.presentation.my_profile.components

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import coil3.size.Size
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme

val isAvatarGlowSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

// 아래로 갈수록 배경에 녹아듦
private val GlowFade = Brush.verticalGradient(
    0f to Color.Black,
    0.25f to Color.Black,
    1f to Color.Transparent
)
private val GlowSaturation = ColorMatrix().apply { setToSaturation(1.5f) }

@Composable
fun ProfileAvatarGlow(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    // 4×4로 줄여 색만 남김, 디스크 캐시는 공유
    val request = remember(imageUrl, context) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .memoryCacheKey("$imageUrl#4x4")
            .size(Size(4, 4))
            .precision(Precision.EXACT)
            .crossfade(false)
            .build()
    }

    Box(
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(GlowFade, blendMode = BlendMode.DstIn)
            }
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(0f, -0.4f),
            colorFilter = ColorFilter.colorMatrix(GlowSaturation),
            modifier = Modifier
                .fillMaxSize()
                .scale(1.25f)
                .blur(60.dp, BlurredEdgeTreatment.Unbounded)
                .alpha(if (isDark) 0.45f else 0.55f)
        )
    }
}
