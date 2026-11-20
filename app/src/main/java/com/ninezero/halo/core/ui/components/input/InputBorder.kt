package com.ninezero.halo.core.ui.components.input

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** 입력칸 테두리, 입력 중 · 오류만 1dp 색 선 */
@Composable
fun Modifier.inputBorder(
    shape: Shape,
    isFocused: Boolean = false,
    isError: Boolean = false
): Modifier {
    val color by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(150),
        label = "inputBorder"
    )
    return border(width = if (isFocused || isError) 1.dp else 0.5.dp, color = color, shape = shape)
}
