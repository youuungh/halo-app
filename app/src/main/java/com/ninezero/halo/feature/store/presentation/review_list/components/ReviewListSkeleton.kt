package com.ninezero.halo.feature.store.presentation.review_list.components

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
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun ReviewSummarySkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좌측: 평점 + 별
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // 우측: 분포 바 5개
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmer()
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .shimmer()
                        )
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmer()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItemSkeleton(
    modifier: Modifier = Modifier
) {
    val horizontalPadding = 8.dp
    val avatarSize = 40.dp
    val avatarSpacing = 12.dp

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(top = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 아바타
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .shimmer()
            )

            Spacer(modifier = Modifier.width(avatarSpacing))

            Column(modifier = Modifier.weight(1f)) {
                // 별점 + 평점
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                }

                // 이름 + 날짜
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(55.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmer()
                    )
                }

                // 본문 1행
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 본문 2행
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@HaloLightDarkPreview
@Composable
private fun ReviewSummarySkeletonPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ReviewSummarySkeleton()
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ReviewItemSkeletonPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ReviewItemSkeleton()
                ReviewItemSkeleton()
                ReviewItemSkeleton()
            }
        }
    }
}
