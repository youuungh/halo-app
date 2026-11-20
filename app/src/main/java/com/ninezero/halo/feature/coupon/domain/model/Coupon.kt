package com.ninezero.halo.feature.coupon.domain.model

import com.ninezero.halo.core.util.toFormattedPrice

data class Coupon(
    val id: Int,
    val creatorId: Int,
    val creatorName: String? = null,
    val code: String,
    val name: String,
    val type: CouponType,
    val discountValue: Long,
    val minOrderAmount: Long?,
    val maxDiscountAmount: Long?,
    val status: UserCouponStatus,
    val expiresAt: Long,
    val daysRemaining: Int,
    val usedAt: Long? = null
) {
    val discountText: String
        get() = when (type) {
            CouponType.FIXED_AMOUNT -> discountValue.toFormattedPrice()
            CouponType.PERCENTAGE -> "${discountValue}%"
            CouponType.FREE_SHIPPING -> "무료 배송 쿠폰"
        }
}

data class CouponListResult(
    val coupons: List<Coupon>,
    val page: Int,
    val hasMore: Boolean
)
