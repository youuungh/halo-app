package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun ProductDetailSkeleton(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 상품 이미지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .shimmer()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 판매자 정보
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                            .clip(CircleShape)
                            .shimmer()
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 상품명
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 평점
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 가격
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 태그
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .shimmer()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 구분선
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .shimmer()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 상세 설명 라인
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (it == 3) 0.6f else 1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductDetailSkeletonPreview() {
    HaloTheme {
        ProductDetailSkeleton()
    }
}
