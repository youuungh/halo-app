package com.ninezero.halo.feature.coupon.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.coupon.data.remote.dto.ClaimCouponRequestDto
import com.ninezero.halo.feature.coupon.data.remote.dto.CouponResponseDto
import com.ninezero.halo.feature.coupon.data.remote.dto.CreateCouponRequestDto
import com.ninezero.halo.feature.coupon.data.remote.dto.PaginatedCouponResponseDto
import com.ninezero.halo.feature.coupon.data.remote.dto.UpdateCouponRequestDto
import com.ninezero.halo.feature.coupon.data.remote.dto.UserCouponListResponseDto
import com.ninezero.halo.feature.coupon.data.remote.dto.UserCouponResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CouponApi {
    // 사용자용

    /** 내 쿠폰 목록 조회 */
    @GET("api/v1/coupons/my-list")
    suspend fun getClaimedCoupons(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<UserCouponListResponseDto>>

    /** 쿠폰 받기 */
    @POST("api/v1/coupons/claim")
    suspend fun claimCoupon(
        @Body request: ClaimCouponRequestDto
    ): Response<ApiResponse<UserCouponResponseDto>>

    // 크리에이터용

    /** 내가 만든 쿠폰 목록 조회 (Creator) */
    @GET("api/v1/coupons/my-coupons")
    suspend fun getCreatedCoupons(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<PaginatedCouponResponseDto>>

    /** 쿠폰 생성 (Creator) */
    @POST("api/v1/coupons")
    suspend fun createCoupon(
        @Body request: CreateCouponRequestDto
    ): Response<ApiResponse<CouponResponseDto>>

    /** 쿠폰 수정 (Creator) */
    @PUT("api/v1/coupons/{couponId}")
    suspend fun updateCoupon(
        @Path("couponId") couponId: Int,
        @Body request: UpdateCouponRequestDto
    ): Response<ApiResponse<CouponResponseDto>>

    /** 쿠폰 삭제 (Creator) */
    @DELETE("api/v1/coupons/{couponId}")
    suspend fun deleteCoupon(
        @Path("couponId") couponId: Int
    ): Response<Unit>
}
