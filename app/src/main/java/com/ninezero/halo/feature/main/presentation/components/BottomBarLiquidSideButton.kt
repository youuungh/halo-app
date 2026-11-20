package com.ninezero.halo.feature.main.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.runtimeShaderEffect
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.isRuntimeShaderSupported
import com.ninezero.halo.core.ui.components.modifiers.popClick
import com.ninezero.halo.core.ui.components.navigation.BottomBarDefaults
import com.ninezero.halo.core.ui.components.navigation.BottomBarSideButton
import kotlinx.coroutines.launch
import kotlin.math.ceil
import androidx.compose.ui.graphics.shadow.Shadow as DropShadow

/** 바에서 물방울처럼 떨어지고 붙는 버튼 움직임 */
@Stable
internal class SideButtonMotion {
    val separation = Animatable(0f)  // 0 = 바 끝에 녹아 있음, 1 = 떨어져 제자리
    val dropScale = Animatable(DROP_MIN_SCALE)
    val iconAlpha = Animatable(0f)
}

/** 다른 화면에서 돌아온 직후면 애니메이션 없이 제자리 */
@Composable
internal fun rememberSideButtonMotion(visible: Boolean): SideButtonMotion {
    val motion = remember { SideButtonMotion() }
    var settled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        repeat(SETTLE_FRAMES) { withFrameNanos { } }
        settled = true
    }
    LaunchedEffect(visible) {
        if (!settled) {
            motion.separation.snapTo(if (visible) 1f else 0f)
            motion.dropScale.snapTo(if (visible) 1f else DROP_MIN_SCALE)
            motion.iconAlpha.snapTo(if (visible) 1f else 0f)
            return@LaunchedEffect
        }
        if (visible) {
            // 위치가 넘치면 다시 붙어서 탄력은 크기에만
            launch { motion.separation.animateTo(1f, tween(SHOW_MILLIS, easing = ShowEasing)) }
            launch { motion.dropScale.animateTo(1f, tween(SHOW_SCALE_MILLIS, easing = ShowScaleEasing)) }
            launch { motion.iconAlpha.animateTo(1f, tween(ICON_FADE_MILLIS, delayMillis = ICON_DELAY_MILLIS)) }
        } else {
            // 누르자마자 출발해 고르게 붙음
            launch { motion.separation.animateTo(0f, tween(HIDE_MILLIS, easing = HideEasing)) }
            // 살짝 부풀었다 빨려 들어감
            launch { motion.dropScale.animateTo(DROP_MIN_SCALE, tween(HIDE_MILLIS, easing = HideScaleEasing)) }
            launch { motion.iconAlpha.animateTo(0f, tween((HIDE_MILLIS * 0.45f).fastRoundToInt())) }
        }
    }
    return motion
}

/** 바가 비켜나는 폭 */
internal val SideButtonSlot = BottomBarDefaults.PillHeight + 8.dp

/** 바 끝과 한 모양으로 합친 굴절 유리 물방울과 아이콘 */
@Composable
internal fun LiquidSideButton(
    sideButton: BottomBarSideButton?,
    motion: SideButtonMotion,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    // 사라지는 동안에도 아이콘을 그리도록 마지막 값 유지
    var lastSideButton by remember { mutableStateOf(sideButton) }
    LaunchedEffect(sideButton) {
        if (sideButton != null) lastSideButton = sideButton
    }
    val visible = sideButton != null
    val separation = motion.separation.value
    val dropScale = motion.dropScale.value
    val current = (sideButton ?: lastSideButton).takeIf { visible || separation > 0f } ?: return

    val hapticFeedback = LocalHapticFeedback.current
    val surface = MaterialTheme.colorScheme.surface
    val geometry = with(LocalDensity.current) {
        LiquidGeometry(
            separation = separation,
            dropScale = dropScale,
            spread = LiquidSpread.toPx(),
            pill = BottomBarDefaults.PillHeight.toPx(),
            slot = SideButtonSlot.toPx(),
            inset = DropInset.toPx()
        )
    }

    Box(modifier = modifier) {
        // 그림자 들어갈 만큼 넓히고 바 자리는 잘라 냄
        Box(
            modifier = Modifier
                .spread(geometry.spread)
                .clip(OutsideBarShape(geometry))
        ) {
            Box(
                modifier = Modifier
                    .dropPosition(geometry, inSpreadLayer = true)
                    .size(BottomBarDefaults.PillHeight)
                    .graphicsLayer {
                        scaleX = dropScale
                        scaleY = dropScale
                    }
                    .dropShadow(
                        CircleShape,
                        DropShadow(radius = 10.dp, color = Color.Black.copy(alpha = 0.18f), offset = DpOffset(0.dp, 3.dp))
                    )
            )
            if (isRuntimeShaderSupported()) {
                // 층 전체에 그리고 셰이더가 합친 모양 밖을 지움
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { RectangleShape },
                            effects = {
                                vibrancy()
                                blur(4.dp.toPx())
                                liquidUnion(geometry, surface)
                            },
                            highlight = null,
                            shadow = null
                        )
                )
            } else {
                // 셰이더가 없으면 바도 lens가 빠져 목 없이 원만
                Box(
                    modifier = Modifier
                        .dropPosition(geometry, inSpreadLayer = true)
                        .size(BottomBarDefaults.PillHeight)
                        .graphicsLayer {
                            scaleX = dropScale
                            scaleY = dropScale
                        }
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { CircleShape },
                            effects = {
                                vibrancy()
                                blur(4.dp.toPx())
                            },
                            shadow = null,
                            onDrawSurface = { drawRect(surface.copy(alpha = SURFACE_ALPHA)) }
                        )
                )
            }
        }

        // 아이콘과 누르는 자리
        Box(
            modifier = Modifier
                .dropPosition(geometry, inSpreadLayer = false)
                .size(BottomBarDefaults.PillHeight)
                .graphicsLayer {
                    scaleX = dropScale
                    scaleY = dropScale
                    alpha = motion.iconAlpha.value
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                }
                .popClick(enabled = visible)
                .clickable(enabled = visible) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    current.onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(current.iconRes),
                contentDescription = current.contentDescription,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** 물방울 위치 계산값, 단위 px */
private data class LiquidGeometry(
    val separation: Float,
    val dropScale: Float,
    val spread: Float,
    val pill: Float,
    val slot: Float,
    val inset: Float
) {
    val radius get() = pill / 2f

    /** 바 오른쪽 끝, 층 왼쪽 위가 바 왼쪽 위에서 spread만큼 바깥 */
    fun barEnd(layerWidth: Float, inSpreadLayer: Boolean = true) =
        layerWidth - (if (inSpreadLayer) spread else 0f) - slot * separation

    /** 물방울 왼쪽 끝, 녹아 있으면 inset만큼 바 안쪽 */
    fun dropLeft(layerWidth: Float, inSpreadLayer: Boolean) =
        layerWidth - (if (inSpreadLayer) spread else 0f) - pill - inset * (1f - separation)
}

private fun Modifier.dropPosition(geometry: LiquidGeometry, inSpreadLayer: Boolean): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        layout(constraints.maxWidth, constraints.maxHeight) {
            val x = geometry.dropLeft(constraints.maxWidth.toFloat(), inSpreadLayer)
            val y = if (inSpreadLayer) geometry.spread else 0f
            placeable.place(x.fastRoundToInt(), y.fastRoundToInt())
        }
    }

/** 그림자가 잘리지 않게 사방으로 넓힌 층 */
private fun Modifier.spread(spread: Float): Modifier = layout { measurable, constraints ->
    val offset = spread.fastRoundToInt()
    val width = constraints.maxWidth + offset * 2
    val height = constraints.maxHeight + offset * 2
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width, minHeight = height, maxHeight = height))
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.place(-offset, -offset)
    }
}

/** 넓힌 층에서 바 알약 자리만 뺀 모양 */
private data class OutsideBarShape(private val geometry: LiquidGeometry) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val spread = geometry.spread
        val bar = Path().apply {
            addRoundRect(
                RoundRect(
                    left = spread,
                    top = spread,
                    right = geometry.barEnd(size.width),
                    bottom = spread + geometry.pill,
                    cornerRadius = CornerRadius(geometry.radius)
                )
            )
        }
        val layer = Path().apply { addRect(Rect(Offset.Zero, size)) }
        return Outline.Generic(Path.combine(PathOperation.Difference, layer, bar))
    }
}

/** 바와 물방울을 합친 모양의 굴절 유리, lens 대신 직접 계산 */
private fun BackdropEffectScope.liquidUnion(geometry: LiquidGeometry, surface: Color) {
    val padding = padding
    val radius = geometry.radius
    val dropLeft = geometry.dropLeft(size.width, inSpreadLayer = true)
    runtimeShaderEffect("LiquidUnion", LIQUID_UNION_SHADER, "content") {
        setFloatUniform("padding", padding)
        setFloatUniform("bar", geometry.spread, geometry.spread, geometry.barEnd(size.width), geometry.spread + geometry.pill)
        setFloatUniform("drop", dropLeft + radius, geometry.spread + radius, radius * geometry.dropScale)
        setFloatUniform("blend", LiquidBlend.toPx())
        setFloatUniform("refractionHeight", 14.dp.toPx())
        setFloatUniform("refractionAmount", -20.dp.toPx())
        setColorUniform("surfaceColor", surface.copy(alpha = 1f))
        setFloatUniform("surfaceAlpha", SURFACE_ALPHA)
        setFloatUniform("rimWidth", ceil(0.5.dp.toPx()))
        setFloatUniform("rimAlpha", 0.5f)
    }
}

// 두 모양 사이가 blend 절반보다 좁을 때만 이어짐
private const val LIQUID_UNION_SHADER = """
uniform shader content;
uniform float padding;
uniform float4 bar;
uniform float3 drop;
uniform float blend;
uniform float refractionHeight;
uniform float refractionAmount;
layout(color) uniform half4 surfaceColor;
uniform float surfaceAlpha;
uniform float rimWidth;
uniform float rimAlpha;

float sdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    float outside = length(max(cornerCoord, 0.0)) - radius;
    float inside = min(max(cornerCoord.x, cornerCoord.y), 0.0);
    return outside + inside;
}

float sdShape(float2 p) {
    float2 halfSize = (bar.zw - bar.xy) * 0.5;
    float d1 = sdRoundedRect(p - (bar.xy + halfSize), halfSize, halfSize.y);
    float d2 = length(p - drop.xy) - drop.z;
    float e = max(blend - abs(d1 - d2), 0.0);
    return min(d1, d2) - e * e * 0.25 / blend;
}

float2 gradShape(float2 p) {
    float2 h = float2(0.5, 0.0);
    float2 g = float2(sdShape(p + h.xy) - sdShape(p - h.xy), sdShape(p + h.yx) - sdShape(p - h.yx));
    return g / max(length(g), 0.0001);
}

float circleMap(float x) {
    return 1.0 - sqrt(1.0 - x * x);
}

half4 main(float2 coord) {
    float2 p = coord - float2(padding);
    float sd = sdShape(p);
    float coverage = clamp(0.5 - sd, 0.0, 1.0);
    if (coverage <= 0.0) {
        return half4(0.0);
    }
    float2 grad = gradShape(p);
    float inner = min(sd, 0.0);
    float2 sampleCoord = coord;
    if (-inner < refractionHeight) {
        sampleCoord += circleMap(1.0 + inner / refractionHeight) * refractionAmount * grad;
    }
    float4 color = float4(content.eval(sampleCoord));
    color = mix(color, float4(surfaceColor), surfaceAlpha);
    float rim = clamp(1.0 + inner / rimWidth, 0.0, 1.0);
    float light = rimAlpha * rim * abs(dot(grad, float2(0.70710678)));
    color.rgb = min(color.rgb + float3(light), color.aaa);
    return half4(color * coverage);
}
"""

private val LiquidSpread = 16.dp
private val DropInset = 24.dp  // 녹아 있을 때 물방울이 바 끝 안쪽으로 들어가 있는 거리
private val LiquidBlend = 8.dp  // 두 원 사이가 이 절반만큼 벌어지면 목이 끊김
private const val SURFACE_ALPHA = 0.16f
private const val DROP_MIN_SCALE = 0.7f
private const val SETTLE_FRAMES = 2
private const val SHOW_MILLIS = 340
private const val SHOW_SCALE_MILLIS = 460
private const val HIDE_MILLIS = 260
private const val ICON_FADE_MILLIS = 160
private const val ICON_DELAY_MILLIS = 120
private val ShowEasing = CubicBezierEasing(0.25f, 0.9f, 0.3f, 1f)
private val HideEasing = CubicBezierEasing(0.35f, 0.1f, 0.6f, 1f)
private val HideScaleEasing = CubicBezierEasing(0.36f, -0.3f, 0.6f, 1f)
private val ShowScaleEasing = CubicBezierEasing(0.34f, 1.45f, 0.5f, 1f)
