package com.ninezero.halo.feature.store.presentation.review_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.core.util.mergeFreshHead
import com.ninezero.halo.feature.auth.domain.usecase.GetCurrentUserIdUseCase
import com.ninezero.halo.feature.store.domain.event.StoreEvent
import com.ninezero.halo.feature.store.domain.event.StoreEventBus
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.domain.repository.ReviewRepository
import com.ninezero.halo.feature.store.domain.usecase.GetProductReviewsUseCase
import com.ninezero.halo.feature.store.domain.usecase.GetReviewSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class ReviewListViewModel @Inject constructor(
    private val getProductReviewsUseCase: GetProductReviewsUseCase,
    private val getReviewSummaryUseCase: GetReviewSummaryUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val reviewRepository: ReviewRepository,
    private val storeEventBus: StoreEventBus,
    savedStateHandle: SavedStateHandle
) : ViewModel(), OrbitContainerHost<ReviewListState, ReviewListState, ReviewListSideEffect> {

    private val productId: Int = savedStateHandle.get<Int>("productId") ?: 0
    private val highlightReviewId: Int? = savedStateHandle.get<Int>("highlightReviewId")
    private var highlightEnsured = false

    // init에서 즉시 로드하므로 초기 상태를 로딩으로 둠
    override val container: OrbitContainer<ReviewListState, ReviewListState, ReviewListSideEffect> =
        orbitContainer(ReviewListState(isLoading = true, highlightReviewId = highlightReviewId))

    private val listGeneration = ListGeneration()

    init {
        loadCurrentUserId()
        loadReviews()
        observeStoreEvents()
    }

    // 하위 화면에서 바뀐 항목만 반영
    private fun observeStoreEvents() {
        storeEventBus.events
            .onEach { event ->
                when (event) {
                    is StoreEvent.ReviewUpdated -> intent {
                        reduce { state.copy(reviews = state.reviews.map { if (it.id == event.review.id) event.review else it }) }
                    }

                    is StoreEvent.ReviewDeleted -> intent {
                        reduce { state.copy(reviews = state.reviews.filter { it.id != event.reviewId }) }
                    }

                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadCurrentUserId() = intent {  // 내 리뷰 더보기 표시 판단
        val userId = getCurrentUserIdUseCase()
        reduce { state.copy(currentUserId = userId) }
    }

    fun onIntent(intent: ReviewListIntent) {
        when (intent) {
            is ReviewListIntent.LoadReviews -> loadReviews()
            is ReviewListIntent.LoadMore -> loadMore()
            is ReviewListIntent.Refresh -> refresh()
            is ReviewListIntent.ScreenResumed -> refreshReviewsQuietly()
            is ReviewListIntent.ChangeSortBy -> changeSortBy(intent.sortBy)
            is ReviewListIntent.ShowOptions -> showOptions(intent.review)
            is ReviewListIntent.DismissOptions -> dismissOptions()
            is ReviewListIntent.ShowDeleteConfirm -> showDeleteConfirm()
            is ReviewListIntent.DismissDeleteConfirm -> dismissDeleteConfirm()
            is ReviewListIntent.ConfirmDelete -> deleteReview()
            is ReviewListIntent.NavigateBack -> navigateBack()
            is ReviewListIntent.EditReview -> editReview(intent.review)
            is ReviewListIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadReviews() = intent {
        reduce { state.copy(isLoading = true, error = null, nextCursor = null) }

        val sortBy = state.sortBy
        val (summaryResult, reviewsResult) = coroutineScope {
            val summary = async { getReviewSummaryUseCase(productId) }
            val reviews = async { getProductReviewsUseCase(productId, sortBy = sortBy.apiValue) }
            summary.await() to reviews.await()
        }
        if (state.sortBy != sortBy) return@intent

        val summary = summaryResult.getOrNull()
        val reviews = reviewsResult.getOrNull()

        if (summary != null && reviews != null) {
            listGeneration.next()
            reduce {
                state.copy(
                    reviews = reviews.items,
                    reviewSummary = summary,
                    isLoading = false,
                    hasMore = reviews.nextCursor != null,
                    nextCursor = reviews.nextCursor,
                    totalReviews = summary.totalReviews
                )
            }
            ensureHighlightReviewLoaded()
        } else {
            val errorMessage = (reviewsResult.exceptionOrNull() ?: summaryResult.exceptionOrNull())?.message
                ?: "리뷰를 불러올 수 없습니다"
            reduce {
                state.copy(
                    isLoading = false,
                    error = errorMessage,
                    snackbarState = SnackbarState(errorMessage)
                )
            }
        }
    }

    /** 하이라이트 리뷰까지 페이지 로드 */
    private fun ensureHighlightReviewLoaded() = intent {
        val targetId = state.highlightReviewId ?: return@intent
        if (highlightEnsured) return@intent
        highlightEnsured = true

        var attempts = 0
        while (state.reviews.none { it.id == targetId } && state.hasMore && attempts < 20) {
            val generation = listGeneration.current()
            val sortBy = state.sortBy
            val result = getProductReviewsUseCase(
                productId,
                cursor = state.nextCursor,
                sortBy = sortBy.apiValue
            ).getOrNull() ?: break
            attempts++
            // 그사이 새로 받았으면 새 목록에서 다시 찾음
            if (listGeneration.current() != generation || state.sortBy != sortBy) continue
            reduce {
                state.copy(
                    reviews = (state.reviews + result.items).distinctBy { it.id },
                    hasMore = result.nextCursor != null,
                    nextCursor = result.nextCursor
                )
            }
        }
    }

    private fun loadMore(): Job = intent {  // 자기를 다시 불러 타입 명시
        if (state.isLoadingMore || state.isLoading || !state.hasMore) return@intent

        reduce { state.copy(isLoadingMore = true) }

        val sortBy = state.sortBy
        val generation = listGeneration.current()
        getProductReviewsUseCase(
            productId,
            cursor = state.nextCursor,
            sortBy = sortBy.apiValue
        )
            .onSuccess { result ->
                if (state.sortBy != sortBy || listGeneration.current() != generation) {
                    reduce { state.copy(isLoadingMore = false) }
                    // 목록 수가 그대로라 트리거 대신 다시 요청
                    if (state.sortBy == sortBy && state.hasMore) loadMore()
                    return@intent
                }
                reduce {
                    state.copy(
                        reviews = (state.reviews + result.items).distinctBy { it.id },
                        isLoadingMore = false,
                        hasMore = result.nextCursor != null,
                        nextCursor = result.nextCursor
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
    private fun refreshReviewsQuietly() = intent {
        if (state.isLoading || state.isRefreshing) return@intent
        val sortBy = state.sortBy
        val summary = getReviewSummaryUseCase(productId).getOrNull()
        // 별점순은 새 리뷰가 앞에 오지 않아 요약만 갱신
        if (sortBy != ReviewSortType.RECENT) {
            reduce {
                state.copy(
                    reviewSummary = summary ?: state.reviewSummary,
                    totalReviews = summary?.totalReviews ?: state.totalReviews
                )
            }
            return@intent
        }
        getProductReviewsUseCase(productId, sortBy = sortBy.apiValue).onSuccess { result ->
            if (state.sortBy != sortBy) return@intent
            listGeneration.next()
            reduce {
                val merged = state.reviews.mergeFreshHead(result.items) { it.id }
                // 뒷부분을 이어 붙였으면 다음 커서도 그대로
                val keepsTail = merged.size > result.items.size
                state.copy(
                    reviews = merged,
                    hasMore = if (keepsTail) state.hasMore else result.nextCursor != null,
                    nextCursor = if (keepsTail) state.nextCursor else result.nextCursor,
                    reviewSummary = summary ?: state.reviewSummary,
                    totalReviews = summary?.totalReviews ?: state.totalReviews,
                    error = null
                )
            }
        }
    }

    private fun refresh() = intent {
        reduce { state.copy(isRefreshing = true, error = null) }

        val sortBy = state.sortBy
        val (summaryResult, reviewsResult) = coroutineScope {
            val summary = async { getReviewSummaryUseCase(productId) }
            val reviews = async { getProductReviewsUseCase(productId, sortBy = sortBy.apiValue) }
            summary.await() to reviews.await()
        }
        if (state.sortBy != sortBy) {
            reduce { state.copy(isRefreshing = false) }
            return@intent
        }

        val summary = summaryResult.getOrNull()
        val reviews = reviewsResult.getOrNull()

        if (summary != null && reviews != null) {
            listGeneration.next()
            reduce {
                state.copy(
                    reviews = reviews.items,
                    reviewSummary = summary,
                    isRefreshing = false,
                    hasMore = reviews.nextCursor != null,
                    nextCursor = reviews.nextCursor,
                    totalReviews = summary.totalReviews
                )
            }
        } else {
            reduce {
                state.copy(
                    isRefreshing = false,
                    snackbarState = SnackbarState(
                        (reviewsResult.exceptionOrNull() ?: summaryResult.exceptionOrNull())?.message
                            ?: "리뷰를 불러올 수 없습니다"
                    )
                )
            }
        }
    }

    private fun changeSortBy(sortBy: ReviewSortType) = intent {
        if (state.sortBy == sortBy) return@intent
        listGeneration.next()  // 옛 정렬로 진행 중이던 더보기 버림
        reduce { state.copy(sortBy = sortBy) }
        loadReviews()
    }

    private fun navigateBack() = intent {
        postSideEffect(ReviewListSideEffect.NavigateBack)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    private fun editReview(review: Review) = intent {
        reduce { state.copy(showOptionSheet = false) }
        postSideEffect(
            ReviewListSideEffect.NavigateToEditReview(
                productId = review.productId,
                orderId = review.orderId,
                productName = review.productName ?: "",
                productImageUrl = review.productImageUrl,
                reviewId = review.id,
                rating = review.rating,
                content = review.content
            )
        )
    }

    // 내 리뷰 옵션 시트
    private fun showOptions(review: Review) = intent {
        reduce { state.copy(selectedReview = review, showOptionSheet = true) }
    }

    private fun dismissOptions() = intent {
        reduce { state.copy(showOptionSheet = false) }
    }

    private fun showDeleteConfirm() = intent {
        reduce { state.copy(showOptionSheet = false, showDeleteConfirm = true) }
    }

    private fun dismissDeleteConfirm() = intent {
        reduce { state.copy(showDeleteConfirm = false, selectedReview = null) }
    }

    private fun deleteReview() = intent {
        val reviewId = state.selectedReview?.id ?: return@intent
        reduce { state.copy(showDeleteConfirm = false) }

        reviewRepository.deleteReview(reviewId)
            .onSuccess {
                reduce {
                    state.copy(
                        reviews = state.reviews.filter { it.id != reviewId },
                        selectedReview = null,
                        snackbarState = SnackbarState("리뷰가 삭제되었습니다")
                    )
                }
                getReviewSummaryUseCase(productId)
                    .onSuccess { summary ->
                        reduce {
                            state.copy(
                                reviewSummary = summary,
                                totalReviews = summary.totalReviews
                            )
                        }
                    }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        selectedReview = null,
                        snackbarState = SnackbarState(error.message ?: "삭제 실패")
                    )
                }
            }
    }
}
