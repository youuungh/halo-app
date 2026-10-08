package com.ninezero.halo.feature.store.presentation.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.checkbox.HaloCheckbox
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.ui.theme.HaloGradient
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun CreatorGroupHeader(
    creator: UserSummary,
    isAllSelected: Boolean,
    onToggleSelection: () -> Unit,
    onCreatorClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSubscribed: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HaloCheckbox(
            checked = isAllSelected,
            onClick = onToggleSelection
        )

        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCreatorClick
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = creator.avatarThumbUrl ?: creator.avatarUrl,
                contentDescription = "${creator.displayName} 프로필",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                error = painterResource(R.drawable.ic_profile_placeholder),
                fallback = painterResource(R.drawable.ic_profile_placeholder)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = creator.displayName ?: creator.username,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize
                ),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            VerifiedBadge(role = creator.role, size = 14.dp)
        }

        if (isSubscribed) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "멤버십",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .background(
                        brush = HaloGradient,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorGroupHeaderPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                CreatorGroupHeader(
                    creator = previewUserSummary(displayName = "크리에이터 샵", role = "CREATOR"),
                    isAllSelected = true,
                    onToggleSelection = {},
                    onCreatorClick = {}
                )
                CreatorGroupHeader(
                    creator = previewUserSummary(id = 2, displayName = "구독 중인 샵"),
                    isAllSelected = false,
                    onToggleSelection = {},
                    onCreatorClick = {},
                    isSubscribed = true
                )
            }
        }
    }
}
