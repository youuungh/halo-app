package com.ninezero.halo.feature.store.presentation.sales_manage

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.CreatorSalesSummary
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.ShippingStatusResponse

data class SalesManageState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    // 상단 매출 요약
    val salesSummary: CreatorSalesSummary? = null,
    val isSummaryLoading: Boolean = true,
    // 주문 목록
    val orders: List<Order> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = true,
    val error: String? = null,
    // 필터
    val selectedFilter: OrderFilter = OrderFilter.ALL,
    // 배송 정보 · 배송 조회 시트
    val deliveryInfoOrderId: Int? = null,
    val trackingSheetOrderId: Int? = null,
    val trackingStatus: ShippingStatusResponse? = null,
    val isTrackingStatusLoading: Boolean = false,
    // 주문 취소·상태 변경
    val showCancelConfirm: Boolean = false,
    val cancelConfirmOrderId: Int? = null,
    val statusUpdatingOrderId: Int? = null,
    val isCancellingOrder: Boolean = false,  // 처리 중인 게 판매 취소인지, 로딩 칸 고르기용
    // 송장 입력 시트
    val showShippingSheet: Boolean = false,
    val shippingOrderId: Int? = null,
    val isEditingShipping: Boolean = false,
    val selectedCarrier: String = "",
    val trackingNumber: String = "",
    val isSavingShipping: Boolean = false,
    val shippingError: String? = null,
    val toastMessage: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface SalesManageIntent {
    data object Load : SalesManageIntent
    data object Refresh : SalesManageIntent
    data object LoadMore : SalesManageIntent
    data object NavigateBack : SalesManageIntent
    data object OpenSalesStats : SalesManageIntent
    // 필터
    data class SelectFilter(val filter: OrderFilter) : SalesManageIntent
    // 배송 정보 · 배송 조회
    data class ShowDeliveryInfo(val orderId: Int) : SalesManageIntent
    data object DismissDeliveryInfo : SalesManageIntent
    data class ShowTrackingSheet(val orderId: Int) : SalesManageIntent
    data object DismissTrackingSheet : SalesManageIntent
    // 주문 취소·상태 변경
    data class ConfirmOrder(val orderId: Int) : SalesManageIntent
    data class ShowCancelConfirm(val orderId: Int) : SalesManageIntent
    data object DismissCancelConfirm : SalesManageIntent
    data object CancelOrder : SalesManageIntent
    // 송장 입력
    data class ShowShippingSheet(val orderId: Int) : SalesManageIntent
    data object DismissShippingSheet : SalesManageIntent
    data class SelectCarrier(val carrier: String) : SalesManageIntent
    data class UpdateTrackingNumber(val trackingNumber: String) : SalesManageIntent
    data object SaveShippingInfo : SalesManageIntent
    data object DismissShippingError : SalesManageIntent
    data object DismissToast : SalesManageIntent
    // Snackbar
    data object SnackbarDismissed : SalesManageIntent
}

sealed interface SalesManageSideEffect {
    data object NavigateBack : SalesManageSideEffect
    data object NavigateToSalesStats : SalesManageSideEffect
}
