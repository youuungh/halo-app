package com.ninezero.halo.feature.store.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.store.data.remote.dto.ReviewDto
import com.ninezero.halo.feature.store.data.remote.dto.ReviewListResponseDto
import com.ninezero.halo.feature.store.data.remote.dto.ReviewSummaryResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {
    /** 상품 리뷰 목록 조회 (공개) */
    @GET("api/v1/reviews/products/{productId}")
    suspend fun getProductReviews(
        @Path("productId") productId: Int,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("sortBy") sortBy: String = "RECENT"  // RECENT/RATING_HIGH/RATING_LOW
    ): Response<ApiResponse<ReviewListResponseDto>>

    /** 리뷰 요약 조회 (공개) */
    @GET("api/v1/reviews/products/{productId}/summary")
    suspend fun getReviewSummary(
        @Path("productId") productId: Int
    ): Response<ApiResponse<ReviewSummaryResponseDto>>

    /** 리뷰 작성 */
    @Multipart
    @POST("api/v1/reviews/products/{productId}")
    suspend fun createReviewWithImages(
        @Path("productId") productId: Int,
        @Part("orderId") orderId: RequestBody,
        @Part("rating") rating: RequestBody,
        @Part("content") content: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<ApiResponse<ReviewDto>>

    /** 리뷰 상세 조회 */
    @GET("api/v1/reviews/{id}")
    suspend fun getReviewById(
        @Path("id") reviewId: Int
    ): Response<ApiResponse<ReviewDto>>

    /** 리뷰 수정 */
    @Multipart
    @PUT("api/v1/reviews/{id}")
    suspend fun updateReviewWithImages(
        @Path("id") reviewId: Int,
        @Part("rating") rating: RequestBody? = null,
        @Part("content") content: RequestBody? = null,
        @Part("keepImages") keepImages: RequestBody? = null,
        @Part images: List<MultipartBody.Part> = emptyList()
    ): Response<ApiResponse<ReviewDto>>

    /** 리뷰 삭제 */
    @DELETE("api/v1/reviews/{id}")
    suspend fun deleteReview(
        @Path("id") reviewId: Int
    ): Response<Unit>

    /** 사용자 리뷰 목록 조회 */
    @GET("api/v1/reviews/users/{userId}")
    suspend fun getUserReviews(
        @Path("userId") userId: Int,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<ReviewListResponseDto>>
}
