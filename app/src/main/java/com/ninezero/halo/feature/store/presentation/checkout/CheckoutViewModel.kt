package com.ninezero.halo.feature.store.presentation.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ninezero.halo.BuildConfig
import com.ninezero.halo.core.local.PreferencesManager
import com.ninezero.halo.core.network.ApiException
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.address.domain.model.Address
import com.ninezero.halo.feature.address.domain.repository.AddressRepository
import com.ninezero.halo.feature.auth.domain.usecase.GetCurrentUserIdUseCase
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.feature.coupon.domain.repository.CouponRepository
import com.ninezero.halo.feature.point.domain.model.PointBalance
import com.ninezero.halo.feature.point.domain.repository.PointRepository
import com.ninezero.halo.feature.store.domain.model.CartItem
import com.ninezero.halo.feature.store.domain.model.CreateDirectOrderRequest
import com.ninezero.halo.feature.store.domain.model.CreateOrderRequest
import com.ninezero.halo.feature.store.domain.model.OrderLine
import com.ninezero.halo.feature.store.domain.model.PaymentStatus
import com.ninezero.halo.feature.store.domain.model.calcCouponDiscount
import com.ninezero.halo.feature.store.domain.model.calcStorePricing
import com.ninezero.halo.feature.store.domain.model.calcUsablePointsCap
import com.ninezero.halo.feature.store.domain.model.isCouponUsable
import com.ninezero.halo.feature.store.domain.model.isFreeShippingReached
import com.ninezero.halo.feature.store.domain.model.toOrderLine
import com.ninezero.halo.feature.store.domain.repository.BillingRepository
import com.ninezero.halo.feature.store.domain.repository.CartRepository
import com.ninezero.halo.feature.store.domain.repository.OrderRepository
import com.ninezero.halo.feature.store.domain.repository.PaymentRepository
import com.ninezero.halo.feature.store.domain.repository.ProductRepository
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.subscription.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val addressRepository: AddressRepository,
    private val couponRepository: CouponRepository,
    private val pointRepository: PointRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val billingRepository: BillingRepository,
    private val preferencesManager: PreferencesManager,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), OrbitContainerHost<CheckoutState, CheckoutState, CheckoutSideEffect> {

    private companion object {
        const val MIN_USABLE_POINTS = 100
        const val KEY_PENDING_TOSS_ORDER_ID = "pendingTossOrderId"
        const val KEY_PENDING_TOSS_ORDER_NUMBER = "pendingTossOrderNumber"
        const val KEY_PENDING_TOSS_AMOUNT = "pendingTossAmount"
    }

    private data class CheckoutLoadResult(
        val orderLines: List<OrderLine>,
        val selectedItemIds: List<Int>,
        val subscribedCreatorIds: Set<Int>,
        val creatorTiers: Map<Int, SubscriptionPlanTier>,
        val addresses: List<Address>,
        val selectedAddress: Address?,
        val defaultAddressId: Int?,
        val availableCoupons: List<Coupon>,
        val couponError: String?,
        val pointBalance: PointBalance,
        val selectedCoupons: Map<Int, Coupon>,
        val draftSelectedCoupons: Map<Int, Coupon>,
        val couponDiscount: Long,
        val usedPoints: Int,
        val pointValidationMessage: String?
    )

    private data class PointUsageResult(
        val usedPoints: Int,
        val message: String?
    )

    private val initialSelectedItemIds: List<Int> =
        savedStateHandle.get<IntArray>("selectedItemIds")?.toList() ?: emptyList()

    // 바로구매면 상품·수량, null이면 장바구니 주문
    private val directProductId: Int? = savedStateHandle.get<Int>("directProductId")?.takeIf { it > 0 }
    private val directQuantity: Int = savedStateHandle.get<Int>("directQuantity")?.takeIf { it > 0 } ?: 1

    override val container: OrbitContainer<CheckoutState, CheckoutState, CheckoutSideEffect> =
        orbitContainer(
            CheckoutState(
                selectedItemIds = initialSelectedItemIds,
                // 토스 결제창 중 프로세스가 죽어도 복귀 후 승인·취소할 주문
                pendingTossOrderId = savedStateHandle[KEY_PENDING_TOSS_ORDER_ID],
                pendingTossOrderNumber = savedStateHandle[KEY_PENDING_TOSS_ORDER_NUMBER],
                pendingTossAmount = savedStateHandle[KEY_PENDING_TOSS_AMOUNT]
            )
        )

    init {
        loadCheckoutData()
        restorePaymentProvider()
    }

    fun onIntent(intent: CheckoutIntent) {
        when (intent) {
            is CheckoutIntent.LoadCheckout -> loadCheckoutData()
            is CheckoutIntent.RefreshAddresses -> refreshAddressesOnly()
            is CheckoutIntent.PlaceOrder -> placeOrder()
            is CheckoutIntent.NavigateToAddressManage -> navigateToAddressManage()
            is CheckoutIntent.NavigateBack -> navigateBack()
            is CheckoutIntent.SnackbarDismissed -> dismissSnackbar()
            is CheckoutIntent.ShowCouponSheet -> showCouponSheet()
            is CheckoutIntent.DismissCouponSheet -> dismissCouponSheet()
            is CheckoutIntent.ToggleCoupon -> toggleCoupon(intent.coupon)
            is CheckoutIntent.UpdateCouponCode -> updateCouponCode(intent.code)
            is CheckoutIntent.ClaimCoupon -> claimCoupon()
            is CheckoutIntent.ApplyCoupons -> applyCoupons()
            is CheckoutIntent.RetryCoupons -> loadCoupons()
            is CheckoutIntent.SelectPaymentProvider -> selectPaymentProvider(intent.provider)
            is CheckoutIntent.UpdateUsedPoints -> updateUsedPoints(intent.points)
            is CheckoutIntent.UseAllPoints -> useAllPoints()
            is CheckoutIntent.TossPaymentSucceeded -> tossPaymentSucceeded(intent.paymentKey)
            is CheckoutIntent.TossPaymentFailed -> tossPaymentFailed(intent.message)
            is CheckoutIntent.TossPaymentCanceled -> tossPaymentCanceled()
            is CheckoutIntent.ShowCardSheet -> showCardSheet()
            is CheckoutIntent.DismissCardSheet -> dismissCardSheet()
            is CheckoutIntent.SelectCardSheetOption -> selectCardSheetOption(intent.key)
            is CheckoutIntent.ConfirmCardSheet -> confirmCardSheet()
            is CheckoutIntent.AddSavedCard -> addSavedCard()
            is CheckoutIntent.SavedCardAuthReturned -> savedCardAuthReturned(intent.authKey)
            is CheckoutIntent.SavedCardRegisterDismissed -> savedCardRegisterDismissed()
        }
    }

    private fun loadCheckoutData() = intent {
        val currentState = state
        reduce { state.copy(isLoading = true, error = null) }

        loadRequiredCheckoutData(currentState)
            .onSuccess { result ->
                reduce {
                    state.copy(
                        isLoading = false,
                        orderLines = result.orderLines,
                        selectedItemIds = result.selectedItemIds,
                        shippingName = result.selectedAddress?.recipientName ?: "",
                        shippingPhone = result.selectedAddress?.recipientPhone ?: "",
                        shippingAddress = result.selectedAddress?.let(::formatShippingAddress).orEmpty(),
                        memo = result.selectedAddress?.memo ?: "",
                        error = null,
                        selectedAddress = result.selectedAddress,
                        selectedAddressId = result.selectedAddress?.id,
                        defaultAddressId = result.defaultAddressId,
                        addresses = result.addresses,
                        availableCoupons = result.availableCoupons,
                        selectedCoupons = result.selectedCoupons,
                        draftSelectedCoupons = result.draftSelectedCoupons,
                        couponDiscount = result.couponDiscount,
                        isCouponLoading = false,
                        couponError = result.couponError,
                        subscribedCreatorIds = result.subscribedCreatorIds,
                        creatorTiers = result.creatorTiers,
                        pointBalance = result.pointBalance,
                        usedPoints = result.usedPoints,
                        pointValidationMessage = result.pointValidationMessage
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        orderLines = null,
                        selectedItemIds = initialSelectedItemIds,
                        shippingName = "",
                        shippingPhone = "",
                        shippingAddress = "",
                        memo = "",
                        error = error.message ?: "주문 정보를 불러올 수 없습니다",
                        selectedAddress = null,
                        selectedAddressId = null,
                        defaultAddressId = null,
                        addresses = emptyList(),
                        availableCoupons = emptyList(),
                        selectedCoupons = emptyMap(),
                        draftSelectedCoupons = emptyMap(),
                        couponDiscount = 0,
                        isCouponLoading = false,
                        couponError = null,
                        subscribedCreatorIds = emptySet(),
                        creatorTiers = emptyMap(),
                        pointBalance = null,
                        usedPoints = 0,
                        pointValidationMessage = null
                    )
                }
            }
    }

    private fun loadCoupons() = intent {
        reduce { state.copy(isCouponLoading = true, couponError = null) }
        loadAvailableCoupons()
            .onSuccess { coupons ->
                reduce {
                    state.copy(
                        isCouponLoading = false,
                        availableCoupons = coupons
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isCouponLoading = false,
                        couponError = error.message ?: "쿠폰을 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun showCouponSheet() = intent {
        reduce {
            state.copy(
                showCouponSheet = true,
                draftSelectedCoupons = state.selectedCoupons
            )
        }
    }

    private fun dismissCouponSheet() = intent {
        reduce {
            state.copy(
                showCouponSheet = false,
                draftSelectedCoupons = emptyMap()
            )
        }
    }

    private fun toggleCoupon(coupon: Coupon) = intent {
        val orderItems = state.orderLines.orEmpty()
        val orderCreatorIds = orderItems.mapTo(mutableSetOf()) { it.product.creator.id }
        if (coupon.creatorId !in orderCreatorIds) {
            reduce {
                state.copy(
                    snackbarState = SnackbarState("특정 크리에이터 상품에만 사용 가능합니다")
                )
            }
            return@intent
        }
        if (!isCouponUsable(coupon, creatorOrderAmount(orderItems, coupon.creatorId))) {
            reduce {
                state.copy(
                    snackbarState = SnackbarState("최소 주문 금액을 충족하지 않습니다")
                )
            }
            return@intent
        }
        if (isFreeShipCouponBlocked(coupon, orderItems, state.subscribedCreatorIds)) {
            reduce {
                state.copy(
                    snackbarState = SnackbarState("이미 무료배송이 적용되어 무료배송 쿠폰은 사용할 수 없습니다")
                )
            }
            return@intent
        }

        val currentSelected = state.draftSelectedCoupons.toMutableMap()
        val creatorId = coupon.creatorId

        if (currentSelected[creatorId]?.id == coupon.id) {
            currentSelected.remove(creatorId)
        } else {
            currentSelected[creatorId] = coupon
        }

        reduce {
            state.copy(
                draftSelectedCoupons = currentSelected
            )
        }
    }

    private fun updateCouponCode(code: String) = blockingIntent {
        reduce { state.copy(couponCode = code) }
    }

    private fun claimCoupon() = intent {
        if (state.couponCode.isBlank()) return@intent
        reduce { state.copy(isClaiming = true) }

        couponRepository.claimCoupon(state.couponCode)
            .onSuccess {
                reduce { state.copy(couponCode = "", isClaiming = false) }
                loadCoupons()
            }
            .onFailure { error ->
                reduce { state.copy(isClaiming = false) }
                postSideEffect(CheckoutSideEffect.ShowToast(error.message ?: "쿠폰 등록 실패"))
            }
    }

    private fun applyCoupons() = intent {
        val orderItems = state.orderLines.orEmpty()
        val orderCreatorIds = orderItems.mapTo(mutableSetOf()) { it.product.creator.id }
        val filteredCoupons = filterCouponsForCurrentOrder(
            coupons = state.draftSelectedCoupons,
            orderItems = orderItems,
            orderCreatorIds = orderCreatorIds,
            subscribedCreatorIds = state.subscribedCreatorIds
        )
        val removedInvalidCoupon = filteredCoupons.size != state.draftSelectedCoupons.size
        val discount = calcCouponDiscount(filteredCoupons, orderItems)
        // 쿠폰이 붙으면 결제 금액이 줄어 기존 포인트가 초과될 수 있음
        val pointUsage = limitUsedPoints(
            requestedPoints = state.usedPoints,
            orderItems = orderItems,
            pointBalance = state.pointBalance?.totalPoints,
            selectedCoupons = filteredCoupons,
            subscribedCreatorIds = state.subscribedCreatorIds
        )
        reduce {
            state.copy(
                selectedCoupons = filteredCoupons,
                draftSelectedCoupons = emptyMap(),
                couponDiscount = discount,
                usedPoints = pointUsage.usedPoints,
                pointValidationMessage = pointUsage.message,
                showCouponSheet = false,
                snackbarState = if (removedInvalidCoupon) {
                    SnackbarState("적용할 수 없는 쿠폰은 제외되었습니다")
                } else {
                    state.snackbarState
                }
            )
        }
    }

    private fun selectPaymentProvider(provider: String) = intent {
        reduce { state.copy(selectedPaymentProvider = provider) }
        persistPaymentProvider(provider)
    }

    // 신용·체크카드 시트

    private fun showCardSheet() = intent {
        val draft = state.selectedPaymentProvider.takeIf { it == "TOSS_BILLING" || it == "TOSS_CARD" }
        reduce { state.copy(showCardSheet = true, cardSheetDraft = draft) }
        loadSavedCard(openRegisterIfNone = false)
    }

    private fun dismissCardSheet() = intent {
        reduce { state.copy(showCardSheet = false) }
    }

    private fun selectCardSheetOption(key: String) = intent {
        reduce { state.copy(cardSheetDraft = key) }
    }

    private fun confirmCardSheet() = intent {
        val draft = state.cardSheetDraft ?: return@intent
        reduce { state.copy(selectedPaymentProvider = draft, showCardSheet = false) }
        persistPaymentProvider(draft)
    }

    private fun restorePaymentProvider() = intent {
        val userId = getCurrentUserIdUseCase() ?: return@intent
        val saved = preferencesManager.getPaymentProvider(userId) ?: return@intent
        reduce { state.copy(selectedPaymentProvider = saved) }
        // 저장카드가 마지막 선택이면 카드 상태를 미리 동기화
        if (saved == "TOSS_BILLING") {
            loadSavedCard(openRegisterIfNone = false)
        }
    }

    private suspend fun persistPaymentProvider(provider: String) {
        val userId = getCurrentUserIdUseCase() ?: return
        preferencesManager.setPaymentProvider(userId, provider)
    }

    private fun loadSavedCard(openRegisterIfNone: Boolean) = intent {
        reduce { state.copy(isSavedCardLoading = true) }
        billingRepository.getMyBillingKey()
            .onSuccess { card ->
                reduce {
                    state.copy(
                        isSavedCardLoading = false,
                        savedCard = card.takeIf { it.registered }
                    )
                }
                if (openRegisterIfNone && !card.registered) {
                    addSavedCard()
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isSavedCardLoading = false,
                        snackbarState = SnackbarState(e.message ?: "결제수단을 불러오지 못했습니다")
                    )
                }
            }
    }

    private fun addSavedCard() = intent {
        val customerKey = billingRepository.getCustomerKey().getOrElse { e ->
            reduce { state.copy(snackbarState = SnackbarState(e.message ?: "결제 준비에 실패했습니다")) }
            return@intent
        }
        reduce {
            state.copy(
                savedCardRegisterUrl = Constants.billingAuthUrl(customerKey),
                showSavedCardRegister = true
            )
        }
    }

    private fun savedCardAuthReturned(authKey: String?) = intent {
        reduce { state.copy(showSavedCardRegister = false) }
        if (authKey == null) return@intent
        billingRepository.issueBillingKey(authKey)
            .onSuccess { card ->
                // 등록 성공 시 시트에서 저장카드를 자동 선택 상태로
                reduce {
                    state.copy(
                        savedCard = card.takeIf { it.registered },
                        cardSheetDraft = if (card.registered) "TOSS_BILLING" else state.cardSheetDraft
                    )
                }
            }
            .onFailure { e ->
                reduce { state.copy(snackbarState = SnackbarState(e.message ?: "카드 등록에 실패했습니다")) }
            }
    }

    private fun savedCardRegisterDismissed() = intent {
        reduce { state.copy(showSavedCardRegister = false) }
    }

    private fun updateUsedPoints(points: String) = blockingIntent {
        val cleaned = points.replace(",", "").replace(" ", "")
        val pointValue = cleaned.toIntOrNull() ?: 0
        val orderItems = state.orderLines.orEmpty()
        val pointUsage = limitUsedPoints(
            requestedPoints = pointValue,
            orderItems = orderItems,
            pointBalance = state.pointBalance?.totalPoints,
            selectedCoupons = state.selectedCoupons,
            subscribedCreatorIds = state.subscribedCreatorIds
        )
        reduce {
            state.copy(
                usedPoints = pointUsage.usedPoints,
                pointValidationMessage = pointUsage.message
            )
        }
    }

    private fun useAllPoints() = intent {
        val orderItems = state.orderLines.orEmpty()
        val pricing = calcStorePricing(
            items = orderItems,
            subscribedCreatorIds = state.subscribedCreatorIds,
            selectedCoupons = state.selectedCoupons
        )
        val balance = state.pointBalance?.totalPoints ?: 0
        // 전액사용 = 보유와 사용제한 중 작은 값
        val target = minOf(balance, calcUsablePointsCap(pricing.subtotal, pricing.totalBeforePoints))
        val pointUsage = limitUsedPoints(
            requestedPoints = target,
            orderItems = orderItems,
            pointBalance = state.pointBalance?.totalPoints,
            selectedCoupons = state.selectedCoupons,
            subscribedCreatorIds = state.subscribedCreatorIds
        )
        reduce {
            state.copy(
                usedPoints = pointUsage.usedPoints,
                pointValidationMessage = pointUsage.message
            )
        }
    }

    private fun placeOrder() = intent {
        // 중복 실행 방지
        if (state.isProcessing) return@intent

        if (state.selectedAddress == null) {
            reduce {
                state.copy(snackbarState = SnackbarState("배송지를 설정해주세요"))
            }
            return@intent
        }
        val orderLines = state.orderLines
        if (orderLines.isNullOrEmpty()) {
            reduce {
                state.copy(snackbarState = SnackbarState("주문 정보를 다시 불러와주세요"))
            }
            return@intent
        }
        // 0원이면 결제수단 없이 주문
        val isFreeOrder = state.paymentAmount() <= 0L
        // 결제수단 미선택 가드
        if (!isFreeOrder && (state.selectedPaymentProvider.isBlank() ||
                    (!BuildConfig.DEBUG && state.selectedPaymentProvider == "MOCK"))
        ) {
            reduce {
                state.copy(snackbarState = SnackbarState("결제수단을 선택해주세요"))
            }
            return@intent
        }

        // 그사이 끝난 딜이 있으면 막고 새로고침
        val nowMillis = System.currentTimeMillis()
        val hasExpiredDeal = orderLines.any {
            it.product.isDealActive && (it.product.dealEndAt ?: 0L) <= nowMillis
        }
        if (hasExpiredDeal) {
            reduce {
                state.copy(snackbarState = SnackbarState("타임딜이 종료된 상품이 있습니다. 변경된 가격을 확인해주세요"))
            }
            loadCheckoutData()
            return@intent
        }

        reduce { state.copy(isProcessing = true) }

        val couponCodes = state.selectedCoupons.values.map { it.code }.takeIf { it.isNotEmpty() }

        // 결제수단 키는 클라이언트 전용
        val methodKey = state.selectedPaymentProvider
        val isBilling = !isFreeOrder && methodKey == "TOSS_BILLING"
        val isTossSdk = !isFreeOrder && methodKey.startsWith("TOSS_") && !isBilling
        val serverProvider = when {
            isFreeOrder -> "NONE"
            isBilling -> "TOSS_BILLING"
            isTossSdk -> "TOSS_PAYMENTS"
            else -> methodKey
        }

        // 저장카드 결제는 빌링 카드 필수
        if (isBilling && state.savedCard == null) {
            reduce { state.copy(isProcessing = false) }
            addSavedCard()
            return@intent
        }

        val memo = state.memo.ifBlank { null }
        val pointsToUse = state.usedPoints.takeIf { it > 0 }?.toString()

        // 이전 토스 시도 주문은 먼저 취소해 포인트·쿠폰 환원
        state.pendingTossOrderId?.let { staleOrderId ->
            if (orderRepository.cancelOrder(staleOrderId, "결제 취소").isSuccess) {
                reduce { state.copy(pendingTossOrderId = null, pendingTossOrderNumber = null, pendingTossAmount = null) }
                savePendingToss(null, null, null)
            }
        }

        val orderResult = if (directProductId != null) {
            orderRepository.createDirectOrder(
                CreateDirectOrderRequest(
                    productId = directProductId,
                    quantity = directQuantity,
                    shippingAddress = state.shippingAddress,
                    shippingPhone = state.shippingPhone,
                    shippingName = state.shippingName,
                    paymentProvider = serverProvider,
                    memo = memo,
                    pointsToUse = pointsToUse,
                    couponCodes = couponCodes
                )
            )
        } else {
            orderRepository.createOrder(
                CreateOrderRequest(
                    shippingAddress = state.shippingAddress,
                    shippingPhone = state.shippingPhone,
                    shippingName = state.shippingName,
                    paymentProvider = serverProvider,
                    memo = memo,
                    pointsToUse = pointsToUse,
                    couponCodes = couponCodes,
                    cartItemIds = state.selectedItemIds.takeIf { it.isNotEmpty() }
                )
            )
        }

        orderResult
            .onSuccess { order ->
                if (isFreeOrder) {
                    if (order.paymentStatus == PaymentStatus.COMPLETED) {
                        reduce { state.copy(isProcessing = false) }
                        postSideEffect(CheckoutSideEffect.NavigateToOrderComplete(order.id, order.orderNumber))
                    } else {
                        // 서버 금액이 0원이 아니면 주문 되돌리고 재계산
                        orderRepository.cancelOrder(order.id, "결제 금액 변경")
                        reduce {
                            state.copy(
                                isProcessing = false,
                                snackbarState = SnackbarState("결제 금액이 바뀌었습니다. 다시 확인해주세요")
                            )
                        }
                        loadCheckoutData()
                    }
                } else if (isBilling) {
                    // 저장카드는 서버 빌링 청구, SDK 없음
                    billingRepository.payByBilling(order.id)
                        .onSuccess {
                            reduce { state.copy(isProcessing = false) }
                            postSideEffect(CheckoutSideEffect.NavigateToOrderComplete(order.id, order.orderNumber))
                        }
                        .onFailure { error ->
                            if (error.isPaymentResultUnknown()) {
                                // 청구 여부 모름, 확정은 서버 정리 작업
                                reduce { state.copy(isProcessing = false) }
                                postSideEffect(CheckoutSideEffect.ShowToast("결제 결과를 확인하고 있습니다. 주문 내역에서 확인해주세요"))
                                postSideEffect(CheckoutSideEffect.NavigateBack)  // 이중 청구 방지
                            } else {
                                val cancelled = orderRepository.cancelOrder(order.id, "결제 실패").isSuccess
                                val message = if (cancelled) {
                                    error.message ?: "결제에 실패했습니다"
                                } else {
                                    // 주문 취소도 실패하면 서버 정리 작업이 포인트·쿠폰을 돌려줌
                                    "결제에 실패했습니다. 사용한 포인트·쿠폰은 잠시 후 돌아옵니다"
                                }
                                reduce {
                                    state.copy(
                                        isProcessing = false,
                                        snackbarState = SnackbarState(message)
                                    )
                                }
                            }
                        }
                } else if (isTossSdk) {
                    // 토스는 SDK 결제창, isProcessing 유지
                    val tossMethod = when (methodKey) {
                        "TOSS_CARD" -> TossPaymentMethodType.CARD
                        "TOSS_TOSSPAY" -> TossPaymentMethodType.TOSSPAY
                        "TOSS_TRANSFER" -> TossPaymentMethodType.TRANSFER
                        "TOSS_MOBILE" -> TossPaymentMethodType.MOBILE
                        else -> TossPaymentMethodType.CARD
                    }
                    paymentRepository.prepareTossPayment(order.id)
                        .onSuccess { info ->
                            reduce {
                                state.copy(
                                    pendingTossOrderId = order.id,
                                    pendingTossOrderNumber = order.orderNumber,
                                    pendingTossAmount = info.amount
                                )
                            }
                            savePendingToss(order.id, order.orderNumber, info.amount)
                            postSideEffect(
                                CheckoutSideEffect.LaunchTossPayment(
                                    orderId = info.orderId,
                                    orderName = info.orderName,
                                    amount = info.amount,
                                    method = tossMethod
                                )
                            )
                        }
                        .onFailure { error ->
                            // 이미 만든 주문은 취소해 포인트·쿠폰 환원
                            val cancelled = orderRepository.cancelOrder(order.id, "결제 준비 실패").isSuccess
                            if (!cancelled) {
                                // 다음 결제 전에 다시 취소
                                reduce { state.copy(pendingTossOrderId = order.id, pendingTossOrderNumber = order.orderNumber) }
                                savePendingToss(order.id, order.orderNumber, null)
                            }
                            val message = if (cancelled) {
                                error.message ?: "결제 준비에 실패했습니다"
                            } else {
                                "결제 준비에 실패했습니다. 사용한 포인트·쿠폰은 잠시 후 돌아옵니다"
                            }
                            reduce {
                                state.copy(
                                    isProcessing = false,
                                    snackbarState = SnackbarState(message)
                                )
                            }
                        }
                } else {
                    paymentRepository.processPayment(order.id)
                        .onSuccess {
                            reduce { state.copy(isProcessing = false) }
                            postSideEffect(CheckoutSideEffect.NavigateToOrderComplete(order.id, order.orderNumber))
                        }
                        .onFailure { error ->
                            reduce {
                                state.copy(
                                    isProcessing = false,
                                    snackbarState = SnackbarState(error.message ?: "결제 처리 실패")
                                )
                            }
                        }
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isProcessing = false,
                        snackbarState = SnackbarState(error.message ?: "주문 생성 실패")
                    )
                }
            }
    }

    private fun tossPaymentSucceeded(paymentKey: String) = intent {  // 서버 confirm으로 실제 승인
        reduce { state.copy(isProcessing = true) }  // 프로세스 복원으로 들어와도 승인 중엔 결제 버튼·뒤로 막음
        val orderId = state.pendingTossOrderId
        val orderNumber = state.pendingTossOrderNumber
        val amount = state.pendingTossAmount
        if (orderId == null || orderNumber == null || amount == null) {
            reduce {
                state.copy(
                    isProcessing = false,
                    snackbarState = SnackbarState("결제 정보를 확인할 수 없습니다")
                )
            }
            return@intent
        }
        paymentRepository.confirmTossPayment(orderId, paymentKey, amount)
            .onSuccess {
                reduce {
                    state.copy(
                        isProcessing = false,
                        pendingTossOrderId = null,
                        pendingTossOrderNumber = null,
                        pendingTossAmount = null
                    )
                }
                savePendingToss(null, null, null)
                postSideEffect(CheckoutSideEffect.NavigateToOrderComplete(orderId, orderNumber))
            }
            .onFailure { error ->
                if (error.isPaymentResultUnknown()) {
                    // 승인 여부 모름, 확정은 서버 정리 작업
                    reduce {
                        state.copy(
                            isProcessing = false,
                            pendingTossOrderId = null,
                            pendingTossOrderNumber = null,
                            pendingTossAmount = null
                        )
                    }
                    savePendingToss(null, null, null)
                    postSideEffect(CheckoutSideEffect.ShowToast("결제 결과를 확인하고 있습니다. 주문 내역에서 확인해주세요"))
                    postSideEffect(CheckoutSideEffect.NavigateBack)  // 이중 구매 방지
                } else {
                    // 승인 거절 확정, 주문 취소로 복원
                    abortPendingTossOrder("결제 실패", error.message ?: "결제 승인에 실패했습니다")
                }
            }
    }

    private fun tossPaymentFailed(message: String) = abortPendingTossOrder("결제 실패", message)

    private fun tossPaymentCanceled() = abortPendingTossOrder("결제 취소", "결제를 취소했습니다")

    // 실패·취소 시 주문 취소로 복원
    private fun abortPendingTossOrder(cancelReason: String, message: String) = intent {
        reduce { state.copy(isProcessing = true) }  // 취소 중 결제 버튼으로 새 주문이 겹치지 않게
        val orderId = state.pendingTossOrderId
        // 취소 성공 시에만 pending 해제
        val cancelled = orderId == null ||
            orderRepository.cancelOrder(orderId, cancelReason).isSuccess
        reduce {
            state.copy(
                isProcessing = false,
                pendingTossOrderId = if (cancelled) null else orderId,
                pendingTossOrderNumber = if (cancelled) null else state.pendingTossOrderNumber,
                pendingTossAmount = if (cancelled) null else state.pendingTossAmount,
                snackbarState = SnackbarState(message)
            )
        }
        if (cancelled) savePendingToss(null, null, null)
    }

    private fun savePendingToss(orderId: Int?, orderNumber: String?, amount: Long?) {
        savedStateHandle[KEY_PENDING_TOSS_ORDER_ID] = orderId
        savedStateHandle[KEY_PENDING_TOSS_ORDER_NUMBER] = orderNumber
        savedStateHandle[KEY_PENDING_TOSS_AMOUNT] = amount
    }

    // 화면의 결제 금액과 같은 계산
    private fun CheckoutState.paymentAmount(): Long =
        calcStorePricing(
            items = orderLines.orEmpty(),
            subscribedCreatorIds = subscribedCreatorIds,
            selectedCoupons = selectedCoupons
        ).totalBeforePoints - usedPoints

    // 연결 끊김·타임아웃·5xx는 결제 결과 불명
    private fun Throwable.isPaymentResultUnknown(): Boolean {
        val code = (this as? ApiException)?.code ?: return true
        return code == 0 || code >= 500
    }

    private fun navigateBack() = intent {
        postSideEffect(CheckoutSideEffect.NavigateBack)
    }

    private fun navigateToAddressManage() = intent {
        postSideEffect(CheckoutSideEffect.NavigateToAddressManage)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    /** 주문 줄과 정리된 장바구니 선택 id */
    private suspend fun loadOrderLines(currentState: CheckoutState): Result<Pair<List<OrderLine>, List<Int>>> {  // 바로구매는 상품 하나
        if (directProductId != null) {
            return productRepository.getProduct(directProductId)
                .map { product -> listOf(OrderLine(product, directQuantity)) to emptyList<Int>() }
                .recoverCatching { error ->
                    throw IllegalStateException(error.message ?: "상품 정보를 불러올 수 없습니다", error)
                }
        }

        val cart = cartRepository.getCart().getOrElse { error ->
            return Result.failure(IllegalStateException(error.message ?: "장바구니를 불러올 수 없습니다", error))
        }
        return normalizeSelectedItemIds(
            cartItems = cart.items,
            requestedSelectedItemIds = currentState.selectedItemIds.ifEmpty { initialSelectedItemIds }
        ).map { selectedIds ->
            val selectedItems = if (selectedIds.isEmpty()) cart.items else cart.items.filter { it.id in selectedIds }
            selectedItems.map { it.toOrderLine() } to selectedIds
        }
    }

    private fun filterCouponsForCurrentOrder(
        coupons: Map<Int, Coupon>,
        orderItems: List<OrderLine>,
        orderCreatorIds: Set<Int>,
        subscribedCreatorIds: Set<Int>
    ): Map<Int, Coupon> {
        return coupons.filterValues { coupon ->
            coupon.creatorId in orderCreatorIds &&
                isCouponUsable(coupon, creatorOrderAmount(orderItems, coupon.creatorId)) &&
                !isFreeShipCouponBlocked(coupon, orderItems, subscribedCreatorIds)
        }
    }

    /** 크리에이터 상품 소계 */
    private fun creatorOrderAmount(orderItems: List<OrderLine>, creatorId: Int): Long =  // 쿠폰 최소 주문 금액 기준
        orderItems
            .filter { it.product.creator.id == creatorId }
            .sumOf { it.subtotal }

    private fun isFreeShipCouponBlocked(
        coupon: Coupon,
        orderItems: List<OrderLine>,
        subscribedCreatorIds: Set<Int>
    ): Boolean {
        if (coupon.type != CouponType.FREE_SHIPPING) return false

        val hasMembershipFreeShipping = coupon.creatorId in subscribedCreatorIds
        if (hasMembershipFreeShipping) return true

        return isFreeShippingReached(creatorOrderAmount(orderItems, coupon.creatorId))
    }

    private fun refreshAddressesOnly() = intent {  // 쿠폰·선택 상품 유지
        val currentState = state
        addressRepository.getAddresses()
            .onSuccess { addresses ->
                val selectedAddress = resolveSelectedAddress(
                    addresses = addresses,
                    currentSelectedAddressId = currentState.selectedAddressId,
                    defaultAddressId = currentState.defaultAddressId
                )
                reduce {
                    state.copy(
                        addresses = addresses,
                        selectedAddress = selectedAddress,
                        selectedAddressId = selectedAddress?.id,
                        defaultAddressId = addresses.firstOrNull { it.isDefault }?.id,
                        shippingName = selectedAddress?.recipientName ?: "",
                        shippingPhone = selectedAddress?.recipientPhone ?: "",
                        shippingAddress = selectedAddress?.let(::formatShippingAddress).orEmpty(),
                        memo = selectedAddress?.memo ?: ""
                    )
                }
            }
    }

    private fun resolveSelectedAddress(
        addresses: List<Address>,
        currentSelectedAddressId: Int?,
        defaultAddressId: Int?
    ): Address? {
        if (addresses.isEmpty()) return null

        val defaultAddress = addresses.firstOrNull { it.isDefault }
        val currentSelectedAddress = currentSelectedAddressId?.let { selectedId ->
            addresses.firstOrNull { it.id == selectedId }
        }

        return when {
            defaultAddress != null && defaultAddressId != defaultAddress.id -> defaultAddress
            currentSelectedAddress != null -> currentSelectedAddress
            defaultAddress != null -> defaultAddress
            else -> addresses.first()
        }
    }

    private fun limitUsedPoints(  // 쿠폰·배송비 반영 금액까지 상한
        requestedPoints: Int,
        orderItems: List<OrderLine>,
        pointBalance: Int?,
        selectedCoupons: Map<Int, Coupon>,
        subscribedCreatorIds: Set<Int>
    ): PointUsageResult {
        val normalizedPoints = requestedPoints.coerceAtLeast(0)
        if (normalizedPoints == 0) {
            return PointUsageResult(usedPoints = 0, message = null)
        }

        val pricing = calcStorePricing(
            items = orderItems,
            subscribedCreatorIds = subscribedCreatorIds,
            selectedCoupons = selectedCoupons
        )
        val subtotal = pricing.subtotal
        if (subtotal <= 0L) {
            return PointUsageResult(usedPoints = 0, message = null)
        }

        val availableBalance = pointBalance ?: Int.MAX_VALUE
        val maxUsablePoints = calcUsablePointsCap(subtotal, pricing.totalBeforePoints)
        val cappedPoints = minOf(normalizedPoints, availableBalance, maxUsablePoints)

        return when {
            normalizedPoints < MIN_USABLE_POINTS -> {
                PointUsageResult(
                    usedPoints = 0,
                    message = "포인트는 최소 ${MIN_USABLE_POINTS}P부터 사용할 수 있습니다"
                )
            }
            // 보유·상한으로 깎은 값이 최소 미만이면 서버가 거절
            cappedPoints < MIN_USABLE_POINTS -> {
                PointUsageResult(
                    usedPoints = 0,
                    message = "사용 가능한 포인트가 ${MIN_USABLE_POINTS}P 미만이라 사용할 수 없습니다"
                )
            }
            normalizedPoints > availableBalance -> {
                PointUsageResult(
                    usedPoints = cappedPoints,
                    message = "보유 포인트까지만 사용할 수 있습니다"
                )
            }
            normalizedPoints > maxUsablePoints -> {
                PointUsageResult(
                    usedPoints = cappedPoints,
                    message = "주문 금액 기준 최대 ${maxUsablePoints}P까지 사용할 수 있습니다"
                )
            }
            else -> PointUsageResult(usedPoints = normalizedPoints, message = null)
        }
    }

    private suspend fun loadRequiredCheckoutData(
        currentState: CheckoutState
    ): Result<CheckoutLoadResult> = coroutineScope {
        val orderLinesDeferred = async { loadOrderLines(currentState) }
        val addressesDeferred = async { addressRepository.getAddresses() }
        val couponsDeferred = async { loadAvailableCoupons() }
        val pointBalanceDeferred = async { loadPointBalance() }

        val (orderItems, normalizedSelectedItemIds) = orderLinesDeferred.await().getOrElse { error ->
            return@coroutineScope Result.failure(error)
        }

        // 멤버십 배송비·적립률은 주문에 든 크리에이터만
        val orderCreatorIds = orderItems.mapTo(mutableSetOf()) { it.product.creator.id }
        val (subscribedCreatorIds, creatorTiers) = loadSubscribedCreatorIds(orderCreatorIds).getOrElse { error ->
            return@coroutineScope Result.failure(error)
        }
        val addresses = addressesDeferred.await().getOrElse { error ->
            return@coroutineScope Result.failure(
                IllegalStateException(error.message ?: "배송지 정보를 불러올 수 없습니다", error)
            )
        }
        // 쿠폰만 실패하면 결제는 열고 쿠폰 시트에서 다시 불러오기
        val couponsResult = couponsDeferred.await()
        val availableCoupons = couponsResult.getOrElse { emptyList() }
        val couponError = couponsResult.exceptionOrNull()?.let { it.message ?: "쿠폰을 불러올 수 없습니다" }
        val pointBalance = pointBalanceDeferred.await().getOrElse { error ->
            return@coroutineScope Result.failure(
                IllegalStateException(error.message ?: "포인트 정보를 불러올 수 없습니다", error)
            )
        }

        val filteredSelectedCoupons = filterCouponsForCurrentOrder(
            coupons = currentState.selectedCoupons,
            orderItems = orderItems,
            orderCreatorIds = orderCreatorIds,
            subscribedCreatorIds = subscribedCreatorIds
        )
        val filteredDraftCoupons = if (currentState.showCouponSheet) {
            filterCouponsForCurrentOrder(
                coupons = currentState.draftSelectedCoupons,
                orderItems = orderItems,
                orderCreatorIds = orderCreatorIds,
                subscribedCreatorIds = subscribedCreatorIds
            )
        } else {
            emptyMap()
        }
        val couponDiscount = calcCouponDiscount(filteredSelectedCoupons, orderItems)
        val pointUsage = limitUsedPoints(
            requestedPoints = currentState.usedPoints,
            orderItems = orderItems,
            pointBalance = pointBalance.totalPoints,
            selectedCoupons = filteredSelectedCoupons,
            subscribedCreatorIds = subscribedCreatorIds
        )
        val selectedAddress = resolveSelectedAddress(
            addresses = addresses,
            currentSelectedAddressId = currentState.selectedAddressId,
            defaultAddressId = currentState.defaultAddressId
        )

        Result.success(
            CheckoutLoadResult(
                orderLines = orderItems,
                selectedItemIds = normalizedSelectedItemIds,
                subscribedCreatorIds = subscribedCreatorIds,
                creatorTiers = creatorTiers,
                addresses = addresses,
                selectedAddress = selectedAddress,
                defaultAddressId = addresses.firstOrNull { it.isDefault }?.id,
                availableCoupons = availableCoupons,
                couponError = couponError,
                pointBalance = pointBalance,
                selectedCoupons = filteredSelectedCoupons,
                draftSelectedCoupons = filteredDraftCoupons,
                couponDiscount = couponDiscount,
                usedPoints = pointUsage.usedPoints,
                pointValidationMessage = pointUsage.message
            )
        )
    }

    private suspend fun loadSubscribedCreatorIds(
        creatorIds: Set<Int>
    ): Result<Pair<Set<Int>, Map<Int, SubscriptionPlanTier>>> {
        if (creatorIds.isEmpty()) {
            return Result.success(emptySet<Int>() to emptyMap())
        }

        return runCatching {
            val results = coroutineScope {
                creatorIds.map { creatorId ->
                    async {
                        val tierInfo = subscriptionRepository
                            .getCreatorTier(creatorId)
                            .getOrElse { throw it }
                        creatorId to tierInfo
                    }
                }.awaitAll()
            }

            val subscribedIds = results
                .filter { it.second.isSubscribed }
                .map { it.first }
                .toSet()

            val tierMap = results.associate { (creatorId, tierInfo) ->
                creatorId to tierInfo.tier
            }

            subscribedIds to tierMap
        }.recoverCatching { error ->
            throw IllegalStateException(
                error.message ?: "멤버십 정보를 불러올 수 없습니다",
                error
            )
        }
    }

    private suspend fun loadAvailableCoupons(): Result<List<Coupon>> {
        return couponRepository.getClaimedCoupons(status = UserCouponStatus.AVAILABLE, limit = 100)
            .mapCatching { it.coupons }
    }

    private suspend fun loadPointBalance(): Result<PointBalance> {
        return pointRepository.getBalance()
    }

    private fun normalizeSelectedItemIds(
        cartItems: List<CartItem>,
        requestedSelectedItemIds: List<Int>
    ): Result<List<Int>> {
        if (cartItems.isEmpty()) {
            return Result.failure(IllegalStateException("주문할 상품이 없습니다"))
        }
        if (requestedSelectedItemIds.isEmpty()) {
            return Result.success(emptyList())
        }

        val validIds = cartItems.map { it.id }.toSet()
        val normalizedIds = requestedSelectedItemIds.filter { it in validIds }
        if (normalizedIds.isEmpty()) {
            return Result.failure(IllegalStateException("주문 상품을 확인할 수 없습니다"))
        }

        return Result.success(normalizedIds)
    }

    private fun formatShippingAddress(address: Address): String {
        return buildString {
            append("[${address.zipCode}] ${address.address}")
            if (!address.addressDetail.isNullOrBlank()) {
                append(" ${address.addressDetail}")
            }
        }
    }
}
