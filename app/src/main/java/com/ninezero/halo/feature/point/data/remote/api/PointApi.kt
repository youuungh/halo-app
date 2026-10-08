package com.ninezero.halo.feature.point.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.point.data.remote.dto.PointBalanceDto
import com.ninezero.halo.feature.point.data.remote.dto.PointHistoryListResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PointApi {
    /** 포인트 잔액 조회 */
    @GET("api/v1/points/balance")
    suspend fun getBalance(): Response<ApiResponse<PointBalanceDto>>

    /** 포인트 내역 조회 */
    @GET("api/v1/points/history")
    suspend fun getHistory(
        @Query("type") type: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ApiResponse<PointHistoryListResponseDto>>
}
