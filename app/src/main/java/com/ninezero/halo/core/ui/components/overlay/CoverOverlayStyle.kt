package com.ninezero.halo.core.ui.components.overlay

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle

/** 가림 오버레이 Haze 스타일 */
@Composable
@ReadOnlyComposable
fun coverOverlayStyle(): HaloBlurStyle {
    val surface = MaterialTheme.colorScheme.surface
    return HaloBlurStyle(
        backgroundColor = surface,
        tint = surface.copy(alpha = 0.35f),
        blurRadius = 10.dp,
        noiseFactor = 0f,
        fallbackTint = surface.copy(alpha = 0.92f)
    )
}

/** 잠금 오버레이 Haze 스타일 */
@Composable
@ReadOnlyComposable
fun lockedOverlayStyle(): HaloBlurStyle = HaloBlurStyle(
    backgroundColor = MaterialTheme.colorScheme.surface,
    tint = Color.Black.copy(alpha = 0.55f),
    blurRadius = 14.dp,
    noiseFactor = 0f
)

/** 가림 오버레이 모서리 */
val CoverOverlayShape = RoundedCornerShape(8.dp)
/** 잠금 오버레이 제목 글자색 */
val LockedOverlayLabelColor = Color.White
/** 잠금 오버레이 보조 글자색 */
val LockedOverlaySubLabelColor = Color.White.copy(alpha = 0.7f)
