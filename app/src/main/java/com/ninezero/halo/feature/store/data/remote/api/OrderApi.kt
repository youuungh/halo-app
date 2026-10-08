package com.ninezero.halo.feature.store.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.store.data.remote.dto.CreateDirectOrderRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.CreateOrderRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.CreatorMonthlySalesDto
import com.ninezero.halo.feature.store.data.remote.dto.CreatorSalesSummaryDto
import com.ninezero.halo.feature.store.data.remote.dto.OrderDto
import com.ninezero.halo.feature.store.data.remote.dto.OrderListResponseDto
import com.ninezero.halo.feature.store.data.remote.dto.ShippingStatusResponseDto
import com.ninezero.halo.feature.store.data.remote.dto.UpdateOrderStatusRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.UpdateShippingRequestDto
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    /** 주문 생성 */
    @POST("api/v1/orders")
    suspend fun createOrder(
        @Body request: CreateOrderRequestDto
    ): Response<ApiResponse<OrderDto>>

    /** 바로구매 주문 생성 */
    @POST("api/v1/orders/direct")
    suspend fun createDirectOrder(
        @Body request: CreateDirectOrderRequestDto
    ): Response<ApiResponse<OrderDto>>

    /** 주문 상세 조회 */
    @GET("api/v1/orders/{id}")
    suspend fun getOrder(
        @Path("id") orderId: Int
    ): Response<ApiResponse<OrderDto>>

    /** 내 주문 목록 조회 */
    @GET("api/v1/orders/my")
    suspend fun getMyOrders(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<OrderListResponseDto>>

    /** 주문 취소 */
    @DELETE("api/v1/orders/{id}")
    suspend fun cancelOrder(
        @Path("id") orderId: Int,
        @Query("reason") reason: String? = null
    ): Response<ApiResponse<OrderDto>>

    /** 크리에이터 그룹 취소 */
    @DELETE("api/v1/orders/{id}/groups/{creatorId}")
    suspend fun cancelOrderGroup(
        @Path("id") orderId: Int,
        @Path("creatorId") creatorId: Int,
        @Query("reason") reason: String? = null
    ): Response<ApiResponse<OrderDto>>

    /** 판매자 주문 목록 조회 (Creator) */
    @GET("api/v1/orders/creator")
    suspend fun getCreatorOrders(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("filter") filter: String? = null
    ): Response<ApiResponse<OrderListResponseDto>>

    /** 판매자 매출 요약 (Creator) */
    @GET("api/v1/orders/creator/sales/summary")
    suspend fun getCreatorSalesSummary(): Response<ApiResponse<CreatorSalesSummaryDto>>

    /** 판매자 월별 매출 통계 (Creator) */
    @GET("api/v1/orders/creator/sales/stats")
    suspend fun getCreatorMonthlySales(
        @Query("month") month: String? = null
    ): Response<ApiResponse<CreatorMonthlySalesDto>>

    /** 주문 상태 변경 (Creator) */
    @PATCH("api/v1/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Int,
        @Body request: UpdateOrderStatusRequestDto
    ): Response<ApiResponse<OrderDto>>

    /** 배송 정보 등록 (Creator) */
    @PUT("api/v1/orders/{id}/shipping")
    suspend fun updateShippingInfo(
        @Path("id") orderId: Int,
        @Body request: UpdateShippingRequestDto
    ): Response<ApiResponse<OrderDto>>

    /** 배송 상태 조회 */
    @GET("api/v1/orders/{id}/shipping")
    suspend fun getShippingStatus(
        @Path("id") orderId: Int,
        @Query("creatorId") creatorId: Int? = null
    ): Response<ApiResponse<ShippingStatusResponseDto>>
}
