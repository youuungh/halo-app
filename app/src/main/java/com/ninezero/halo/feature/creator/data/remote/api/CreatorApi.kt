package com.ninezero.halo.feature.creator.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.creator.data.remote.dto.CreatorApplicationRequest
import com.ninezero.halo.feature.creator.data.remote.dto.CreatorApplicationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface CreatorApi {
    /** 크리에이터 신청 */
    @POST("api/v1/creator/apply")
    suspend fun submitApplication(
        @Body request: CreatorApplicationRequest
    ): Response<ApiResponse<CreatorApplicationResponse>>

    /** 내 신청 조회 */
    @GET("api/v1/creator/application/me")
    suspend fun getMyApplication(): Response<ApiResponse<CreatorApplicationResponse>>

    /** 신청 취소 */
    @DELETE("api/v1/creator/application/me")
    suspend fun cancelApplication(): Response<Unit>

    /** 크리에이터 셀프 해제 */
    @POST("api/v1/creator/revoke")
    suspend fun revokeCreator(): Response<ApiResponse<String>>
}
