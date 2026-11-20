package com.ninezero.halo.feature.store.presentation.checkout.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer

fun LazyListScope.checkoutSkeleton() {
    // 배송지
    item(key = "skeleton_shipping") {
        SectionHeaderSkeleton()
        BlockSkeleton(height = 112.dp, corner = 12.dp)
    }

    // 주문 상품
    item(key = "skeleton_order_items") {
        SectionHeaderSkeleton()
        BlockSkeleton(height = 240.dp, corner = 20.dp)
    }

    // 포인트
    item(key = "skeleton_point") {
        SectionHeaderSkeleton()
        BlockSkeleton(height = 72.dp, corner = 20.dp)
    }

    // 결제 금액
    item(key = "skeleton_summary") {
        SectionHeaderSkeleton()
        BlockSkeleton(height = 150.dp, corner = 20.dp)
    }
}

@Composable
private fun SectionHeaderSkeleton() {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .width(64.dp)
            .height(16.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .shimmer()
    )
}

@Composable
private fun BlockSkeleton(height: Dp, corner: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(corner))
            .shimmer()
            .height(height)
    )
}
