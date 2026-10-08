package com.ninezero.halo.feature.store.presentation.order_detail

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.ShippingStatusResponse

data class OrderDetailState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val shippingStatus: ShippingStatusResponse? = null,
    val showCancelConfirm: Boolean = false,
    val cancelConfirmCreatorId: Int? = null,
    val isCancelling: Boolean = false,
    // 주문 취소 시트
    val showCancelSheet: Boolean = false,
    val selectedCancelReason: CancelReason = CancelReason.CHANGED_MIND,
    val cancelReasonEtc: String = "",
    val error: String? = null,
    val snackbarState: SnackbarState? = null,
    val showShippingSheet: Boolean = false,
    val shippingSheetCreatorId: Int? = null,
)

sealed interface OrderDetailIntent {
    // 데이터 로딩
    data object Load : OrderDetailIntent

    // 주문 취소
    data class ShowCancelSheet(val creatorId: Int) : OrderDetailIntent
    data object DismissCancelSheet : OrderDetailIntent
    data class SelectCancelReason(val reason: CancelReason) : OrderDetailIntent
    data class UpdateCancelReasonEtc(val text: String) : OrderDetailIntent
    data object ProceedCancelFromSheet : OrderDetailIntent
    data object DismissCancelConfirm : OrderDetailIntent
    data object ConfirmCancel : OrderDetailIntent

    // 리뷰 작성
    data class WriteReview(val item: OrderItem) : OrderDetailIntent

    // 장바구니
    data class AddToCart(val item: OrderItem) : OrderDetailIntent

    // 배송조회
    data class ShowShippingSheet(val item: OrderItem) : OrderDetailIntent
    data object DismissShippingSheet : OrderDetailIntent

    data object NavigateBack : OrderDetailIntent
    data object NavigateToCart : OrderDetailIntent
    data object SnackbarDismissed : OrderDetailIntent
}

enum class CancelReason(val label: String) {
    CHANGED_MIND("단순 변심"),
    DELIVERY_DELAY("배송 지연"),
    ETC("기타")
}

sealed interface OrderDetailSideEffect {
    data object NavigateBack : OrderDetailSideEffect
    data object NavigateToCart : OrderDetailSideEffect
    data class NavigateToCreateReview(
        val productId: Int,
        val orderId: Int,
        val productName: String,
        val productImageUrl: String?
    ) : OrderDetailSideEffect
}
