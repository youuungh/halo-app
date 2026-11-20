package com.ninezero.halo.feature.store.presentation.cart.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.badge.MembershipBadge
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.media.Avatar
import com.ninezero.halo.core.ui.components.toggle.HaloCheckbox
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.core.ui.theme.HaloTheme

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
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HaloCheckbox(
            checked = isAllSelected,
            onClick = onToggleSelection
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCreatorClick
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                model = creator.avatarThumbUrl ?: creator.avatarUrl,
                size = 24.dp,
                contentDescription = "${creator.displayName} 프로필"
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = creator.displayName ?: creator.username,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize
                ),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            VerifiedBadge(role = creator.role, size = 14.dp)

            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSubscribed) {
            Spacer(modifier = Modifier.width(8.dp))
            MembershipBadge()
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
