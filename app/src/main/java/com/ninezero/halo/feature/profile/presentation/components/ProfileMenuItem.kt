package com.ninezero.halo.feature.profile.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.theme.DarkMenuTile
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme

private val LightTileOutline = Shadow(radius = 0.dp, spread = 0.5.dp, color = Color.Black.copy(alpha = 0.05f))
private val DarkTileOutline = Shadow(radius = 0.dp, spread = 0.5.dp, color = Color.White.copy(alpha = 0.10f))
private val LightTileShadow = Shadow(radius = 4.dp, color = Color.Black.copy(alpha = 0.08f))
private val DarkTileShadow = Shadow(radius = 4.dp, color = Color.Black.copy(alpha = 0.5f))

@Composable
fun ProfileMenuItem(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCreatorMenu: Boolean = false
) {
    val contentColor = if (isCreatorMenu) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val shape = MaterialTheme.shapes.large

    Column(
        modifier = modifier
            .aspectRatio(4f / 3f)
            .bounceClick()
            .profileTileSurface(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) { onClick() }
            .padding(horizontal = 11.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = contentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isCreatorMenu) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun Modifier.profileTileSurface(shape: Shape): Modifier {
    val isDark = LocalIsDarkTheme.current
    return this
        .dropShadow(shape, if (isDark) DarkTileOutline else LightTileOutline)
        .dropShadow(shape, if (isDark) DarkTileShadow else LightTileShadow)
        .clip(shape)
        .background(if (isDark) DarkMenuTile else Color.White)
}
