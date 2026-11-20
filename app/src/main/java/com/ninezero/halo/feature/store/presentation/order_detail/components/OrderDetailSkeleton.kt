package com.ninezero.halo.feature.store.presentation.order_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer

fun LazyListScope.orderDetailSkeleton() {
    // 주문 정보: 주문번호 · 주문일시
    item(key = "skeleton_order_info") {
        SectionHeaderSkeleton()
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            repeat(2) {
                InfoRowSkeleton()
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OrderSectionDivider()
    }

    // 주문 상품
    item(key = "skeleton_items") {
        SectionHeaderSkeleton()
        // 크리에이터 상품 카드 모양만
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(MaterialTheme.shapes.medium)
                .shimmer()
                .height(196.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OrderSectionDivider()
    }

    // 배송 정보: 이름 · 연락처 한 줄, 주소 한 줄
    item(key = "skeleton_shipping") {
        SectionHeaderSkeleton()
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBar(width = 48.dp, height = 16.dp)
                Spacer(modifier = Modifier.width(6.dp))
                SkeletonBar(width = 96.dp, height = 12.dp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBar(width = 200.dp, height = 14.dp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OrderSectionDivider()
    }

    // 결제 정보 자리
    item(key = "skeleton_payment") {
        SectionHeaderSkeleton()
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            repeat(2) {
                InfoRowSkeleton()
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBar(width = 88.dp, height = 14.dp)
                SkeletonBar(width = 96.dp, height = 18.dp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            repeat(2) {
                InfoRowSkeleton()
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
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
internal fun InfoRowSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(12.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .shimmer()
        )
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(12.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .shimmer()
        )
    }
}
