package com.ninezero.halo.feature.store.presentation.sales_stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.Primary
import com.ninezero.halo.core.util.toCompactWon
import com.ninezero.halo.core.util.toFormattedPrice
import com.ninezero.halo.feature.store.domain.model.BestSeller
import com.ninezero.halo.feature.store.domain.model.MonthRevenue
import java.time.YearMonth
import kotlin.math.roundToInt

private val BAR_AREA_HEIGHT = 92.dp
private val VALUE_LABEL_RESERVE = 22.dp
private val MONTH_LABEL_RESERVE = 20.dp

@Composable
fun SalesTrendBars(
    trend: List<MonthRevenue>,
    selectedMonth: YearMonth,
    modifier: Modifier = Modifier
) {
    if (trend.isEmpty()) return
    val maxRevenue = (trend.maxOfOrNull { it.revenue } ?: 0L).coerceAtLeast(1L)
    val maxMonth = trend.maxByOrNull { it.revenue }?.month
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall

    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val inactiveBar = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(BAR_AREA_HEIGHT + VALUE_LABEL_RESERVE + MONTH_LABEL_RESERVE)
    ) {
        val n = trend.size
        val w = size.width
        val areaTop = VALUE_LABEL_RESERVE.toPx()
        val areaH = BAR_AREA_HEIGHT.toPx()
        val barW = (w / n) * 0.62f
        val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        val minBar = 3.dp.toPx()

        // edge-to-edge 중심 좌표
        fun centerX(i: Int): Float =
            if (n > 1) barW / 2f + i * (w - barW) / (n - 1) else w / 2f

        val tops = trend.mapIndexed { i, item ->
            val ratio = item.revenue.toFloat() / maxRevenue
            Offset(centerX(i), areaTop + areaH * (1f - ratio))
        }

        // 막대 + 값 라벨, 선택한 달 · 최고 달만
        trend.forEachIndexed { i, item ->
            val cx = tops[i].x
            val isSelected = item.month == selectedMonth
            val fillTop = tops[i].y.coerceAtMost(areaTop + areaH - minBar)
            drawRoundRect(
                color = if (isSelected) Primary else inactiveBar,
                topLeft = Offset(cx - barW / 2f, fillTop),
                size = Size(barW, areaTop + areaH - fillTop),
                cornerRadius = corner
            )

            if (isSelected || item.month == maxMonth) {
                val measured = textMeasurer.measure(
                    text = item.revenue.toCompactWon(),
                    style = labelStyle.copy(
                        color = if (isSelected) Primary else onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                val tx = (cx - measured.size.width / 2f).coerceIn(0f, w - measured.size.width)
                val ty = (fillTop - measured.size.height - 4.dp.toPx()).coerceAtLeast(0f)
                drawText(measured, topLeft = Offset(tx, ty))
            }
        }

        // 월 라벨
        val monthLabelY = areaTop + areaH + 6.dp.toPx()
        trend.forEachIndexed { i, item ->
            val isSelected = item.month == selectedMonth
            val measured = textMeasurer.measure(
                text = "${item.month.monthValue}월",
                style = labelStyle.copy(
                    color = if (isSelected) Primary else onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
            val tx = (centerX(i) - measured.size.width / 2f).coerceIn(0f, w - measured.size.width)
            drawText(measured, topLeft = Offset(tx, monthLabelY))
        }
    }
}

@Composable
fun BestSellerRow(
    rank: Int,
    item: BestSeller,
    totalRevenue: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sharePercent = if (totalRevenue > 0L) (item.revenue * 100.0 / totalRevenue).roundToInt() else 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RankBadge(rank = rank)
        Spacer(modifier = Modifier.width(10.dp))
        AsyncImage(
            model = item.productImageUrl,
            contentDescription = item.productName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.quantity}개 판매 · 매출의 ${sharePercent}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.revenue.toFormattedPrice(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RankBadge(rank: Int) {
    val isFirst = rank == 1
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(if (isFirst) Primary else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isFirst) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val previewTrend = listOf(
    MonthRevenue(YearMonth.of(2026, 3), 1_840_000L),
    MonthRevenue(YearMonth.of(2026, 4), 2_310_000L),
    MonthRevenue(YearMonth.of(2026, 5), 1_650_000L),
    MonthRevenue(YearMonth.of(2026, 6), 2_980_000L),
    MonthRevenue(YearMonth.of(2026, 7), 3_420_000L),
    MonthRevenue(YearMonth.of(2026, 8), 4_150_000L)
)

@HaloLightDarkPreview
@Composable
private fun SalesTrendBarsPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                SalesTrendBars(
                    trend = previewTrend,
                    selectedMonth = YearMonth.of(2026, 8)
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesTrendBarsMiddleSelectedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                SalesTrendBars(
                    trend = previewTrend,
                    selectedMonth = YearMonth.of(2026, 5)
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun BestSellerRowPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                BestSellerRow(
                    rank = 1,
                    item = BestSeller(1, "프리미엄 무선 이어폰 Pro Max", null, 32, 2_848_000L),
                    totalRevenue = 4_135_000L,
                    onClick = {}
                )
                BestSellerRow(
                    rank = 2,
                    item = BestSeller(2, "노이즈캔슬링 헤드폰", null, 7, 1_113_000L),
                    totalRevenue = 4_135_000L,
                    onClick = {}
                )
                BestSellerRow(
                    rank = 3,
                    item = BestSeller(3, "이름이 아주 긴 상품입니다 줄바꿈 확인용 텍스트", null, 6, 174_000L),
                    totalRevenue = 4_135_000L,
                    onClick = {}
                )
            }
        }
    }
}
