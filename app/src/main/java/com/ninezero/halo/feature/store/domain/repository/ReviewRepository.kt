package com.ninezero.halo.feature.store.domain.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.domain.model.ReviewSummary
import okhttp3.MultipartBody

interface ReviewRepository {
    /** 상품 리뷰 목록 조회 */
    suspend fun getReviews(productId: Int, cursor: String?, limit: Int, sortBy: String): Result<CursorPage<Review>>
    /** 리뷰 요약 조회 */
    suspend fun getReviewSummary(productId: Int): Result<ReviewSummary>

    /** 리뷰 작성 */
    suspend fun createReviewWithImages(
        productId: Int,
        orderId: Int,
        rating: Int,
        content: String,
        images: List<MultipartBody.Part>
    ): Result<Review>
    /** 리뷰 상세 조회 */
    suspend fun getReviewById(reviewId: Int): Result<Review>
    /** 리뷰 수정 */
    suspend fun updateReviewWithImages(
        reviewId: Int,
        rating: Int?,
        content: String?,
        keepImages: List<String>?,
        newImages: List<MultipartBody.Part>
    ): Result<Review>
    /** 리뷰 삭제 */
    suspend fun deleteReview(reviewId: Int): Result<Unit>
    /** 내 리뷰 목록 조회 */
    suspend fun getMyReviews(cursor: String?, limit: Int): Result<CursorPage<Review>>
}
