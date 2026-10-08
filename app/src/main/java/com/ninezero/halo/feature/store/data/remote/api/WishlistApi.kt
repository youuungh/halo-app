package com.ninezero.halo.feature.store.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.store.data.remote.dto.CartDto
import com.ninezero.halo.feature.store.data.remote.dto.WishlistListResponseDto
import com.ninezero.halo.feature.store.data.remote.dto.WishlistStatusResponseDto
import com.ninezero.halo.feature.store.data.remote.dto.WishlistToggleResponseDto
import retrofit2.Response
import retrofit2.http.*

interface WishlistApi {
    /** 위시리스트 토글 */
    @POST("api/v1/wishlists/products/{productId}")
    suspend fun toggleWishlist(
        @Path("productId") productId: Int
    ): Response<ApiResponse<WishlistToggleResponseDto>>

    /** 위시리스트에서 장바구니로 이동 */
    @POST("api/v1/wishlists/products/{productId}/move-to-cart")
    suspend fun moveToCart(
        @Path("productId") productId: Int
    ): Response<ApiResponse<CartDto>>

    /** 위시리스트 목록 조회 */
    @GET("api/v1/wishlists")
    suspend fun getWishlist(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<WishlistListResponseDto>>

    /** 위시리스트 상태 확인 */
    @GET("api/v1/wishlists/products/{productId}/status")
    suspend fun checkWishlistStatus(
        @Path("productId") productId: Int
    ): Response<ApiResponse<WishlistStatusResponseDto>>
}
