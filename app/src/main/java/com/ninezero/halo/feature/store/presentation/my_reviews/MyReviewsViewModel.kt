package com.ninezero.halo.feature.store.presentation.my_reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.core.util.mergeFreshHead
import com.ninezero.halo.feature.store.domain.event.StoreEvent
import com.ninezero.halo.feature.store.domain.event.StoreEventBus
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.domain.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class MyReviewsViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val storeEventBus: StoreEventBus
) : ViewModel(), OrbitContainerHost<MyReviewsState, MyReviewsState, MyReviewsSideEffect> {

    override val container: OrbitContainer<MyReviewsState, MyReviewsState, MyReviewsSideEffect> =
        orbitContainer(MyReviewsState())

    private val listGeneration = ListGeneration()

    init {
        loadReviews()
        observeStoreEvents()
    }

    // 하위 화면에서 바뀐 항목만 반영
    private fun observeStoreEvents() {
        storeEventBus.events
            .onEach { event ->
                when (event) {
                    is StoreEvent.ReviewUpdated -> intent {
                        val updated = event.review
                        reduce {
                            state.copy(
                                reviews = state.reviews.map {
                                    // 수정 응답에 상품 정보가 없어 로컬 값 보존
                                    if (it.id == updated.id) {
                                        updated.copy(productName = it.productName, productImageUrl = it.productImageUrl, canAccess = it.canAccess)
                                    } else {
                                        it
                                    }
                                }
                            )
                        }
                    }

                    is StoreEvent.ReviewDeleted -> intent {
                        reduce { state.copy(reviews = state.reviews.filter { it.id != event.reviewId }) }
                    }

                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: MyReviewsIntent) {
        when (intent) {
            is MyReviewsIntent.LoadReviews -> loadReviews()
            is MyReviewsIntent.LoadMore -> loadMore()
            is MyReviewsIntent.Refresh -> refresh()
            is MyReviewsIntent.ScreenResumed -> refreshReviewsQuietly()
            is MyReviewsIntent.ShowOptions -> showOptions(intent.review)
            is MyReviewsIntent.DismissOptions -> dismissOptions()
            is MyReviewsIntent.ShowDeleteConfirm -> showDeleteConfirm()
            is MyReviewsIntent.DismissDeleteConfirm -> dismissDeleteConfirm()
            is MyReviewsIntent.ConfirmDelete -> deleteReview()
            is MyReviewsIntent.NavigateBack -> navigateBack()
            is MyReviewsIntent.NavigateToProductDetail -> navigateToProductDetail(intent.review)
            is MyReviewsIntent.EditReview -> editReview(intent.review)
            is MyReviewsIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadReviews() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        reviewRepository.getMyReviews(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    state.copy(
                        isLoading = false,
                        reviews = result.items,
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "리뷰를 불러올 수 없습니다"
                    )
                }
            }
    }

    // 첫 페이지만 받아 앞에 합침
    private fun refreshReviewsQuietly() = intent {
        if (state.isLoading || state.isRefreshing) return@intent
        reviewRepository.getMyReviews(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    val merged = state.reviews.mergeFreshHead(result.items) { it.id }
                    // 뒷부분을 이어 붙였으면 다음 커서도 그대로
                    val keepsTail = merged.size > result.items.size
                    state.copy(
                        reviews = merged,
                        hasMore = if (keepsTail) state.hasMore else result.nextCursor != null,
                        nextCursor = if (keepsTail) state.nextCursor else result.nextCursor,
                        error = null
                    )
                }
            }
    }

    private fun refresh() = intent {
        reduce { state.copy(isRefreshing = true, error = null) }
        reviewRepository.getMyReviews(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    state.copy(
                        isRefreshing = false,
                        isLoading = false,
                        reviews = result.items,
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

    private fun loadMore(): Job = intent {  // 자기를 다시 불러 타입 명시
        if (!state.hasMore || state.isLoadingMore || state.isLoading) return@intent

        reduce { state.copy(isLoadingMore = true) }

        val generation = listGeneration.current()
        val cursor = state.nextCursor

        reviewRepository.getMyReviews(cursor = cursor, limit = 20)
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
                        reviews = (state.reviews + result.items).distinctBy { it.id },
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

    private fun editReview(review: Review) = intent {
        reduce { state.copy(showOptionSheet = false) }
        postSideEffect(
            MyReviewsSideEffect.NavigateToEditReview(
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

    private fun navigateBack() = intent {
        postSideEffect(MyReviewsSideEffect.NavigateBack)
    }

    private fun navigateToProductDetail(review: Review) = intent {
        // 멤버십 전용 상품 접근 제한 체크
        if (!review.canAccess) {
            reduce { state.copy(snackbarState = SnackbarState("멤버십 전용 상품입니다")) }
            return@intent
        }
        postSideEffect(MyReviewsSideEffect.NavigateToProductDetail(review.productId))
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    // 리뷰 옵션 시트
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
