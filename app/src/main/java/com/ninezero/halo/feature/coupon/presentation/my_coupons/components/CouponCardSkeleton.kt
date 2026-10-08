package com.ninezero.halo.feature.coupon.presentation.my_coupons.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun CouponCardSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .border(
                0.3.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
    ) {
        // 좌측: 할인 금액 + 쿠폰 정보
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            // 할인 금액
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            Spacer(modifier = Modifier.height(6.dp))
            // 쿠폰명
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            Spacer(modifier = Modifier.height(4.dp))
            // 유효기간
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 최소 주문 금액
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
        }

        // 우측: 상태 영역
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(84.dp)
                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                .shimmer(),
            contentAlignment = Alignment.Center
        ) {}
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponCardSkeletonPreview() {
    HaloTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CouponCardSkeleton()
            Spacer(modifier = Modifier.height(8.dp))
            CouponCardSkeleton()
            Spacer(modifier = Modifier.height(8.dp))
            CouponCardSkeleton()
        }
    }
}
