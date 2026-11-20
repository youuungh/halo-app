package com.ninezero.halo.core.ui.components.bottomsheet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val SheetCornerRadius = 34.dp
private val StretchUpLimit = 24.dp
private val DismissDistance = 120.dp
private const val DISMISS_VELOCITY = 1800f
private val ReturnSpec = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)

/** 화면 가장자리에서 떠 있는 짧은 시트 */
@Composable
fun FloatingBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    showDragHandle: Boolean = true,
    dismissEnabled: Boolean = true,  // 전송·결제 중엔 끌어도 닫히지 않음
    fullHeight: Boolean = false,  // 상태바 바로 아래까지 채우는 시트
    content: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    // 카드 위끝을 상태바 아래 8에 맞춤
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topGap = if (fullHeight) (statusBarTop + 8.dp - StretchUpLimit).coerceAtLeast(0.dp) else 0.dp

    // 떠 있는 카드는 안쪽에서 그려서 시트 자체는 투명
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RectangleShape,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        dragHandle = null,
        sheetGesturesEnabled = false,  // 드래그는 카드가 직접 처리
        // 꽉 찬 시트는 상태바 여백을 topGap으로 직접 줌
        contentWindowInsets = { if (fullHeight) WindowInsets(0) else BottomSheetDefaults.windowInsets },
        modifier = modifier.padding(top = topGap)
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 8.dp)
                .padding(top = StretchUpLimit, bottom = 8.dp)  // 위로 늘어날 자리, 없으면 시트 경계에서 잘림
                .sheetDrag(
                    color = MaterialTheme.colorScheme.surface,
                    cornerRadius = SheetCornerRadius,
                    dismissEnabled = dismissEnabled,
                    onDismiss = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
                    }
                )
                .clip(RoundedCornerShape(SheetCornerRadius))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (showDragHandle) BottomSheetDragHandle()
            content()
        }
    }
}

/** 당길수록 덜 늘어나는 고무줄 거리 */
private fun resistPull(distance: Float, limit: Float): Float =
    (1f - 1f / (distance * 0.55f / limit + 1f)) * limit

/** 세로로만 따라옴, 아래로 충분히 당기면 닫힘 */
@Composable
private fun Modifier.sheetDrag(
    color: Color,
    cornerRadius: Dp,
    dismissEnabled: Boolean,
    onDismiss: () -> Unit
): Modifier {
    val canDismiss by rememberUpdatedState(dismissEnabled)
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val upLimit = with(density) { StretchUpLimit.toPx() }
    val dismissDistance = with(density) { DismissDistance.toPx() }
    val radius = with(density) { cornerRadius.toPx() }

    return this
        // 움직이기 전 좌표로 재야 손가락을 1:1로 따라옴
        .pointerInput(Unit) {
            val tracker = VelocityTracker()
            var raw = 0f
            detectVerticalDragGestures(
                onDragStart = {
                    tracker.resetTracking()
                    raw = offsetY.value
                    scope.launch { offsetY.stop() }
                },
                onDragEnd = {
                    val velocity = tracker.calculateVelocity().y
                    if (canDismiss && (raw > dismissDistance || velocity > DISMISS_VELOCITY)) {
                        onDismiss()
                    } else {
                        scope.launch { offsetY.animateTo(0f, ReturnSpec) }
                    }
                },
                onDragCancel = {
                    scope.launch { offsetY.animateTo(0f, ReturnSpec) }
                }
            ) { change, dragAmount ->
                change.consume()
                tracker.addPosition(change.uptimeMillis, change.position)
                raw += dragAmount
                val target = if (raw >= 0f) raw else -resistPull(-raw, upLimit)
                scope.launch { offsetY.snapTo(target) }
            }
        }
        .graphicsLayer { translationY = offsetY.value }
        // 위로 늘어날 때 아래 틈을 같은 색으로 채움
        .drawBehind {
            val stretch = -offsetY.value
            if (stretch > 0f) {
                drawRoundRect(
                    color = color,
                    size = Size(size.width, size.height + stretch),
                    cornerRadius = CornerRadius(radius)
                )
            }
        }
}
