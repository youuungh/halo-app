package com.ninezero.halo.feature.feed.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.feed.data.remote.dto.LikeResponse
import com.ninezero.halo.feature.feed.data.remote.dto.LikedListResponse
import retrofit2.Response
import retrofit2.http.*

interface LikeApi {
    /** 포스트 좋아요 토글 */
    @POST("api/v1/likes/posts/{postId}")
    suspend fun togglePostLike(
        @Path("postId") postId: Int
    ): Response<ApiResponse<LikeResponse>>

    /** 댓글 좋아요 토글 */
    @POST("api/v1/likes/comments/{commentId}")
    suspend fun toggleCommentLike(
        @Path("commentId") commentId: Int
    ): Response<ApiResponse<LikeResponse>>

    /** 내가 좋아요한 항목 목록 조회 */
    @GET("api/v1/likes/my")
    suspend fun getMyLikedItems(
        @Query("targetType") targetType: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<LikedListResponse>>
}
