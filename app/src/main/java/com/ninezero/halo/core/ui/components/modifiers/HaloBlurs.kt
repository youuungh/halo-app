package com.ninezero.halo.core.ui.components.modifiers

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.Easing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive

/** 화면 공통 블러 스타일 */
object HaloBlurs {

    /** 위에서 아래로 옅어지는 블러 */
    fun fadeDown(color: Color, start: Float = 0.7f) = HaloBlurStyle(
        backgroundColor = color,
        tint = color,
        noiseFactor = 0f,
        progressive = HazeProgressive.verticalGradient(startIntensity = start, endIntensity = 0f)
    )

    /** 아래로 갈수록 짙어지는 블러 */
    fun fadeUp(color: Color, end: Float = 0.5f, easing: Easing = EaseIn) = HaloBlurStyle(
        backgroundColor = color,
        tint = color,
        noiseFactor = 0f,
        progressive = HazeProgressive.verticalGradient(easing = easing, startIntensity = 0f, endIntensity = end)
    )

    /** 유리 칩·탭·입력칸 블러 */
    @Composable
    @ReadOnlyComposable
    fun glassField(background: Color) = HaloBlurStyle(
        backgroundColor = background,
        tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        blurRadius = 20.dp,
        noiseFactor = 0f
    )

    /** 유리 탭 인디케이터 블러 */
    @Composable
    @ReadOnlyComposable
    fun glassIndicator(background: Color) = HaloBlurStyle(
        backgroundColor = background,
        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        blurRadius = 20.dp,
        noiseFactor = 0f
    )

    /** 팝업 메뉴 블러 */
    @Composable
    @ReadOnlyComposable
    fun popupMenu() = HaloBlurStyle(
        backgroundColor = MaterialTheme.colorScheme.background,
        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        blurRadius = 30.dp,
        noiseFactor = 0f
    )

    /** 떠 있는 필터 헤더 블러 */
    fun floatingHeader(color: Color) = HaloBlurStyle(
        backgroundColor = color,
        tint = color.copy(alpha = 0.4f),
        blurRadius = 20.dp,
        noiseFactor = 0f
    )

    /** 미디어 뷰어 컨트롤 블러 */
    val MediaControl = HaloBlurStyle(
        backgroundColor = Color.Black,
        tint = Color.Black.copy(alpha = 0.34f),
        blurRadius = 16.dp,
        noiseFactor = 0f
    )
}
