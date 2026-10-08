package com.ninezero.halo.feature.feed.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.core.network.CursorDto
import com.ninezero.halo.core.network.dto.UploadPrepareDto
import com.ninezero.halo.feature.feed.data.remote.dto.PinPostResponse
import com.ninezero.halo.feature.feed.data.remote.dto.PostDto
import com.ninezero.halo.feature.feed.data.remote.dto.PostRequest
import com.ninezero.halo.feature.feed.data.remote.dto.UpdatePostRequest
import com.ninezero.halo.feature.feed.data.remote.dto.VideoUploadPrepareRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PostApi {
    /** 포스트 상세 조회 (공개) */
    @GET("api/v1/posts/{postId}")
    suspend fun getPostById(
        @Path("postId") postId: Int
    ): Response<ApiResponse<PostDto>>

    /** 사용자 포스트 목록 조회 (공개) */
    @GET("api/v1/posts")
    suspend fun getUserPosts(
        @Query("userId") userId: Int,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String? = null,
        @Query("contextType") contextType: String? = null
    ): Response<ApiResponse<CursorDto<PostDto>>>

    /** 포스트 작성 */
    @POST("api/v1/posts")
    suspend fun createPost(
        @Body request: PostRequest
    ): Response<ApiResponse<PostDto>>

    /** 포스트 수정 */
    @PUT("api/v1/posts/{postId}")
    suspend fun updatePost(
        @Path("postId") postId: Int,
        @Body request: UpdatePostRequest
    ): Response<ApiResponse<PostDto>>

    /** 미디어 포함 포스트 수정 */
    @Multipart
    @PUT("api/v1/posts/{postId}")
    suspend fun updatePostWithMedia(
        @Path("postId") postId: Int,
        @Part("content") content: RequestBody? = null,
        @Part("keepMediaIds") keepMediaIds: RequestBody? = null,
        @Part("reorder") reorder: RequestBody? = null,
        @Part("items") items: RequestBody? = null,
        @Part mediaFiles: List<MultipartBody.Part> = emptyList()
    ): Response<ApiResponse<PostDto>>

    /** 포스트 영상 업로드 준비 */
    @POST("api/v1/posts/{postId}/uploads")
    suspend fun prepareUpload(
        @Path("postId") postId: Int,
        @Body request: VideoUploadPrepareRequest
    ): Response<ApiResponse<UploadPrepareDto>>

    /** 포스트 삭제 */
    @DELETE("api/v1/posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: Int
    ): Response<Unit>

    /** 포스트 피드 고정/해제 (Creator) */
    @PUT("api/v1/posts/{postId}/pin")
    suspend fun togglePinPost(
        @Path("postId") postId: Int
    ): Response<ApiResponse<PinPostResponse>>

    /** 내 포스트 목록 조회 */
    @GET("api/v1/posts/my")
    suspend fun getMyPosts(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String? = null,
        @Query("contextType") contextType: String? = null
    ): Response<ApiResponse<CursorDto<PostDto>>>
}
