package com.ninezero.halo.feature.coupon.presentation.my_coupons

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus

internal fun previewMyCouponsState(): MyCouponsState {
    return MyCouponsState(
        isLoading = false,
        coupons = previewCoupons
    )
}

internal fun previewMyCouponsLoadingMoreState(): MyCouponsState {
    return MyCouponsState(
        isLoading = true,
        coupons = previewCoupons,
        hasMore = true
    )
}

internal fun previewMyCouponsEmptyState(): MyCouponsState {
    return MyCouponsState(isLoading = false)
}

internal fun previewMyCouponsLoadingState(): MyCouponsState {
    return MyCouponsState(isLoading = true)
}

internal fun previewMyCouponsErrorState(): MyCouponsState {
    return MyCouponsState(
        isLoading = false,
        error = "쿠폰을 불러올 수 없습니다"
    )
}

private val previewCoupons = listOf(
    previewCoupon(
        id = 1,
        code = "WELCOME10",
        name = "신규 가입 할인 쿠폰",
        type = CouponType.PERCENTAGE,
        discountValue = "10",
        minOrderAmount = "10000",
        maxDiscountAmount = "5000",
        status = UserCouponStatus.AVAILABLE,
        expiresDaysFromNow = 30,
        daysRemaining = 30
    ),
    previewCoupon(
        id = 2,
        code = "SAVE5000",
        name = "5천원 할인 쿠폰",
        type = CouponType.FIXED_AMOUNT,
        discountValue = "5000",
        minOrderAmount = "30000",
        status = UserCouponStatus.AVAILABLE,
        expiresDaysFromNow = 3,
        daysRemaining = 2
    ),
    previewCoupon(
        id = 3,
        code = "SUMMER20",
        name = "여름 시즌 특별 할인",
        type = CouponType.PERCENTAGE,
        discountValue = "20",
        minOrderAmount = "50000",
        maxDiscountAmount = "10000",
        status = UserCouponStatus.AVAILABLE,
        expiresDaysFromNow = 14,
        daysRemaining = 14
    ),
    previewCoupon(
        id = 4,
        code = "MEGA10K",
        name = "만원 할인 쿠폰",
        type = CouponType.FIXED_AMOUNT,
        discountValue = "10000",
        minOrderAmount = "70000",
        status = UserCouponStatus.AVAILABLE,
        expiresDaysFromNow = 60,
        daysRemaining = 60
    ),
    previewCoupon(
        id = 5,
        code = "USED1",
        name = "사용된 쿠폰",
        type = CouponType.FIXED_AMOUNT,
        discountValue = "3000",
        status = UserCouponStatus.USED,
        useCount = 1,
        remainingUseCount = 0,
        claimedDaysAgo = 30,
        usedDaysAgo = 5,
        expiresDaysFromNow = 10,
        daysRemaining = 10
    ),
    previewCoupon(
        id = 6,
        code = "USED2",
        name = "봄맞이 15% 할인",
        type = CouponType.PERCENTAGE,
        discountValue = "15",
        minOrderAmount = "20000",
        status = UserCouponStatus.USED,
        useCount = 1,
        remainingUseCount = 0,
        claimedDaysAgo = 60,
        usedDaysAgo = 10,
        expiresDaysFromNow = 5,
        daysRemaining = 5
    ),
    previewCoupon(
        id = 7,
        code = "EXPIRED1",
        name = "만료된 할인 쿠폰",
        type = CouponType.FIXED_AMOUNT,
        discountValue = "2000",
        minOrderAmount = "15000",
        status = UserCouponStatus.EXPIRED,
        remainingUseCount = 0,
        claimedDaysAgo = 90,
        expiresDaysFromNow = -1,
        daysRemaining = 0
    )
)

private fun previewCoupon(
    id: Int,
    code: String,
    name: String,
    type: CouponType,
    discountValue: String,
    status: UserCouponStatus,
    expiresDaysFromNow: Long,
    daysRemaining: Int,
    minOrderAmount: String? = null,
    maxDiscountAmount: String? = null,
    useCount: Int = 0,
    remainingUseCount: Int = 1,
    claimedDaysAgo: Long = 0,
    usedDaysAgo: Long? = null,
    now: Long = System.currentTimeMillis()
): Coupon {
    return Coupon(
        id = id,
        creatorId = 1,
        code = code,
        name = name,
        type = type,
        discountValue = discountValue,
        minOrderAmount = minOrderAmount,
        maxDiscountAmount = maxDiscountAmount,
        status = status,
        expiresAt = now + expiresDaysFromNow * 86_400_000L,
        daysRemaining = daysRemaining
    )
}

@HaloLightDarkPreview
@Composable
private fun MyCouponsScreenPreview() {
    HaloScreenPreview {
        MyCouponsScreenContent(
            state = previewMyCouponsState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyCouponsScreenLoadingMorePreview() {
    HaloScreenPreview {
        MyCouponsScreenContent(
            state = previewMyCouponsLoadingMoreState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyCouponsScreenLoadingPreview() {
    HaloScreenPreview {
        MyCouponsScreenContent(
            state = previewMyCouponsLoadingState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyCouponsScreenEmptyPreview() {
    HaloScreenPreview {
        MyCouponsScreenContent(
            state = previewMyCouponsEmptyState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyCouponsScreenErrorPreview() {
    HaloScreenPreview {
        MyCouponsScreenContent(
            state = previewMyCouponsErrorState(),
            onIntent = {}
        )
    }
}
