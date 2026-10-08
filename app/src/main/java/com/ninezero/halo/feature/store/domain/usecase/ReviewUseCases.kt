package com.ninezero.halo.feature.store.domain.usecase

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.domain.model.ReviewSummary
import com.ninezero.halo.feature.store.domain.repository.ReviewRepository
import javax.inject.Inject

class GetProductReviewsUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(
        productId: Int,
        cursor: String? = null,
        limit: Int = 20,
        sortBy: String = "RECENT"
    ): Result<CursorPage<Review>> {
        if (productId <= 0) {
            return Result.failure(IllegalArgumentException("유효하지 않은 상품 ID입니다"))
        }
        return repository.getReviews(productId, cursor, limit, sortBy)
    }
}

class GetReviewSummaryUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(productId: Int): Result<ReviewSummary> {
        if (productId <= 0) {
            return Result.failure(IllegalArgumentException("유효하지 않은 상품 ID입니다"))
        }
        return repository.getReviewSummary(productId)
    }
}
