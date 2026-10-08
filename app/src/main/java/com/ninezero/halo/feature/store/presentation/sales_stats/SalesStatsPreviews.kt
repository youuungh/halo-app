package com.ninezero.halo.feature.store.presentation.sales_stats

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.store.domain.model.BestSeller
import com.ninezero.halo.feature.store.domain.model.CreatorMonthlySales
import com.ninezero.halo.feature.store.domain.model.MonthRevenue
import com.ninezero.halo.feature.store.domain.model.SalesStatusBreakdown
import java.time.YearMonth

private val previewMonth: YearMonth = YearMonth.of(2026, 8)
private val previewRegistrationMonth: YearMonth = YearMonth.of(2025, 11)

private val previewMonthlyTrend = listOf(
    MonthRevenue(YearMonth.of(2026, 3), 1_840_000L),
    MonthRevenue(YearMonth.of(2026, 4), 2_310_000L),
    MonthRevenue(YearMonth.of(2026, 5), 1_650_000L),
    MonthRevenue(YearMonth.of(2026, 6), 2_980_000L),
    MonthRevenue(YearMonth.of(2026, 7), 3_420_000L),
    MonthRevenue(YearMonth.of(2026, 8), 4_150_000L)
)

private val previewBestSellers = listOf(
    BestSeller(
        productName = "프리미엄 무선 이어폰 Pro Max",
        productImageUrl = null,
        quantity = 32,
        revenue = 2_848_000L
    ),
    BestSeller(
        productName = "노이즈캔슬링 헤드폰",
        productImageUrl = null,
        quantity = 7,
        revenue = 1_113_000L
    ),
    BestSeller(
        productName = "USB-C 고속 충전기",
        productImageUrl = null,
        quantity = 6,
        revenue = 174_000L
    )
)

private val previewMonthly = CreatorMonthlySales(
    month = previewMonth,
    grossRevenue = 4_150_000L,
    refundAmount = 320_000L,
    netRevenue = 3_830_000L,
    orderCount = 45,
    avgOrderValue = 92_222L,
    statusBreakdown = SalesStatusBreakdown(
        pending = 3,
        confirmed = 5,
        shipping = 8,
        delivered = 27,
        cancelled = 2
    ),
    monthlyTrend = previewMonthlyTrend,
    bestSellers = previewBestSellers
)

internal fun previewSalesStatsState(): SalesStatsState = SalesStatsState(
    isLoading = false,
    monthly = previewMonthly,
    selectedMonth = previewMonth,
    registrationMonth = previewRegistrationMonth
)

internal fun previewSalesStatsEmptyMonthState(): SalesStatsState = SalesStatsState(
    isLoading = false,
    monthly = CreatorMonthlySales(
        month = previewMonth,
        grossRevenue = 0L,
        refundAmount = 0L,
        netRevenue = 0L,
        orderCount = 0,
        avgOrderValue = 0L,
        statusBreakdown = SalesStatusBreakdown(0, 0, 0, 0, 0),
        monthlyTrend = emptyList(),
        bestSellers = emptyList()
    ),
    selectedMonth = previewMonth,
    registrationMonth = previewRegistrationMonth
)

internal fun previewSalesStatsLoadingState(): SalesStatsState = SalesStatsState(isLoading = true)

internal fun previewSalesStatsErrorState(): SalesStatsState = SalesStatsState(
    isLoading = false,
    error = "네트워크 연결을 확인해주세요",
    selectedMonth = previewMonth,
    registrationMonth = previewRegistrationMonth
)

internal fun previewSalesStatsMonthPickerState(): SalesStatsState = SalesStatsState(
    isLoading = false,
    monthly = previewMonthly,
    selectedMonth = previewMonth,
    registrationMonth = previewRegistrationMonth,
    showMonthPicker = true
)

@HaloLightDarkPreview
@Composable
private fun SalesStatsScreenPreview() {
    HaloScreenPreview {
        SalesStatsScreenContent(
            state = previewSalesStatsState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesStatsScreenEmptyMonthPreview() {
    HaloScreenPreview {
        SalesStatsScreenContent(
            state = previewSalesStatsEmptyMonthState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesStatsScreenLoadingPreview() {
    HaloScreenPreview {
        SalesStatsScreenContent(
            state = previewSalesStatsLoadingState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesStatsScreenErrorPreview() {
    HaloScreenPreview {
        SalesStatsScreenContent(
            state = previewSalesStatsErrorState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesStatsScreenMonthPickerPreview() {
    HaloScreenPreview {
        SalesStatsScreenContent(
            state = previewSalesStatsMonthPickerState(),
            onIntent = {}
        )
    }
}
