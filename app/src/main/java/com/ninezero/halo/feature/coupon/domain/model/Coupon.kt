package com.ninezero.halo.feature.coupon.domain.model

import com.ninezero.halo.core.util.toFormattedPrice

data class Coupon(
    val id: Int,
    val creatorId: Int,
    val creatorName: String? = null,
    val code: String,
    val name: String,
    val type: CouponType,
    val discountValue: String,
    val minOrderAmount: String?,
    val maxDiscountAmount: String?,
    val status: UserCouponStatus,
    val expiresAt: Long,
    val daysRemaining: Int
) {
    val discountText: String
        get() = when (type) {
            CouponType.FIXED_AMOUNT -> discountValue.toBigDecimal().toInt().toFormattedPrice()
            CouponType.PERCENTAGE -> "${discountValue}%"
            CouponType.FREE_SHIPPING -> "무료 배송 쿠폰"
        }
}

data class CouponListResult(
    val coupons: List<Coupon>,
    val page: Int,
    val hasMore: Boolean
)
