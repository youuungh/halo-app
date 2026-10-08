package com.ninezero.halo.feature.store.presentation.order_list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.core.ui.components.layout.PagedListScreenShell
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.presentation.order_list.components.OrderCard
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun OrderListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrderDetail: (Int) -> Unit,
    onNavigateToCart: () -> Unit = {},
    onNavigateToCreateReview: (productId: Int, orderId: Int, productName: String, productImageUrl: String?) -> Unit = { _, _, _, _ -> },
    viewModel: OrderListViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    var hasInitialResumeHandled by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasInitialResumeHandled) {
            viewModel.onIntent(OrderListIntent.ScreenResumed)
        } else {
            hasInitialResumeHandled = true
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is OrderListSideEffect.NavigateBack -> onNavigateBack()
            is OrderListSideEffect.NavigateToDetail -> onNavigateToOrderDetail(sideEffect.orderId)
            is OrderListSideEffect.NavigateToCreateReview -> onNavigateToCreateReview(
                sideEffect.productId, sideEffect.orderId, sideEffect.productName, sideEffect.productImageUrl
            )
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onAction = {
            if (state.snackbarState?.actionLabel == "보러가기") {
                onNavigateToCart()
            }
        },
        onDismiss = { viewModel.onIntent(OrderListIntent.SnackbarDismissed) }
    )

    OrderListScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = { viewModel.onIntent(OrderListIntent.NavigateBack) },
        onOrderClick = { orderId -> viewModel.onIntent(OrderListIntent.NavigateToDetail(orderId)) },
        onRefresh = { viewModel.onIntent(OrderListIntent.Refresh) },
        onRetry = { viewModel.onIntent(OrderListIntent.Load) },
        onLoadMore = { viewModel.onIntent(OrderListIntent.LoadMore) },
        onToggleExpand = { orderId -> viewModel.onIntent(OrderListIntent.ToggleOrderExpand(orderId)) },
        onWriteReview = { item, orderId -> viewModel.onIntent(OrderListIntent.WriteReview(item, orderId)) },
        onAddToCart = { item -> viewModel.onIntent(OrderListIntent.AddToCart(item)) }
    )
}

@Composable
internal fun OrderListScreenContent(
    state: OrderListState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateBack: () -> Unit = {},
    onOrderClick: (Int) -> Unit = {},
    onRefresh: () -> Unit = {},
    onRetry: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onToggleExpand: (Int) -> Unit = {},
    onWriteReview: (OrderItem, Int) -> Unit = { _, _ -> },
    onAddToCart: (OrderItem) -> Unit = {}
) {
    PagedListScreenShell(
        title = "주문/배송",
        isRefreshing = state.isRefreshing,
        isLoading = state.isLoadingMore || state.isLoading,
        hasMore = state.hasMore,
        isEmpty = state.orders.isEmpty(),
        error = state.error,
        emptyTitle = "주문 내역이 없어요",
        emptyDescription = "상품을 주문하면 여기에 표시돼요",
        onNavigateBack = onNavigateBack,
        onRefresh = onRefresh,
        onRetry = onRetry,
        onLoadMore = onLoadMore,
        snackbarHostState = snackbarHostState
    ) { listState, headerHeight ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
        ) {
            items(
                items = state.orders,
                key = { "order_${it.id}" }
            ) { order ->
                OrderCard(
                    order = order,
                    isExpanded = order.id in state.expandedOrderIds,
                    onOrderClick = { onOrderClick(order.id) },
                    onToggleExpand = { onToggleExpand(order.id) },
                    onWriteReview = { item -> onWriteReview(item, order.id) },
                    onAddToCart = onAddToCart
                )
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }

            if (state.isLoadingMore) {
                item(key = "loading_more") {
                    ListLoadMoreIndicator()
                }
            }
        }
    }
}
