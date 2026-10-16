package com.ninezero.halo.feature.store.presentation.checkout

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.BuildConfig
import com.ninezero.halo.core.ui.components.bottomsheet.WebViewAuthBottomSheet
import com.ninezero.halo.core.ui.components.button.GlassActionButton
import com.ninezero.halo.core.ui.components.counter.AnimatedCounter
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.toFormattedPrice
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.calcMaxUsablePoints
import com.ninezero.halo.feature.store.domain.model.calcSingleCouponDiscount
import com.ninezero.halo.feature.store.domain.model.calcStorePricing
import com.ninezero.halo.feature.store.domain.model.calcUsablePointsCap
import com.ninezero.halo.feature.store.domain.model.isFreeShippingReached
import com.ninezero.halo.feature.store.presentation.checkout.components.CardPaymentBottomSheet
import com.ninezero.halo.feature.store.presentation.checkout.components.CouponSection
import com.ninezero.halo.feature.store.presentation.checkout.components.CouponSelectBottomSheet
import com.ninezero.halo.feature.store.presentation.checkout.components.EarnPointsSection
import com.ninezero.halo.feature.store.presentation.checkout.components.OrderSummarySection
import com.ninezero.halo.feature.store.presentation.checkout.components.PaymentProviderSection
import com.ninezero.halo.feature.store.presentation.checkout.components.PointSection
import com.ninezero.halo.feature.store.presentation.checkout.components.checkoutSkeleton
import com.ninezero.halo.feature.store.presentation.components.OrderCreatorGroupHeader
import com.ninezero.halo.feature.store.presentation.components.OrderDetailItemRow
import com.tosspayments.paymentsdk.TossPayments
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.model.paymentinfo.TossCardPaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossMobilePaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentMethod
import com.tosspayments.paymentsdk.model.paymentinfo.TossTransferPaymentInfo
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrderComplete: (Int, String) -> Unit,
    onNavigateToAddressManage: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = LocalActivity.current
    val tossPayments = remember { TossPayments(BuildConfig.TOSS_CLIENT_KEY) }
    // 토스 표준 결제창 결과 수신
    val tossLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        when (result.resultCode) {
            TossPayments.RESULT_PAYMENT_SUCCESS -> {
                val success = data?.let {
                    IntentCompat.getParcelableExtra(
                        it,
                        TossPayments.EXTRA_PAYMENT_RESULT_SUCCESS,
                        TossPaymentResult.Success::class.java
                    )
                }
                if (success != null) {
                    viewModel.onIntent(CheckoutIntent.TossPaymentSucceeded(success.paymentKey))
                } else {
                    viewModel.onIntent(CheckoutIntent.TossPaymentFailed("결제 결과를 확인할 수 없습니다"))
                }
            }
            TossPayments.RESULT_PAYMENT_FAILED -> {
                val fail = data?.let {
                    IntentCompat.getParcelableExtra(
                        it,
                        TossPayments.EXTRA_PAYMENT_RESULT_FAILED,
                        TossPaymentResult.Fail::class.java
                    )
                }
                viewModel.onIntent(CheckoutIntent.TossPaymentFailed(fail?.errorMessage ?: "결제에 실패했습니다"))
            }
            else -> viewModel.onIntent(CheckoutIntent.TossPaymentCanceled)
        }
    }
    var hasInitialResumeHandled by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasInitialResumeHandled) {
            viewModel.onIntent(CheckoutIntent.RefreshAddresses)
        } else {
            hasInitialResumeHandled = true
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CheckoutSideEffect.NavigateBack -> onNavigateBack()
            is CheckoutSideEffect.NavigateToOrderComplete -> onNavigateToOrderComplete(sideEffect.orderId, sideEffect.orderNumber)
            is CheckoutSideEffect.NavigateToAddressManage -> onNavigateToAddressManage()
            is CheckoutSideEffect.ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
            is CheckoutSideEffect.LaunchTossPayment -> {
                if (activity != null) {
                    val orderId = sideEffect.orderId
                    val orderName = sideEffect.orderName
                    val amount = sideEffect.amount
                    when (sideEffect.method) {
                        TossPaymentMethodType.CARD -> tossPayments.requestCardPayment(
                            activity, TossCardPaymentInfo(orderId, orderName, amount), tossLauncher
                        )
                        TossPaymentMethodType.TOSSPAY -> tossPayments.requestPayment(
                            activity, TossPaymentMethod.TossPay, TossPaymentInfo(orderId, orderName, amount), tossLauncher
                        )
                        TossPaymentMethodType.TRANSFER -> tossPayments.requestTransferPayment(
                            activity, TossTransferPaymentInfo(orderId, orderName, amount), tossLauncher
                        )
                        TossPaymentMethodType.MOBILE -> tossPayments.requestMobilePayment(
                            activity, TossMobilePaymentInfo(orderId, orderName, amount), tossLauncher
                        )
                    }
                } else {
                    viewModel.onIntent(CheckoutIntent.TossPaymentFailed("결제를 시작할 수 없습니다"))
                }
            }
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(CheckoutIntent.SnackbarDismissed) }
    )

    CheckoutScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = { viewModel.onIntent(CheckoutIntent.NavigateBack) },
        onPlaceOrder = { viewModel.onIntent(CheckoutIntent.PlaceOrder) },
        onRetry = { viewModel.onIntent(CheckoutIntent.LoadCheckout) },
        onNavigateToAddressManage = { viewModel.onIntent(CheckoutIntent.NavigateToAddressManage) },
        onShowCouponSheet = { viewModel.onIntent(CheckoutIntent.ShowCouponSheet) },
        onDismissCouponSheet = { viewModel.onIntent(CheckoutIntent.DismissCouponSheet) },
        onToggleCoupon = { viewModel.onIntent(CheckoutIntent.ToggleCoupon(it)) },
        onUpdateCouponCode = { viewModel.onIntent(CheckoutIntent.UpdateCouponCode(it)) },
        onClaimCoupon = { viewModel.onIntent(CheckoutIntent.ClaimCoupon) },
        onApplyCoupons = { viewModel.onIntent(CheckoutIntent.ApplyCoupons) },
        onRetryCoupons = { viewModel.onIntent(CheckoutIntent.RetryCoupons) },
        onSelectPaymentProvider = { viewModel.onIntent(CheckoutIntent.SelectPaymentProvider(it)) },
        onShowCardSheet = { viewModel.onIntent(CheckoutIntent.ShowCardSheet) },
        onUpdateUsedPoints = { viewModel.onIntent(CheckoutIntent.UpdateUsedPoints(it)) },
        onUseAllPoints = { viewModel.onIntent(CheckoutIntent.UseAllPoints) }
    )

    // 신용·체크카드 결제 선택 시트
    if (state.showCardSheet) {
        CardPaymentBottomSheet(
            savedCard = state.savedCard,
            isLoading = state.isSavedCardLoading,
            draftSelection = state.cardSheetDraft,
            error = state.cardSheetError,
            onSelectOption = { viewModel.onIntent(CheckoutIntent.SelectCardSheetOption(it)) },
            onAddCard = { viewModel.onIntent(CheckoutIntent.AddSavedCard) },
            onConfirm = { viewModel.onIntent(CheckoutIntent.ConfirmCardSheet) },
            onErrorDismiss = { viewModel.onIntent(CheckoutIntent.DismissCardSheetError) },
            onDismiss = { viewModel.onIntent(CheckoutIntent.DismissCardSheet) }
        )
    }

    // 저장카드 등록 WebView
    val savedCardUrl = state.savedCardRegisterUrl
    if (state.showSavedCardRegister && savedCardUrl != null) {
        WebViewAuthBottomSheet(
            url = savedCardUrl,
            title = "카드 등록",
            allowedHostSuffixes = listOf(
                Constants.BASE_URL.toUri().host ?: "10.0.2.2",
                "tosspayments.com"
            ),
            returnScheme = "haloapp",
            onReturn = { uri ->
                val authKey = uri.getQueryParameter("authKey")
                if (authKey != null) {
                    viewModel.onIntent(CheckoutIntent.SavedCardAuthReturned(authKey))
                } else {
                    viewModel.onIntent(
                        CheckoutIntent.SavedCardAuthFailed(uri.getQueryParameter("code"), uri.getQueryParameter("message"))
                    )
                }
            },
            onDismiss = { viewModel.onIntent(CheckoutIntent.SavedCardRegisterDismissed) }
        )
    }
}

@Composable
internal fun CheckoutScreenContent(
    state: CheckoutState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateBack: () -> Unit = {},
    onPlaceOrder: () -> Unit = {},
    onRetry: () -> Unit = {},
    onNavigateToAddressManage: () -> Unit = {},
    onShowCouponSheet: () -> Unit = {},
    onDismissCouponSheet: () -> Unit = {},
    onToggleCoupon: (Coupon) -> Unit = {},
    onUpdateCouponCode: (String) -> Unit = {},
    onClaimCoupon: () -> Unit = {},
    onApplyCoupons: () -> Unit = {},
    onRetryCoupons: () -> Unit = {},
    onSelectPaymentProvider: (String) -> Unit = {},
    onShowCardSheet: () -> Unit = {},
    onUpdateUsedPoints: (String) -> Unit = {},
    onUseAllPoints: () -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    var headerHeight by rememberSaveableDp()
    val numberFormat = rememberKoreanNumberFormat()
    val orderLines = state.orderLines
    val orderItems = orderLines.orEmpty()
    val orderCreatorIds = remember(orderItems) {
        orderItems.mapTo(mutableSetOf()) { it.product.creator.id }
    }
    val orderAmountByCreatorId = remember(orderItems) {
        orderItems
            .groupBy { it.product.creator.id }
            .mapValues { (_, items) -> items.sumOf { it.subtotal } }
    }
    val pricing = remember(orderItems, state.subscribedCreatorIds, state.selectedCoupons) {
        calcStorePricing(
            items = orderItems,
            subscribedCreatorIds = state.subscribedCreatorIds,
            selectedCoupons = state.selectedCoupons
        )
    }
    val subtotal = pricing.subtotal
    val shippingFee = pricing.shipping.feeBeforeCoupon
    val couponDiscount = state.couponDiscount
    val shippingDiscount = pricing.shipping.freeShipDiscount
    val orderTotal = pricing.totalBeforePoints
    val paymentAmount = orderTotal - state.usedPoints
    val isFreeOrder = paymentAmount <= 0L  // 포인트·쿠폰으로 전액, 결제수단 없이 주문
    // 정책 상한과 결제 금액 둘 다 상한
    val maxUsablePoints = remember(subtotal, orderTotal) { calcUsablePointsCap(subtotal, orderTotal) }
    val isRateCapped = maxUsablePoints == calcMaxUsablePoints(subtotal)
    val couponInfoByCreator = remember(orderItems, orderCreatorIds, state.subscribedCreatorIds) {
        val creatorNamesById = orderItems
            .groupBy { it.product.creator.id }
            .mapValues { (_, items) ->
                val creator = items.first().product.creator
                creator.displayName ?: creator.username
            }
        val freeShipByCreator = orderCreatorIds
            .associateWith { creatorId ->
                when {
                    creatorId in state.subscribedCreatorIds -> "멤버십 무료배송 적용 중"
                    isFreeShippingReached(orderAmountByCreatorId[creatorId] ?: 0L) ->
                        "${Constants.Commerce.FREE_SHIPPING_THRESHOLD.toFormattedPrice()} 이상 주문으로 무료배송 적용"
                    else -> ""
                }
            }
            .filterValues { it.isNotBlank() }
        creatorNamesById.mapValues { (creatorId, creatorName) ->
            CouponCreatorInfo(
                name = creatorName,
                freeShipBlockReason = freeShipByCreator[creatorId]
            )
        }
    }

    if (state.showCouponSheet) {
        CouponSelectBottomSheet(
            coupons = state.availableCoupons,
            orderCreatorIds = orderCreatorIds,
            orderAmountByCreatorId = orderAmountByCreatorId,
            couponInfoByCreator = couponInfoByCreator,
            draftSelectedCoupons = state.draftSelectedCoupons,
            canApply = state.draftSelectedCoupons != state.selectedCoupons,
            couponCode = state.couponCode,
            isClaiming = state.isClaiming,
            isLoading = state.isCouponLoading,
            error = state.couponError,
            onToggleCoupon = onToggleCoupon,
            onUpdateCouponCode = onUpdateCouponCode,
            onClaimCoupon = onClaimCoupon,
            onApply = onApplyCoupons,
            onDismiss = onDismissCouponSheet,
            onRetry = onRetryCoupons
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }
    ) {
        HaloPullToRefreshBox(
            isRefreshing = state.isLoading && orderLines != null,
            onRefresh = onRetry,
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
            when {
                // 로딩 상태
                state.isLoading && orderLines == null -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 100.dp)
                    ) {
                        checkoutSkeleton()
                    }
                }
                // 에러 상태
                state.error != null && orderLines == null -> {
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
                // 컨텐츠
                orderLines != null -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 100.dp)
                    ) {
                        // 배송지
                        item(key = "shipping_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "배송지",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (state.addresses.isNotEmpty()) {
                                    Text(
                                        text = "변경",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .bounceClick()
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = onNavigateToAddressManage
                                            )
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                        item(key = "shipping_form") {
                            if (state.selectedAddress != null) {
                                val addr = state.selectedAddress
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .border(
                                            width = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = addr.recipientName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = addr.recipientPhone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val fullAddress = buildString {
                                        append(addr.address)
                                        if (!addr.addressDetail.isNullOrBlank()) {
                                            append(" ${addr.addressDetail}")
                                        }
                                        append(" (${addr.zipCode})")
                                    }
                                    Text(
                                        text = fullAddress,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!addr.memo.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "배송 메모: ${addr.memo}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .border(
                                            width = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(onClick = onNavigateToAddressManage)
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+ 배송지 추가",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // 주문 상품
                        item(key = "order_header") {
                            Text(
                                text = "주문 상품",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        val groupedItems = orderItems.groupBy { it.product.creator.id }

                        groupedItems.entries.forEachIndexed { groupIndex, (creatorId, items) ->
                            val creator = items.first().product.creator
                            val groupSubtotal = items.sumOf { it.subtotal }

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
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    OrderCreatorGroupHeader(
                                        creatorName = creator.displayName ?: creator.username,
                                        creatorAvatarUrl = creator.avatarThumbUrl ?: creator.avatarUrl,
                                        creatorRole = creator.role,
                                        isSubscribed = creatorId in state.subscribedCreatorIds
                                    )
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )

                                    items.forEachIndexed { index, line ->
                                        OrderDetailItemRow(
                                            item = OrderItem(
                                                id = line.product.id,
                                                productId = line.product.id,
                                                productName = line.product.name,
                                                productImageUrl = line.product.imageUrls.firstOrNull(),
                                                price = line.product.price,
                                                originalPrice = line.product.originalPrice,
                                                quantity = line.quantity,
                                                subtotal = line.subtotal.toString()
                                            ),
                                            numberFormat = numberFormat,
                                            showActionButtons = false,
                                            showPrice = false,
                                            showLineTotal = true
                                        )
                                        if (index < items.lastIndex) {
                                            HorizontalDivider(
                                                thickness = 0.5.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    }

                                    // 그룹 소계, 쿠폰 반영
                                    val appliedCoupon = state.selectedCoupons[creatorId]
                                    val groupCouponDiscount = appliedCoupon
                                        ?.let { calcSingleCouponDiscount(it, groupSubtotal) }
                                        ?: 0L
                                    val discountedSubtotal = (groupSubtotal - groupCouponDiscount).coerceAtLeast(0L)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 12.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "상품금액",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (groupCouponDiscount > 0L) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "쿠폰 적용 -${numberFormat.format(groupCouponDiscount)}원",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        if (groupCouponDiscount > 0L) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "${numberFormat.format(groupSubtotal)}원",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textDecoration = TextDecoration.LineThrough
                                                )
                                                Text(
                                                    text = "${numberFormat.format(discountedSubtotal)}원",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "${numberFormat.format(groupSubtotal)}원",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 결제수단
                        if (!isFreeOrder) {
                            item(key = "payment_method") {
                                Spacer(modifier = Modifier.height(12.dp))
                                PaymentProviderSection(
                                    selectedProvider = state.selectedPaymentProvider,
                                    savedCard = state.savedCard,
                                    onShowCardSheet = onShowCardSheet,
                                    onSelectProvider = onSelectPaymentProvider
                                )
                            }
                        }

                        // 할인/포인트
                        item(key = "discount_section") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .border(
                                        width = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                CouponSection(
                                    availableCount = state.availableCoupons.size,
                                    selectedCount = state.selectedCoupons.size,
                                    totalDiscount = state.couponDiscount + shippingDiscount,
                                    numberFormat = numberFormat,
                                    onShowCouponSheet = onShowCouponSheet
                                )
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                PointSection(
                                    pointBalance = state.pointBalance,
                                    usedPoints = state.usedPoints,
                                    maxUsablePoints = maxUsablePoints,
                                    isRateCapped = isRateCapped,
                                    validationMessage = state.pointValidationMessage,
                                    numberFormat = numberFormat,
                                    onUpdateUsedPoints = onUpdateUsedPoints,
                                    onUseAllPoints = onUseAllPoints
                                )
                            }
                        }

                        // 주문 요약
                        item(key = "order_summary") {
                            Spacer(modifier = Modifier.height(24.dp))
                            OrderSummarySection(
                                subtotal = subtotal,
                                shippingFee = shippingFee,
                                couponDiscount = couponDiscount,
                                shippingDiscount = shippingDiscount,
                                totalAmount = orderTotal,
                                numberFormat = numberFormat
                            )
                        }

                        // 적립 예정 포인트
                        item(key = "earn_points") {
                            Spacer(modifier = Modifier.height(8.dp))
                            EarnPointsSection(
                                orderItems = orderItems,
                                creatorTiers = state.creatorTiers,
                                numberFormat = numberFormat
                            )
                        }
                    }
                }
            }
        }

        HazeScreenHeader(
            title = "주문/결제",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = onNavigateBack,
            onHeightMeasured = { headerHeight = it }
        )

        // 하단 바
        if (orderLines != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .haloBlur(state = hazeState) {
                        style = HaloBlurStyle(
                            backgroundColor = backgroundColor,
                            tint = HaloBlurTint(backgroundColor),
                            noiseFactor = 0f
                        )
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0f,
                            endIntensity = 0.5f
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                GlassActionButton(
                    onClick = { if (!state.isProcessing) onPlaceOrder() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AnimatedCounter(
                            count = paymentAmount.coerceAtLeast(0).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            suffix = if (isFreeOrder) "원 주문하기" else "원 결제하기",
                            modifier = Modifier.align(Alignment.Center)
                        )

                        if (state.isProcessing) {
                            LoadingIndicator(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                size = LoadingIndicatorSize.Small,
                                color = MaterialTheme.colorScheme.inverseOnSurface
                            )
                        }
                    }
                }
            }
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            bottomBarPadding = if (orderLines != null) 64.dp else 0.dp
        )

        // 결제 중 터치·뒤로가기 차단, 나가면 요청 취소
        BackHandler(enabled = state.isProcessing) {}
        if (state.isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { /* consume */ }
                    }
            )
        }
    }
}
