package com.ninezero.halo.core.ui.components.popup

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.glassEdgeLight
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.theme.DarkGlassLightTone
import com.ninezero.halo.core.ui.theme.GlassLightTone
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt

private val MenuShape = RoundedCornerShape(20.dp)

// 누른 자리에서 커지기 시작하는 배율
private const val COLLAPSED_SCALE = 0.4f

// 그림자와 튕김이 창에 잘리지 않는 여유
private val MenuWindowMargin = 24.dp

/** 누른 자리에서 펼쳐지는 블러 팝업 메뉴 */
@Composable
fun HaloPopupMenu(
    hazeState: HazeState?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopStart,
    offset: IntOffset = IntOffset.Zero,
    content: @Composable ColumnScope.() -> Unit
) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    val margin = with(LocalDensity.current) { MenuWindowMargin.roundToPx() }
    val positionProvider = remember(alignment, offset, margin) { MenuPositionProvider(alignment, offset, margin) }
    val rim = if (LocalIsDarkTheme.current) DarkMenuRim else LightMenuRim

    // 바깥 탭과 뒤로가기는 접힌 뒤에 닫음
    if (visibleState.isIdle && !visibleState.targetState) {
        LaunchedEffect(Unit) { onDismiss() }
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = { visibleState.targetState = false },
        properties = PopupProperties(focusable = true, clippingEnabled = false)
    ) {
        val transition = rememberTransition(visibleState, label = "HaloPopupMenu")
        val scale = transition.animateFloat(
            transitionSpec = {
                if (targetState) {
                    spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)
                } else {
                    tween(150)
                }
            },
            label = "scale"
        ) { if (it) 1f else COLLAPSED_SCALE }
        val alpha = transition.animateFloat(
            transitionSpec = { tween(if (targetState) 100 else 150) },
            label = "alpha"
        ) { if (it) 1f else 0f }

        ScaledMenuLayout(
            scale = { scale.value },
            alignment = alignment,
            margin = margin,
            modifier = Modifier
                .graphicsLayer { this.alpha = alpha.value }
                // 창의 여유 영역을 눌러도 바깥 탭처럼 닫음
                .pointerInput(Unit) { detectTapGestures { visibleState.targetState = false } },
            surface = {
                Box(
                    modifier = Modifier
                        .dropShadow(MenuShape, rim.floating)
                        // 블러가 첫 프레임을 그리기 전에 안쪽 그림자가 비치지 않게
                        .background(MaterialTheme.colorScheme.surface, MenuShape)
                        .clip(MenuShape)
                        .then(if (hazeState != null) Modifier.haloBlur(hazeState, HaloBlurs.popupMenu()) else Modifier)
                        .innerShadow(MenuShape, rim.top)
                        .innerShadow(MenuShape, rim.bottom)
                        .border(0.5.dp, rim.border, MenuShape)
                )
            }
        ) {
            Column(
                modifier = modifier
                    .width(IntrinsicSize.Max)
                    .widthIn(min = 150.dp)
                    .clip(MenuShape)
                    // 메뉴 안쪽 탭은 닫기로 넘어가지 않게
                    .pointerInput(Unit) { detectTapGestures { } },
                content = content
            )
        }
    }
}

// 창 크기는 고정하고 배경은 레이아웃 크기로 줄이며 내용만 확대
@Composable
private fun ScaledMenuLayout(
    scale: () -> Float,
    alignment: Alignment,
    margin: Int,
    modifier: Modifier,
    surface: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Layout(contents = listOf(surface, content), modifier = modifier) { (surfaces, contents), constraints ->
        val menu = contents.single().measure(constraints)
        val factor = scale()
        val size = IntSize((menu.width * factor).roundToInt(), (menu.height * factor).roundToInt())
        val glass = surfaces.single().measure(Constraints.fixed(size.width, size.height))
        layout(menu.width + margin * 2, menu.height + margin * 2) {
            // 펼쳐지는 기준 모서리를 메뉴 자리에 고정
            val position = alignment.align(size, IntSize(menu.width, menu.height), layoutDirection) +
                IntOffset(margin, margin)
            glass.place(position)
            menu.placeWithLayer(position) {
                scaleX = factor
                scaleY = factor
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

// 창의 여유를 뺀 메뉴 자리만 기준으로 맞추고 화면 안에 둠
private class MenuPositionProvider(
    private val alignment: Alignment,
    private val offset: IntOffset,
    private val margin: Int
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val menu = IntSize(popupContentSize.width - margin * 2, popupContentSize.height - margin * 2)
        val anchor = alignment.align(IntSize.Zero, anchorBounds.size, layoutDirection)
        val corner = alignment.align(IntSize.Zero, menu, layoutDirection)
        val offsetX = if (layoutDirection == LayoutDirection.Ltr) offset.x else -offset.x
        val x = anchorBounds.left + anchor.x - corner.x + offsetX
        val y = anchorBounds.top + anchor.y - corner.y + offset.y
        return IntOffset(
            x.coerceIn(0, maxOf(0, windowSize.width - menu.width)) - margin,
            y.coerceIn(0, maxOf(0, windowSize.height - menu.height)) - margin
        )
    }
}

/** 메뉴의 그림자 · 테두리 · 위아래 빛 */
private class MenuRim(light: Color, val border: Color, floatingAlpha: Float) {
    val floating = Shadow(radius = 16.dp, color = Color.Black.copy(alpha = floatingAlpha), offset = DpOffset(0.dp, 8.dp))
    val top = glassEdgeLight(40.dp, light)
    val bottom = glassEdgeLight((-40).dp, light)
}

private val LightMenuRim = MenuRim(GlassLightTone, Color.Black.copy(alpha = 0.14f), 0.12f)

// 다크에서 어두운 선은 검은 고리로 보여 밝은 선으로 둠
private val DarkMenuRim = MenuRim(DarkGlassLightTone, Color.White.copy(alpha = 0.1f), 0.28f)
