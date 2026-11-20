package com.ninezero.halo.feature.store.domain.model

import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
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
        val originalPrice = item.product.originalPrice ?: return@sumOf 0L
        val currentPrice = item.product.price
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
    creatorAmount >= (coupon.minOrderAmount ?: 0L)

fun calcSingleCouponDiscount(coupon: Coupon, creatorAmount: Long): Long {  // 크리에이터 소계 초과 불가
    if (!isCouponUsable(coupon, creatorAmount)) return 0L

    val discount = when (coupon.type) {
        CouponType.PERCENTAGE -> {
            val rate = BigDecimal.valueOf(coupon.discountValue)
            val calculated = BigDecimal.valueOf(creatorAmount)
                .multiply(rate.divide(BigDecimal(100)))
                .setScale(0, RoundingMode.HALF_UP)
                .toLong()
            val maxDiscountAmount = coupon.maxDiscountAmount
            if (maxDiscountAmount != null) minOf(calculated, maxDiscountAmount) else calculated
        }
        CouponType.FIXED_AMOUNT -> coupon.discountValue
        CouponType.FREE_SHIPPING -> 0L
    }
    return discount.coerceAtMost(creatorAmount)
}

/** 쿠폰이 줄이는 금액, 무료배송은 기본 배송비 */
fun calcCouponBenefit(coupon: Coupon, creatorAmount: Long): Long =
    if (coupon.type == CouponType.FREE_SHIPPING) {
        if (isCouponUsable(coupon, creatorAmount)) Constants.Commerce.DEFAULT_SHIPPING_FEE else 0L
    } else {
        calcSingleCouponDiscount(coupon, creatorAmount)
    }

/** 이 주문에 쓸 수 있는 쿠폰인지, 서버 조건과 같음 */
fun isCouponApplicable(
    coupon: Coupon,
    orderItems: List<OrderLine>,
    subscribedCreatorIds: Set<Int>
): Boolean {
    val creatorItems = orderItems.filter { it.product.creator.id == coupon.creatorId }
    if (creatorItems.isEmpty()) return false
    val creatorAmount = creatorItems.sumOf { it.subtotal }
    if (!isCouponUsable(coupon, creatorAmount)) return false
    // 이미 무료배송이면 무료배송 쿠폰은 거절됨
    val isAlreadyFreeShipping = coupon.creatorId in subscribedCreatorIds || isFreeShippingReached(creatorAmount)
    return !(coupon.type == CouponType.FREE_SHIPPING && isAlreadyFreeShipping)
}

/** 크리에이터별 할인이 가장 큰 쿠폰, 같으면 먼저 만료 */
fun pickBestCoupons(
    coupons: List<Coupon>,
    orderItems: List<OrderLine>,
    subscribedCreatorIds: Set<Int>
): Map<Int, Coupon> {
    val amountByCreator = orderItems
        .groupBy { it.product.creator.id }
        .mapValues { (_, items) -> items.sumOf { it.subtotal } }
    return coupons
        .filter { isCouponApplicable(it, orderItems, subscribedCreatorIds) }
        .groupBy { it.creatorId }
        .mapValues { (creatorId, creatorCoupons) ->
            val amount = amountByCreator[creatorId] ?: 0L
            creatorCoupons.maxWith(
                compareBy<Coupon> { calcCouponBenefit(it, amount) }.thenByDescending { it.expiresAt }
            )
        }
        .filter { (creatorId, coupon) -> calcCouponBenefit(coupon, amountByCreator[creatorId] ?: 0L) > 0L }
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
fun calcUsablePointsLimit(subtotal: Long, totalBeforePoints: Long): Int =  // 정책 상한과 결제 예정 금액 중 작은 값
    minOf(calcMaxUsablePoints(subtotal).toLong(), totalBeforePoints.coerceAtLeast(0L)).toInt()

/** 등급별 적립률 %, 서버 Constants.Point와 같음 */
fun earnRatePercent(tier: SubscriptionPlanTier?): Int = when (tier) {
    SubscriptionPlanTier.TIER1 -> 5
    SubscriptionPlanTier.TIER2 -> 10
    else -> 1
}

/** 적립 예정 포인트, 서버와 같은 반올림 */
fun calcEarnPoints(amount: Long, ratePercent: Int): Long =
    BigDecimal.valueOf(amount)
        .multiply(BigDecimal(ratePercent))
        .divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
        .toLong()
