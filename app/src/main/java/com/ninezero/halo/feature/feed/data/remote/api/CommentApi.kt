package com.ninezero.halo.feature.feed.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.feed.data.remote.dto.CommentDto
import com.ninezero.halo.feature.feed.data.remote.dto.CommentListResponse
import com.ninezero.halo.feature.feed.data.remote.dto.CommentRequest
import com.ninezero.halo.feature.feed.data.remote.dto.UpdateCommentRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface CommentApi {
    /** 댓글 조상 체인 조회 (공개) */
    @GET("api/v1/comments/{commentId}/thread")
    suspend fun getCommentThread(
        @Path("commentId") commentId: Int
    ): Response<ApiResponse<List<CommentDto>>>

    /** 포스트 댓글 목록 조회 (공개) */
    @GET("api/v1/comments")
    suspend fun getCommentsByPostId(
        @Query("postId") postId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "POPULAR"  // POPULAR·LATEST
    ): Response<ApiResponse<CommentListResponse>>

    /** 대댓글 목록 조회 (공개) */
    @GET("api/v1/comments/{commentId}/replies")
    suspend fun getRepliesByCommentId(
        @Path("commentId") commentId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "POPULAR"  // POPULAR·LATEST
    ): Response<ApiResponse<CommentListResponse>>

    /** 댓글 작성 */
    @POST("api/v1/comments")
    suspend fun createComment(
        @Body request: CommentRequest
    ): Response<ApiResponse<CommentDto>>

    /** 댓글 수정 */
    @PUT("api/v1/comments/{commentId}")
    suspend fun updateComment(
        @Path("commentId") commentId: Int,
        @Body request: UpdateCommentRequest
    ): Response<ApiResponse<CommentDto>>

    /** 미디어 포함 댓글 수정 */
    @Multipart
    @PUT("api/v1/comments/{commentId}")
    suspend fun updateCommentWithMedia(
        @Path("commentId") commentId: Int,
        @Part("content") content: RequestBody? = null,
        @Part("keepMediaIds") keepMediaIds: RequestBody? = null,
        @Part("reorder") reorder: RequestBody? = null,
        @Part mediaFiles: List<MultipartBody.Part> = emptyList()
    ): Response<ApiResponse<CommentDto>>

    /** 댓글 삭제 */
    @DELETE("api/v1/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: Int
    ): Response<Unit>
}
