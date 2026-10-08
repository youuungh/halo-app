package com.ninezero.halo.feature.store.presentation.wishlist

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.Wishlist

data class WishlistState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val items: List<Wishlist> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = true,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,
    // 삭제 undo
    val deletedItem: Wishlist? = null,
    val deletedItemIndex: Int = 0,
    // 장바구니 이동 undo 버퍼
    val pendingCartMove: Wishlist? = null,
    val pendingCartMoveIndex: Int = 0
)

sealed interface WishlistIntent {
    // 데이터 로딩
    data object Load : WishlistIntent
    data object LoadMore : WishlistIntent
    data object Refresh : WishlistIntent
    data object ScreenResumed : WishlistIntent

    // 아이템 액션
    data class RemoveItem(val productId: Int) : WishlistIntent
    data class MoveToCart(val productId: Int) : WishlistIntent

    // 네비게이션
    data object NavigateBack : WishlistIntent
    data class NavigateToProductDetail(val productId: Int) : WishlistIntent

    // Snackbar
    data object SnackbarActionClicked : WishlistIntent
    data object SnackbarSecondaryActionClicked : WishlistIntent
    data object SnackbarDismissed : WishlistIntent
}

sealed interface WishlistSideEffect {
    data object NavigateBack : WishlistSideEffect
    data object NavigateToCart : WishlistSideEffect
    data class NavigateToProductDetail(val productId: Int) : WishlistSideEffect
}
