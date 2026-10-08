package com.ninezero.halo.feature.feed.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.feed.data.remote.dto.ReportRequestDto
import com.ninezero.halo.feature.feed.data.remote.dto.ReportResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportApi {
    /** 신고 */
    @POST("api/v1/reports")
    suspend fun report(
        @Body request: ReportRequestDto
    ): Response<ApiResponse<ReportResponseDto>>
}
