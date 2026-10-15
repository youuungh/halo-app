package com.ninezero.halo.feature.store.presentation.checkout

import com.ninezero.halo.BuildConfig
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.address.domain.model.Address
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.point.domain.model.PointBalance
import com.ninezero.halo.feature.store.domain.model.BillingCard
import com.ninezero.halo.feature.store.domain.model.OrderLine
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier

data class CouponCreatorInfo(
    val name: String,
    val freeShipBlockReason: String? = null
)

data class CheckoutState(
    val isLoading: Boolean = true,
    val orderLines: List<OrderLine>? = null,  // null = 아직 못 받음
    val selectedItemIds: List<Int> = emptyList(),  // 장바구니 주문의 선택 줄
    val shippingName: String = "",
    val shippingPhone: String = "",
    val shippingAddress: String = "",
    val memo: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,
    // 배송지 관리
    val selectedAddress: Address? = null,
    val selectedAddressId: Int? = null,
    val defaultAddressId: Int? = null,
    val addresses: List<Address> = emptyList(),
    // 쿠폰
    val availableCoupons: List<Coupon> = emptyList(),
    val selectedCoupons: Map<Int, Coupon> = emptyMap(),
    val draftSelectedCoupons: Map<Int, Coupon> = emptyMap(),
    val couponDiscount: Long = 0,
    val showCouponSheet: Boolean = false,
    val couponCode: String = "",
    val isClaiming: Boolean = false,
    val isCouponLoading: Boolean = true,
    val couponError: String? = null,
    val subscribedCreatorIds: Set<Int> = emptySet(),
    val creatorTiers: Map<Int, SubscriptionPlanTier> = emptyMap(),
    // 결제수단
    val selectedPaymentProvider: String = if (BuildConfig.DEBUG) "MOCK" else "",
    // Toss 결제 진행
    val pendingTossOrderId: Int? = null,
    val pendingTossOrderNumber: String? = null,
    val pendingTossAmount: Long? = null,
    // 저장카드 결제
    val savedCard: BillingCard? = null,
    val isSavedCardLoading: Boolean = false,
    val showSavedCardRegister: Boolean = false,
    val savedCardRegisterUrl: String? = null,
    // 신용·체크카드 시트
    val showCardSheet: Boolean = false,
    val cardSheetDraft: String? = null,  // TOSS_BILLING 저장카드·TOSS_CARD 일반결제
    val cardSheetError: String? = null,
    // 포인트
    val pointBalance: PointBalance? = null,
    val usedPoints: Int = 0,
    val pointValidationMessage: String? = null
)

sealed interface CheckoutIntent {
    // 데이터 로딩
    data object LoadCheckout : CheckoutIntent
    data object RefreshAddresses : CheckoutIntent

    // 주문
    data object PlaceOrder : CheckoutIntent

    // 배송지
    data object NavigateToAddressManage : CheckoutIntent

    data object NavigateBack : CheckoutIntent
    data object SnackbarDismissed : CheckoutIntent

    // 쿠폰
    data object ShowCouponSheet : CheckoutIntent
    data object DismissCouponSheet : CheckoutIntent
    data class ToggleCoupon(val coupon: Coupon) : CheckoutIntent
    data class UpdateCouponCode(val code: String) : CheckoutIntent
    data object ClaimCoupon : CheckoutIntent
    data object ApplyCoupons : CheckoutIntent
    data object RetryCoupons : CheckoutIntent
    // 결제수단
    data class SelectPaymentProvider(val provider: String) : CheckoutIntent
    // 포인트
    data class UpdateUsedPoints(val points: String) : CheckoutIntent
    data object UseAllPoints : CheckoutIntent
    // 토스 결제 SDK 콜백
    data class TossPaymentSucceeded(val paymentKey: String) : CheckoutIntent
    data class TossPaymentFailed(val message: String) : CheckoutIntent
    data object TossPaymentCanceled : CheckoutIntent
    // 신용·체크카드 시트
    data object ShowCardSheet : CheckoutIntent
    data object DismissCardSheet : CheckoutIntent
    data class SelectCardSheetOption(val key: String) : CheckoutIntent
    data object ConfirmCardSheet : CheckoutIntent
    data object DismissCardSheetError : CheckoutIntent
    // 저장카드 등록
    data object AddSavedCard : CheckoutIntent
    data class SavedCardAuthReturned(val authKey: String?) : CheckoutIntent
    data class SavedCardAuthFailed(val code: String?, val message: String?) : CheckoutIntent
    data object SavedCardRegisterDismissed : CheckoutIntent
}

sealed interface CheckoutSideEffect {
    data object NavigateBack : CheckoutSideEffect
    data class NavigateToOrderComplete(val orderId: Int, val orderNumber: String) : CheckoutSideEffect
    data object NavigateToAddressManage : CheckoutSideEffect
    data class ShowToast(val message: String) : CheckoutSideEffect
    data class LaunchTossPayment(
        val orderId: String,                //서버 거래ID
        val orderName: String,
        val amount: Long,                   // 서버 금액
        val method: TossPaymentMethodType
    ) : CheckoutSideEffect
}

// 결제수단별 SDK 분기
enum class TossPaymentMethodType { CARD, TOSSPAY, TRANSFER, MOBILE }
