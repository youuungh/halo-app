package com.ninezero.halo.feature.store.presentation.order_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.core.util.mergeFreshHead
import com.ninezero.halo.feature.store.domain.event.StoreEvent
import com.ninezero.halo.feature.store.domain.event.StoreEventBus
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.purchaseBlockedMessage
import com.ninezero.halo.feature.store.domain.repository.CartRepository
import com.ninezero.halo.feature.store.domain.repository.OrderRepository
import com.ninezero.halo.feature.store.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class OrderListViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val storeEventBus: StoreEventBus
) : ViewModel(), OrbitContainerHost<OrderListState, OrderListState, OrderListSideEffect> {

    override val container: OrbitContainer<OrderListState, OrderListState, OrderListSideEffect> =
        orbitContainer(OrderListState())

    private val listGeneration = ListGeneration()

    init {
        loadOrders()
        observeStoreEvents()
    }

    // 하위 화면에서 바뀐 항목만 반영
    private fun observeStoreEvents() {
        storeEventBus.events
            .onEach { event ->
                when (event) {
                    is StoreEvent.OrderUpdated -> intent {
                        reduce { state.copy(orders = state.orders.map { if (it.id == event.order.id) event.order else it }) }
                    }

                    is StoreEvent.ReviewCreated -> intent {
                        val review = event.review
                        fun List<OrderItem>.reviewed() = map { if (it.productId == review.productId) it.copy(hasReview = true) else it }
                        reduce {
                            state.copy(
                                orders = state.orders.map { order ->
                                    if (order.id != review.orderId) order
                                    else order.copy(
                                        items = order.items.reviewed(),
                                        creatorGroups = order.creatorGroups.map { it.copy(items = it.items.reviewed()) }
                                    )
                                }
                            )
                        }
                    }

                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: OrderListIntent) {
        when (intent) {
            is OrderListIntent.Load -> loadOrders()
            is OrderListIntent.LoadMore -> loadMore()
            is OrderListIntent.Refresh -> refresh()
            is OrderListIntent.ScreenResumed -> refreshOrdersQuietly()
            is OrderListIntent.ToggleOrderExpand -> toggleExpand(intent.orderId)
            is OrderListIntent.WriteReview -> writeReview(intent.item, intent.orderId)
            is OrderListIntent.AddToCart -> addToCart(intent.item)
            is OrderListIntent.NavigateBack -> navigateBack()
            is OrderListIntent.NavigateToDetail -> navigateToDetail(intent.orderId)
            is OrderListIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadOrders() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        orderRepository.getMyOrders(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    state.copy(
                        isLoading = false,
                        orders = result.items,
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "주문 목록을 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun loadMore(): Job = intent {  // 자기를 다시 불러 타입 명시
        if (!state.hasMore || state.isLoadingMore || state.isLoading) return@intent

        reduce { state.copy(isLoadingMore = true) }

        val generation = listGeneration.current()
        val cursor = state.nextCursor

        orderRepository.getMyOrders(cursor = cursor, limit = 20)
            .onSuccess { result ->
                if (listGeneration.current() != generation) {
                    reduce { state.copy(isLoadingMore = false) }
                    // 목록 수가 그대로라 트리거 대신 다시 요청
                    if (state.hasMore) loadMore()
                    return@intent
                }
                reduce {
                    state.copy(
                        isLoadingMore = false,
                        orders = (state.orders + result.items).distinctBy { it.id },
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoadingMore = false,
                        snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")
                    )
                }
            }
    }

    // 첫 페이지만 받아 앞에 합침
    private fun refreshOrdersQuietly() = intent {
        if (state.isLoading || state.isRefreshing) return@intent
        orderRepository.getMyOrders(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    val merged = state.orders.mergeFreshHead(result.items) { it.id }
                    // 뒷부분을 이어 붙였으면 다음 커서도 그대로
                    val keepsTail = merged.size > result.items.size
                    state.copy(
                        orders = merged,
                        hasMore = if (keepsTail) state.hasMore else result.nextCursor != null,
                        nextCursor = if (keepsTail) state.nextCursor else result.nextCursor,
                        error = null
                    )
                }
            }
    }

    private fun refresh() = intent {
        reduce { state.copy(isRefreshing = true, error = null) }

        orderRepository.getMyOrders(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    state.copy(
                        isRefreshing = false,
                        isLoading = false,
                        orders = result.items,
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        snackbarState = SnackbarState(error.message ?: "새로고침 실패")
                    )
                }
            }
    }

    private fun toggleExpand(orderId: Int) = intent {
        val expanded = state.expandedOrderIds
        reduce {
            state.copy(
                expandedOrderIds = if (orderId in expanded) expanded - orderId else expanded + orderId
            )
        }
    }

    private fun writeReview(item: OrderItem, orderId: Int) = intent {
        postSideEffect(
            OrderListSideEffect.NavigateToCreateReview(
                productId = item.productId,
                orderId = orderId,
                productName = item.productName,
                productImageUrl = item.productImageUrl
            )
        )
    }

    private fun addToCart(item: OrderItem) = intent {
        val product = productRepository.getProduct(item.productId)
            .onFailure { error ->
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "상품 정보를 확인할 수 없습니다")) }
            }
            .getOrNull() ?: return@intent

        product.purchaseBlockedMessage()?.let { message ->
            reduce { state.copy(snackbarState = SnackbarState(message)) }
            return@intent
        }

        cartRepository.addToCart(productId = item.productId, quantity = 1)
            .onSuccess {
                reduce {
                    state.copy(
                        snackbarState = SnackbarState(
                            message = "장바구니에 담았습니다",
                            actionLabel = "보러가기"
                        )
                    )
                }
            }
            .onFailure { error ->
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "장바구니 담기 실패")) }
            }
    }

    private fun navigateBack() = intent {
        postSideEffect(OrderListSideEffect.NavigateBack)
    }

    private fun navigateToDetail(orderId: Int) = intent {
        postSideEffect(OrderListSideEffect.NavigateToDetail(orderId))
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
