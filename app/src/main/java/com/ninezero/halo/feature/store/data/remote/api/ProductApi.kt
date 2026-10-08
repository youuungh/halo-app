package com.ninezero.halo.feature.store.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.core.network.CursorDto
import com.ninezero.halo.feature.store.data.remote.dto.CreateProductRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.CreatorStoreSectionsResponseDto
import com.ninezero.halo.feature.store.data.remote.dto.ProductDto
import com.ninezero.halo.feature.store.data.remote.dto.ProductListResponseDto
import com.ninezero.halo.feature.store.data.remote.dto.SetProductDealRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.UpdateProductRequestDto
import retrofit2.Response
import retrofit2.http.*

interface ProductApi {
    /** 상품 상세 조회 (공개) */
    @GET("api/v1/products/{id}")
    suspend fun getProduct(
        @Path("id") productId: Int
    ): Response<ApiResponse<ProductDto>>

    /** 연관 상품 조회 (공개) */
    @GET("api/v1/products/{id}/related")
    suspend fun getRelatedProducts(
        @Path("id") productId: Int,
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<List<ProductDto>>>

    /** 크리에이터 스토어 섹션 조회 (공개) */
    @GET("api/v1/products/creator/{creatorId}/sections")
    suspend fun getCreatorStoreSections(
        @Path("creatorId") creatorId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "LATEST"  // LATEST/PRICE_LOW/PRICE_HIGH
    ): Response<ApiResponse<CreatorStoreSectionsResponseDto>>

    /** 태그별 상품 조회 (공개) */
    @GET("api/v1/products/tag/{tagId}")
    suspend fun getProductsByTag(
        @Path("tagId") tagId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<ProductListResponseDto>>

    /** 상품 등록 (Creator) */
    @POST("api/v1/products")
    suspend fun createProduct(
        @Body request: CreateProductRequestDto
    ): Response<ApiResponse<ProductDto>>

    /** 내 상품 목록 조회 (Creator) */
    @GET("api/v1/products/my")
    suspend fun getMyProducts(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<CursorDto<ProductDto>>>

    /** 상품 수정 (Creator) */
    @PUT("api/v1/products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: Int,
        @Body request: UpdateProductRequestDto
    ): Response<ApiResponse<ProductDto>>

    /** 상품 판매 상태 토글 (Creator) */
    @PATCH("api/v1/products/{id}/status")
    suspend fun toggleProductStatus(
        @Path("id") productId: Int
    ): Response<ApiResponse<String>>

    /** 타임딜 설정/해제 (Creator) */
    @PATCH("api/v1/products/{id}/deal")
    suspend fun setProductDeal(
        @Path("id") productId: Int,
        @Body request: SetProductDealRequestDto
    ): Response<ApiResponse<ProductDto>>

    /** 상품 삭제 (Creator) */
    @DELETE("api/v1/products/{id}")
    suspend fun deleteProduct(
        @Path("id") productId: Int
    ): Response<Unit>
}
