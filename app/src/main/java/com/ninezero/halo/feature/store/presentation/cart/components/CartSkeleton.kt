package com.ninezero.halo.feature.store.presentation.cart.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer

@Composable
fun CartSkeleton(topPadding: Dp) {
    LazyColumn(
        userScrollEnabled = false,
        contentPadding = PaddingValues(top = topPadding)
    ) {
        items(listOf(0, 1, 2, 3)) { groupIndex ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = if (groupIndex == 0) 0.dp else 8.dp)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
            ) {
                CreatorGroupHeaderSkeleton()

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                CartItemRowSkeleton()
            }
        }
    }
}

@Composable
private fun CreatorGroupHeaderSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 체크박스 플레이스홀더
        Box(
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 15.dp)
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )

        // 아바타 원형
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .shimmer()
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 이름 텍스트 플레이스홀더
        Box(
            modifier = Modifier
                .height(14.dp)
                .width(80.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmer()
        )
    }
}

@Composable
private fun CartItemRowSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // 체크박스
            Box(modifier = Modifier.size(48.dp))

            // 상품 이미지 플레이스홀더
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .shimmer()
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 상품 정보
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }

        // 수량 셀렉터
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(140.dp))
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmer()
            )
        }
    }
}
