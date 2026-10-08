package com.ninezero.halo.feature.store.domain.model

import java.time.YearMonth

data class CreatorSalesSummary(
    val thisMonthRevenue: Long,      // 환불 전
    val thisMonthNetRevenue: Long,   // 매출 - 환불
    val lastMonthRevenue: Long,      // 전월 대비용
    val cumulativeRevenue: Long,
    val pendingCount: Int,           // 결제완료·미확정
    val confirmedCount: Int,         // 확인·배송준비
    val shippingCount: Int,
    val deliveredCount: Int,
    val cancelledCount: Int
)

data class CreatorMonthlySales(
    val month: YearMonth,
    val grossRevenue: Long,
    val refundAmount: Long,
    val netRevenue: Long,
    val orderCount: Int,
    val avgOrderValue: Long,
    val statusBreakdown: SalesStatusBreakdown,
    val monthlyTrend: List<MonthRevenue>,
    val bestSellers: List<BestSeller>
)

data class SalesStatusBreakdown(
    val pending: Int,
    val confirmed: Int,
    val shipping: Int,
    val delivered: Int,
    val cancelled: Int
)

data class MonthRevenue(
    val month: YearMonth,
    val revenue: Long
)

data class BestSeller(
    val productName: String,
    val productImageUrl: String?,
    val quantity: Int,
    val revenue: Long
)
