package com.ninezero.halo.feature.store.domain.usecase

import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productId: Int): Result<Product> {
        if (productId <= 0) {
            return Result.failure(IllegalArgumentException("유효하지 않은 상품 ID입니다"))
        }
        return repository.getProduct(productId)
    }
}
