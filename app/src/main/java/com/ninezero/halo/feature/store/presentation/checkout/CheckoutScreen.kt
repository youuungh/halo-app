package com.ninezero.halo.feature.store.presentation.checkout

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.BuildConfig
import com.ninezero.halo.core.ui.components.bottomsheet.WebViewAuthBottomSheet
import com.ninezero.halo.core.ui.components.button.PrimaryButton
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.list.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.list.ListErrorState
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.text.AnimatedCounter
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.toFormattedPrice
import com.ninezero.halo.feature.address.domain.model.Address
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.store.domain.model.calcCouponBenefit
import com.ninezero.halo.feature.store.domain.model.calcMaxUsablePoints
import com.ninezero.halo.feature.store.domain.model.calcSingleCouponDiscount
import com.ninezero.halo.feature.store.domain.model.calcStorePricing
import com.ninezero.halo.feature.store.domain.model.calcUsablePointsLimit
import com.ninezero.halo.feature.store.domain.model.earnRatePercent
import com.ninezero.halo.feature.store.domain.model.isCouponApplicable
import com.ninezero.halo.feature.store.domain.model.isFreeShippingReached
import com.ninezero.halo.feature.store.presentation.checkout.components.CardPaymentBottomSheet
import com.ninezero.halo.feature.store.presentation.checkout.components.CheckoutCreatorCard
import com.ninezero.halo.feature.store.presentation.checkout.components.CheckoutSectionTitle
import com.ninezero.halo.feature.store.presentation.checkout.components.CouponBoxState
import com.ninezero.halo.feature.store.presentation.checkout.components.CouponSelectBottomSheet
import com.ninezero.halo.feature.store.presentation.checkout.components.PaymentAmountSection
import com.ninezero.halo.feature.store.presentation.checkout.components.PaymentProviderSection
import com.ninezero.halo.feature.store.presentation.checkout.components.PointSection
import com.ninezero.halo.feature.store.presentation.checkout.components.checkoutSkeleton
import com.ninezero.halo.feature.store.presentation.components.calcMembershipBreakdown
import com.tosspayments.paymentsdk.TossPayments
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.model.paymentinfo.TossCardPaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossMobilePaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentInfo
import com.tosspayments.paymentsdk.model.paymentinfo.TossPaymentMethod
import com.tosspayments.paymentsdk.model.paymentinfo.TossTransferPaymentInfo
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
    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
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
    val maxUsablePoints = remember(subtotal, orderTotal) { calcUsablePointsLimit(subtotal, orderTotal) }
    val isRateLimited = maxUsablePoints == calcMaxUsablePoints(subtotal)
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

    val membership = remember(orderItems, pricing, state.subscribedCreatorIds, state.creatorTiers) {
        calcMembershipBreakdown(
            items = orderItems,
            pricing = pricing,
            subscribedCreatorIds = state.subscribedCreatorIds,
            tierOf = { creatorId -> state.creatorTiers[creatorId]?.takeIf { creatorId in state.subscribedCreatorIds } }
        )
    }
    val couponStateByCreator = remember(
        orderItems,
        state.availableCoupons,
        state.selectedCoupons,
        state.isCouponLoading,
        state.couponError,
        state.subscribedCreatorIds
    ) {
        orderCreatorIds.associateWith { creatorId ->
            val applied = state.selectedCoupons[creatorId]
            val usableCount = state.availableCoupons.count { coupon ->
                coupon.creatorId == creatorId && isCouponApplicable(coupon, orderItems, state.subscribedCreatorIds)
            }
            when {
                applied != null -> CouponBoxState.Applied(
                    couponName = applied.name,
                    amount = calcCouponBenefit(applied, orderAmountByCreatorId[creatorId] ?: 0L)
                )
                state.isCouponLoading -> CouponBoxState.Loading
                state.couponError != null -> CouponBoxState.Failed
                usableCount > 0 -> CouponBoxState.Available(usableCount)
                else -> CouponBoxState.None
            }
        }
    }

    if (state.showCouponSheet) {
        CouponSelectBottomSheet(
            coupons = state.availableCoupons,
            orderAmountByCreatorId = orderAmountByCreatorId,
            couponInfoByCreator = couponInfoByCreator,
            draftSelectedCoupons = state.draftSelectedCoupons,
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
                        ListErrorState(
                            title = "주문 정보를 불러오지 못했습니다",
                            reason = state.error,
                            onRetry = onRetry
                        )
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
                            // '변경' 글자 끝을 카드 끝선에 맞춤
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
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
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .bounceClick()
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = onNavigateToAddressManage
                                            )
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                        item(key = "shipping_form") {
                            ShippingAddressContent(
                                selectedAddress = state.selectedAddress,
                                onNavigateToAddressManage = onNavigateToAddressManage
                            )
                        }

                        // 주문 상품
                        item(key = "order_header") {
                            CheckoutSectionTitle(title = "주문 상품", count = orderItems.size)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        val groupedItems = orderItems.groupBy { it.product.creator.id }

                        groupedItems.entries.forEachIndexed { groupIndex, (creatorId, items) ->
                            val creator = items.first().product.creator
                            val isSubscribed = creatorId in state.subscribedCreatorIds
                            val appliedCoupon = state.selectedCoupons[creatorId]

                            item(key = "creator_group_$creatorId") {
                                CheckoutCreatorCard(
                                    creator = creator,
                                    items = items,
                                    isSubscribed = isSubscribed,
                                    earnRatePercent = earnRatePercent(
                                        state.creatorTiers[creatorId]?.takeIf { isSubscribed }
                                    ),
                                    shippingFee = pricing.shipping.finalShippingFeeByCreator[creatorId] ?: 0L,
                                    couponDiscount = appliedCoupon
                                        ?.let { calcSingleCouponDiscount(it, items.sumOf { line -> line.subtotal }) }
                                        ?: 0L,
                                    couponState = couponStateByCreator[creatorId] ?: CouponBoxState.None,
                                    numberFormat = numberFormat,
                                    onCouponClick = onShowCouponSheet,
                                    modifier = Modifier.padding(top = if (groupIndex == 0) 0.dp else 12.dp)
                                )
                            }
                        }

                        // 결제수단
                        if (!isFreeOrder) {
                            item(key = "payment_method") {
                                Spacer(modifier = Modifier.height(28.dp))
                                CheckoutSectionTitle(title = "결제수단")
                                Spacer(modifier = Modifier.height(12.dp))
                                PaymentProviderSection(
                                    selectedProvider = state.selectedPaymentProvider,
                                    savedCard = state.savedCard,
                                    onShowCardSheet = onShowCardSheet,
                                    onSelectProvider = onSelectPaymentProvider
                                )
                            }
                        }

                        // 포인트
                        item(key = "point_section") {
                            Spacer(modifier = Modifier.height(28.dp))
                            CheckoutSectionTitle(
                                title = "포인트",
                                trailing = "보유 ${numberFormat.format(state.pointBalance?.totalPoints ?: 0)}P"
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            PointSection(
                                pointBalance = state.pointBalance,
                                usedPoints = state.usedPoints,
                                maxUsablePoints = maxUsablePoints,
                                isRateLimited = isRateLimited,
                                validationMessage = state.pointValidationMessage,
                                numberFormat = numberFormat,
                                onUpdateUsedPoints = onUpdateUsedPoints,
                                onUseAllPoints = onUseAllPoints,
                                bottomBarHeight = bottomBarHeight
                            )
                        }

                        // 결제 금액
                        item(key = "payment_amount") {
                            Spacer(modifier = Modifier.height(28.dp))
                            CheckoutSectionTitle(title = "결제 금액")
                            Spacer(modifier = Modifier.height(12.dp))
                            PaymentAmountSection(
                                productAmount = subtotal + pricing.productDiscount,
                                productDiscount = pricing.productDiscount,
                                couponDiscount = couponDiscount,
                                shippingFee = shippingFee + membership.freeShippingAmount,
                                shippingCouponDiscount = shippingDiscount,
                                membership = membership,
                                usedPoints = state.usedPoints,
                                paymentAmount = paymentAmount,
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
                    .onGloballyPositioned { bottomBarHeight = with(density) { it.size.height.toDp() } }
                    .haloBlur(hazeState, HaloBlurs.fadeUp(backgroundColor))
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                PrimaryButton(
                    onClick = { if (!state.isProcessing) onPlaceOrder() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedCounter(
                            count = paymentAmount.coerceAtLeast(0).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            suffix = if (isFreeOrder) "원 주문하기" else "원 결제하기",
                            shiftDigits = false,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        if (state.isProcessing) {
                            LoadingIndicator(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                size = LoadingIndicatorSize.Small,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            bottomBarPadding = if (orderLines != null) 60.dp else 0.dp  // 버튼 48 + 아래 여백 12
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

/** 배송지 칸, 없으면 등록 안내 */
@Composable
private fun ShippingAddressContent(
    selectedAddress: Address?,
    onNavigateToAddressManage: () -> Unit
) {
    if (selectedAddress != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .cardBorder(MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            Text(
                text = selectedAddress.recipientName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = selectedAddress.recipientPhone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            val fullAddress = buildString {
                append(selectedAddress.address)
                if (!selectedAddress.addressDetail.isNullOrBlank()) {
                    append(" ${selectedAddress.addressDetail}")
                }
                append(" (${selectedAddress.zipCode})")
            }
            Text(
                text = fullAddress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!selectedAddress.memo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "배송 메모: ${selectedAddress.memo}",
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
                .cardBorder(MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium)
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
