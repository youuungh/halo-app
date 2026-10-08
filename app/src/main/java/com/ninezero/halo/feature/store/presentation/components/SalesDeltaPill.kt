package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.Error
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.ui.theme.Primary
import com.ninezero.halo.ui.theme.Success
import java.util.Locale
import kotlin.math.abs

@Composable
fun SalesDeltaPill(
    thisMonth: Long,
    lastMonth: Long,
    modifier: Modifier = Modifier,
    prefix: String = "전월 대비",
    verb: Boolean = false
) {
    val isDark = LocalIsDarkTheme.current
    val bgAlpha = if (isDark) 0.20f else 0.12f

    when {
        lastMonth <= 0L && thisMonth > 0L -> {
            DeltaPillContainer(color = Primary, bgAlpha = bgAlpha, modifier = modifier) {
                Text(
                    text = if (verb) "이번 달 첫 매출이에요" else "첫 매출",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
            }
        }

        lastMonth > 0L -> {
            val deltaPercent = (thisMonth - lastMonth) * 100.0 / lastMonth
            val isUp = deltaPercent >= 0.0
            val color = if (isUp) Success else Error
            val pctText = String.format(Locale.KOREA, "%.1f%%", abs(deltaPercent))
            DeltaPillContainer(color = color, bgAlpha = bgAlpha, modifier = modifier) {
                if (!verb) {
                    Icon(
                        painter = painterResource(
                            if (isUp) R.drawable.ic_trending_up else R.drawable.ic_trending_down
                        ),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = if (verb) {
                        "$prefix $pctText ${if (isUp) "늘었어요" else "줄었어요"}"
                    } else {
                        "$prefix $pctText"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun DeltaPillContainer(
    color: Color,
    bgAlpha: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = bgAlpha))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesDeltaPillPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SalesDeltaPill(thisMonth = 4_150_000L, lastMonth = 3_420_000L)  // 증가
                SalesDeltaPill(thisMonth = 2_100_000L, lastMonth = 3_420_000L)  // 감소
                SalesDeltaPill(thisMonth = 3_420_000L, lastMonth = 3_420_000L)  // 동일
                SalesDeltaPill(thisMonth = 1_200_000L, lastMonth = 0L)          // 지난달 매출 없음
                SalesDeltaPill(thisMonth = 4_150_000L, lastMonth = 3_420_000L, verb = true)
            }
        }
    }
}
