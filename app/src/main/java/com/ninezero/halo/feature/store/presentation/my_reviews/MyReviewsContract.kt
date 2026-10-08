package com.ninezero.halo.feature.store.presentation.my_reviews

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.Review

data class MyReviewsState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = true,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,
    val selectedReview: Review? = null,
    val showOptionSheet: Boolean = false,
    val showDeleteConfirm: Boolean = false
)

sealed interface MyReviewsIntent {
    // 데이터 로딩
    data object LoadReviews : MyReviewsIntent
    data object LoadMore : MyReviewsIntent
    data object Refresh : MyReviewsIntent
    data object ScreenResumed : MyReviewsIntent

    // 리뷰 옵션 시트
    data class ShowOptions(val review: Review) : MyReviewsIntent
    data object DismissOptions : MyReviewsIntent
    data object ShowDeleteConfirm : MyReviewsIntent
    data object DismissDeleteConfirm : MyReviewsIntent
    data object ConfirmDelete : MyReviewsIntent

    data object NavigateBack : MyReviewsIntent
    data class NavigateToProductDetail(val review: Review) : MyReviewsIntent
    data class EditReview(val review: Review) : MyReviewsIntent
    data object SnackbarDismissed : MyReviewsIntent
}

sealed interface MyReviewsSideEffect {
    data object NavigateBack : MyReviewsSideEffect
    data class NavigateToProductDetail(val productId: Int) : MyReviewsSideEffect
    data class NavigateToEditReview(
        val productId: Int,
        val orderId: Int,
        val productName: String,
        val productImageUrl: String?,
        val reviewId: Int,
        val rating: Int,
        val content: String
    ) : MyReviewsSideEffect
}
