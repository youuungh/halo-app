package com.ninezero.halo.core.ui.components.modifiers

import androidx.compose.runtime.Immutable
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
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

@Immutable
data class HaloBlurStyle(
    val backgroundColor: Color = Color.Unspecified,
    val tint: HaloBlurTint? = null,
    val blurRadius: Dp = Dp.Unspecified,
    val noiseFactor: Float = -1f
)

@Immutable
data class HaloBlurTint(val color: Color)

class HaloBlurScope internal constructor(var style: HaloBlurStyle) {
    var progressive: HazeProgressive? = null
}

fun Modifier.haloBlur(
    state: HazeState,
    style: HaloBlurStyle = HaloBlurStyle(),
    block: (HaloBlurScope.() -> Unit)? = null
): Modifier {
    val scope = HaloBlurScope(style).apply { block?.invoke(this) }
    val resolved = scope.style
    return hazeBlur(
        input = HazeInput.Sources(state),
        style = HazeBlurStyle {
            if (resolved.backgroundColor.isSpecified) backgroundColor(resolved.backgroundColor)
            resolved.tint?.let { colorEffects(listOf(HazeColorEffect.tint(it.color))) }
            if (resolved.blurRadius.isSpecified) blurRadius(resolved.blurRadius)
            if (resolved.noiseFactor >= 0f) noiseFactor(resolved.noiseFactor)
            scope.progressive?.let { progressive(it) }
        },
        performanceMode = HazePerformanceMode.Quality
    )
}
