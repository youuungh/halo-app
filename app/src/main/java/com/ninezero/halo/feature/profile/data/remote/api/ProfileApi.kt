package com.ninezero.halo.feature.profile.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.core.network.PagedDto
import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.feature.feed.data.remote.dto.PostDto
import com.ninezero.halo.feature.feed.data.remote.dto.PostListResponse
import com.ninezero.halo.feature.profile.data.remote.dto.CommunityPostRequest
import com.ninezero.halo.feature.profile.data.remote.dto.CommunityResponseDto
import com.ninezero.halo.feature.profile.data.remote.dto.CreatorProfileSectionDto
import com.ninezero.halo.feature.profile.data.remote.dto.UpdateProfileRequestDto
import com.ninezero.halo.feature.profile.data.remote.dto.UserCommentThreadsResponseDto
import com.ninezero.halo.feature.profile.data.remote.dto.UserProfileDto
import com.ninezero.halo.feature.store.data.remote.dto.CreatorStoreSectionsResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProfileApi {
    /** 내 프로필 조회 */
    @GET("api/v1/profile/me")
    suspend fun getMyProfile(): Response<ApiResponse<UserProfileDto>>

    /** 프로필 조회 (공개) */
    @GET("api/v1/profile/{userId}")
    suspend fun getUserProfile(
        @Path("userId") userId: Int
    ): Response<ApiResponse<UserProfileDto>>

    /** 프로필 수정 */
    @PUT("api/v1/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequestDto
    ): Response<ApiResponse<UserProfileDto>>

    /** 프로필 이미지 업로드 */
    @Multipart
    @POST("api/v1/profile/avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<ApiResponse<UserProfileDto>>

    /** 사용자 댓글 스레드 조회 (공개) */
    @GET("api/v1/users/{userId}/comments/threads")
    suspend fun getUserCommentThreads(
        @Path("userId") userId: Int,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String? = null,
        @Query("contextType") contextType: String? = null
    ): Response<ApiResponse<UserCommentThreadsResponseDto>>

    /** 크리에이터 프로필 섹션 조회 (공개) */
    @GET("api/v1/posts/creator/{creatorId}/sections")
    suspend fun getCreatorHomeSections(
        @Path("creatorId") creatorId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<CreatorProfileSectionDto>>

    /** 크리에이터 스토어 섹션 조회 (공개) */
    @GET("api/v1/products/creator/{creatorId}/sections")
    suspend fun getCreatorStoreSections(
        @Path("creatorId") creatorId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String? = null
    ): Response<ApiResponse<CreatorStoreSectionsResponseDto>>

    /** 크리에이터 고정 포스트 조회 (공개) */
    @GET("api/v1/posts/creator/{creatorId}/pinned")
    suspend fun getPinnedPosts(
        @Path("creatorId") creatorId: Int
    ): Response<ApiResponse<PostListResponse>>

    /** 태그별 포스트 조회 (공개) */
    @GET("api/v1/posts/tag/{tagId}")
    suspend fun getPostsByTag(
        @Path("tagId") tagId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<PostListResponse>>

    /** 커뮤니티 글 목록 조회 (공개) */
    @GET("api/v1/community/creator/{creatorId}")
    suspend fun getCommunityPosts(
        @Path("creatorId") creatorId: Int,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "LATEST"
    ): Response<ApiResponse<CommunityResponseDto>>

    /** 커뮤니티 글 삭제 */
    @DELETE("api/v1/community/{postId}")
    suspend fun deleteCommunityPost(
        @Path("postId") postId: Int
    ): Response<Unit>

    /** 커뮤니티 공지 고정/해제 (Creator) */
    @PUT("api/v1/community/{postId}/pin")
    suspend fun togglePinCommunityPost(
        @Path("postId") postId: Int
    ): Response<ApiResponse<Map<String, Boolean>>>

    /** 커뮤니티 글 작성 */
    @POST("api/v1/community/creator/{creatorId}")
    suspend fun createCommunityPost(
        @Path("creatorId") creatorId: Int,
        @Body request: CommunityPostRequest
    ): Response<ApiResponse<PostDto>>

    /** 사용자 검색 (공개) */
    @GET("api/v1/users/search")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<PagedDto<UserSummaryDto>>>
}
