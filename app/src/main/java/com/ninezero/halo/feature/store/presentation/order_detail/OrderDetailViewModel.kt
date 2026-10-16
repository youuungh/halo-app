package com.ninezero.halo.feature.store.presentation.order_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.purchaseBlockedMessage
import com.ninezero.halo.feature.store.domain.repository.CartRepository
import com.ninezero.halo.feature.store.domain.repository.OrderRepository
import com.ninezero.halo.feature.store.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) : ViewModel(), OrbitContainerHost<OrderDetailState, OrderDetailState, OrderDetailSideEffect> {

    private val orderId: Int = savedStateHandle["orderId"] ?: 0
    private val shippingGeneration = ListGeneration()  // 배송 시트를 열고 닫을 때마다 올림, 늦게 온 옛 응답은 버림
    override val container: OrbitContainer<OrderDetailState, OrderDetailState, OrderDetailSideEffect> =
        orbitContainer(OrderDetailState())

    init {
        loadOrder()
    }

    fun onIntent(intent: OrderDetailIntent) {
        when (intent) {
            is OrderDetailIntent.Load -> loadOrder()
            is OrderDetailIntent.ShowCancelSheet -> showCancelSheet(intent.creatorId)
            is OrderDetailIntent.DismissCancelSheet -> dismissCancelSheet()
            is OrderDetailIntent.SelectCancelReason -> selectCancelReason(intent.reason)
            is OrderDetailIntent.UpdateCancelReasonEtc -> updateCancelReasonEtc(intent.text)
            is OrderDetailIntent.ProceedCancelFromSheet -> proceedCancelFromSheet()
            is OrderDetailIntent.DismissCancelConfirm -> dismissCancelConfirm()
            is OrderDetailIntent.ConfirmCancel -> cancelOrder()
            is OrderDetailIntent.WriteReview -> writeReview(intent.item)
            is OrderDetailIntent.AddToCart -> addToCart(intent.item)
            is OrderDetailIntent.OpenProduct -> openProduct(intent.item)
            is OrderDetailIntent.ShowShippingSheet -> showShippingSheet(intent.item)
            is OrderDetailIntent.DismissShippingSheet -> dismissShippingSheet()
            is OrderDetailIntent.NavigateBack -> navigateBack()
            is OrderDetailIntent.NavigateToCart -> navigateToCart()
            is OrderDetailIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadOrder() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        orderRepository.getOrder(orderId)
            .onSuccess { order ->
                reduce { state.copy(isLoading = false, order = order) }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "주문 정보를 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun showCancelSheet(creatorId: Int) = intent {
        reduce {
            state.copy(
                showCancelSheet = true,
                cancelConfirmCreatorId = creatorId,
                selectedCancelReason = CancelReason.CHANGED_MIND,
                cancelReasonEtc = ""
            )
        }
    }

    private fun dismissCancelSheet() = intent {
        reduce { state.copy(showCancelSheet = false, cancelConfirmCreatorId = null) }
    }

    private fun selectCancelReason(reason: CancelReason) = intent {
        reduce { state.copy(selectedCancelReason = reason) }
    }

    private fun updateCancelReasonEtc(text: String) = blockingIntent {
        reduce { state.copy(cancelReasonEtc = text) }
    }

    private fun proceedCancelFromSheet() = intent {
        reduce { state.copy(showCancelSheet = false, showCancelConfirm = true) }
    }

    private fun dismissCancelConfirm() = intent {
        reduce { state.copy(showCancelConfirm = false, cancelConfirmCreatorId = null) }
    }

    private fun cancelOrder() = intent {
        val creatorId = state.cancelConfirmCreatorId ?: return@intent
        val reason = if (state.selectedCancelReason == CancelReason.ETC) {
            state.cancelReasonEtc.trim().ifBlank { CancelReason.ETC.label }
        } else {
            state.selectedCancelReason.label
        }
        reduce { state.copy(showCancelConfirm = false, isCancelling = true) }

        orderRepository.cancelOrderGroup(orderId, creatorId, reason)
            .onSuccess { cancelledOrder ->
                reduce {
                    state.copy(
                        isCancelling = false,
                        order = cancelledOrder,
                        cancelConfirmCreatorId = null,
                        snackbarState = SnackbarState("주문이 취소되었습니다")
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isCancelling = false,
                        cancelConfirmCreatorId = null,
                        snackbarState = SnackbarState(error.message ?: "주문 취소 실패")
                    )
                }
            }
    }

    private fun writeReview(item: OrderItem) = intent {
        postSideEffect(
            OrderDetailSideEffect.NavigateToCreateReview(
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

    // 판매종료·볼 수 없는 상품은 들어가지 않고 안내
    private fun openProduct(item: OrderItem) = intent {
        val blockedMessage = when {
            !item.isProductActive -> "판매가 종료된 상품입니다"
            !item.canAccess -> "${item.requiredTier} 멤버십 전용 상품입니다"
            else -> null
        }
        if (blockedMessage != null) {
            reduce { state.copy(snackbarState = SnackbarState(blockedMessage)) }
        } else {
            postSideEffect(OrderDetailSideEffect.NavigateToProductDetail(item.productId))
        }
    }

    private fun showShippingSheet(item: OrderItem) = intent {
        val creatorId = item.creatorId
        val generation = shippingGeneration.next()
        reduce {
            state.copy(
                showShippingSheet = true,
                shippingSheetCreatorId = creatorId,
                shippingStatus = null,
                isShippingStatusLoading = true
            )
        }

        orderRepository.getShippingStatus(orderId, creatorId)
            .onSuccess { shipping ->
                if (shippingGeneration.current() != generation) return@intent
                reduce { state.copy(shippingStatus = shipping, isShippingStatusLoading = false) }
            }
            .onFailure {
                if (shippingGeneration.current() != generation) return@intent
                // 실패는 조용히, 시트는 주문에 있는 값으로
                reduce { state.copy(isShippingStatusLoading = false) }
            }
    }

    private fun dismissShippingSheet() = intent {
        shippingGeneration.next()
        reduce {
            state.copy(
                showShippingSheet = false,
                shippingSheetCreatorId = null,
                shippingStatus = null,
                isShippingStatusLoading = false
            )
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(OrderDetailSideEffect.NavigateBack)
    }

    private fun navigateToCart() = intent {
        postSideEffect(OrderDetailSideEffect.NavigateToCart)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
