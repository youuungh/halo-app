package com.ninezero.halo.feature.store.presentation.review_list.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toFormattedCount
import com.ninezero.halo.feature.store.domain.model.ReviewSummary
import com.ninezero.halo.feature.store.presentation.components.StarRating
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
internal fun ReviewSummaryCard(
    summary: ReviewSummary,
    modifier: Modifier = Modifier
) {
    val locale = LocalLocale.current.platformLocale

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = String.format(locale, "%.1f", summary.avgRating),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StarRating(rating = summary.avgRating, starSize = 14.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${summary.totalReviews.toFormattedCount()}개의 리뷰",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (star in 5 downTo 1) {
                    // key(star)로 saveable 슬롯 분리
                    key(star) {
                        val count = summary.ratingDistribution[star] ?: 0
                        val targetRatio = if (summary.totalReviews > 0) {
                            count.toFloat() / summary.totalReviews
                        } else 0f
                        var animatedTarget by rememberSaveable { mutableFloatStateOf(0f) }
                        LaunchedEffect(targetRatio) { animatedTarget = targetRatio }
                        val ratio by animateFloatAsState(
                            targetValue = animatedTarget,
                            animationSpec = tween(durationMillis = 800),
                            label = "ratingBarAnim_$star"
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$star",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(12.dp)
                            )
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFFFC107),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = count.toFormattedCount(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ReviewSummaryCardPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                ReviewSummaryCard(
                    summary = ReviewSummary(
                        avgRating = 4.5,
                        totalReviews = 128,
                        ratingDistribution = mapOf(5 to 72, 4 to 35, 3 to 12, 2 to 6, 1 to 3)
                    )
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ReviewSummaryCardEmptyPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                ReviewSummaryCard(
                    summary = ReviewSummary(
                        avgRating = 0.0,
                        totalReviews = 0,
                        ratingDistribution = emptyMap()
                    )
                )
            }
        }
    }
}
