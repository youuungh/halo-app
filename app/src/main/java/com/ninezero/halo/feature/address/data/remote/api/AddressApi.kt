package com.ninezero.halo.feature.address.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.address.data.remote.dto.AddressResponseDto
import com.ninezero.halo.feature.address.data.remote.dto.CreateAddressRequestDto
import com.ninezero.halo.feature.address.data.remote.dto.UpdateAddressRequestDto
import retrofit2.Response
import retrofit2.http.*

interface AddressApi {
    /** 배송지 목록 조회 */
    @GET("api/v1/addresses")
    suspend fun getAddresses(): Response<ApiResponse<List<AddressResponseDto>>>

    /** 배송지 상세 조회 */
    @GET("api/v1/addresses/{id}")
    suspend fun getAddress(
        @Path("id") id: Int
    ): Response<ApiResponse<AddressResponseDto>>

    /** 배송지 추가 */
    @POST("api/v1/addresses")
    suspend fun createAddress(
        @Body request: CreateAddressRequestDto
    ): Response<ApiResponse<AddressResponseDto>>

    /** 배송지 수정 */
    @PUT("api/v1/addresses/{id}")
    suspend fun updateAddress(
        @Path("id") id: Int,
        @Body request: UpdateAddressRequestDto
    ): Response<ApiResponse<AddressResponseDto>>

    /** 배송지 삭제 */
    @DELETE("api/v1/addresses/{id}")
    suspend fun deleteAddress(
        @Path("id") id: Int
    ): Response<Unit>
}
