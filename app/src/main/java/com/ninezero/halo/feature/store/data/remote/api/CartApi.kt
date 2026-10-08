package com.ninezero.halo.feature.store.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.store.data.remote.dto.AddToCartRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.CartDto
import com.ninezero.halo.feature.store.data.remote.dto.CartItemCountResponseDto
import com.ninezero.halo.feature.store.data.remote.dto.UpdateCartRequestDto
import retrofit2.Response
import retrofit2.http.*

interface CartApi {
    /** 장바구니 조회 */
    @GET("api/v1/cart")
    suspend fun getCart(): Response<ApiResponse<CartDto>>

    /** 장바구니 항목 수 조회 */
    @GET("api/v1/cart/count")
    suspend fun getCartItemCount(): Response<ApiResponse<CartItemCountResponseDto>>

    /** 장바구니 담기 */
    @POST("api/v1/cart")
    suspend fun addToCart(@Body request: AddToCartRequestDto): Response<ApiResponse<CartDto>>

    /** 장바구니 수량 변경 */
    @PUT("api/v1/cart/{id}")
    suspend fun updateCartItem(
        @Path("id") cartItemId: Int,
        @Body request: UpdateCartRequestDto
    ): Response<ApiResponse<CartDto>>

    /** 장바구니 항목 삭제 */
    @DELETE("api/v1/cart/{id}")
    suspend fun removeFromCart(@Path("id") cartItemId: Int): Response<Unit>

    /** 장바구니 비우기 */
    @DELETE("api/v1/cart")
    suspend fun clearCart(): Response<Unit>
}
