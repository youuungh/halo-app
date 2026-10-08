package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.background
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
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloGradient
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun OrderCreatorGroupHeader(
    creatorName: String,
    creatorAvatarUrl: String?,
    modifier: Modifier = Modifier,
    creatorRole: String? = null,
    isSubscribed: Boolean = false,
    status: String? = null,
    shippingStatus: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = creatorAvatarUrl,
            contentDescription = "$creatorName 프로필",
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
private fun OrderCreatorGroupHeaderPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                OrderCreatorGroupHeader(
                    creatorName = "크리에이터 샵",
                    creatorAvatarUrl = null,
                    creatorRole = "CREATOR",
                    status = "CONFIRMED",
                    shippingStatus = "IN_TRANSIT"
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
                    status = "CANCELLED"
                )
            }
        }
    }
}
