package com.ninezero.halo.core.ui.components.modifiers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.launch

private val BounceSpec: AnimationSpec<Float> = spring(visibilityThreshold = 0.01f)

private val PopSpec: AnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

/** bounceClick·popClick 공통 노드 */
private class PressScaleNode(
    var pressedScale: Float,
    var spec: AnimationSpec<Float>
) : DelegatingNode(), LayoutModifierNode {

    private val scale = Animatable(1f)

    init {
        delegate(
            SuspendingPointerInputModifierNode {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    coroutineScope.launch { scale.animateTo(pressedScale, spec) }
                    waitForUpOrCancellation()
                    coroutineScope.launch { scale.animateTo(1f, spec) }
                }
            }
        )
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale.value
                scaleY = scale.value
            }
        }
    }
}

private data class PressScaleElement(
    val pressedScale: Float,
    val spec: AnimationSpec<Float>
) : ModifierNodeElement<PressScaleNode>() {
    override fun create() = PressScaleNode(pressedScale, spec)

    override fun update(node: PressScaleNode) {
        node.pressedScale = pressedScale
        node.spec = spec
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "pressScale"
        properties["pressedScale"] = pressedScale
    }
}

/** 눌림 축소 클릭 효과 */
fun Modifier.bounceClick(
    enabled: Boolean = true,
    scaleFactor: Float = 0.96f
): Modifier =
    if (!enabled) this
    else this then PressScaleElement(pressedScale = scaleFactor, spec = BounceSpec)

/** 눌림 확대 클릭 효과 */
fun Modifier.popClick(
    enabled: Boolean = true,
    shrinkScale: Float = 0.85f
): Modifier =
    if (!enabled) this
    else this then PressScaleElement(pressedScale = shrinkScale, spec = PopSpec)

/** Shimmer 효과 */
fun Modifier.shimmer(): Modifier = composed {
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.surface

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    this.background(
        brush = Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset(translateAnim - 500f, translateAnim - 500f),
            end = Offset(translateAnim, translateAnim)
        )
    )
}
