package com.ninezero.halo.feature.store.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.store.data.remote.dto.PaymentPrepareResponseDto
import com.ninezero.halo.feature.store.data.remote.dto.TossConfirmRequestDto
import retrofit2.Response
import retrofit2.http.*

interface PaymentApi {
    /** Mock 결제 */
    @POST("api/v1/payments")
    suspend fun processPayment(
        @Query("orderId") orderId: Int
    ): Response<ApiResponse<String>>

    /** 토스 결제 준비 */
    @POST("api/v1/payments/toss/prepare")
    suspend fun prepareTossPayment(
        @Query("orderId") orderId: Int
    ): Response<ApiResponse<PaymentPrepareResponseDto>>

    /** 토스 결제 승인 */
    @POST("api/v1/payments/toss/confirm")
    suspend fun confirmTossPayment(
        @Body request: TossConfirmRequestDto
    ): Response<ApiResponse<String>>
}
