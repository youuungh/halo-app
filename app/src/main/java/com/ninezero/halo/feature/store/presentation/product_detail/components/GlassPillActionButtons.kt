package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
fun GlassPillActionButtons(
    hazeState: HazeState,
    backgroundColor: Color,
    iconTint: Color,
    onCartClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
    cartItemCount: Int = 0
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .hazeEffect(state = hazeState) {
                style = HazeStyle(
                    backgroundColor = backgroundColor.copy(alpha = 0.7f),
                    tint = HazeTint(backgroundColor.copy(alpha = 0.4f)),
                    blurRadius = 20.dp,
                    noiseFactor = 0f
                )
            }
            .border(
                width = 0.5.dp,
                color = iconTint.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 장바구니 버튼
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true)
                ) { onCartClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_cart),
                contentDescription = "장바구니",
                modifier = Modifier.size(22.dp),
                tint = iconTint
            )
            if (cartItemCount > 0) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = 0.dp)
                        .size(6.dp),
                    containerColor = MaterialTheme.colorScheme.error
                )
            }
        }

        // 구분선
        Box(
            modifier = Modifier
                .width(0.5.dp)
                .height(20.dp)
                .background(iconTint.copy(alpha = 0.3f))
        )

        // 공유 버튼
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true)
                ) { onShareClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = "공유",
                modifier = Modifier.size(22.dp),
                tint = iconTint
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun GlassPillActionButtonsPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                GlassPillActionButtons(
                    hazeState = remember { HazeState() },
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    iconTint = MaterialTheme.colorScheme.onSurface,
                    onCartClick = {},
                    onShareClick = {},
                    cartItemCount = 3
                )
            }
        }
    }
}
