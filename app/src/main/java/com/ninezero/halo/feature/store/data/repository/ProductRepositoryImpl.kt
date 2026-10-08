package com.ninezero.halo.feature.store.data.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.model.PaginatedResult
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallNoData
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.core.util.toUtcIsoTimestamp
import com.ninezero.halo.feature.store.data.remote.api.ProductApi
import com.ninezero.halo.feature.store.data.remote.dto.ProductDto
import com.ninezero.halo.feature.store.data.remote.dto.SetProductDealRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.toDomain
import com.ninezero.halo.feature.store.data.remote.dto.toDto
import com.ninezero.halo.feature.store.domain.event.StoreEventBus
import com.ninezero.halo.feature.store.domain.model.CreateProductRequest
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.UpdateProductRequest
import com.ninezero.halo.feature.store.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val api: ProductApi,
    private val storeEventBus: StoreEventBus
) : ProductRepository {
    override suspend fun getProduct(productId: Int): Result<Product> {
        return safeApiCall {
            api.getProduct(productId)
        }.map { it.toDomain() }
    }

    override suspend fun getMyProducts(cursor: String?, limit: Int): Result<CursorPage<Product>> {
        return safeApiCall {
            api.getMyProducts(cursor, limit)
        }.map { response ->
            CursorPage(response.items.map { dto: ProductDto -> dto.toDomain() }, response.nextCursor)
        }
    }

    override suspend fun createProduct(request: CreateProductRequest): Result<Product> {
        return safeApiCall {
            api.createProduct(request.toDto())
        }.map { it.toDomain() }
    }

    override suspend fun updateProduct(productId: Int, request: UpdateProductRequest): Result<Product> {
        return safeApiCall {
            api.updateProduct(productId, request.toDto())
        }.map { it.toDomain() }
            .onSuccess { storeEventBus.emitProductUpdated(it) }
    }

    override suspend fun toggleProductStatus(productId: Int): Result<Unit> {
        return safeApiCallNoData {
            api.toggleProductStatus(productId)
        }
    }

    override suspend fun setProductDeal(
        productId: Int,
        dealPrice: String,
        startAtMillis: Long,
        endAtMillis: Long
    ): Result<Product> {
        return safeApiCall {
            api.setProductDeal(
                productId,
                SetProductDealRequestDto(
                    dealPrice = dealPrice,
                    startAt = startAtMillis.toUtcIsoTimestamp(),
                    endAt = endAtMillis.toUtcIsoTimestamp()
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun clearProductDeal(productId: Int): Result<Product> {
        // 모든 필드 null → 서버가 딜 해제로 처리
        return safeApiCall {
            api.setProductDeal(productId, SetProductDealRequestDto())
        }.map { it.toDomain() }
    }

    override suspend fun deleteProduct(productId: Int): Result<Unit> {
        return safeApiCallUnit {
            api.deleteProduct(productId)
        }
    }

    override suspend fun getCreatorProducts(creatorId: Int, excludeProductId: Int, limit: Int): Result<List<Product>> {
        return safeApiCall {
            api.getCreatorStoreSections(creatorId, page = 1, limit = limit + 10)
        }.map { response ->
            response.allProducts.items
                .map { it.toDomain() }
                .filter { it.id != excludeProductId }
                .take(limit)
        }
    }

    override suspend fun getRelatedProducts(productId: Int, limit: Int): Result<List<Product>> {
        return safeApiCall {
            api.getRelatedProducts(productId, limit)
        }.map { products ->
            products.map { it.toDomain() }
        }
    }

    override suspend fun getProductsByTag(
        tagId: Int,
        page: Int,
        limit: Int
    ): Result<PaginatedResult<Product>> {
        return safeApiCall {
            api.getProductsByTag(tagId, page, limit)
        }.map { response ->
            PaginatedResult(response.items.map { it.toDomain() }, response.hasNext)
        }
    }
}
