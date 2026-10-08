package com.ninezero.halo.feature.store.presentation.sales_stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toCompactWon
import com.ninezero.halo.core.util.toFormattedPrice
import com.ninezero.halo.feature.store.domain.model.BestSeller
import com.ninezero.halo.feature.store.domain.model.MonthRevenue
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.Primary
import java.time.YearMonth

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

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val trackColor = onSurface.copy(alpha = 0.05f)
    val inactiveBar = onSurface.copy(alpha = 0.12f)
    val lineColor = Primary.copy(alpha = 0.55f)
    val dotRing = MaterialTheme.colorScheme.surface

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

        // 막대
        trend.forEachIndexed { i, item ->
            val cx = tops[i].x
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(cx - barW / 2f, areaTop),
                size = Size(barW, areaH),
                cornerRadius = corner
            )
            val fillTop = tops[i].y.coerceAtMost(areaTop + areaH - minBar)
            drawRoundRect(
                color = if (item.month == selectedMonth) Primary else inactiveBar,
                topLeft = Offset(cx - barW / 2f, fillTop),
                size = Size(barW, areaTop + areaH - fillTop),
                cornerRadius = corner
            )
        }

        // 트렌드 라인
        val path = Path().apply {
            tops.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 점 + 값 라벨
        trend.forEachIndexed { i, item ->
            val p = tops[i]
            val isSelected = item.month == selectedMonth
            drawCircle(dotRing, radius = (if (isSelected) 5.5f else 3.5f).dp.toPx(), center = p)
            drawCircle(Primary, radius = (if (isSelected) 3.5f else 2f).dp.toPx(), center = p)

            if (isSelected || item.month == maxMonth) {
                val measured = textMeasurer.measure(
                    text = item.revenue.toCompactWon(),
                    style = labelStyle.copy(
                        color = if (isSelected) Primary else onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                val tx = (p.x - measured.size.width / 2f).coerceIn(0f, w - measured.size.width)
                val ty = (p.y - measured.size.height - 6.dp.toPx()).coerceAtLeast(0f)
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
    topRevenue: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RankBadge(rank = rank)
            Spacer(modifier = Modifier.width(10.dp))
            AsyncImage(
                model = item.productImageUrl,
                contentDescription = item.productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.quantity}개 판매",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.revenue.toFormattedPrice(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        val ratio = if (topRevenue > 0L) {
            (item.revenue.toFloat() / topRevenue).coerceIn(0.02f, 1f)
        } else {
            0f
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(Primary.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Primary.copy(alpha = 0.55f))
            )
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    if (rank == 1) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "1",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    } else {
        Box(
            modifier = Modifier.size(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }
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
                    item = BestSeller("프리미엄 무선 이어폰 Pro Max", null, 32, 2_848_000L),
                    topRevenue = 2_848_000L
                )
                BestSellerRow(
                    rank = 2,
                    item = BestSeller("노이즈캔슬링 헤드폰", null, 7, 1_113_000L),
                    topRevenue = 2_848_000L
                )
                BestSellerRow(
                    rank = 3,
                    item = BestSeller("이름이 아주 긴 상품입니다 줄바꿈 확인용 텍스트", null, 6, 174_000L),
                    topRevenue = 2_848_000L
                )
            }
        }
    }
}
