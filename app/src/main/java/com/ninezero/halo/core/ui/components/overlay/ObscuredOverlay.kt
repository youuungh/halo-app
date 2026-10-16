package com.ninezero.halo.core.ui.components.overlay

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import dev.chrisbanes.haze.HazeState

/** 가림 오버레이, hazeSource를 둔 Box 안에서 호출 */
@Composable
fun BoxScope.ObscuredOverlay(
    hazeState: HazeState,
    @DrawableRes icon: Int,
    label: String,
    contentDescription: String = label,
    color: Color = ObscuredOverlayLabelColor
) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(RoundedCornerShape(8.dp))
            .haloBlur(
                state = hazeState,
                style = obscuredOverlayStyle()
            )
            .pointerInput(Unit) {},  // 아래 숨은 버튼으로 터치 안 넘김
        contentAlignment = Alignment.Center
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
}
