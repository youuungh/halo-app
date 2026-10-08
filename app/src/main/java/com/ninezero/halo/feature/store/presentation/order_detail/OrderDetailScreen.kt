package com.ninezero.halo.feature.store.presentation.order_detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.PaymentStatus
import com.ninezero.halo.feature.store.domain.model.creatorGroupsOrFallback
import com.ninezero.halo.feature.store.presentation.components.OrderCreatorGroupHeader
import com.ninezero.halo.feature.store.presentation.components.OrderDetailItemRow
import com.ninezero.halo.feature.store.presentation.order_detail.components.CancelOrderBottomSheet
import com.ninezero.halo.feature.store.presentation.order_detail.components.InfoRow
import com.ninezero.halo.feature.store.presentation.order_detail.components.OrderEarnPointsSection
import com.ninezero.halo.feature.store.presentation.order_detail.components.OrderSectionDivider
import com.ninezero.halo.feature.store.presentation.order_detail.components.OrderSectionHeader
import com.ninezero.halo.feature.store.presentation.order_detail.components.ShippingTrackingBottomSheet
import com.ninezero.halo.feature.store.presentation.order_detail.components.getPaymentProviderText
import com.ninezero.halo.feature.store.presentation.order_detail.components.isCancellable
import com.ninezero.halo.feature.store.presentation.order_detail.components.orderDetailSkeleton
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.util.concurrent.TimeUnit

@Composable
fun OrderDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit = {},
    onNavigateToCreateReview: (productId: Int, orderId: Int, productName: String, productImageUrl: String?) -> Unit = { _, _, _, _ -> },
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    // 복귀 시 자동 새로고침, 첫 진입은 스킵
    // saveable 가드로 복귀 새로고침 보장
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
        onUpdateCancelReasonEtc = { viewModel.onIntent(OrderDetailIntent.UpdateCancelReasonEtc(it)) },
        onProceedCancel = { viewModel.onIntent(OrderDetailIntent.ProceedCancelFromSheet) },
        onConfirmCancel = { viewModel.onIntent(OrderDetailIntent.ConfirmCancel) },
        onDismissCancel = { viewModel.onIntent(OrderDetailIntent.DismissCancelConfirm) },
        onRetry = { viewModel.onIntent(OrderDetailIntent.Load) },
        onWriteReview = { item -> viewModel.onIntent(OrderDetailIntent.WriteReview(item)) },
        onShippingInquiry = { item -> viewModel.onIntent(OrderDetailIntent.ShowShippingSheet(item)) },
        onAddToCart = { item -> viewModel.onIntent(OrderDetailIntent.AddToCart(item)) },
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
    onUpdateCancelReasonEtc: (String) -> Unit = {},
    onProceedCancel: () -> Unit = {},
    onConfirmCancel: () -> Unit = {},
    onDismissCancel: () -> Unit = {},
    onRetry: () -> Unit = {},
    onWriteReview: (OrderItem) -> Unit = {},
    onShippingInquiry: (OrderItem) -> Unit = {},
    onAddToCart: (OrderItem) -> Unit = {},
    onDismissShippingSheet: () -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val listState = rememberLazyListState()
    var headerHeight by rememberSaveableDp()
    val numberFormat = rememberKoreanNumberFormat()
    val order = state.order

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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onRetry,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
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
                            val elapsedDays = TimeUnit.MILLISECONDS.toDays(now - base).toInt()
                            val daysLeft =
                                Constants.Commerce.REVIEW_WRITE_DEADLINE_DAYS - elapsedDays
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
                            OrderSectionHeader("주문 상품")
                        }

                        creatorGroups.forEachIndexed { groupIndex, group ->
                            val creatorName = group.creatorName
                            val creatorAvatar = group.creatorAvatarUrl
                            val creatorId = group.creatorId ?: 0

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
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clip(RoundedCornerShape(12.dp))
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

                                    group.items.forEachIndexed { index, item ->
                                        OrderDetailItemRow(
                                            item = item,
                                            numberFormat = numberFormat,
                                            // 삭제된 상품은 진입 자체를 막음
                                            canWriteReview = canWriteReview && !item.hasReview && item.isProductActive,
                                            reviewDeadlineLabel = reviewDeadlineLabel,
                                            // 취소된 주문 그룹은 배송조회·장바구니 담기 액션을 숨김
                                            showActionButtons = group.status != OrderStatus.CANCELLED,
                                            // 영수증이라 라인 합계만
                                            showLineTotal = true,
                                            onWriteReview = { onWriteReview(item) },
                                            onShippingInquiry = { onShippingInquiry(item) },
                                            onAddToCart = { onAddToCart(item) }
                                        )
                                        if (index < group.items.lastIndex) {
                                            HorizontalDivider(
                                                thickness = 0.5.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                    alpha = 0.3f
                                                ),
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
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
                                            val groupRefundValue =
                                                group.refundAmount?.toLongOrNull()
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
                                    } else if (group.isCancellable() && order.paymentStatus != PaymentStatus.PENDING) {  // 결제 확인 중엔 청구 여부가 정해진 뒤
                                        HorizontalDivider(
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                        OutlinedButton(
                                            onClick = { group.creatorId?.let(onCancelOrder) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                                .height(36.dp)
                                                .bounceClick(),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(
                                                0.5.dp,
                                                MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            ),
                                            contentPadding = PaddingValues(0.dp),
                                            enabled = !state.isCancelling
                                        ) {
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = "주문 취소",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )

                                                if (state.isCancelling && state.cancelConfirmCreatorId == group.creatorId) {
                                                    LoadingIndicator(
                                                        modifier = Modifier
                                                            .align(Alignment.CenterEnd)
                                                            .padding(end = 12.dp),
                                                        size = LoadingIndicatorSize.Small,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
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
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                InfoRow("수취인", order.shippingName)
                                InfoRow("연락처", order.shippingPhone)
                                InfoRow("주소", order.shippingAddress)
                                if (order.memo != null) {
                                    InfoRow("배송메모", order.memo)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OrderSectionDivider()
                        }

                        // 결제 요약
                        item(key = "payment_summary") {
                            OrderSectionHeader("결제정보")
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                val originalTotal = displayOrderItems.sumOf { item ->
                                    val orig = item.originalPrice?.toLongOrNull()
                                        ?: (item.price.toLongOrNull() ?: 0)
                                    orig * item.quantity
                                }
                                val subtotal =
                                    displayOrderItems.sumOf { it.subtotal.toLongOrNull() ?: 0 }
                                val productDiscount = originalTotal - subtotal
                                InfoRow("상품금액", "${numberFormat.format(originalTotal)}원")
                                if (productDiscount > 0) {
                                    InfoRow("상품할인", "-${numberFormat.format(productDiscount)}원")
                                }
                                displayCreatorGroups.forEach { group ->
                                    val shippingFee = group.shippingFee.toLongOrNull() ?: 0
                                    val label = if (displayCreatorGroups.size == 1) {
                                        "배송비"
                                    } else {
                                        "배송비 · ${group.creatorName ?: "스토어"}"
                                    }
                                    InfoRow(
                                        label,
                                        if (shippingFee > 0) "${numberFormat.format(shippingFee)}원" else "무료"
                                    )
                                }
                                val points = order.pointsUsed.toLongOrNull() ?: 0
                                if (points > 0) {
                                    InfoRow("포인트 사용", "-${numberFormat.format(points)}원")
                                }
                                val coupon = order.couponDiscount.toLongOrNull() ?: 0
                                if (coupon > 0) {
                                    InfoRow("쿠폰할인", "-${numberFormat.format(coupon)}원")
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "최종 결제금액",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${numberFormat.format(order.totalPrice.toLongOrNull() ?: 0)}원",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (order.paymentProvider != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    InfoRow(
                                        "결제방식",
                                        getPaymentProviderText(order.paymentProvider)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 적립 포인트
                        if (order.status != OrderStatus.CANCELLED && order.paymentStatus != PaymentStatus.REFUNDED) {
                            item(key = "earn_points") {
                                OrderSectionDivider()
                                OrderEarnPointsSection(
                                    creatorGroups = order.creatorGroupsOrFallback(),
                                    numberFormat = numberFormat
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
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
            reasonEtc = state.cancelReasonEtc,
            isCancelling = state.isCancelling,
            numberFormat = numberFormat,
            onSelectReason = onSelectCancelReason,
            onUpdateReasonEtc = onUpdateCancelReasonEtc,
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

    if (state.showShippingSheet && state.order != null) {
        val selectedGroup = state.order.creatorGroupsOrFallback()
            .firstOrNull { it.creatorId == state.shippingSheetCreatorId }
            ?: state.order.creatorGroupsOrFallback().firstOrNull()
        ShippingTrackingBottomSheet(
            group = selectedGroup,
            shippingStatus = state.shippingStatus,
            onDismiss = onDismissShippingSheet
        )
    }
}

