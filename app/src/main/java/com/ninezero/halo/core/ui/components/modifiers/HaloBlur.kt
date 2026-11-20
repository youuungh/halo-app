package com.ninezero.halo.core.ui.components.modifiers

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isSpecified
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeBlurStyleScope
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

/** haloBlur에 넘기는 바탕 · 색 · 반경 묶음 */
@Immutable
data class HaloBlurStyle(
    val backgroundColor: Color = Color.Unspecified,
    val tint: Color = Color.Unspecified,
    val blurRadius: Dp = Dp.Unspecified,
    val noiseFactor: Float = -1f,
    val fallbackTint: Color = Color.Unspecified,
    val progressive: HazeProgressive? = null
)

/** HaloBlurStyle로 거는 블러, 같은 값이면 스타일 재사용 */
@Composable
fun Modifier.haloBlur(state: HazeState, style: HaloBlurStyle): Modifier = haloHazeBlur(
    state = state,
    style = remember(style) { HazeBlurStyle { applyHaloStyle(style) } },
    progressive = style.progressive != null
)

/** 스크롤 값을 리컴포지션 없이 읽는 블러 */
@Composable
fun Modifier.haloBlur(state: HazeState, style: () -> HaloBlurStyle): Modifier {
    // 점진 여부는 스크롤과 무관하므로 한 번만 읽음
    val progressive = remember(style) { Snapshot.withoutReadObservation { style().progressive != null } }
    return haloHazeBlur(
        state = state,
        style = remember(style) { HazeBlurStyle { applyHaloStyle(style()) } },
        progressive = progressive
    )
}

private fun Modifier.haloHazeBlur(state: HazeState, style: HazeBlurStyle, progressive: Boolean): Modifier =
    hazeBlur(
        input = HazeInput.Sources(state),
        style = style,
        performanceMode = if (progressive) HazePerformanceMode.Balanced else HazePerformanceMode.Quality
    )

private fun HazeBlurStyleScope.applyHaloStyle(style: HaloBlurStyle) {
    if (style.backgroundColor.isSpecified) backgroundColor(style.backgroundColor)
    if (style.tint.isSpecified) colorEffects(listOf(HazeColorEffect.tint(style.tint)))
    if (style.blurRadius.isSpecified) blurRadius(style.blurRadius)
    if (style.noiseFactor >= 0f) noiseFactor(style.noiseFactor)
    if (style.fallbackTint.isSpecified) fallbackColorEffect(HazeColorEffect.tint(style.fallbackTint))
    style.progressive?.let { progressive(it.softFadeOut()) }
}

private val SoftFadeOutEasing = CubicBezierEasing(0.4f, 0f, 0.95f, 1f)

private fun HazeProgressive.softFadeOut(): HazeProgressive =
    if (this is HazeProgressive.LinearGradient && startIntensity > endIntensity) copy(easing = SoftFadeOutEasing) else this
