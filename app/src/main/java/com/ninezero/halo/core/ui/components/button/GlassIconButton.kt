package com.ninezero.halo.core.ui.components.button

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/** 글래스 아이콘 버튼 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    hazeTintColor: Color? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 40.dp,
    iconSize: Dp = 24.dp,
    enabled: Boolean = true
) {
    val actualIconTint = if (enabled) iconTint else iconTint.copy(alpha = 0.38f)

    if (hazeState != null) {
        Box(
            modifier = modifier
                .then(if (enabled) Modifier.bounceClick() else Modifier)
                .size(size)
                .clip(CircleShape)
                .hazeEffect(state = hazeState) {
                    style = HazeStyle(
                        backgroundColor = backgroundColor.copy(alpha = 0.7f),
                        tint = HazeTint(hazeTintColor ?: backgroundColor.copy(alpha = 0.4f)),
                        blurRadius = 20.dp,
                        noiseFactor = 0f
                    )
                }
                .border(
                    width = 0.5.dp,
                    color = iconTint.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    enabled = enabled
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
                tint = actualIconTint
            )
        }
    } else {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = backgroundColor,
            shadowElevation = 4.dp,
            enabled = enabled,
            modifier = modifier
                .then(if (enabled) Modifier.bounceClick() else Modifier)
                .size(size)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(iconSize),
                    tint = actualIconTint
                )
            }
        }
    }
}
