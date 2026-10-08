package com.ninezero.halo.feature.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun UserProfileHeaderSkeleton(
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.surface

    Column(modifier = modifier.fillMaxWidth()) {
        // 커버 이미지 영역 + 프로필 헤더 오버레이
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        ) {
            // 커버 이미지 스켈레톤
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .shimmer()
            )

            // 그라데이션 오버레이
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Transparent,
                                backgroundColor.copy(alpha = 0.8f),
                                backgroundColor
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // 프로필 헤더 콘텐츠
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 프로필 이미지 + 정보
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 프로필 이미지
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .shimmer()
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 24.dp)
                    ) {
                        // 이름
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmer()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 유저이름
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmer()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 통계
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(48.dp)
                        ) {
                            repeat(3) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Box(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .shimmer()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .shimmer()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // 소개
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )

            // 위치
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }

            // 팔로우 + 메시지 버튼
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmer()
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmer()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun UserProfileHeaderSkeletonPreview() {
    HaloTheme {
        Surface {
            UserProfileHeaderSkeleton()
        }
    }
}
