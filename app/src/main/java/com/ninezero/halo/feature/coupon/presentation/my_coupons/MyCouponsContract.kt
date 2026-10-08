package com.ninezero.halo.feature.coupon.presentation.my_coupons

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus

data class MyCouponsState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val coupons: List<Coupon> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null,
    // 필터
    val selectedStatus: UserCouponStatus? = null,
    // 쿠폰 등록
    val couponCode: String = "",
    val isClaiming: Boolean = false,
    val toastMessage: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface MyCouponsIntent {
    data object LoadCoupons : MyCouponsIntent
    data object RefreshCoupons : MyCouponsIntent
    data object LoadMoreCoupons : MyCouponsIntent
    data object NavigateBack : MyCouponsIntent
    // 필터
    data class FilterByStatus(val status: UserCouponStatus?) : MyCouponsIntent
    // 쿠폰 등록
    data class UpdateCouponCode(val code: String) : MyCouponsIntent
    data object ClaimCoupon : MyCouponsIntent
    data object DismissToast : MyCouponsIntent
    data object SnackbarDismissed : MyCouponsIntent
}

sealed interface MyCouponsSideEffect {
    data object NavigateBack : MyCouponsSideEffect
}
