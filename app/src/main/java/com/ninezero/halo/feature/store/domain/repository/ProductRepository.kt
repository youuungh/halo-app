package com.ninezero.halo.feature.store.domain.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.model.PaginatedResult
import com.ninezero.halo.feature.store.domain.model.CreateProductRequest
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.UpdateProductRequest

interface ProductRepository {
    /** 상품 상세 조회 */
    suspend fun getProduct(productId: Int): Result<Product>
    /** 내 상품 목록 조회 */
    suspend fun getMyProducts(cursor: String?, limit: Int): Result<CursorPage<Product>>
    /** 상품 등록 */
    suspend fun createProduct(request: CreateProductRequest): Result<Product>
    /** 상품 수정 */
    suspend fun updateProduct(productId: Int, request: UpdateProductRequest): Result<Product>
    /** 상품 판매 상태 토글 */
    suspend fun toggleProductStatus(productId: Int): Result<Unit>
    /** 타임딜 설정 */
    suspend fun setProductDeal(productId: Int, dealPrice: String, startAtMillis: Long, endAtMillis: Long): Result<Product>
    /** 타임딜 해제 */
    suspend fun clearProductDeal(productId: Int): Result<Product>
    /** 상품 삭제 */
    suspend fun deleteProduct(productId: Int): Result<Unit>
    /** 크리에이터 상품 조회 */
    suspend fun getCreatorProducts(creatorId: Int, excludeProductId: Int, limit: Int): Result<List<Product>>
    /** 연관 상품 조회 */
    suspend fun getRelatedProducts(productId: Int, limit: Int): Result<List<Product>>
    /** 태그별 상품 조회 */
    suspend fun getProductsByTag(tagId: Int, page: Int = 1, limit: Int = 20): Result<PaginatedResult<Product>>
}
