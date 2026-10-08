package com.ninezero.halo.feature.store.domain.repository

import com.ninezero.halo.feature.store.domain.model.Cart
import com.ninezero.halo.feature.store.domain.model.CartItem
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    /** 앱 전역 장바구니 카운트 */
    val cartCount: StateFlow<Int>

    /** 장바구니 조회 */
    suspend fun getCart(): Result<Cart>

    /** 장바구니 수 재동기화 */
    suspend fun refreshCartCount(): Result<Int>

    /** 로컬 장바구니 수 초기화 */
    fun resetCartCount()

    /** 장바구니 담기 */
    suspend fun addToCart(productId: Int, quantity: Int): Result<CartItem>

    /** 장바구니 수량 변경 */
    suspend fun updateCartItem(cartItemId: Int, quantity: Int): Result<CartItem>

    /** 장바구니 항목 삭제 */
    suspend fun removeFromCart(cartItemId: Int): Result<Unit>

    /** 장바구니 비우기 */
    suspend fun clearCart(): Result<Unit>
}
