package com.ninezero.halo.feature.coupon.presentation.my_coupons.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme

@Composable
fun CouponCardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonBar(width = 120.dp, height = 16.dp)
            Spacer(modifier = Modifier.weight(1f))
            SkeletonBar(width = 72.dp, height = 12.dp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        SkeletonBar(width = 96.dp, height = 22.dp)
        Spacer(modifier = Modifier.height(6.dp))
        SkeletonBar(width = 130.dp, height = 12.dp)
        Spacer(modifier = Modifier.height(4.dp))
        SkeletonBar(width = 150.dp, height = 12.dp)
    }
}

@Composable
private fun SkeletonBar(width: Dp, height: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(MaterialTheme.shapes.extraSmall)
            .shimmer()
    )
}

@HaloLightDarkPreview
@Composable
private fun CouponCardSkeletonPreview() {
    HaloTheme {
        Column {
            CouponCardSkeleton()
            CouponCardSkeleton()
            CouponCardSkeleton()
        }
    }
}
