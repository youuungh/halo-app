package com.ninezero.halo.feature.store.presentation.my_reviews

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
import com.ninezero.halo.core.ui.components.bottomsheet.EditDeleteOptionBottomSheet
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.PagedListScreenShell
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.viewer.ViewerOverlayHost
import com.ninezero.halo.core.ui.components.viewer.rememberViewerOverlayHostState
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.presentation.my_reviews.components.MyReviewItem
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MyReviewsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (Int) -> Unit = {},
    onNavigateToEditReview: (productId: Int, orderId: Int, productName: String, productImageUrl: String?, reviewId: Int, rating: Int, content: String) -> Unit = { _, _, _, _, _, _, _ -> },
    viewModel: MyReviewsViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    // 복귀 시 재조회, 최초는 스킵
    var hasInitialResumeHandled by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasInitialResumeHandled) {
            viewModel.onIntent(MyReviewsIntent.ScreenResumed)
        } else {
            hasInitialResumeHandled = true
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MyReviewsSideEffect.NavigateBack -> onNavigateBack()
            is MyReviewsSideEffect.NavigateToProductDetail -> onNavigateToProductDetail(sideEffect.productId)
            is MyReviewsSideEffect.NavigateToEditReview -> onNavigateToEditReview(
                sideEffect.productId, sideEffect.orderId, sideEffect.productName,
                sideEffect.productImageUrl, sideEffect.reviewId, sideEffect.rating, sideEffect.content
            )
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(MyReviewsIntent.SnackbarDismissed) }
    )

    MyReviewsScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = { viewModel.onIntent(MyReviewsIntent.NavigateBack) },
        onReviewClick = { review -> viewModel.onIntent(MyReviewsIntent.NavigateToProductDetail(review)) },
        onRefresh = { viewModel.onIntent(MyReviewsIntent.Refresh) },
        onRetry = { viewModel.onIntent(MyReviewsIntent.LoadReviews) },
        onLoadMore = { viewModel.onIntent(MyReviewsIntent.LoadMore) },
        onMoreClick = { review -> viewModel.onIntent(MyReviewsIntent.ShowOptions(review)) },
        onDismissOptions = { viewModel.onIntent(MyReviewsIntent.DismissOptions) },
        onEditClick = {
            state.selectedReview?.let { review ->
                viewModel.onIntent(MyReviewsIntent.EditReview(review))
            }
        },
        onDeleteClick = { viewModel.onIntent(MyReviewsIntent.ShowDeleteConfirm) },
        onConfirmDelete = { viewModel.onIntent(MyReviewsIntent.ConfirmDelete) },
        onDismissDeleteConfirm = { viewModel.onIntent(MyReviewsIntent.DismissDeleteConfirm) }
    )
}

@Composable
internal fun MyReviewsScreenContent(
    state: MyReviewsState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateBack: () -> Unit = {},
    onReviewClick: (Review) -> Unit = {},
    onRefresh: () -> Unit = {},
    onRetry: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onMoreClick: (Review) -> Unit = {},
    onDismissOptions: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onDismissDeleteConfirm: () -> Unit = {}
) {
    val viewerHostState = rememberViewerOverlayHostState()

    PagedListScreenShell(
        title = "내 리뷰",
        isRefreshing = state.isRefreshing,
        isLoading = state.isLoadingMore || state.isLoading,
        hasMore = state.hasMore,
        isEmpty = state.reviews.isEmpty(),
        error = state.error,
        emptyTitle = "아직 작성한 리뷰가 없어요",
        emptyDescription = "상품을 구매하고 리뷰를 남겨보세요",
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
                items = state.reviews,
                key = { "my_review_${it.id}" }
            ) { review ->
                Column(modifier = Modifier.animateItem()) {
                    MyReviewItem(
                        review = review,
                        onClick = { onReviewClick(review) },
                        onMoreClick = { onMoreClick(review) },
                        onImageClick = { images, index ->
                            viewerHostState.showImageViewer(images, index)
                        }
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

    ViewerOverlayHost(state = viewerHostState)

    if (state.showOptionSheet) {
        EditDeleteOptionBottomSheet(
            onDismiss = onDismissOptions,
            onEdit = onEditClick,
            onDelete = onDeleteClick
        )
    }

    if (state.showDeleteConfirm) {
        ConfirmDialog(
            title = "리뷰를 삭제하시겠어요?",
            confirmText = "삭제",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDeleteConfirm
        )
    }
}

