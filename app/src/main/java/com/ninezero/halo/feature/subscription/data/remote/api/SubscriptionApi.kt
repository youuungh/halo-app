package com.ninezero.halo.feature.subscription.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.subscription.data.remote.dto.CreateSubscriptionPlanRequestDto
import com.ninezero.halo.feature.subscription.data.remote.dto.SubscribeRequestDto
import com.ninezero.halo.feature.subscription.data.remote.dto.SubscriberListResponseDto
import com.ninezero.halo.feature.subscription.data.remote.dto.SubscriptionDto
import com.ninezero.halo.feature.subscription.data.remote.dto.SubscriptionListResponseDto
import com.ninezero.halo.feature.subscription.data.remote.dto.SubscriptionPlanDto
import com.ninezero.halo.feature.subscription.data.remote.dto.SubscriptionPlanListResponseDto
import com.ninezero.halo.feature.subscription.data.remote.dto.SubscriptionStatusResponseDto
import com.ninezero.halo.feature.subscription.data.remote.dto.UpdateSubscriptionPlanRequestDto
import com.ninezero.halo.feature.subscription.data.remote.dto.UserTierResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SubscriptionApi {
    /** 크리에이터 플랜 목록 조회 (공개) */
    @GET("api/v1/subscription-plans/creator/{creatorId}")
    suspend fun getCreatorPlans(
        @Path("creatorId") creatorId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<SubscriptionPlanListResponseDto>>

    /** 내 플랜 목록 조회 (Creator) */
    @GET("api/v1/subscription-plans/my")
    suspend fun getMyPlans(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<SubscriptionPlanListResponseDto>>

    /** 플랜 생성 (Creator) */
    @POST("api/v1/subscription-plans")
    suspend fun createPlan(
        @Body request: CreateSubscriptionPlanRequestDto
    ): Response<ApiResponse<SubscriptionPlanDto>>

    /** 플랜 수정 (Creator) */
    @PUT("api/v1/subscription-plans/{planId}")
    suspend fun updatePlan(
        @Path("planId") planId: Int,
        @Body request: UpdateSubscriptionPlanRequestDto
    ): Response<ApiResponse<SubscriptionPlanDto>>

    /** 플랜 삭제 (Creator) */
    @DELETE("api/v1/subscription-plans/{planId}")
    suspend fun deletePlan(
        @Path("planId") planId: Int
    ): Response<ApiResponse<String>>

    /** 구독 */
    @POST("api/v1/subscriptions")
    suspend fun subscribe(
        @Body request: SubscribeRequestDto
    ): Response<ApiResponse<SubscriptionDto>>

    /** 구독 업그레이드 */
    @POST("api/v1/subscriptions/upgrade")
    suspend fun upgradeSubscription(
        @Body request: SubscribeRequestDto
    ): Response<ApiResponse<SubscriptionDto>>

    /** 내 구독 목록 조회 */
    @GET("api/v1/subscriptions/my")
    suspend fun getMySubscriptions(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<SubscriptionListResponseDto>>

    /** 내 구독자 목록 조회 (Creator) */
    @GET("api/v1/subscriptions/subscribers")
    suspend fun getMySubscribers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): Response<ApiResponse<SubscriberListResponseDto>>

    /** 크리에이터 구독 상태 확인 */
    @GET("api/v1/subscriptions/check/{creatorId}")
    suspend fun getSubscriptionStatus(
        @Path("creatorId") creatorId: Int
    ): Response<ApiResponse<SubscriptionStatusResponseDto>>

    /** 내 구독 티어 확인 */
    @GET("api/v1/subscriptions/my-tier/{creatorId}")
    suspend fun getCreatorTier(
        @Path("creatorId") creatorId: Int
    ): Response<ApiResponse<UserTierResponseDto>>

    /** 구독 취소 */
    @DELETE("api/v1/subscriptions/{subscriptionId}")
    suspend fun cancelSubscription(
        @Path("subscriptionId") subscriptionId: Int
    ): Response<ApiResponse<String>>

    /** 자동갱신 설정 변경 */
    @PATCH("api/v1/subscriptions/{subscriptionId}/auto-renew")
    suspend fun updateAutoRenew(
        @Path("subscriptionId") subscriptionId: Int,
        @Query("autoRenew") autoRenew: Boolean
    ): Response<ApiResponse<String>>
}
