package com.ninezero.halo.feature.coupon.data.remote.dto

import com.ninezero.halo.core.network.PagedDto
import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.core.util.toEnumOrDefault
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponDiscountTarget
import com.ninezero.halo.feature.coupon.domain.model.CouponListResult
import com.ninezero.halo.feature.coupon.domain.model.CouponStatus
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.CreatorCoupon
import com.ninezero.halo.feature.coupon.domain.model.CreatorCouponListResult
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import kotlinx.serialization.Serializable

@Serializable
data class CouponResponseDto(
    val id: Int,
    val creatorId: Int,
    val creatorName: String? = null,
    val code: String,
    val name: String,
    val description: String? = null,
    val type: String,
    val discountTarget: String,
    val discountValue: String,
    val minOrderAmount: String,
    val maxDiscountAmount: String? = null,
    val totalQuantity: Int,
    val issuedQuantity: Int,
    val remainingQuantity: Int,
    val maxUseCount: Int,
    val targetIds: List<Int>? = null,
    val status: String,
    val startDate: String,
    val endDate: String,
    val createdAt: String,
    val updatedAt: String
) {
    fun toDomain(): CreatorCoupon = CreatorCoupon(
        id = id,
        code = code,
        name = name,
        description = description,
        type = type.toEnumOrDefault(CouponType.FIXED_AMOUNT),
        discountTarget = discountTarget.toEnumOrDefault(CouponDiscountTarget.ALL),
        discountValue = discountValue,
        minOrderAmount = minOrderAmount,
        maxDiscountAmount = maxDiscountAmount,
        totalQuantity = totalQuantity,
        issuedQuantity = issuedQuantity,
        maxUseCount = maxUseCount,
        status = status.toEnumOrDefault(CouponStatus.ACTIVE),
        startDate = startDate.parseTimestamp(),
        endDate = endDate.parseTimestamp()
    )
}

@Serializable
data class UserCouponResponseDto(
    val id: Int,
    val coupon: CouponResponseDto,
    val status: String,
    val useCount: Int,
    val maxUseCount: Int,
    val remainingUseCount: Int,
    val usedAt: String? = null,
    val orderId: Int? = null,
    val claimedAt: String,
    val expiresAt: String,
    val daysRemaining: Int
) {
    fun toDomain(): Coupon = Coupon(
        id = id,
        creatorId = coupon.creatorId,
        creatorName = coupon.creatorName,
        code = coupon.code,
        name = coupon.name,
        type = coupon.type.toEnumOrDefault(CouponType.FIXED_AMOUNT),
        discountValue = coupon.discountValue,
        minOrderAmount = coupon.minOrderAmount.takeIf { it != "0" },
        maxDiscountAmount = coupon.maxDiscountAmount,
        status = status.toEnumOrDefault(UserCouponStatus.AVAILABLE),
        expiresAt = expiresAt.parseTimestamp(),
        daysRemaining = daysRemaining
    )
}

typealias PaginatedCouponResponseDto = PagedDto<CouponResponseDto>

fun PaginatedCouponResponseDto.toDomain(): CreatorCouponListResult = CreatorCouponListResult(
    coupons = items.map { it.toDomain() },
    page = page,
    hasMore = hasNext
)

typealias PaginatedUserCouponResponseDto = PagedDto<UserCouponResponseDto>

@Serializable
data class UserCouponListResponseDto(
    val coupons: PaginatedUserCouponResponseDto,
    val availableCount: Int
) {
    fun toDomain(): CouponListResult = CouponListResult(
        coupons = coupons.items.map { it.toDomain() },
        page = coupons.page,
        hasMore = coupons.hasNext
    )
}

@Serializable
data class CreateCouponRequestDto(
    val code: String? = null,
    val name: String,
    val description: String? = null,
    val type: String,
    val discountTarget: String,
    val discountValue: String,
    val minOrderAmount: String? = null,
    val maxDiscountAmount: String? = null,
    val totalQuantity: Int,
    val maxUseCount: Int = 1,
    val targetIds: List<Int>? = null,
    val startDate: String,
    val endDate: String
)

@Serializable
data class UpdateCouponRequestDto(
    val name: String? = null,
    val description: String? = null,
    val minOrderAmount: String? = null,
    val maxDiscountAmount: String? = null,
    val totalQuantity: Int? = null,
    val maxUseCount: Int? = null,
    val status: String? = null,
    val startDate: String? = null,
    val endDate: String? = null
)

@Serializable
data class ClaimCouponRequestDto(
    val code: String
)
