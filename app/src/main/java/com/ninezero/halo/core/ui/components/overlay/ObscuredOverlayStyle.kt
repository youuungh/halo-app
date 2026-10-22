package com.ninezero.halo.core.ui.components.overlay

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint

/** 가림 오버레이 Haze 스타일 */
@Composable
@ReadOnlyComposable
fun obscuredOverlayStyle(): HaloBlurStyle {
    val surface = MaterialTheme.colorScheme.surface
    return HaloBlurStyle(
        backgroundColor = surface,
        tint = HaloBlurTint(surface.copy(alpha = 0.35f)),
        blurRadius = 10.dp,
        noiseFactor = 0f,
        fallbackTint = HaloBlurTint(surface.copy(alpha = 0.92f))
    )
}

/** 잠금 오버레이 Haze 스타일 */
@Composable
@ReadOnlyComposable
fun lockedOverlayStyle(): HaloBlurStyle = HaloBlurStyle(
    backgroundColor = MaterialTheme.colorScheme.surface,
    tint = HaloBlurTint(Color.Black.copy(alpha = 0.55f)),
    blurRadius = 14.dp,
    noiseFactor = 0f
)

val ObscuredOverlayShape = RoundedCornerShape(8.dp)
val LockedOverlayLabelColor = Color.White
val LockedOverlaySubLabelColor = Color.White.copy(alpha = 0.7f)
