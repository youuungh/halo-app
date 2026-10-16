package com.ninezero.halo.feature.coupon.presentation.coupon_manage

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.coupon.domain.model.CouponDiscountTarget
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.CreatorCoupon
import java.time.LocalDateTime

data class CouponManageState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val coupons: List<CreatorCoupon> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null,
    // 옵션 시트
    val optionSheetCoupon: CreatorCoupon? = null,
    // 쿠폰 생성/수정 시트
    val showCouponBottomSheet: Boolean = false,
    val editingCoupon: CreatorCoupon? = null,
    val couponFormState: CouponFormState = CouponFormState(),
    val isSaving: Boolean = false,
    val saveError: String? = null,
    // 삭제 확인 다이얼로그
    val showDeleteDialog: Boolean = false,
    val deletingCoupon: CreatorCoupon? = null,
    val snackbarState: SnackbarState? = null
)

data class CouponFormState(
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val type: CouponType = CouponType.FIXED_AMOUNT,
    val discountTarget: CouponDiscountTarget = CouponDiscountTarget.ALL,
    val discountValue: String = "",
    val minOrderAmount: String = "",
    val maxDiscountAmount: String = "",
    val totalQuantity: String = "100",
    val maxUseCount: String = "1",
    val startDate: LocalDateTime = LocalDateTime.now(),
    val endDate: LocalDateTime = LocalDateTime.now().plusMonths(1)
) {
    val isValid: Boolean
        get() = name.isNotBlank()
                && (type == CouponType.FREE_SHIPPING || discountValue.isNotBlank())
                && totalQuantity.isNotBlank()
                && (totalQuantity.toIntOrNull() ?: 0) > 0
                && maxUseCount.isNotBlank()
                && (maxUseCount.toIntOrNull() ?: 0) > 0
                && (code.isBlank() || (code.length >= 4 && code.matches("[A-Z0-9]+".toRegex())))
}

sealed interface CouponManageIntent {
    data object LoadCoupons : CouponManageIntent
    data object RefreshCoupons : CouponManageIntent
    data object LoadMoreCoupons : CouponManageIntent
    data object NavigateBack : CouponManageIntent
    // 옵션 시트
    data class ShowCouponOptions(val coupon: CreatorCoupon) : CouponManageIntent
    data object DismissCouponOptions : CouponManageIntent
    // 쿠폰 생성/수정 시트
    data object ShowCreateCouponSheet : CouponManageIntent
    data class ShowEditCouponSheet(val coupon: CreatorCoupon) : CouponManageIntent
    data object DismissCouponSheet : CouponManageIntent
    // 폼 입력
    data class UpdateCode(val code: String) : CouponManageIntent
    data class UpdateName(val name: String) : CouponManageIntent
    data class UpdateDescription(val description: String) : CouponManageIntent
    data class UpdateType(val type: CouponType) : CouponManageIntent
    data class UpdateDiscountValue(val value: String) : CouponManageIntent
    data class UpdateMinOrderAmount(val value: String) : CouponManageIntent
    data class UpdateMaxDiscountAmount(val value: String) : CouponManageIntent
    data class UpdateTotalQuantity(val value: String) : CouponManageIntent
    data class UpdateMaxUseCount(val value: String) : CouponManageIntent
    data class UpdateStartDate(val date: LocalDateTime) : CouponManageIntent
    data class UpdateEndDate(val date: LocalDateTime) : CouponManageIntent
    // 저장
    data object SaveCoupon : CouponManageIntent
    // 활성화/비활성화
    data class ToggleCouponActive(val coupon: CreatorCoupon) : CouponManageIntent
    // 삭제
    data class ShowDeleteDialog(val coupon: CreatorCoupon) : CouponManageIntent
    data object DismissDeleteDialog : CouponManageIntent
    data object ConfirmDelete : CouponManageIntent
    // 저장 실패, 시트 안 스낵바
    data object DismissSaveError : CouponManageIntent
    data object SnackbarDismissed : CouponManageIntent
}

sealed interface CouponManageSideEffect {
    data object NavigateBack : CouponManageSideEffect
}
