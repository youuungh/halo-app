package com.ninezero.halo.feature.store.presentation.sales_manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.list.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.list.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.list.ListErrorState
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.store.domain.model.creatorGroupsOrFallback
import com.ninezero.halo.feature.store.presentation.components.ShippingTrackingBottomSheet
import com.ninezero.halo.feature.store.presentation.sales_manage.components.DeliveryInfoBottomSheet
import com.ninezero.halo.feature.store.presentation.sales_manage.components.SalesOrderCard
import com.ninezero.halo.feature.store.presentation.sales_manage.components.SalesSummaryCard
import com.ninezero.halo.feature.store.presentation.sales_manage.components.SalesSummaryCardSkeleton
import com.ninezero.halo.feature.store.presentation.sales_manage.components.ShippingInputBottomSheet
import com.ninezero.halo.feature.store.presentation.sales_manage.components.salesManageSkeleton
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SalesManageScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSalesStats: () -> Unit,
    viewModel: SalesManageViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SalesManageSideEffect.NavigateBack -> onNavigateBack()
            is SalesManageSideEffect.NavigateToSalesStats -> onNavigateToSalesStats()
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(SalesManageIntent.SnackbarDismissed) }
    )

    SalesManageScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}

private fun LazyListScope.salesSummaryHeaderItem(
    state: SalesManageState,
    onIntent: (SalesManageIntent) -> Unit
) {
    val summary = state.salesSummary
    when {
        state.isSummaryLoading && summary == null -> item { SalesSummaryCardSkeleton() }
        summary != null -> item {
            SalesSummaryCard(
                summary = summary,
                selectedFilter = state.selectedFilter,
                onClick = { onIntent(SalesManageIntent.OpenSalesStats) },
                onSelectFilter = { onIntent(SalesManageIntent.SelectFilter(it)) }
            )
        }
    }
}

@Composable
internal fun SalesManageScreenContent(
    state: SalesManageState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (SalesManageIntent) -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val listState = rememberLazyListState()
    var headerHeight by rememberSaveableDp()

    InfiniteScrollTrigger(
        listState = listState,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        onLoadMore = { onIntent(SalesManageIntent.LoadMore) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(SalesManageIntent.Refresh) },
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
            when {
                state.isLoading && state.orders.isEmpty() -> {
                    LazyColumn(
                        userScrollEnabled = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
                    ) {
                        salesSummaryHeaderItem(state, onIntent)
                        salesManageSkeleton()
                    }
                }

                state.error != null && state.orders.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = headerHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        ListErrorState(
                            title = "주문 목록을 불러오지 못했습니다",
                            reason = state.error,
                            onRetry = { onIntent(SalesManageIntent.Load) }
                        )
                    }
                }

                state.orders.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
                    ) {
                        salesSummaryHeaderItem(state, onIntent)
                    }
                    // 다른 목록 화면처럼 화면 가운데
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // 내 멤버십 · 내 쿠폰처럼 제목 한 줄
                        Text(
                            text = if (state.selectedFilter == OrderFilter.ALL) "들어온 주문이 없어요" else "${state.selectedFilter.label} 주문이 없어요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
                    ) {
                        salesSummaryHeaderItem(state, onIntent)

                        items(
                            items = state.orders,
                            key = { it.id }
                        ) { order ->
                            // 위치 이동 애니메이션은 끔, 따로 출렁임 방지
                            Column(modifier = Modifier.animateItem(placementSpec = null)) {
                                SalesOrderCard(
                                    order = order,
                                    onConfirm = { onIntent(SalesManageIntent.ConfirmOrder(order.id)) },
                                    onCancel = { onIntent(SalesManageIntent.ShowCancelConfirm(order.id)) },
                                    onRegisterShipping = { onIntent(SalesManageIntent.ShowShippingSheet(order.id)) },
                                    onEditShipping = { onIntent(SalesManageIntent.ShowShippingSheet(order.id)) },
                                    onShowDeliveryInfo = { onIntent(SalesManageIntent.ShowDeliveryInfo(order.id)) },
                                    onTrackShipping = { onIntent(SalesManageIntent.ShowTrackingSheet(order.id)) },
                                    isStatusUpdating = state.statusUpdatingOrderId == order.id,
                                    isCancelling = state.isCancellingOrder,
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            }
                        }

                        if (state.isLoading && state.orders.isNotEmpty()) {
                            item {
                                ListLoadMoreIndicator()
                            }
                        }
                    }
                }
            }
        }

        HazeScreenHeader(
            title = "판매 관리",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = { onIntent(SalesManageIntent.NavigateBack) },
            onHeightMeasured = { headerHeight = it }
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        CenterToast(
            visible = state.toastMessage != null,
            message = state.toastMessage ?: "",
            onDismiss = { onIntent(SalesManageIntent.DismissToast) }
        )
    }

    if (state.showShippingSheet) {
        ShippingInputBottomSheet(
            isEditing = state.isEditingShipping,
            selectedCarrier = state.selectedCarrier,
            trackingNumber = state.trackingNumber,
            isSaving = state.isSavingShipping,
            shippingError = state.shippingError,
            onSelectCarrier = { onIntent(SalesManageIntent.SelectCarrier(it)) },
            onTrackingNumberChange = { onIntent(SalesManageIntent.UpdateTrackingNumber(it)) },
            onSave = { onIntent(SalesManageIntent.SaveShippingInfo) },
            onShippingErrorDismiss = { onIntent(SalesManageIntent.DismissShippingError) },
            onDismiss = { onIntent(SalesManageIntent.DismissShippingSheet) }
        )
    }

    state.orders.firstOrNull { it.id == state.deliveryInfoOrderId }?.let { order ->
        DeliveryInfoBottomSheet(
            order = order,
            onDismiss = { onIntent(SalesManageIntent.DismissDeliveryInfo) }
        )
    }

    state.orders.firstOrNull { it.id == state.trackingSheetOrderId }?.let { order ->
        // 판매 목록 주문은 내 상품만 담겨 묶음이 하나
        ShippingTrackingBottomSheet(
            group = order.creatorGroupsOrFallback().firstOrNull(),
            shippingStatus = state.trackingStatus,
            isLoading = state.isTrackingStatusLoading,
            onDismiss = { onIntent(SalesManageIntent.DismissTrackingSheet) }
        )
    }

    if (state.showCancelConfirm) {
        ConfirmDialog(
            title = "판매를 취소하시겠어요?",
            message = "판매자 사유로 주문이 취소됩니다.",
            confirmText = "판매 취소",
            dismissText = "돌아가기",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(SalesManageIntent.CancelOrder) },
            onDismiss = { onIntent(SalesManageIntent.DismissCancelConfirm) }
        )
    }
}
