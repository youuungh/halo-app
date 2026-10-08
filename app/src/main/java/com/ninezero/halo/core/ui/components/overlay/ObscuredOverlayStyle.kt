package com.ninezero.halo.core.ui.components.overlay

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

/** 가림 오버레이 Haze 스타일 */
@Composable
@ReadOnlyComposable
fun obscuredOverlayStyle(): HazeStyle = HazeStyle(
    backgroundColor = MaterialTheme.colorScheme.surface,
    tint = HazeTint(Color.Black.copy(alpha = 0.55f)),
    blurRadius = 14.dp,
    noiseFactor = 0f
)

val ObscuredOverlayLabelColor = Color.White
val ObscuredOverlaySubLabelColor = Color.White.copy(alpha = 0.7f)
