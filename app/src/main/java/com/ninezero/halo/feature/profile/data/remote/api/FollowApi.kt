package com.ninezero.halo.feature.profile.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.feature.profile.data.remote.dto.FollowListResponseDto
import com.ninezero.halo.feature.profile.data.remote.dto.FollowRequestDto
import com.ninezero.halo.feature.profile.data.remote.dto.FollowResponseDto
import com.ninezero.halo.feature.profile.data.remote.dto.FollowedCreatorsResponseDto
import com.ninezero.halo.feature.profile.data.remote.dto.NotificationSettingResponseDto
import retrofit2.Response
import retrofit2.http.*

interface FollowApi {
    /** 팔로우 */
    @POST("api/v1/follows")
    suspend fun followUser(
        @Body request: FollowRequestDto
    ): Response<ApiResponse<FollowResponseDto>>

    /** 언팔로우 */
    @DELETE("api/v1/follows/{userId}")
    suspend fun unfollowUser(
        @Path("userId") userId: Int
    ): Response<ApiResponse<FollowResponseDto>>

    /** 사용자 팔로워 목록 조회 (공개) */
    @GET("api/v1/follows/{userId}/followers")
    suspend fun getFollowers(
        @Path("userId") userId: Int,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): Response<ApiResponse<FollowListResponseDto>>

    /** 사용자 팔로잉 목록 조회 (공개) */
    @GET("api/v1/follows/{userId}/following")
    suspend fun getFollowing(
        @Path("userId") userId: Int,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): Response<ApiResponse<FollowListResponseDto>>

    /** 내 팔로워 목록 조회 */
    @GET("api/v1/follows/my/followers")
    suspend fun getMyFollowers(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): Response<ApiResponse<FollowListResponseDto>>

    /** 내 팔로잉 목록 조회 */
    @GET("api/v1/follows/my/following")
    suspend fun getMyFollowing(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): Response<ApiResponse<FollowListResponseDto>>

    /** 팔로우 추천 조회 */
    @GET("api/v1/follows/recommendations")
    suspend fun getFollowRecommendations(
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<UserSummaryDto>>>

    /** 팔로우 크리에이터 목록 조회 */
    @GET("api/v1/follows/my/following/creators")
    suspend fun getFollowedCreators(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<FollowedCreatorsResponseDto>>

    /** 새 글 알림 설정 토글 */
    @PUT("api/v1/follows/{userId}/notifications/posts")
    suspend fun toggleNewPostNotification(
        @Path("userId") userId: Int,
        @Query("enabled") enabled: Boolean
    ): Response<ApiResponse<NotificationSettingResponseDto>>

    /** 새 상품 알림 설정 토글 */
    @PUT("api/v1/follows/{userId}/notifications/products")
    suspend fun toggleNewProductNotification(
        @Path("userId") userId: Int,
        @Query("enabled") enabled: Boolean
    ): Response<ApiResponse<NotificationSettingResponseDto>>
}
