package com.ninezero.halo.feature.store.data.repository

import com.ninezero.halo.core.network.ApiException
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.feature.store.data.remote.api.CartApi
import com.ninezero.halo.feature.store.data.remote.dto.AddToCartRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.UpdateCartRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.toDomain
import com.ninezero.halo.feature.store.domain.model.Cart
import com.ninezero.halo.feature.store.domain.model.CartItem
import com.ninezero.halo.feature.store.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val api: CartApi
) : CartRepository {
    private val _cartCount = MutableStateFlow(0)
    override val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

    override suspend fun getCart(): Result<Cart> {
        return safeApiCall { api.getCart() }
            .map { cartDto ->
                _cartCount.value = cartDto.totalItems
                cartDto.toDomain()
            }
    }

    override suspend fun refreshCartCount(): Result<Int> {
        return safeApiCall { api.getCartItemCount() }
            .map { response ->
                _cartCount.value = response.count
                response.count
            }
    }

    override fun resetCartCount() {
        _cartCount.value = 0
    }

    override suspend fun addToCart(productId: Int, quantity: Int): Result<CartItem> {
        return safeApiCall {
            api.addToCart(AddToCartRequestDto(productId, quantity))
        }.mapCatching { cartDto ->
            _cartCount.value = cartDto.totalItems
            cartDto.items.find { it.product.id == productId }?.toDomain()
                ?: throw ApiException(code = 0, message = "장바구니에 담은 상품을 찾지 못했습니다")
        }
    }

    override suspend fun updateCartItem(cartItemId: Int, quantity: Int): Result<CartItem> {
        return safeApiCall {
            api.updateCartItem(cartItemId, UpdateCartRequestDto(quantity))
        }.mapCatching { cartDto ->
            _cartCount.value = cartDto.totalItems
            cartDto.items.find { it.id == cartItemId }?.toDomain()
                ?: throw ApiException(code = 0, message = "수정한 장바구니 항목을 찾지 못했습니다")
        }
    }

    override suspend fun removeFromCart(cartItemId: Int): Result<Unit> {
        return safeApiCallUnit { api.removeFromCart(cartItemId) }
            .onSuccess { refreshCartCount() }
    }

    override suspend fun clearCart(): Result<Unit> {
        return safeApiCallUnit { api.clearCart() }
            .onSuccess {
                _cartCount.value = 0
                refreshCartCount()
            }
    }
}
