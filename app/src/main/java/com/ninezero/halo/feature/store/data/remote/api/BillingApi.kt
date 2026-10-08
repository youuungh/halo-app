package com.ninezero.halo.feature.store.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.store.data.remote.dto.BillingIssueRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.BillingKeyResponseDto
import com.ninezero.halo.feature.store.data.remote.dto.BillingPayRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.CustomerKeyResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface BillingApi {
    /** customerKey 조회/발급 */
    @GET("api/v1/billing/customer-key")
    suspend fun getCustomerKey(): Response<ApiResponse<CustomerKeyResponseDto>>

    /** 빌링키 발급 */
    @POST("api/v1/billing/issue")
    suspend fun issueBillingKey(@Body request: BillingIssueRequestDto): Response<ApiResponse<BillingKeyResponseDto>>

    /** 등록 빌링 카드 조회 */
    @GET("api/v1/billing/my")
    suspend fun getMyBillingKey(): Response<ApiResponse<BillingKeyResponseDto>>

    /** 등록 빌링 카드 삭제 */
    @DELETE("api/v1/billing/my")
    suspend fun deleteBillingKey(): Response<Unit>

    /** 빌링키로 주문 결제 */
    @POST("api/v1/billing/pay")
    suspend fun payByBilling(@Body request: BillingPayRequestDto): Response<ApiResponse<String>>
}
