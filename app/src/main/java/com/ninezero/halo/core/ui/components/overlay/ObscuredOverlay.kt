package com.ninezero.halo.core.ui.components.overlay

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import dev.chrisbanes.haze.HazeState

/** 가림 오버레이 */
@Composable
fun BoxScope.ObscuredOverlay(
    hazeState: HazeState,
    @DrawableRes icon: Int,
    label: String,
    contentDescription: String = label,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    val veilColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)

    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(ObscuredOverlayShape)
            .haloBlur(
                state = hazeState,
                style = obscuredOverlayStyle()
            )
            .ellipseVeil(veilColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier.pointerInput(Unit) {}  // 아래 숨은 버튼으로 터치 안 넘김
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        OverlayLabel(icon = icon, label = label, contentDescription = contentDescription, color = color)
    }
}

/** 잠금 오버레이 */
@Composable
fun BoxScope.LockedOverlay(
    hazeState: HazeState,
    @DrawableRes icon: Int,
    label: String,
    contentDescription: String = label
) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(ObscuredOverlayShape)
            .haloBlur(
                state = hazeState,
                style = lockedOverlayStyle()
            )
            .pointerInput(Unit) {},  // 아래 숨은 버튼으로 터치 안 넘김
        contentAlignment = Alignment.Center
    ) {
        OverlayLabel(icon = icon, label = label, contentDescription = contentDescription, color = LockedOverlayLabelColor)
    }
}

@Composable
private fun OverlayLabel(
    @DrawableRes icon: Int,
    label: String,
    contentDescription: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

// 문구 뒤만 배경색으로 옅게 덮는 타원, 가장자리로 갈수록 투명
private fun Modifier.ellipseVeil(color: Color): Modifier = drawWithCache {
    if (size.minDimension <= 0f) return@drawWithCache onDrawBehind {}  // 반지름 0 그라데이션은 예외
    val radiusX = size.width * 0.65f
    val radiusY = size.height * 0.8f
    val brush = Brush.radialGradient(
        colors = listOf(color, color.copy(alpha = 0f)),  // 투명 검정으로 가면 중간이 회색으로 탁해짐
        center = size.center,
        radius = radiusX
    )
    onDrawBehind {
        // 원 그라데이션을 세로로 눌러 타원으로
        scale(scaleX = 1f, scaleY = radiusY / radiusX, pivot = center) {
            drawRect(
                brush = brush,
                topLeft = Offset(0f, center.y - radiusX),
                size = Size(size.width, radiusX * 2)
            )
        }
    }
}
