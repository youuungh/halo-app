package com.ninezero.halo.feature.store.presentation.cart

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.Cart
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo

data class CartState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isDeleting: Boolean = false,
    val cart: Cart? = null,
    val selectedItemIds: Set<Int> = emptySet(),
    val creatorTierMap: Map<Int, UserTierInfo> = emptyMap(),
    val error: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface CartIntent {
    // 데이터 로딩
    data object LoadCart : CartIntent
    data object Refresh : CartIntent

    // 아이템 액션
    data class UpdateQuantity(val cartItemId: Int, val quantity: Int) : CartIntent
    data class RemoveItem(val cartItemId: Int) : CartIntent
    data object RemoveSelectedItems : CartIntent

    // 선택
    data class ToggleItemSelection(val cartItemId: Int) : CartIntent
    data class ToggleCreatorSelection(val creatorId: Int) : CartIntent
    data object ToggleSelectAll : CartIntent

    // 네비게이션
    data object NavigateBack : CartIntent
    data object NavigateToCheckout : CartIntent
    data class NavigateToProductDetail(val productId: Int) : CartIntent
    data class NavigateToUserProfile(val userId: Int) : CartIntent

    // Snackbar
    data class ShowRestrictedContentSnackbar(val message: String) : CartIntent
    data object SnackbarDismissed : CartIntent
}

sealed interface CartSideEffect {
    // 네비게이션
    data object NavigateBack : CartSideEffect
    data class NavigateToCheckout(val selectedItemIds: List<Int>) : CartSideEffect
    data class NavigateToProductDetail(val productId: Int) : CartSideEffect
    data class NavigateToUserProfile(val userId: Int) : CartSideEffect
}
