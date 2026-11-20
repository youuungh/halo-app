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
import com.ninezero.halo.core.ui.theme.CardShape
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.MembershipColors
import com.ninezero.halo.core.ui.theme.Primary
import com.ninezero.halo.core.util.toFormattedPrice
import com.ninezero.halo.feature.store.domain.model.CreatorSalesSummary
import com.ninezero.halo.feature.store.presentation.sales_manage.OrderFilter

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(MembershipColors.cardBrush)
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
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
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (hasSales) {
                Text(
                    text = summary.thisMonthRevenue.toFormattedPrice(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "지난달 ${summary.lastMonthRevenue.toFormattedPrice()} · 누적 ${summary.cumulativeRevenue.toFormattedPrice()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "아직 매출이 없어요",
                    style = MaterialTheme.typography.titleMedium,
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

        if (hasSales) {
            StepFilter(
                summary = summary,
                selectedFilter = selectedFilter,
                onSelectFilter = onSelectFilter
            )
        }
    }
}

@Composable
private fun StepFilter(
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
        StepSegment(
            label = "전체",
            count = totalCount,
            filter = OrderFilter.ALL,
            selectedFilter = selectedFilter,
            onSelectFilter = onSelectFilter,
            modifier = Modifier.weight(1f)
        )
        StepDot()
        StepSegment(
            label = "대기",
            count = waitingCount,
            filter = OrderFilter.PENDING,
            selectedFilter = selectedFilter,
            onSelectFilter = onSelectFilter,
            modifier = Modifier.weight(1.1f)
        )
        StepArrow()
        StepSegment(
            label = "배송중",
            count = summary.shippingCount,
            filter = OrderFilter.SHIPPED,
            selectedFilter = selectedFilter,
            onSelectFilter = onSelectFilter,
            modifier = Modifier.weight(1.1f)
        )
        StepArrow()
        StepSegment(
            label = "완료",
            count = summary.deliveredCount,
            filter = OrderFilter.DELIVERED,
            selectedFilter = selectedFilter,
            onSelectFilter = onSelectFilter,
            modifier = Modifier.weight(1.1f)
        )
        StepDot()
        StepSegment(
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
private fun StepDot() {
    Text(
        text = "·",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.padding(horizontal = 1.dp)
    )
}

@Composable
private fun StepSegment(
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
            .bounceClick()  // 선택 배경까지 함께 작아지도록 먼저 적용
            .clip(MaterialTheme.shapes.medium)
            .background(background)
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
private fun StepArrow() {
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
                .height(112.dp)
                .clip(CardShape)
                .shimmer()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(MaterialTheme.shapes.medium)
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
