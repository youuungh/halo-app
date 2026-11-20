package com.ninezero.halo.feature.store.domain.usecase

import com.ninezero.halo.feature.store.domain.model.purchaseBlockedMessage
import com.ninezero.halo.feature.store.domain.repository.CartRepository
import com.ninezero.halo.feature.store.domain.repository.ProductRepository
import javax.inject.Inject

/** 주문했던 상품 다시 담기 */
class AddOrderItemToCartUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(productId: Int): Result<Unit> {
        val product = productRepository.getProduct(productId).getOrElse { error ->
            return Result.failure(IllegalStateException(error.message ?: "상품 정보를 확인할 수 없습니다"))
        }
        product.purchaseBlockedMessage()?.let { return Result.failure(IllegalStateException(it)) }

        return cartRepository.addToCart(productId = productId, quantity = 1).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { error -> Result.failure(IllegalStateException(error.message ?: "장바구니 담기 실패")) }
        )
    }
}
