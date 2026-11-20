package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.badge.MembershipBadge
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.media.Avatar
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus

@Composable
fun OrderCreatorGroupHeader(
    creatorName: String,
    creatorAvatarUrl: String?,
    modifier: Modifier = Modifier,
    creatorRole: String? = null,
    isSubscribed: Boolean = false,
    status: OrderStatus? = null,
    shippingStatus: ShippingStatus? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            model = creatorAvatarUrl,
            size = 24.dp,
            contentDescription = "$creatorName 프로필"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = creatorName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        VerifiedBadge(role = creatorRole, size = 14.dp)
        Spacer(modifier = Modifier.weight(1f))

        if (shippingStatus != null) {
            GroupShippingBadge(status = status, shippingStatus = shippingStatus)
        }

        if (isSubscribed) {
            if (shippingStatus != null) Spacer(modifier = Modifier.width(6.dp))
            MembershipBadge()
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderCreatorGroupHeaderPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                OrderCreatorGroupHeader(
                    creatorName = "크리에이터 샵",
                    creatorAvatarUrl = null,
                    creatorRole = "CREATOR",
                    status = OrderStatus.CONFIRMED,
                    shippingStatus = ShippingStatus.IN_TRANSIT
                )
                OrderCreatorGroupHeader(
                    creatorName = "구독 중인 샵",
                    creatorAvatarUrl = null,
                    isSubscribed = true
                )
                OrderCreatorGroupHeader(
                    creatorName = "탈퇴한 판매자",
                    creatorAvatarUrl = null,
                    creatorRole = "USER",
                    status = OrderStatus.CANCELLED
                )
            }
        }
    }
}
