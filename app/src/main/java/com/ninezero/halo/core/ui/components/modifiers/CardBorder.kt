package com.ninezero.halo.core.ui.components.modifiers

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** 테두리, 옅은 0.5 선 */
@Composable
fun Modifier.cardBorder(shape: Shape): Modifier =
    border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape)
