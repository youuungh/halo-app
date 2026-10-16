package com.ninezero.halo.core.ui.components.button

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import dev.chrisbanes.haze.HazeState

/** 글래스 액션 버튼 */
@Composable
fun GlassActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    disabledBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    disabledContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val activeColor = if (enabled) backgroundColor else disabledBackgroundColor
    val activeContentColor = if (enabled) contentColor else disabledContentColor

    if (hazeState != null) {
        Box(
            modifier = modifier
                .then(if (enabled) Modifier.bounceClick() else Modifier)
                .height(40.dp)
                .clip(shape)
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = activeColor.copy(alpha = 0.7f),
                        tint = HaloBlurTint(activeColor.copy(alpha = 0.4f)),
                        blurRadius = 20.dp,
                        noiseFactor = 0f
                    )
                }
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = shape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    enabled = enabled
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides activeContentColor) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier
                .then(if (enabled) Modifier.bounceClick() else Modifier)
                .height(40.dp),
            shape = shape,
            color = activeColor,
            shadowElevation = 4.dp,
            enabled = enabled
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalContentColor provides activeContentColor) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        content = content
                    )
                }
            }
        }
    }
}
