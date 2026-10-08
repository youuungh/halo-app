package com.ninezero.halo.feature.store.presentation.sales_manage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toFormattedPrice
import com.ninezero.halo.feature.store.domain.model.CreatorSalesSummary
import com.ninezero.halo.feature.store.presentation.components.GlassCard
import com.ninezero.halo.feature.store.presentation.components.SalesDeltaPill
import com.ninezero.halo.feature.store.presentation.components.SalesInfoTooltip
import com.ninezero.halo.feature.store.presentation.components.SummaryStatCell
import com.ninezero.halo.feature.store.presentation.sales_manage.OrderFilter
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.Primary

@Composable
fun SalesSummaryCard(
    summary: CreatorSalesSummary,
    selectedFilter: OrderFilter,
    onClick: () -> Unit,
    onSelectFilter: (OrderFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasSales = summary.cumulativeRevenue > 0L || summary.thisMonthRevenue > 0L

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassCard(onClick = onClick) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "이번 달 매출",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "매출 통계",
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                if (hasSales) {
                    Text(
                        text = summary.thisMonthRevenue.toFormattedPrice(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SalesDeltaPill(
                        thisMonth = summary.thisMonthRevenue,
                        lastMonth = summary.lastMonthRevenue
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryStatCell(
                            label = "누적 매출",
                            value = summary.cumulativeRevenue.toFormattedPrice(),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatCell(
                            label = "순매출",
                            value = summary.thisMonthNetRevenue.toFormattedPrice(),
                            modifier = Modifier.weight(1f),
                            labelTrailing = {
                                SalesInfoTooltip(
                                    text = "환불 금액을 제외한 순매출이에요.",
                                    size = 13.dp
                                )
                            }
                        )
                    }
                } else {
                    Text(
                        text = "아직 매출이 없어요",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "상품이 판매되면 여기에 표시돼요",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (hasSales) {
            PipelineFilter(
                summary = summary,
                selectedFilter = selectedFilter,
                onSelectFilter = onSelectFilter
            )
        }
    }
}

@Composable
private fun PipelineFilter(
    summary: CreatorSalesSummary,
    selectedFilter: OrderFilter,
    onSelectFilter: (OrderFilter) -> Unit
) {
    val waitingCount = summary.pendingCount + summary.confirmedCount
    val totalCount = waitingCount + summary.shippingCount +
        summary.deliveredCount + summary.cancelledCount

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PipelineSegment(
            label = "전체",
            count = totalCount,
            filter = OrderFilter.ALL,
            selectedFilter = selectedFilter,
            onSelectFilter = onSelectFilter,
            modifier = Modifier.weight(1f)
        )
        PipelineDot()
        PipelineSegment(
            label = "대기",
            count = waitingCount,
            filter = OrderFilter.PENDING,
            selectedFilter = selectedFilter,
            onSelectFilter = onSelectFilter,
            modifier = Modifier.weight(1.1f)
        )
        PipelineArrow()
        PipelineSegment(
            label = "배송중",
            count = summary.shippingCount,
            filter = OrderFilter.SHIPPED,
            selectedFilter = selectedFilter,
            onSelectFilter = onSelectFilter,
            modifier = Modifier.weight(1.1f)
        )
        PipelineArrow()
        PipelineSegment(
            label = "완료",
            count = summary.deliveredCount,
            filter = OrderFilter.DELIVERED,
            selectedFilter = selectedFilter,
            onSelectFilter = onSelectFilter,
            modifier = Modifier.weight(1.1f)
        )
        PipelineDot()
        PipelineSegment(
            label = "취소",
            count = summary.cancelledCount,
            filter = OrderFilter.CANCELLED,
            selectedFilter = selectedFilter,
            onSelectFilter = onSelectFilter,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PipelineDot() {
    Text(
        text = "·",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.padding(horizontal = 1.dp)
    )
}

@Composable
private fun PipelineSegment(
    label: String,
    count: Int,
    filter: OrderFilter,
    selectedFilter: OrderFilter,
    onSelectFilter: (OrderFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = selectedFilter == filter
    val countColor = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
    val background = if (isSelected) Primary.copy(alpha = 0.10f) else Color.Transparent

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .bounceClick()
            .clickable {
                onSelectFilter(if (isSelected) OrderFilter.ALL else filter)
            }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = countColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PipelineArrow() {
    Text(
        text = "›",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    )
}

@Composable
fun SalesSummaryCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(20.dp))
                .shimmer()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .shimmer()
        )
    }
}

private val previewSummary = CreatorSalesSummary(
    thisMonthRevenue = 4_150_000L,
    thisMonthNetRevenue = 3_830_000L,
    lastMonthRevenue = 3_420_000L,
    cumulativeRevenue = 28_900_000L,
    pendingCount = 3,
    confirmedCount = 5,
    shippingCount = 8,
    deliveredCount = 27,
    cancelledCount = 2
)

@HaloLightDarkPreview
@Composable
private fun SalesSummaryCardPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(12.dp)) {
                SalesSummaryCard(
                    summary = previewSummary,
                    selectedFilter = OrderFilter.ALL,
                    onClick = {},
                    onSelectFilter = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesSummaryCardFilteredPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(12.dp)) {
                SalesSummaryCard(
                    summary = previewSummary,
                    selectedFilter = OrderFilter.SHIPPED,
                    onClick = {},
                    onSelectFilter = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesSummaryCardNoSalesPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(12.dp)) {
                SalesSummaryCard(
                    summary = CreatorSalesSummary(0L, 0L, 0L, 0L, 0, 0, 0, 0, 0),
                    selectedFilter = OrderFilter.ALL,
                    onClick = {},
                    onSelectFilter = {}
                )
            }
        }
    }
}
