package com.ninezero.halo.feature.point.data.remote.dto

import com.ninezero.halo.core.network.PagedDto
import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.core.util.toEnumOrDefault
import com.ninezero.halo.feature.point.domain.model.PointBalance
import com.ninezero.halo.feature.point.domain.model.PointHistory
import com.ninezero.halo.feature.point.domain.model.PointHistoryPage
import com.ninezero.halo.feature.point.domain.model.PointType
import kotlinx.serialization.Serializable

@Serializable
data class PointBalanceDto(
    val balance: String,
    val totalEarned: String = "0",
    val totalUsed: String = "0",
    val totalExpired: String = "0",
    val expiringPoints: String = "0",
    val expiresAt: String? = null
) {
    fun toDomain(): PointBalance = PointBalance(
        totalPoints = balance.toBigDecimal().toInt(),
        expiringPoints = expiringPoints.toBigDecimal().toInt(),
        expiresAt = expiresAt?.parseTimestamp()?.takeIf { it > 0L }
    )
}

@Serializable
data class PointHistoryDto(
    val id: Int,
    val type: String,
    val amount: String,
    val balanceBefore: String = "0",
    val balanceAfter: String = "0",
    val orderId: Int? = null,
    val description: String,
    val expiresAt: String? = null,
    val createdAt: String
) {
    fun toDomain(): PointHistory = PointHistory(
        id = id,
        type = type.toEnumOrDefault(PointType.EARN),
        amount = amount.toBigDecimalOrNull()?.toInt() ?: 0,
        description = description,
        createdAt = createdAt.parseTimestamp()
    )
}

typealias PointHistoryListResponseDto = PagedDto<PointHistoryDto>

fun PointHistoryListResponseDto.toDomain(): PointHistoryPage = PointHistoryPage(
    histories = items.map { it.toDomain() },
    page = page,
    hasMore = hasNext
)
