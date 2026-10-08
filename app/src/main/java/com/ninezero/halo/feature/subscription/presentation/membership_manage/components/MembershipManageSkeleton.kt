package com.ninezero.halo.feature.subscription.presentation.membership_manage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun SummarySectionSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .shimmer()
            .height(100.dp)
    )
}

@Composable
fun PlanItemSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 플랜명 + Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .shimmer()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 가격
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tier · 설명
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 구독자 수
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MembershipManageSkeletonPreview() {
    HaloTheme {
        Column {
            SummarySectionSkeleton()
            PlanItemSkeleton()
            PlanItemSkeleton()
            PlanItemSkeleton()
        }
    }
}
