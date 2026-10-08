package com.ninezero.halo.feature.coupon.domain.model

import com.ninezero.halo.core.util.toFormattedPrice

data class CreatorCoupon(
    val id: Int,
    val code: String,
    val name: String,
    val description: String?,
    val type: CouponType,
    val discountTarget: CouponDiscountTarget,
    val discountValue: String,
    val minOrderAmount: String,
    val maxDiscountAmount: String?,
    val totalQuantity: Int,
    val issuedQuantity: Int,
    val maxUseCount: Int,
    val status: CouponStatus,
    val startDate: Long,
    val endDate: Long
) {
    val discountText: String
        get() = when (type) {
            CouponType.FIXED_AMOUNT -> discountValue.toBigDecimal().toInt().toFormattedPrice()
            CouponType.PERCENTAGE -> "${discountValue}%"
            CouponType.FREE_SHIPPING -> "무료 배송"
        }

    val isExpired: Boolean
        get() = System.currentTimeMillis() > endDate

    val isActive: Boolean
        get() = status == CouponStatus.ACTIVE
}

data class CreatorCouponListResult(
    val coupons: List<CreatorCoupon>,
    val page: Int,
    val hasMore: Boolean
)

data class CreateCouponRequest(
    val code: String?,
    val name: String,
    val description: String?,
    val type: CouponType,
    val discountTarget: CouponDiscountTarget,
    val discountValue: String,
    val minOrderAmount: String?,
    val maxDiscountAmount: String?,
    val totalQuantity: Int,
    val maxUseCount: Int,
    val targetIds: List<Int>?,
    val startDate: Long,
    val endDate: Long
)

data class UpdateCouponRequest(
    val name: String?,
    val description: String?,
    val minOrderAmount: String?,
    val maxDiscountAmount: String?,
    val totalQuantity: Int?,
    val maxUseCount: Int?,
    val status: CouponStatus?,
    val startDate: Long?,
    val endDate: Long?
)
