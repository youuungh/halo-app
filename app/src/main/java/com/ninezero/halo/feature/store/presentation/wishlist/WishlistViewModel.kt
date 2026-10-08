package com.ninezero.halo.feature.store.presentation.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.network.ApiException
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.core.util.mergeFreshHead
import com.ninezero.halo.feature.store.domain.event.StoreEvent
import com.ninezero.halo.feature.store.domain.event.StoreEventBus
import com.ninezero.halo.feature.store.domain.model.Wishlist
import com.ninezero.halo.feature.store.domain.model.WishlistPurchaseState
import com.ninezero.halo.feature.store.domain.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val storeEventBus: StoreEventBus
) : ViewModel(), OrbitContainerHost<WishlistState, WishlistState, WishlistSideEffect> {

    override val container: OrbitContainer<WishlistState, WishlistState, WishlistSideEffect> =
        orbitContainer(WishlistState())

    private val listGeneration = ListGeneration()

    init {
        loadWishlist()
        observeStoreEvents()
    }

    // 하위 화면에서 바뀐 항목만 반영
    private fun observeStoreEvents() {
        storeEventBus.events
            .onEach { event ->
                when (event) {
                    is StoreEvent.WishlistRemoved -> intent {
                        reduce { state.copy(items = state.items.filter { it.product.id != event.productId }) }
                    }

                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: WishlistIntent) {
        when (intent) {
            is WishlistIntent.Load -> loadWishlist()
            is WishlistIntent.LoadMore -> loadMore()
            is WishlistIntent.Refresh -> refresh()
            is WishlistIntent.ScreenResumed -> refreshWishlistQuietly()
            is WishlistIntent.RemoveItem -> removeItem(intent.productId)
            is WishlistIntent.MoveToCart -> moveToCart(intent.productId)
            is WishlistIntent.NavigateBack -> navigateBack()
            is WishlistIntent.NavigateToProductDetail -> navigateToProductDetail(intent.productId)
            is WishlistIntent.SnackbarActionClicked -> snackbarActionClicked()
            is WishlistIntent.SnackbarSecondaryActionClicked -> snackbarSecondaryActionClicked()
            is WishlistIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadWishlist() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        wishlistRepository.getWishlist(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    state.copy(
                        isLoading = false,
                        items = result.items,
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "위시리스트를 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun loadMore(): Job = intent {  // 자기를 다시 불러 타입 명시
        if (!state.hasMore || state.isLoadingMore || state.isLoading) return@intent

        reduce { state.copy(isLoadingMore = true) }

        val generation = listGeneration.current()
        val cursor = state.nextCursor

        wishlistRepository.getWishlist(cursor = cursor, limit = 20)
            .onSuccess { result ->
                if (listGeneration.current() != generation) {
                    reduce { state.copy(isLoadingMore = false) }
                    // 목록 수가 그대로라 트리거 대신 다시 요청
                    if (state.hasMore) loadMore()
                    return@intent
                }
                reduce {
                    state.copy(
                        isLoadingMore = false,
                        items = (state.items + result.items).distinctBy { it.id },
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoadingMore = false,
                        snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")
                    )
                }
            }
    }

    // 첫 페이지만 받아 앞에 합침
    private fun refreshWishlistQuietly() = intent {
        if (state.isLoading || state.isRefreshing) return@intent
        // 보류 건 먼저 커밋
        commitPendingRemoval()
        commitPendingCartMove()
        wishlistRepository.getWishlist(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    val merged = state.items.mergeFreshHead(result.items) { it.id }
                    // 뒷부분을 이어 붙였으면 다음 커서도 그대로
                    val keepsTail = merged.size > result.items.size
                    state.copy(
                        items = merged,
                        hasMore = if (keepsTail) state.hasMore else result.nextCursor != null,
                        nextCursor = if (keepsTail) state.nextCursor else result.nextCursor,
                        error = null
                    )
                }
            }
    }

    private fun refresh() = intent {
        // 보류 건 먼저 커밋
        commitPendingRemoval()
        commitPendingCartMove()
        reduce { state.copy(isRefreshing = true, error = null) }

        wishlistRepository.getWishlist(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    state.copy(
                        isRefreshing = false,
                        isLoading = false,
                        items = result.items,
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        snackbarState = SnackbarState(error.message ?: "새로고침 실패")
                    )
                }
            }
    }

    private fun removeItem(productId: Int) = intent {
        // 이전 보류 건 커밋 후 index 계산
        commitPendingRemoval()
        commitPendingCartMove()

        val index = state.items.indexOfFirst { it.product.id == productId }
        if (index < 0) return@intent
        val item = state.items[index]

        reduce {
            state.copy(
                items = state.items.filter { it.product.id != productId },
                deletedItem = item,
                deletedItemIndex = index,
                snackbarState = SnackbarState("위시리스트에서 삭제되었습니다", actionLabel = "취소")
            )
        }
    }

    // undo는 삭제 시점 index로 제자리 삽입
    private fun List<Wishlist>.restoredAt(index: Int, item: Wishlist): List<Wishlist> =
        toMutableList().apply { add(index.coerceIn(0, size), item) }

    // 취소: 보류 중인 삭제/이동을 로컬 복원
    private fun snackbarActionClicked() = intent {
        val deleted = state.deletedItem
        val pendingMove = state.pendingCartMove
        when {
            deleted != null -> reduce {
                state.copy(
                    items = state.items.restoredAt(state.deletedItemIndex, deleted),
                    deletedItem = null,
                    snackbarState = null
                )
            }

            pendingMove != null -> reduce {
                state.copy(
                    items = state.items.restoredAt(state.pendingCartMoveIndex, pendingMove),
                    pendingCartMove = null,
                    snackbarState = null
                )
            }

            else -> postSideEffect(WishlistSideEffect.NavigateToCart)
        }
    }

    // 보러가기: 보류 중 이동 먼저 커밋
    private fun snackbarSecondaryActionClicked() = intent {
        if (commitPendingCartMove()) {
            postSideEffect(WishlistSideEffect.NavigateToCart)
        }
    }

    // 보류 중인 삭제를 서버에 반영
    private suspend fun Syntax<WishlistState, WishlistSideEffect>.commitPendingRemoval() {
        val deleted = state.deletedItem ?: return
        val index = state.deletedItemIndex
        reduce { state.copy(deletedItem = null) }
        wishlistRepository.toggleWishlist(deleted.product.id)
            .onFailure {
                reduce {
                    state.copy(
                        items = state.items.restoredAt(index, deleted),
                        snackbarState = SnackbarState("삭제에 실패했습니다")
                    )
                }
            }
    }

    // 보류 중인 장바구니 이동을 서버에 반영
    // 성공 여부, 실패 시 이동 안 함
    private suspend fun Syntax<WishlistState, WishlistSideEffect>.commitPendingCartMove(): Boolean {
        val pending = state.pendingCartMove ?: return true
        val index = state.pendingCartMoveIndex
        reduce { state.copy(pendingCartMove = null) }
        return wishlistRepository.moveToCart(pending.product.id)
            .onFailure { error ->
                val message = when (error) {
                    is ApiException -> when (error.code) {
                        403 -> "멤버십 전용 상품입니다"
                        404 -> "상품을 찾을 수 없습니다"
                        422 -> when (pending.product.purchaseState) {
                            WishlistPurchaseState.DISCONTINUED -> "판매종료 상품입니다"
                            WishlistPurchaseState.SOLD_OUT -> "품절된 상품입니다"
                            WishlistPurchaseState.NO_ACCESS -> "멤버십 전용 상품입니다"
                            else -> "구매할 수 없는 상품입니다"
                        }

                        else -> error.message.ifBlank { "장바구니 이동 실패" }
                    }

                    else -> error.message ?: "장바구니 이동 실패"
                }
                reduce {
                    state.copy(
                        items = state.items.restoredAt(index, pending),
                        snackbarState = SnackbarState(message)
                    )
                }
            }
            .isSuccess
    }

    private fun moveToCart(productId: Int) = intent {
        // 이전 보류 건 먼저 커밋
        commitPendingRemoval()
        commitPendingCartMove()

        val index = state.items.indexOfFirst { it.product.id == productId }
        if (index < 0) return@intent
        val item = state.items[index]

        reduce {
            state.copy(
                items = state.items.filter { it.product.id != productId },
                pendingCartMove = item,
                pendingCartMoveIndex = index,
                snackbarState = SnackbarState(
                    "장바구니로 이동했습니다",
                    actionLabel = "취소",
                    secondaryActionLabel = "보러가기"
                )
            )
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(WishlistSideEffect.NavigateBack)
    }

    private fun navigateToProductDetail(productId: Int) = intent {
        postSideEffect(WishlistSideEffect.NavigateToProductDetail(productId))
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
        // 취소 없이 스낵바가 사라짐 → 삭제/이동 확정
        commitPendingRemoval()
        commitPendingCartMove()
    }
}
