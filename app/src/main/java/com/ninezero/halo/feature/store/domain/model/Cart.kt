package com.ninezero.halo.feature.store.domain.model

data class Cart(
    val items: List<CartItem>,
)

data class CartItem(
    val id: Int,
    val product: Product,
    val quantity: Int,
)
