package com.ninezero.halo.feature.store.presentation.order_list

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.ShippingStatusResponse

data class OrderListState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val orders: List<Order> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = true,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,
    val expandedOrderIds: Set<Int> = emptySet(),
    // 배송조회 시트
    val shippingSheetOrderId: Int? = null,
    val shippingSheetCreatorId: Int? = null,
    val shippingStatus: ShippingStatusResponse? = null,
    val isShippingStatusLoading: Boolean = false
)

sealed interface OrderListIntent {
    // 데이터 로딩
    data object Load : OrderListIntent
    data object LoadMore : OrderListIntent
    data object Refresh : OrderListIntent
    data object ScreenResumed : OrderListIntent
    data class ToggleOrderExpand(val orderId: Int) : OrderListIntent
    data class WriteReview(val item: OrderItem, val orderId: Int) : OrderListIntent
    data class AddToCart(val item: OrderItem) : OrderListIntent
    data class ShowShippingSheet(val orderId: Int, val creatorId: Int?) : OrderListIntent
    data object DismissShippingSheet : OrderListIntent
    data object NavigateBack : OrderListIntent
    data class NavigateToDetail(val orderId: Int) : OrderListIntent
    // Snackbar
    data object SnackbarDismissed : OrderListIntent
}

sealed interface OrderListSideEffect {
    data object NavigateBack : OrderListSideEffect
    data class NavigateToDetail(val orderId: Int) : OrderListSideEffect
    data class NavigateToCreateReview(
        val productId: Int,
        val orderId: Int,
        val productName: String,
        val productImageUrl: String?
    ) : OrderListSideEffect
}
