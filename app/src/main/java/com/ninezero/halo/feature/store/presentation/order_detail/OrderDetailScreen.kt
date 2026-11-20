package com.ninezero.halo.feature.store.presentation.order_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.list.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.list.ListErrorState
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.store.domain.model.OrderCreatorGroup
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.PaymentStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.feature.store.domain.model.creatorGroupsOrFallback
import com.ninezero.halo.feature.store.domain.model.isCancellable
import com.ninezero.halo.feature.store.presentation.components.CartCircleButton
import com.ninezero.halo.feature.store.presentation.components.OrderCreatorGroupHeader
import com.ninezero.halo.feature.store.presentation.components.OrderDetailItemRow
import com.ninezero.halo.feature.store.presentation.components.OrderGroupAction
import com.ninezero.halo.feature.store.presentation.components.OrderGroupActionBar
import com.ninezero.halo.feature.store.presentation.components.ShippingTrackingBottomSheet
import com.ninezero.halo.feature.store.presentation.order_detail.components.CancelOrderBottomSheet
import com.ninezero.halo.feature.store.presentation.order_detail.components.InfoRow
import com.ninezero.halo.feature.store.presentation.order_detail.components.OrderPaymentAmounts
import com.ninezero.halo.feature.store.presentation.order_detail.components.OrderPaymentSection
import com.ninezero.halo.feature.store.presentation.order_detail.components.OrderSectionDivider
import com.ninezero.halo.feature.store.presentation.order_detail.components.OrderSectionHeader
import com.ninezero.halo.feature.store.presentation.order_detail.components.OrderShippingInfo
import com.ninezero.halo.feature.store.presentation.order_detail.components.ReviewPickerSheet
import com.ninezero.halo.feature.store.presentation.order_detail.components.getPaymentProviderText
import com.ninezero.halo.feature.store.presentation.order_detail.components.orderDetailSkeleton
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.util.concurrent.TimeUnit

@Composable
fun OrderDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (productId: Int) -> Unit,
    onNavigateToCart: () -> Unit = {},
    onNavigateToCreateReview: (productId: Int, orderId: Int, productName: String, productImageUrl: String?) -> Unit = { _, _, _, _ -> },
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    // 복귀 시 자동 새로고침, 첫 진입은 스킵. saveable 가드로 복귀 새로고침 보장
    var hasInitialResumeHandled by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasInitialResumeHandled) {
            viewModel.onIntent(OrderDetailIntent.Load)
        } else {
            hasInitialResumeHandled = true
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is OrderDetailSideEffect.NavigateBack -> onNavigateBack()
            is OrderDetailSideEffect.NavigateToCart -> onNavigateToCart()
            is OrderDetailSideEffect.NavigateToProductDetail -> onNavigateToProductDetail(sideEffect.productId)
            is OrderDetailSideEffect.NavigateToCreateReview -> onNavigateToCreateReview(
                sideEffect.productId,
                sideEffect.orderId,
                sideEffect.productName,
                sideEffect.productImageUrl
            )
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onAction = {
            viewModel.onIntent(OrderDetailIntent.NavigateToCart)
        },
        onDismiss = { viewModel.onIntent(OrderDetailIntent.SnackbarDismissed) }
    )

    OrderDetailScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = { viewModel.onIntent(OrderDetailIntent.NavigateBack) },
        onCancelOrder = { creatorId ->
            viewModel.onIntent(
                OrderDetailIntent.ShowCancelSheet(
                    creatorId
                )
            )
        },
        onDismissCancelSheet = { viewModel.onIntent(OrderDetailIntent.DismissCancelSheet) },
        onSelectCancelReason = { viewModel.onIntent(OrderDetailIntent.SelectCancelReason(it)) },
        onUpdateOtherCancelReason = { viewModel.onIntent(OrderDetailIntent.UpdateOtherCancelReason(it)) },
        onProceedCancel = { viewModel.onIntent(OrderDetailIntent.ProceedCancelFromSheet) },
        onConfirmCancel = { viewModel.onIntent(OrderDetailIntent.ConfirmCancel) },
        onDismissCancel = { viewModel.onIntent(OrderDetailIntent.DismissCancelConfirm) },
        onRetry = { viewModel.onIntent(OrderDetailIntent.Load) },
        onWriteReview = { item -> viewModel.onIntent(OrderDetailIntent.WriteReview(item)) },
        onTrackShipping = { item -> viewModel.onIntent(OrderDetailIntent.ShowShippingSheet(item)) },
        onAddToCart = { item -> viewModel.onIntent(OrderDetailIntent.AddToCart(item)) },
        onProductImageClick = { item -> viewModel.onIntent(OrderDetailIntent.OpenProduct(item)) },
        onDismissShippingSheet = { viewModel.onIntent(OrderDetailIntent.DismissShippingSheet) }
    )
}

@Composable
internal fun OrderDetailScreenContent(
    state: OrderDetailState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateBack: () -> Unit = {},
    onCancelOrder: (Int) -> Unit = {},
    onDismissCancelSheet: () -> Unit = {},
    onSelectCancelReason: (CancelReason) -> Unit = {},
    onUpdateOtherCancelReason: (String) -> Unit = {},
    onProceedCancel: () -> Unit = {},
    onConfirmCancel: () -> Unit = {},
    onDismissCancel: () -> Unit = {},
    onRetry: () -> Unit = {},
    onWriteReview: (OrderItem) -> Unit = {},
    onTrackShipping: (OrderItem) -> Unit = {},
    onAddToCart: (OrderItem) -> Unit = {},
    onProductImageClick: (OrderItem) -> Unit = {},
    onDismissShippingSheet: () -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val listState = rememberLazyListState()
    var headerHeight by rememberSaveableDp()
    val numberFormat = rememberKoreanNumberFormat()
    val order = state.order
    var reviewPickerItems by remember { mutableStateOf<List<OrderItem>>(emptyList()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HaloPullToRefreshBox(
            isRefreshing = state.isLoading && order != null,
            onRefresh = onRetry,
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
            when {
                order == null && state.isLoading -> {
                    LazyColumn(
                        userScrollEnabled = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
                    ) {
                        orderDetailSkeleton()
                    }
                }

                state.error != null && order == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = headerHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        ListErrorState(
                            title = "주문 정보를 불러오지 못했습니다",
                            reason = state.error,
                            onRetry = onRetry
                        )
                    }
                }

                order != null -> {
                    val isDelivered = order.status == OrderStatus.DELIVERED
                    val creatorGroups = order.creatorGroupsOrFallback()
                    val activeCreatorGroups =
                        creatorGroups.filter { it.status != OrderStatus.CANCELLED }
                    val displayCreatorGroups = activeCreatorGroups.ifEmpty { creatorGroups }
                    val displayOrderItems =
                        displayCreatorGroups.flatMap { it.items }.ifEmpty { order.items }

                    // 리뷰 마감 계산
                    val reviewDeadlineBaseMillis =
                        remember(isDelivered, order.actualDeliveryDate, order.updatedAt) {
                            if (!isDelivered) null
                            else order.actualDeliveryDate ?: order.updatedAt
                        }
                    val reviewDeadlineLabel = remember(reviewDeadlineBaseMillis) {
                        reviewDeadlineBaseMillis?.let { base ->
                            val now = System.currentTimeMillis()
                            val daysPassed = TimeUnit.MILLISECONDS.toDays(now - base).toInt()
                            val daysLeft =
                                Constants.Commerce.REVIEW_WRITE_DEADLINE_DAYS - daysPassed
                            when {
                                daysLeft < 0 -> "만료"
                                daysLeft == 0 -> "D-Day"
                                else -> "D-$daysLeft"
                            }
                        }
                    }
                    val canWriteReview =
                        isDelivered && reviewDeadlineLabel != null && reviewDeadlineLabel != "만료"

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
                    ) {
                        // 주문 정보
                        item(key = "order_info") {
                            OrderSectionHeader("주문 정보")
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                InfoRow("주문번호", order.orderNumber)
                                InfoRow("주문일시", order.createdAt.toFormattedDate("yyyy.MM.dd HH:mm"))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OrderSectionDivider()
                        }

                        // 주문 상품
                        item(key = "items_header") {
                            OrderSectionHeader("주문 상품", count = displayOrderItems.size)
                        }

                        creatorGroups.forEachIndexed { groupIndex, group ->
                            val creatorName = group.creatorName
                            val creatorAvatar = group.creatorAvatarUrl
                            val creatorId = group.creatorId ?: 0
                            val isGroupCancelled = group.status == OrderStatus.CANCELLED

                            item(key = "creator_group_$creatorId") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = if (groupIndex == 0) 0.dp else 8.dp
                                        )
                                        .border(
                                            width = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.5f
                                            ),
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .clip(MaterialTheme.shapes.medium)
                                ) {
                                    if (creatorName != null) {
                                        OrderCreatorGroupHeader(
                                            creatorName = creatorName,
                                            creatorAvatarUrl = creatorAvatar,
                                            creatorRole = group.creatorRole,
                                            isSubscribed = group.isSubscribed,
                                            status = group.status,
                                            shippingStatus = group.shippingStatus
                                        )
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                    }

                                    // 상품 사이는 선 없이 여백만
                                    group.items.forEach { item ->
                                        OrderDetailItemRow(
                                            item = item,
                                            numberFormat = numberFormat,
                                            // 영수증이라 라인 합계만
                                            showLineTotal = true,
                                            onImageClick = { onProductImageClick(item) },
                                            // 취소된 그룹·삭제된 상품은 다시 담기 없음
                                            trailing = if (!isGroupCancelled && item.isProductActive) {
                                                { CartCircleButton(onClick = { onAddToCart(item) }) }
                                            } else {
                                                null
                                            }
                                        )
                                    }

                                    val groupRefundedAt = group.refundedAt
                                    if (groupRefundedAt != null) {
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                        Column(
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 10.dp
                                            )
                                        ) {
                                            Text(
                                                text = "환불 정보",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val groupRefundValue = group.refundAmount
                                            if (groupRefundValue != null) {
                                                InfoRow(
                                                    "환불 금액",
                                                    "${numberFormat.format(groupRefundValue)}원"
                                                )
                                            }
                                            val groupRefundReason = group.refundReason
                                            if (!groupRefundReason.isNullOrBlank()) {
                                                InfoRow("환불 사유", groupRefundReason)
                                            }
                                            InfoRow(
                                                "환불일시",
                                                groupRefundedAt.toFormattedDate("yyyy.MM.dd HH:mm")
                                            )
                                        }
                                    } else if (!isGroupCancelled) {
                                        OrderGroupActionBar(
                                            groupActions(
                                                group = group,
                                                // 결제 확인 중엔 청구 여부가 정해진 뒤
                                                canCancel = group.isCancellable() && order.paymentStatus != PaymentStatus.PENDING,
                                                reviewItems = if (canWriteReview) {
                                                    group.items.filter { !it.hasReview && it.isProductActive }
                                                } else {
                                                    emptyList()
                                                },
                                                reviewDeadlineLabel = reviewDeadlineLabel,
                                                isCancelling = state.isCancelling && state.cancelConfirmCreatorId == group.creatorId,
                                                cancelEnabled = !state.isCancelling,
                                                onCancel = { group.creatorId?.let(onCancelOrder) },
                                                onTrackShipping = { onTrackShipping(group.items.first()) },
                                                onWriteReview = { items ->
                                                    if (items.size == 1) onWriteReview(items.first()) else reviewPickerItems = items
                                                }
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        item(key = "items_divider") {
                            Spacer(modifier = Modifier.height(8.dp))
                            OrderSectionDivider()
                        }

                        // 배송지 정보
                        item(key = "shipping_info") {
                            OrderSectionHeader("배송 정보")
                            OrderShippingInfo(
                                name = order.shippingName,
                                phone = order.shippingPhone,
                                address = order.shippingAddress,
                                memo = order.memo
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OrderSectionDivider()
                        }

                        // 결제 요약
                        item(key = "payment_summary") {
                            OrderSectionHeader("결제 정보")
                            val originalTotal = displayOrderItems.sumOf { item ->
                                val originalPrice = item.originalPrice ?: item.price
                                originalPrice * item.quantity
                            }
                            val subtotal = displayOrderItems.sumOf { it.subtotal }
                            OrderPaymentSection(
                                amounts = OrderPaymentAmounts(
                                    productAmount = originalTotal,
                                    productDiscount = originalTotal - subtotal,
                                    couponDiscount = order.couponDiscount,
                                    pointsUsed = order.pointsUsed,
                                    totalPrice = order.totalPrice,
                                    paymentProviderText = order.paymentProvider?.let(::getPaymentProviderText)
                                ),
                                groups = displayCreatorGroups,
                                showEarn = order.status != OrderStatus.CANCELLED && order.paymentStatus != PaymentStatus.REFUNDED,
                                numberFormat = numberFormat
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        HazeScreenHeader(
            title = "주문 상세",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = onNavigateBack,
            onHeightMeasured = { headerHeight = it }
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (state.isCancelling) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
            )
        }
    }

    if (state.showCancelSheet && order != null) {
        CancelOrderBottomSheet(
            order = order,
            creatorId = state.cancelConfirmCreatorId,
            selectedReason = state.selectedCancelReason,
            otherReason = state.otherCancelReason,
            isCancelling = state.isCancelling,
            numberFormat = numberFormat,
            onSelectReason = onSelectCancelReason,
            onUpdateOtherReason = onUpdateOtherCancelReason,
            onProceed = onProceedCancel,
            onDismiss = onDismissCancelSheet
        )
    }

    if (state.showCancelConfirm) {
        val cancelGroup = state.order?.creatorGroupsOrFallback()
            ?.firstOrNull { it.creatorId == state.cancelConfirmCreatorId }
        ConfirmDialog(
            title = cancelGroup?.creatorName?.let { "$it 상품을 취소하시겠어요?" } ?: "주문을 취소하시겠어요?",
            confirmText = "주문 취소",
            dismissText = "돌아가기",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = onConfirmCancel,
            onDismiss = onDismissCancel
        )
    }

    if (reviewPickerItems.isNotEmpty()) {
        ReviewPickerSheet(
            items = reviewPickerItems,
            numberFormat = numberFormat,
            onPick = { item ->
                reviewPickerItems = emptyList()
                onWriteReview(item)
            },
            onDismiss = { reviewPickerItems = emptyList() }
        )
    }

    if (state.showShippingSheet && state.order != null) {
        val selectedGroup = state.order.creatorGroupsOrFallback()
            .firstOrNull { it.creatorId == state.shippingSheetCreatorId }
            ?: state.order.creatorGroupsOrFallback().firstOrNull()
        ShippingTrackingBottomSheet(
            group = selectedGroup,
            shippingStatus = state.shippingStatus,
            isLoading = state.isShippingStatusLoading,
            onDismiss = onDismissShippingSheet
        )
    }
}

/** 그룹 아래 글자 버튼: 취소 · 배송조회 · 리뷰 */
@Composable
private fun groupActions(
    group: OrderCreatorGroup,
    canCancel: Boolean,
    reviewItems: List<OrderItem>,
    reviewDeadlineLabel: String?,
    isCancelling: Boolean,
    cancelEnabled: Boolean,
    onCancel: () -> Unit,
    onTrackShipping: () -> Unit,
    onWriteReview: (List<OrderItem>) -> Unit
): List<OrderGroupAction> {
    val colors = MaterialTheme.colorScheme
    if (canCancel) {
        return listOf(
            OrderGroupAction(
                label = "주문 취소",
                color = colors.error,
                isLoading = isCancelling,
                enabled = cancelEnabled,
                onClick = onCancel
            )
        )
    }
    val isMoving = group.shippingStatus in setOf(ShippingStatus.SHIPPED, ShippingStatus.IN_TRANSIT, ShippingStatus.OUT_FOR_DELIVERY)
    val tracking = OrderGroupAction(
        label = "배송조회",
        color = if (isMoving) colors.primary else colors.onSurface,
        onClick = onTrackShipping
    )
    if (reviewItems.isEmpty()) return listOf(tracking)
    val review = OrderGroupAction(
        label = if (reviewItems.size > 1) "리뷰 쓰기 ${reviewItems.size}" else "리뷰 쓰기",
        color = colors.primary,
        badge = reviewDeadlineLabel,
        onClick = { onWriteReview(reviewItems) }
    )
    return listOf(tracking, review)
}
