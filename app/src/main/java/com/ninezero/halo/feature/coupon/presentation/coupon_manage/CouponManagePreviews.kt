package com.ninezero.halo.feature.coupon.presentation.coupon_manage

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.coupon.domain.model.CouponDiscountTarget
import com.ninezero.halo.feature.coupon.domain.model.CouponStatus
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.CreatorCoupon

private val previewCoupons = listOf(
    CreatorCoupon(
        id = 1, code = "WELCOME10", name = "신규 팬 할인 쿠폰",
        description = null, type = CouponType.PERCENTAGE, discountTarget = CouponDiscountTarget.ALL,
        discountValue = "10", minOrderAmount = "10000", maxDiscountAmount = "5000",
        totalQuantity = 100, issuedQuantity = 23, maxUseCount = 1, status = CouponStatus.ACTIVE,
        startDate = System.currentTimeMillis(), endDate = System.currentTimeMillis() + 30 * 86_400_000L
    ),
    CreatorCoupon(
        id = 2, code = "SAVE5000", name = "5천원 할인 쿠폰",
        description = null, type = CouponType.FIXED_AMOUNT, discountTarget = CouponDiscountTarget.ALL,
        discountValue = "5000", minOrderAmount = "30000", maxDiscountAmount = null,
        totalQuantity = 200, issuedQuantity = 45, maxUseCount = 1, status = CouponStatus.ACTIVE,
        startDate = System.currentTimeMillis(), endDate = System.currentTimeMillis() + 7 * 86_400_000L
    ),
    CreatorCoupon(
        id = 3, code = "EXPIRED", name = "만료된 쿠폰",
        description = null, type = CouponType.FIXED_AMOUNT, discountTarget = CouponDiscountTarget.ALL,
        discountValue = "3000", minOrderAmount = "0", maxDiscountAmount = null,
        totalQuantity = 50, issuedQuantity = 50, maxUseCount = 1, status = CouponStatus.EXPIRED,
        startDate = System.currentTimeMillis() - 30 * 86_400_000L, endDate = System.currentTimeMillis() - 1 * 86_400_000L
    )
)

@HaloLightDarkPreview
@Composable
private fun CouponManageScreenPreview() {
    HaloScreenPreview {
        CouponManageScreenContent(
            state = CouponManageState(isLoading = false, coupons = previewCoupons),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponManageScreenDeleteDialogPreview() {
    HaloScreenPreview {
        CouponManageScreenContent(
            state = CouponManageState(isLoading = false, coupons = previewCoupons, showDeleteDialog = true, deletingCoupon = previewCoupons.first()),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponManageScreenLoadingPreview() {
    HaloScreenPreview {
        CouponManageScreenContent(
            state = CouponManageState(isLoading = true),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponManageScreenEmptyPreview() {
    HaloScreenPreview {
        CouponManageScreenContent(
            state = CouponManageState(isLoading = false, coupons = emptyList()),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponManageScreenErrorPreview() {
    HaloScreenPreview {
        CouponManageScreenContent(
            state = CouponManageState(isLoading = false, coupons = emptyList(), error = "쿠폰을 불러올 수 없습니다"),
            onIntent = {}
        )
    }
}
