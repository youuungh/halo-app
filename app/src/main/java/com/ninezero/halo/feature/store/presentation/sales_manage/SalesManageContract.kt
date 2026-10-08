package com.ninezero.halo.feature.store.presentation.sales_manage

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.CreatorSalesSummary
import com.ninezero.halo.feature.store.domain.model.Order

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

    // 필터·펼침
    val selectedFilter: OrderFilter = OrderFilter.ALL,
    val expandedOrderIds: Set<Int> = emptySet(),

    // 주문 취소·상태 변경
    val showCancelConfirm: Boolean = false,
    val cancelConfirmOrderId: Int? = null,
    val statusUpdatingOrderId: Int? = null,

    // 송장 입력 시트
    val showShippingSheet: Boolean = false,
    val shippingOrderId: Int? = null,
    val selectedCarrier: String = "",
    val trackingNumber: String = "",
    val isSavingShipping: Boolean = false,

    val toastMessage: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface SalesManageIntent {
    data object Load : SalesManageIntent
    data object Refresh : SalesManageIntent
    data object LoadMore : SalesManageIntent

    // 네비게이션
    data object NavigateBack : SalesManageIntent
    data object OpenSalesStats : SalesManageIntent

    // 필터·펼침
    data class SelectFilter(val filter: OrderFilter) : SalesManageIntent
    data class ToggleOrderExpand(val orderId: Int) : SalesManageIntent

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

    data object DismissToast : SalesManageIntent
    data object SnackbarDismissed : SalesManageIntent
}

sealed interface SalesManageSideEffect {
    data object NavigateBack : SalesManageSideEffect
    data object NavigateToSalesStats : SalesManageSideEffect
    data class ShowToast(val message: String) : SalesManageSideEffect
}
