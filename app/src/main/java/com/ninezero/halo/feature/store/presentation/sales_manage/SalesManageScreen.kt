package com.ninezero.halo.feature.store.presentation.sales_manage

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.ListEmptyState
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
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
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SalesManageSideEffect.NavigateBack -> onNavigateBack()
            is SalesManageSideEffect.NavigateToSalesStats -> onNavigateToSalesStats()
            is SalesManageSideEffect.ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { onIntent(SalesManageIntent.Load) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
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
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.selectedFilter == OrderFilter.ALL) {
                                    ListEmptyState(
                                        title = "들어온 주문이 없어요",
                                        description = "상품이 판매되면 여기에 표시돼요"
                                    )
                                } else {
                                    ListEmptyState(title = "${state.selectedFilter.label} 주문이 없어요")
                                }
                            }
                        }
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
                            Column(modifier = Modifier.animateItem()) {
                                SalesOrderCard(
                                    order = order,
                                    isExpanded = order.id in state.expandedOrderIds,
                                    onConfirm = { onIntent(SalesManageIntent.ConfirmOrder(order.id)) },
                                    onCancel = { onIntent(SalesManageIntent.ShowCancelConfirm(order.id)) },
                                    onRegisterShipping = { onIntent(SalesManageIntent.ShowShippingSheet(order.id)) },
                                    onToggleExpand = { onIntent(SalesManageIntent.ToggleOrderExpand(order.id)) },
                                    isStatusUpdating = state.statusUpdatingOrderId == order.id
                                )
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }

                        if (state.isLoading && state.orders.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
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
            selectedCarrier = state.selectedCarrier,
            trackingNumber = state.trackingNumber,
            isSaving = state.isSavingShipping,
            onSelectCarrier = { onIntent(SalesManageIntent.SelectCarrier(it)) },
            onTrackingNumberChange = { onIntent(SalesManageIntent.UpdateTrackingNumber(it)) },
            onSave = { onIntent(SalesManageIntent.SaveShippingInfo) },
            onDismiss = { onIntent(SalesManageIntent.DismissShippingSheet) }
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
