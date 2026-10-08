package com.ninezero.halo.feature.subscription.presentation.my_subscriptions.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun SubscriptionCardSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .shimmer()
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 정보
        Column(modifier = Modifier.weight(1f)) {
            // 크리에이터명 + StatusBadge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmer()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 플랜명 · 가격
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 만료일 · 남은 일수
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
        }

        // 스위치
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .shimmer()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SubscriptionCardSkeletonPreview() {
    HaloTheme {
        Column {
            SubscriptionCardSkeleton()
            SubscriptionCardSkeleton()
            SubscriptionCardSkeleton()
        }
    }
}
