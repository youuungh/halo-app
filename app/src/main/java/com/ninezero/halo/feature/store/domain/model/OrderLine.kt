package com.ninezero.halo.feature.store.domain.model

data class OrderLine(  // 장바구니 주문·바로구매 공용
    val product: Product,
    val quantity: Int
) {
    /** 판매가 × 수량 */
    val subtotal: Long
        get() = (product.price.toLongOrNull() ?: 0L) * quantity
}

fun CartItem.toOrderLine() = OrderLine(product = product, quantity = quantity)
