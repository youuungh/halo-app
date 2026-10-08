package com.ninezero.halo.feature.store.data.remote.dto

import com.ninezero.halo.feature.store.domain.model.BestSeller
import com.ninezero.halo.feature.store.domain.model.CreatorMonthlySales
import com.ninezero.halo.feature.store.domain.model.CreatorSalesSummary
import com.ninezero.halo.feature.store.domain.model.MonthRevenue
import com.ninezero.halo.feature.store.domain.model.SalesStatusBreakdown
import kotlinx.serialization.Serializable
import java.time.YearMonth

@Serializable
data class CreatorSalesSummaryDto(
    val thisMonthRevenue: String,
    val thisMonthNetRevenue: String,
    val lastMonthRevenue: String,
    val cumulativeRevenue: String,
    val pendingCount: Int,
    val confirmedCount: Int,
    val shippingCount: Int,
    val deliveredCount: Int,
    val cancelledCount: Int = 0
)

@Serializable
data class CreatorMonthlySalesDto(
    val month: String,
    val grossRevenue: String,
    val refundAmount: String,
    val netRevenue: String,
    val orderCount: Int,
    val quantitySold: Int = 0,
    val avgOrderValue: String,
    val shippingFees: String,
    val couponDiscounts: String,
    val statusBreakdown: SalesStatusBreakdownDto,
    val monthlyTrend: List<MonthRevenueDto> = emptyList(),
    val bestSellers: List<BestSellerDto> = emptyList()
)

@Serializable
data class SalesStatusBreakdownDto(
    val pending: Int,
    val confirmed: Int,
    val shipping: Int,
    val delivered: Int,
    val cancelled: Int
)

@Serializable
data class MonthRevenueDto(
    val month: String,
    val revenue: String
)

@Serializable
data class BestSellerDto(
    val productId: Int,
    val productName: String,
    val productImageUrl: String? = null,
    val quantity: Int,
    val revenue: String
)

private fun String.toAmountLong(): Long = toBigDecimalOrNull()?.toLong() ?: 0L

private fun String.toYearMonth(): YearMonth =
    runCatching { YearMonth.parse(this) }.getOrElse { YearMonth.now() }

fun CreatorSalesSummaryDto.toDomain(): CreatorSalesSummary = CreatorSalesSummary(
    thisMonthRevenue = thisMonthRevenue.toAmountLong(),
    thisMonthNetRevenue = thisMonthNetRevenue.toAmountLong(),
    lastMonthRevenue = lastMonthRevenue.toAmountLong(),
    cumulativeRevenue = cumulativeRevenue.toAmountLong(),
    pendingCount = pendingCount,
    confirmedCount = confirmedCount,
    shippingCount = shippingCount,
    deliveredCount = deliveredCount,
    cancelledCount = cancelledCount
)

fun CreatorMonthlySalesDto.toDomain(): CreatorMonthlySales = CreatorMonthlySales(
    month = month.toYearMonth(),
    grossRevenue = grossRevenue.toAmountLong(),
    refundAmount = refundAmount.toAmountLong(),
    netRevenue = netRevenue.toAmountLong(),
    orderCount = orderCount,
    avgOrderValue = avgOrderValue.toAmountLong(),
    statusBreakdown = statusBreakdown.toDomain(),
    monthlyTrend = monthlyTrend.map { it.toDomain() },
    bestSellers = bestSellers.map { it.toDomain() }
)

fun SalesStatusBreakdownDto.toDomain(): SalesStatusBreakdown = SalesStatusBreakdown(
    pending = pending,
    confirmed = confirmed,
    shipping = shipping,
    delivered = delivered,
    cancelled = cancelled
)

fun MonthRevenueDto.toDomain(): MonthRevenue = MonthRevenue(
    month = month.toYearMonth(),
    revenue = revenue.toAmountLong()
)

fun BestSellerDto.toDomain(): BestSeller = BestSeller(
    productName = productName,
    productImageUrl = productImageUrl,
    quantity = quantity,
    revenue = revenue.toAmountLong()
)
