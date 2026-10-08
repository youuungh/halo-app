package com.ninezero.halo.feature.feed.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun ReplyCurveContainer(
    parentLineX: Dp,
    curveEndX: Dp,
    avatarCenterY: Dp,
    topLead: Dp,
    lineColor: Color,
    lineWidth: Dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val startX = parentLineX.toPx()
                val endX = curveEndX.toPx()
                val centerY = avatarCenterY.toPx()
                // 곡선 앞 짧은 직선, 부모 직선과 매끈하게 연결
                val lead = 8.dp.toPx().coerceAtMost(centerY * 0.5f)
                val path = Path().apply {
                    moveTo(startX, 0f)
                    lineTo(startX, lead)
                    quadraticTo(startX, centerY, endX, centerY)
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    // 캡 Butt로 부모 직선 끝과 맞물림
                    style = Stroke(width = lineWidth.toPx(), cap = StrokeCap.Butt)
                )
            }
    ) {
        Box(modifier = Modifier.padding(top = topLead)) {
            content()
        }
    }
}
