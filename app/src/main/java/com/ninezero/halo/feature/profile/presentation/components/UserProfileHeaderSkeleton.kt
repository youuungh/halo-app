package com.ninezero.halo.feature.profile.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.layout.collapsingGlassTopBarHeight
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme

private val SkeletonBlockShape = RoundedCornerShape(12.dp)
private val SkeletonLineShape = RoundedCornerShape(4.dp)

@Composable
fun UserProfileHeaderSkeleton(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = collapsingGlassTopBarHeight() + 4.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .shimmer()
            )
            Spacer(modifier = Modifier.height(16.dp))
            SkeletonLine(modifier = Modifier.width(100.dp), height = 20.dp)
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonLine(modifier = Modifier.width(80.dp), height = 14.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 팔로우·구독·메시지 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(SkeletonBlockShape)
                            .shimmer()
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 통계
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SkeletonLine(modifier = Modifier.width(32.dp), height = 18.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        SkeletonLine(modifier = Modifier.width(40.dp), height = 12.dp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // 소개
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                SkeletonLine(modifier = Modifier.fillMaxWidth(0.8f), height = 14.dp)
                Spacer(modifier = Modifier.height(10.dp))
                SkeletonLine(modifier = Modifier.width(60.dp), height = 12.dp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 탭 자리
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(40.dp)
                .clip(SkeletonBlockShape)
                .shimmer()
        )
    }
}

@Composable
private fun SkeletonLine(modifier: Modifier, height: Dp) {
    Box(
        modifier = modifier
            .height(height)
            .clip(SkeletonLineShape)
            .shimmer()
    )
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
