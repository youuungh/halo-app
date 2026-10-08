package com.ninezero.halo.feature.store.domain.model

import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import java.math.BigDecimal
import java.math.RoundingMode

data class ShippingCostSummary(
    val baseFeeByCreator: Map<Int, Long>,
    val preCouponFeeByCreator: Map<Int, Long>,
    val finalShippingFeeByCreator: Map<Int, Long>,
    val membershipFreeShipIds: Set<Int>,
    val freeShipByAmountIds: Set<Int>,
) {
    val feeBeforeCoupon: Long
        get() = preCouponFeeByCreator.values.sum()

    val finalShippingFee: Long
        get() = finalShippingFeeByCreator.values.sum()

    val freeShipDiscount: Long
        get() = feeBeforeCoupon - finalShippingFee

    val membershipFreeShipFee: Long
        get() = membershipFreeShipIds.sumOf { creatorId ->
            baseFeeByCreator[creatorId] ?: 0L
        }
}

data class StorePricingSummary(
    val subtotal: Long,
    val productDiscount: Long,
    val couponDiscount: Long,
    val shipping: ShippingCostSummary
) {
    val totalBeforePoints: Long
        get() = subtotal + shipping.finalShippingFee - couponDiscount
}

fun calcStorePricing(
    items: List<OrderLine>,
    subscribedCreatorIds: Set<Int> = emptySet(),
    selectedCoupons: Map<Int, Coupon> = emptyMap()
): StorePricingSummary {
    val subtotal = items.sumOf { it.subtotal }
    val productDiscount = items.sumOf { item ->
        val originalPrice = item.product.originalPrice?.toLongOrNull() ?: return@sumOf 0L
        val currentPrice = item.product.price.toLongOrNull() ?: return@sumOf 0L
        if (originalPrice > currentPrice) {
            (originalPrice - currentPrice) * item.quantity
        } else {
            0L
        }
    }
    val couponDiscount = calcCouponDiscount(selectedCoupons, items)
    val shipping = calcShippingCostSummary(items, subscribedCreatorIds, selectedCoupons)

    return StorePricingSummary(
        subtotal = subtotal,
        productDiscount = productDiscount,
        couponDiscount = couponDiscount,
        shipping = shipping
    )
}

fun calcCouponDiscount(
    selectedCoupons: Map<Int, Coupon>,
    items: List<OrderLine>
): Long {
    var totalDiscount = 0L

    for ((creatorId, coupon) in selectedCoupons) {
        val creatorItems = items.filter { it.product.creator.id == creatorId }
        if (creatorItems.isEmpty()) continue

        val creatorAmount = creatorItems.sumOf { it.subtotal }
        totalDiscount += calcSingleCouponDiscount(coupon, creatorAmount)
    }

    return totalDiscount
}

fun isFreeShippingReached(creatorSubtotal: Long): Boolean =
    creatorSubtotal >= Constants.Commerce.FREE_SHIPPING_THRESHOLD.toLong()

fun isCouponUsable(coupon: Coupon, creatorAmount: Long): Boolean =
    creatorAmount >= (coupon.minOrderAmount?.toLongOrNull() ?: 0L)

fun calcSingleCouponDiscount(coupon: Coupon, creatorAmount: Long): Long {  // 크리에이터 소계 초과 불가
    if (!isCouponUsable(coupon, creatorAmount)) return 0L

    val discount = when (coupon.type) {
        CouponType.PERCENTAGE -> {
            val rate = coupon.discountValue.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val calculated = BigDecimal.valueOf(creatorAmount)
                .multiply(rate.divide(BigDecimal(100)))
                .setScale(0, RoundingMode.HALF_UP)
                .toLong()
            val maxDiscountAmount = coupon.maxDiscountAmount?.toLongOrNull()
            if (maxDiscountAmount != null) minOf(calculated, maxDiscountAmount) else calculated
        }
        CouponType.FIXED_AMOUNT -> coupon.discountValue.toLongOrNull() ?: 0L
        CouponType.FREE_SHIPPING -> 0L
    }
    return discount.coerceAtMost(creatorAmount)
}

fun calcShippingCostSummary(
    items: List<OrderLine>,
    subscribedCreatorIds: Set<Int> = emptySet(),
    selectedCoupons: Map<Int, Coupon> = emptyMap()
): ShippingCostSummary {
    val standardShippingFee = Constants.Commerce.DEFAULT_SHIPPING_FEE
    val itemsByCreator = items.groupBy { it.product.creator.id }

    val baseFeeByCreator = itemsByCreator.keys.associateWith { standardShippingFee }
    val membershipFreeShipIds = mutableSetOf<Int>()
    val freeShipByAmountIds = mutableSetOf<Int>()
    val preCouponFeeByCreator = mutableMapOf<Int, Long>()
    val subtotalByCreator = itemsByCreator.mapValues { (_, creatorItems) ->
        creatorItems.sumOf { it.subtotal }
    }

    for ((creatorId, _) in itemsByCreator) {
        val creatorSubtotal = subtotalByCreator[creatorId] ?: 0L
        val preCouponFee = when {
            creatorId in subscribedCreatorIds -> {
                membershipFreeShipIds += creatorId
                0L
            }
            isFreeShippingReached(creatorSubtotal) -> {
                freeShipByAmountIds += creatorId
                0L
            }
            else -> standardShippingFee
        }
        preCouponFeeByCreator[creatorId] = preCouponFee
    }

    // 최소 주문 금액 미달 무료배송 쿠폰은 제외
    val freeShipCouponIds = selectedCoupons
        .filter { (creatorId, coupon) ->
            coupon.type == CouponType.FREE_SHIPPING &&
                (preCouponFeeByCreator[creatorId] ?: 0L) > 0L &&
                isCouponUsable(coupon, subtotalByCreator[creatorId] ?: 0L)
        }
        .keys

    val finalShippingFeeByCreator = preCouponFeeByCreator.mapValues { (creatorId, fee) ->
        if (creatorId in freeShipCouponIds) 0L else fee
    }

    return ShippingCostSummary(
        baseFeeByCreator = baseFeeByCreator,
        preCouponFeeByCreator = preCouponFeeByCreator,
        finalShippingFeeByCreator = finalShippingFeeByCreator,
        membershipFreeShipIds = membershipFreeShipIds,
        freeShipByAmountIds = freeShipByAmountIds
    )
}

/** 포인트 정책 상한 */
fun calcMaxUsablePoints(subtotal: Long): Int {  // 소계의 50%
    if (subtotal <= 0L) return 0

    return BigDecimal.valueOf(subtotal)
        .multiply(BigDecimal("0.50"))
        .setScale(0, RoundingMode.HALF_UP)
        .toInt()
}

/** 실제 사용 가능 포인트 상한 */
fun calcUsablePointsCap(subtotal: Long, totalBeforePoints: Long): Int =  // 정책 상한과 결제 예정 금액 중 작은 값
    minOf(calcMaxUsablePoints(subtotal).toLong(), totalBeforePoints.coerceAtLeast(0L)).toInt()
