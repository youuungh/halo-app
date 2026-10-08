package com.ninezero.halo.feature.store.presentation.sales_manage

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

private const val PAGE_SIZE = 20

@HiltViewModel
class SalesManageViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel(), OrbitContainerHost<SalesManageState, SalesManageState, SalesManageSideEffect> {

    override val container: OrbitContainer<SalesManageState, SalesManageState, SalesManageSideEffect> =
        orbitContainer(SalesManageState())

    private val listGeneration = ListGeneration()  // 새로고침 뒤 늦게 온 더보기 응답 버림

    init {
        load()
        loadSummary()
    }

    fun onIntent(intent: SalesManageIntent) {
        when (intent) {
            is SalesManageIntent.Load -> {
                load()
                loadSummary()
            }
            is SalesManageIntent.Refresh -> {
                refresh()
                loadSummary()
            }
            is SalesManageIntent.LoadMore -> loadMore()
            is SalesManageIntent.NavigateBack -> navigateBack()
            is SalesManageIntent.OpenSalesStats -> openSalesStats()
            is SalesManageIntent.SelectFilter -> selectFilter(intent.filter)
            is SalesManageIntent.ToggleOrderExpand -> toggleOrderExpand(intent.orderId)
            is SalesManageIntent.ConfirmOrder -> confirmOrder(intent.orderId)
            is SalesManageIntent.ShowCancelConfirm -> showCancelConfirm(intent.orderId)
            is SalesManageIntent.DismissCancelConfirm -> dismissCancelConfirm()
            is SalesManageIntent.CancelOrder -> cancelOrder()
            is SalesManageIntent.ShowShippingSheet -> showShippingSheet(intent.orderId)
            is SalesManageIntent.DismissShippingSheet -> dismissShippingSheet()
            is SalesManageIntent.SelectCarrier -> selectCarrier(intent.carrier)
            is SalesManageIntent.UpdateTrackingNumber -> updateTrackingNumber(intent.trackingNumber)
            is SalesManageIntent.SaveShippingInfo -> saveShippingInfo()
            is SalesManageIntent.DismissToast -> dismissToast()
            is SalesManageIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun load() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        val filter = state.selectedFilter

        orderRepository.getCreatorOrders(null, PAGE_SIZE, filter.toQuery()).onSuccess { result ->
            if (state.selectedFilter != filter) return@intent
            listGeneration.next()
            reduce {
                state.copy(
                    isLoading = false,
                    orders = result.items,
                    nextCursor = result.nextCursor,
                    hasMore = result.nextCursor != null
                )
            }
        }.onFailure { error ->
            if (state.selectedFilter != filter) return@intent
            reduce {
                state.copy(
                    isLoading = false,
                    error = error.message ?: "주문 목록을 불러올 수 없습니다"
                )
            }
        }
    }

    private fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }

        val filter = state.selectedFilter
        orderRepository.getCreatorOrders(null, PAGE_SIZE, filter.toQuery()).onSuccess { result ->
            if (state.selectedFilter != filter) {
                reduce { state.copy(isRefreshing = false) }
                return@intent
            }
            listGeneration.next()
            reduce {
                state.copy(
                    isRefreshing = false,
                    isLoading = false,  // 버린 더보기의 로딩 표시
                    orders = result.items,
                    nextCursor = result.nextCursor,
                    hasMore = result.nextCursor != null,
                    error = null
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isRefreshing = false,
                    toastMessage = error.message ?: "새로고침 실패"
                )
            }
        }
    }

    private fun loadMore() = intent {
        if (state.isLoading || !state.hasMore) return@intent

        reduce { state.copy(isLoading = true) }

        val filter = state.selectedFilter
        val generation = listGeneration.current()
        orderRepository.getCreatorOrders(state.nextCursor, PAGE_SIZE, filter.toQuery()).onSuccess { result ->
            if (state.selectedFilter != filter) return@intent
            if (listGeneration.current() != generation) return@intent
            reduce {
                state.copy(
                    isLoading = false,
                    orders = (state.orders + result.items).distinctBy { it.id },
                    nextCursor = result.nextCursor,
                    hasMore = result.nextCursor != null
                )
            }
        }.onFailure { e ->
            if (state.selectedFilter != filter || listGeneration.current() != generation) return@intent
            reduce {
                state.copy(
                    isLoading = false,
                    snackbarState = SnackbarState(message = e.message ?: "더 불러오기 실패")
                )
            }
        }
    }

    private fun loadSummary() = intent {
        // 스켈레톤은 첫 로드만
        reduce { state.copy(isSummaryLoading = state.salesSummary == null) }

        orderRepository.getCreatorSalesSummary().onSuccess { summary ->
            reduce { state.copy(salesSummary = summary, isSummaryLoading = false) }
        }.onFailure {
            // 요약 실패는 카드만 숨기고 주문 목록은 정상 노출
            reduce { state.copy(salesSummary = null, isSummaryLoading = false) }
        }
    }

    private fun openSalesStats() = intent {
        postSideEffect(SalesManageSideEffect.NavigateToSalesStats)
    }

    private fun selectFilter(filter: OrderFilter) = intent {
        if (state.selectedFilter == filter) return@intent
        listGeneration.next()  // 필터를 빠르게 오가도 옛 필터 더보기가 붙지 않게

        // 서버 필터, 1페이지부터
        reduce {
            state.copy(
                selectedFilter = filter,
                isLoading = true,
                orders = emptyList(),
                nextCursor = null,
                hasMore = true,
                error = null
            )
        }

        orderRepository.getCreatorOrders(null, PAGE_SIZE, filter.toQuery()).onSuccess { result ->
            if (state.selectedFilter != filter) return@intent
            listGeneration.next()
            reduce {
                state.copy(
                    isLoading = false,
                    orders = result.items,
                    nextCursor = result.nextCursor,
                    hasMore = result.nextCursor != null
                )
            }
        }.onFailure { error ->
            if (state.selectedFilter != filter) return@intent
            reduce {
                state.copy(
                    isLoading = false,
                    error = error.message ?: "주문 목록을 불러올 수 없습니다"
                )
            }
        }
    }

    private fun OrderFilter.toQuery(): String? = if (this == OrderFilter.ALL) null else name

    private fun List<Order>.applyActiveFilter(  // 필터 버킷 벗어난 주문 제거
        activeFilter: OrderFilter
    ): List<Order> =
        if (activeFilter == OrderFilter.ALL) this else filter { it.matches(activeFilter) }

    private fun toggleOrderExpand(orderId: Int) = intent {
        val newExpanded = state.expandedOrderIds.toMutableSet()
        if (orderId in newExpanded) newExpanded.remove(orderId) else newExpanded.add(orderId)
        reduce { state.copy(expandedOrderIds = newExpanded) }
    }

    private fun confirmOrder(orderId: Int) = intent {
        reduce { state.copy(statusUpdatingOrderId = orderId) }

        orderRepository.updateOrderStatus(orderId, OrderStatus.CONFIRMED).onSuccess { updated ->
            reduce {
                state.copy(
                    statusUpdatingOrderId = null,
                    orders = state.orders.map { if (it.id == orderId) updated else it }
                        .applyActiveFilter(state.selectedFilter),
                    snackbarState = SnackbarState(message = "주문이 확인되었습니다")
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    statusUpdatingOrderId = null,
                    snackbarState = SnackbarState(message = error.message ?: "주문 확인에 실패했습니다")
                )
            }
        }
    }

    private fun showCancelConfirm(orderId: Int) = intent {
        reduce {
            state.copy(
                showCancelConfirm = true,
                cancelConfirmOrderId = orderId
            )
        }
    }

    private fun dismissCancelConfirm() = intent {
        reduce {
            state.copy(
                showCancelConfirm = false,
                cancelConfirmOrderId = null
            )
        }
    }

    private fun cancelOrder() = intent {
        val orderId = state.cancelConfirmOrderId ?: return@intent

        reduce {
            state.copy(
                showCancelConfirm = false,
                cancelConfirmOrderId = null,
                statusUpdatingOrderId = orderId
            )
        }

        orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED).onSuccess { updated ->
            reduce {
                state.copy(
                    statusUpdatingOrderId = null,
                    orders = state.orders.map { if (it.id == orderId) updated else it }
                        .applyActiveFilter(state.selectedFilter),
                    snackbarState = SnackbarState(message = "판매가 취소되었습니다")
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    statusUpdatingOrderId = null,
                    snackbarState = SnackbarState(message = error.message ?: "판매 취소에 실패했습니다")
                )
            }
        }
    }

    private fun showShippingSheet(orderId: Int) = intent {
        reduce {
            state.copy(
                showShippingSheet = true,
                shippingOrderId = orderId,
                selectedCarrier = "",
                trackingNumber = ""
            )
        }
    }

    private fun dismissShippingSheet() = intent {
        reduce {
            state.copy(
                showShippingSheet = false,
                shippingOrderId = null,
                selectedCarrier = "",
                trackingNumber = ""
            )
        }
    }

    private fun selectCarrier(carrier: String) = intent {
        reduce { state.copy(selectedCarrier = carrier) }
    }

    private fun updateTrackingNumber(trackingNumber: String) = blockingIntent {
        reduce { state.copy(trackingNumber = trackingNumber) }
    }

    private fun saveShippingInfo() = intent {
        val orderId = state.shippingOrderId ?: return@intent
        if (state.selectedCarrier.isBlank() || state.trackingNumber.isBlank()) {
            postSideEffect(SalesManageSideEffect.ShowToast("택배사와 운송장번호를 입력해주세요"))
            return@intent
        }

        reduce { state.copy(isSavingShipping = true) }

        orderRepository.updateShippingInfo(
            orderId = orderId,
            trackingNumber = state.trackingNumber.trim(),
            carrier = state.selectedCarrier
        ).onSuccess { updated ->
            reduce {
                state.copy(
                    isSavingShipping = false,
                    showShippingSheet = false,
                    shippingOrderId = null,
                    selectedCarrier = "",
                    trackingNumber = "",
                    orders = state.orders.map { if (it.id == orderId) updated else it }
                        .applyActiveFilter(state.selectedFilter)
                )
            }
            postSideEffect(SalesManageSideEffect.ShowToast("송장번호가 등록되었습니다"))
        }.onFailure { error ->
            reduce { state.copy(isSavingShipping = false) }
            postSideEffect(SalesManageSideEffect.ShowToast(error.message ?: "송장번호 등록에 실패했습니다"))
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(SalesManageSideEffect.NavigateBack)
    }

    private fun dismissToast() = intent {
        reduce { state.copy(toastMessage = null) }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
