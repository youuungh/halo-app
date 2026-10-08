package com.ninezero.halo.feature.store.presentation.cart

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.Cart
import com.ninezero.halo.feature.store.domain.model.CartItem
import com.ninezero.halo.feature.store.domain.model.isPurchasable
import com.ninezero.halo.feature.store.domain.repository.CartRepository
import com.ninezero.halo.feature.store.domain.repository.CartSelectionStore
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo
import com.ninezero.halo.feature.subscription.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val cartSelectionStore: CartSelectionStore
) : ViewModel(), OrbitContainerHost<CartState, CartState, CartSideEffect> {

    private data class CartLoadResult(
        val cart: Cart,
        val creatorTierMap: Map<Int, UserTierInfo>
    )

    override val container: OrbitContainer<CartState, CartState, CartSideEffect> =
        orbitContainer(CartState())

    private var quantityUpdateJob: Job? = null  // 주문하기 직전 확정한 수량

    init {
        loadCart()
    }

    fun onIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.LoadCart -> loadCart()
            is CartIntent.Refresh -> refresh()
            is CartIntent.UpdateQuantity -> quantityUpdateJob = updateQuantity(intent.cartItemId, intent.quantity)
            is CartIntent.RemoveItem -> removeItem(intent.cartItemId)
            is CartIntent.RemoveSelectedItems -> removeSelectedItems()
            is CartIntent.ToggleItemSelection -> toggleItemSelection(intent.cartItemId)
            is CartIntent.ToggleCreatorSelection -> toggleCreatorSelection(intent.creatorId)
            is CartIntent.ToggleSelectAll -> toggleSelectAll()
            is CartIntent.NavigateBack -> navigateBack()
            is CartIntent.NavigateToCheckout -> navigateToCheckout()
            is CartIntent.NavigateToProductDetail -> navigateToProductDetail(intent.productId)
            is CartIntent.NavigateToUserProfile -> navigateToUserProfile(intent.userId)
            is CartIntent.ShowRestrictedContentSnackbar -> showRestrictedSnackbar(intent.message)
            is CartIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadCart() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        loadCartWithTierInfo()
            .onSuccess { cart ->
                val availableIds = cart.cart.items.filter(::isPurchasable).map { it.id }.toSet()
                val resolvedSelection = cartSelectionStore.reconcile(availableIds)
                reduce {
                    state.copy(
                        isLoading = false,
                        cart = cart.cart,
                        selectedItemIds = resolvedSelection,
                        creatorTierMap = cart.creatorTierMap
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        cart = null,
                        selectedItemIds = emptySet(),
                        creatorTierMap = emptyMap(),
                        error = error.message ?: "장바구니를 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun refresh() = intent {
        reduce { state.copy(isRefreshing = true, error = null) }

        loadCartWithTierInfo()
            .onSuccess { cart ->
                val availableIds = cart.cart.items.filter(::isPurchasable).map { it.id }.toSet()
                val resolvedSelection = cartSelectionStore.reconcile(availableIds)
                reduce {
                    state.copy(
                        isRefreshing = false,
                        isLoading = false,
                        cart = cart.cart,
                        selectedItemIds = resolvedSelection,
                        creatorTierMap = cart.creatorTierMap
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    // 복귀 조회 오류로 받아 둔 장바구니를 지우지 않음
                    // 있으면 스낵바만, 비었을 때만 오류 화면
                    if (state.cart != null) {
                        state.copy(
                            isRefreshing = false,
                            isLoading = false,
                            snackbarState = SnackbarState(error.message ?: "새로고침 실패")
                        )
                    } else {
                        state.copy(
                            isRefreshing = false,
                            isLoading = false,
                            cart = null,
                            selectedItemIds = emptySet(),
                            creatorTierMap = emptyMap(),
                            error = error.message ?: "장바구니를 불러올 수 없습니다"
                        )
                    }
                }
            }
    }

    private suspend fun loadCreatorTierInfo(creatorIds: Set<Int>): Map<Int, UserTierInfo> {  // 멤버십 무료배송 판단용
        return coroutineScope {
            creatorIds.map { creatorId ->
                async {
                    // 티어 조회 부분 실패 허용
                    // 실패 시 null, 배송비 청구 fail-safe
                    val tierInfo = subscriptionRepository.getCreatorTier(creatorId).getOrNull()
                    creatorId to tierInfo?.takeIf { it.isSubscribed }
                }
            }.awaitAll()
                .filter { it.second != null }
                .associate { it.first to it.second!! }
        }
    }

    /** 장바구니 재조회 */
    private suspend fun loadCartWithTierInfo(previous: CartState? = null): Result<CartLoadResult> {  // 크리에이터가 그대로면 이전 멤버십 정보 재사용
        return cartRepository.getCart().map { cart ->
            val creatorIds = cart.items.map { it.product.creator.id }.toSet()
            val knownCreatorIds = previous?.cart?.items?.map { it.product.creator.id }?.toSet().orEmpty()
            val tierMap = if (previous != null && knownCreatorIds.containsAll(creatorIds)) {
                previous.creatorTierMap.filterKeys { it in creatorIds }
            } else {
                loadCreatorTierInfo(creatorIds)
            }
            CartLoadResult(cart = cart, creatorTierMap = tierMap)
        }
    }

    private fun updateQuantity(cartItemId: Int, quantity: Int) = intent {
        if (quantity < 1) return@intent

        cartRepository.updateCartItem(cartItemId, quantity)
            .onSuccess {
                loadCartWithTierInfo(previous = state)
                    .onSuccess { cart ->
                        val validIds = cart.cart.items.map { it.id }.toSet()
                        reduce {
                            state.copy(
                                cart = cart.cart,
                                selectedItemIds = state.selectedItemIds.intersect(validIds),
                                creatorTierMap = cart.creatorTierMap,
                                error = null
                            )
                        }
                    }
                    .onFailure { error ->
                        // 재조회만 실패, 받아 둔 장바구니 유지
                        reduce { state.copy(snackbarState = SnackbarState(error.message ?: "장바구니를 새로 불러오지 못했습니다")) }
                    }
            }
            .onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "수량 변경 실패"))
                }
            }
    }

    private fun removeItem(cartItemId: Int) = intent {
        cartRepository.removeFromCart(cartItemId)
            .onSuccess {
                loadCartWithTierInfo(previous = state)
                    .onSuccess { cart ->
                        val validIds = cart.cart.items.map { it.id }.toSet()
                        reduce {
                            state.copy(
                                cart = cart.cart,
                                selectedItemIds = state.selectedItemIds.intersect(validIds),
                                creatorTierMap = cart.creatorTierMap,
                                error = null,
                                snackbarState = SnackbarState("상품이 삭제되었습니다")
                            )
                        }
                    }
                    .onFailure { error ->
                        // 재조회만 실패, 받아 둔 장바구니 유지
                        reduce { state.copy(snackbarState = SnackbarState(error.message ?: "장바구니를 새로 불러오지 못했습니다")) }
                    }
            }
            .onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "삭제 실패"))
                }
            }
    }

    // 아이템 선택
    private fun toggleItemSelection(cartItemId: Int) = intent {
        val newSelection = if (cartItemId in state.selectedItemIds) {
            state.selectedItemIds - cartItemId
        } else {
            state.selectedItemIds + cartItemId
        }
        cartSelectionStore.remember(newSelection)
        reduce { state.copy(selectedItemIds = newSelection) }
    }

    private fun toggleCreatorSelection(creatorId: Int) = intent {
        val cart = state.cart ?: return@intent
        val creatorItemIds = cart.items
            .filter { it.product.creator.id == creatorId && isPurchasable(it) }
            .map { it.id }
            .toSet()

        val allSelected = creatorItemIds.all { it in state.selectedItemIds }
        val newSelection = if (allSelected) {
            state.selectedItemIds - creatorItemIds
        } else {
            state.selectedItemIds + creatorItemIds
        }
        cartSelectionStore.remember(newSelection)
        reduce { state.copy(selectedItemIds = newSelection) }
    }

    private fun toggleSelectAll() = intent {
        val cart = state.cart ?: return@intent
        val allIds = cart.items.filter(::isPurchasable).map { it.id }.toSet()

        val allSelected = allIds == state.selectedItemIds
        val newSelection = if (allSelected) emptySet() else allIds
        cartSelectionStore.remember(newSelection)
        reduce { state.copy(selectedItemIds = newSelection) }
    }

    private fun removeSelectedItems() = intent {
        val selectedIds = state.selectedItemIds
        val allIds = state.cart?.items?.map { it.id }?.toSet() ?: return@intent
        if (selectedIds.isEmpty()) return@intent

        reduce { state.copy(isDeleting = true) }

        val deletedCount: Int
        val hasFailure: Boolean

        if (selectedIds == allIds) {
            val result = cartRepository.clearCart()
            deletedCount = if (result.isSuccess) selectedIds.size else 0
            hasFailure = result.isFailure
        } else {
            val results = coroutineScope {
                selectedIds.map { id ->
                    async { cartRepository.removeFromCart(id) }
                }.awaitAll()
            }

            deletedCount = results.count { it.isSuccess }
            hasFailure = results.any { it.isFailure }
        }

        loadCartWithTierInfo(previous = state)
            .onSuccess { cart ->
                val validIds = cart.cart.items.map { it.id }.toSet()
                reduce {
                    state.copy(
                        isDeleting = false,
                        cart = cart.cart,
                        selectedItemIds = state.selectedItemIds.intersect(validIds),
                        creatorTierMap = cart.creatorTierMap,
                        error = null,
                        snackbarState = SnackbarState(
                            when {
                                !hasFailure -> "${deletedCount}개 상품이 삭제되었습니다"
                                deletedCount > 0 -> "${deletedCount}개 삭제, 일부 실패했습니다"
                                else -> "삭제에 실패했습니다"
                            }
                        )
                    )
                }
            }
            .onFailure { error ->
                // 재조회만 실패, 받아 둔 장바구니 유지
                reduce {
                    state.copy(
                        isDeleting = false,
                        snackbarState = SnackbarState(error.message ?: "장바구니를 새로 불러오지 못했습니다")
                    )
                }
            }
    }

    private fun navigateBack() = intent {
        postSideEffect(CartSideEffect.NavigateBack)
    }

    private fun navigateToCheckout() = intent {
        quantityUpdateJob?.join()  // 결제 화면이 장바구니를 읽기 전에 수량 반영
        val cart = state.cart
        if (cart == null || cart.items.isEmpty()) {
            reduce { state.copy(snackbarState = SnackbarState("장바구니가 비어있습니다")) }
            return@intent
        }
        val availableSelectedIds = cart.items
            .filter { item ->
                item.id in state.selectedItemIds &&
                isPurchasable(item)
            }
            .map { it.id }
        if (availableSelectedIds.isEmpty()) {
            reduce { state.copy(snackbarState = SnackbarState("주문 가능한 상품을 선택해주세요")) }
            return@intent
        }
        postSideEffect(CartSideEffect.NavigateToCheckout(availableSelectedIds))
    }

    private fun navigateToProductDetail(productId: Int) = intent {
        postSideEffect(CartSideEffect.NavigateToProductDetail(productId))
    }

    private fun navigateToUserProfile(userId: Int) = intent {
        postSideEffect(CartSideEffect.NavigateToUserProfile(userId))
    }

    private fun showRestrictedSnackbar(message: String) = intent {
        reduce { state.copy(snackbarState = SnackbarState(message)) }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    // 도메인 단일 정본 사용
    private fun isPurchasable(item: CartItem): Boolean = item.product.isPurchasable()
}
