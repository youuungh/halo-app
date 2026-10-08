package com.ninezero.halo.feature.store.presentation.review_list

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.domain.model.ReviewSummary

enum class ReviewSortType(val apiValue: String, val displayName: String) {
    RECENT("RECENT", "최신순"),
    RATING_HIGH("RATING_HIGH", "높은 평점순"),
    RATING_LOW("RATING_LOW", "낮은 평점순")
}

data class ReviewListState(
    val reviews: List<Review> = emptyList(),
    val reviewSummary: ReviewSummary? = null,
    val highlightReviewId: Int? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,
    val sortBy: ReviewSortType = ReviewSortType.RECENT,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val totalReviews: Int = 0,
    // 내 리뷰 관리
    val currentUserId: Int? = null,
    val selectedReview: Review? = null,
    val showOptionSheet: Boolean = false,
    val showDeleteConfirm: Boolean = false
)

sealed interface ReviewListIntent {
    // 데이터 로딩
    data object LoadReviews : ReviewListIntent
    data object LoadMore : ReviewListIntent
    data object Refresh : ReviewListIntent
    data object ScreenResumed : ReviewListIntent
    data class ChangeSortBy(val sortBy: ReviewSortType) : ReviewListIntent

    // 내 리뷰 옵션 시트
    data class ShowOptions(val review: Review) : ReviewListIntent
    data object DismissOptions : ReviewListIntent
    data object ShowDeleteConfirm : ReviewListIntent
    data object DismissDeleteConfirm : ReviewListIntent
    data object ConfirmDelete : ReviewListIntent

    data object NavigateBack : ReviewListIntent
    data class EditReview(val review: Review) : ReviewListIntent
    data object SnackbarDismissed : ReviewListIntent
}

sealed interface ReviewListSideEffect {
    data object NavigateBack : ReviewListSideEffect
    data class NavigateToEditReview(
        val productId: Int,
        val orderId: Int,
        val productName: String,
        val productImageUrl: String?,
        val reviewId: Int,
        val rating: Int,
        val content: String
    ) : ReviewListSideEffect
}
