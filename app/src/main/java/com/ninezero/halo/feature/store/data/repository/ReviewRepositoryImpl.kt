package com.ninezero.halo.feature.store.data.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.network.auth.TokenProvider
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.feature.store.data.remote.api.ReviewApi
import com.ninezero.halo.feature.store.data.remote.dto.toDomain
import com.ninezero.halo.feature.store.domain.event.StoreEventBus
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.domain.model.ReviewSummary
import com.ninezero.halo.feature.store.domain.repository.ReviewRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val api: ReviewApi,
    private val tokenProvider: TokenProvider,
    private val storeEventBus: StoreEventBus
) : ReviewRepository {

    override suspend fun getReviews(
        productId: Int,
        cursor: String?,
        limit: Int,
        sortBy: String
    ): Result<CursorPage<Review>> {
        return safeApiCall {
            api.getProductReviews(productId, cursor, limit, sortBy)
        }.map { response -> CursorPage(response.items.map { it.toDomain() }, response.nextCursor) }
    }

    override suspend fun getReviewSummary(productId: Int): Result<ReviewSummary> {
        return safeApiCall {
            api.getReviewSummary(productId)
        }.map { it.toDomain() }
    }

    override suspend fun createReviewWithImages(
        productId: Int,
        orderId: Int,
        rating: Int,
        content: String,
        images: List<MultipartBody.Part>
    ): Result<Review> {
        fun String.toTextBody() = toRequestBody("text/plain".toMediaType())

        return safeApiCall {
            api.createReviewWithImages(
                productId = productId,
                orderId = orderId.toString().toTextBody(),
                rating = rating.toString().toTextBody(),
                content = content.toTextBody(),
                images = images
            )
        }.map { it.toDomain() }
            .onSuccess { storeEventBus.emitReviewCreated(it) }
    }

    override suspend fun getReviewById(reviewId: Int): Result<Review> {
        return safeApiCall {
            api.getReviewById(reviewId)
        }.map { it.toDomain() }
    }

    override suspend fun updateReviewWithImages(
        reviewId: Int,
        rating: Int?,
        content: String?,
        keepImages: List<String>?,
        newImages: List<MultipartBody.Part>
    ): Result<Review> {
        fun String.toTextBody() = toRequestBody("text/plain".toMediaType())

        return safeApiCall {
            api.updateReviewWithImages(
                reviewId = reviewId,
                rating = rating?.toString()?.toTextBody(),
                content = content?.toTextBody(),
                // null이면 파트 자체를 빼서 서버가 기존 목록을 유지하게 함
                keepImages = keepImages?.let { Json.encodeToString(it).toTextBody() },
                images = newImages
            )
        }.map { it.toDomain() }
            .onSuccess { storeEventBus.emitReviewUpdated(it) }
    }

    override suspend fun deleteReview(reviewId: Int): Result<Unit> {
        return safeApiCallUnit {
            api.deleteReview(reviewId)
        }.onSuccess { storeEventBus.emitReviewDeleted(reviewId) }
    }

    override suspend fun getMyReviews(cursor: String?, limit: Int): Result<CursorPage<Review>> {
        val userId = tokenProvider.getUserId()
            ?: return Result.failure(IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다"))

        return safeApiCall {
            api.getUserReviews(userId, cursor, limit)
        }.map { response -> CursorPage(response.items.map { it.toDomain() }, response.nextCursor) }
    }
}
