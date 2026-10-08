package com.ninezero.halo.feature.store.presentation.wishlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.core.ui.components.layout.PagedListScreenShell
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.store.presentation.wishlist.components.WishlistItem
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun WishlistScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit = {},
    onNavigateToProductDetail: (Int) -> Unit = {},
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    var hasInitialResumeHandled by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasInitialResumeHandled) {
            viewModel.onIntent(WishlistIntent.ScreenResumed)
        } else {
            hasInitialResumeHandled = true
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is WishlistSideEffect.NavigateBack -> onNavigateBack()
            is WishlistSideEffect.NavigateToCart -> onNavigateToCart()
            is WishlistSideEffect.NavigateToProductDetail -> onNavigateToProductDetail(sideEffect.productId)
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onAction = { viewModel.onIntent(WishlistIntent.SnackbarActionClicked) },
        onSecondaryAction = { viewModel.onIntent(WishlistIntent.SnackbarSecondaryActionClicked) },
        onDismiss = { viewModel.onIntent(WishlistIntent.SnackbarDismissed) }
    )

    WishlistScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = { viewModel.onIntent(WishlistIntent.NavigateBack) },
        onRemoveItem = { productId -> viewModel.onIntent(WishlistIntent.RemoveItem(productId)) },
        onMoveToCart = { productId -> viewModel.onIntent(WishlistIntent.MoveToCart(productId)) },
        onItemClick = { productId -> viewModel.onIntent(WishlistIntent.NavigateToProductDetail(productId)) },
        onRefresh = { viewModel.onIntent(WishlistIntent.Refresh) },
        onRetry = { viewModel.onIntent(WishlistIntent.Load) },
        onLoadMore = { viewModel.onIntent(WishlistIntent.LoadMore) }
    )
}

@Composable
internal fun WishlistScreenContent(
    state: WishlistState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateBack: () -> Unit = {},
    onRemoveItem: (Int) -> Unit = {},
    onMoveToCart: (Int) -> Unit = {},
    onItemClick: (Int) -> Unit = {},
    onRefresh: () -> Unit = {},
    onRetry: () -> Unit = {},
    onLoadMore: () -> Unit = {}
) {
    PagedListScreenShell(
        title = "위시리스트",
        isRefreshing = state.isRefreshing,
        isLoading = state.isLoadingMore || state.isLoading,
        hasMore = state.hasMore,
        isEmpty = state.items.isEmpty(),
        error = state.error,
        emptyTitle = "위시리스트가 비어있어요",
        emptyDescription = "마음에 드는 상품을 위시리스트에 담아보세요",
        onNavigateBack = onNavigateBack,
        onRefresh = onRefresh,
        onRetry = onRetry,
        onLoadMore = onLoadMore,
        snackbarHostState = snackbarHostState
    ) { listState, headerHeight ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
        ) {
            items(
                items = state.items,
                key = { "wishlist_${it.id}" }
            ) { wishlist ->
                Column(modifier = Modifier.animateItem()) {
                    WishlistItem(
                        wishlist = wishlist,
                        onClick = { onItemClick(wishlist.product.id) },
                        onRemove = { onRemoveItem(wishlist.product.id) },
                        onMoveToCart = { onMoveToCart(wishlist.product.id) }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }

            if (state.isLoadingMore) {
                item(key = "loading_more") {
                    ListLoadMoreIndicator()
                }
            }
        }
    }
}
