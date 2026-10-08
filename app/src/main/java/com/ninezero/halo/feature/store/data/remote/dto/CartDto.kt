package com.ninezero.halo.feature.store.data.remote.dto

import com.ninezero.halo.feature.store.domain.model.Cart
import com.ninezero.halo.feature.store.domain.model.CartItem
import kotlinx.serialization.Serializable

@Serializable
data class CartDto(
    val items: List<CartItemDto>,
    val totalItems: Int,
    val totalPrice: String
)

@Serializable
data class CartItemDto(
    val id: Int,
    val product: ProductDto,
    val quantity: Int,
    val subtotal: String,
    val createdAt: String
)

@Serializable
data class CartItemCountResponseDto(
    val count: Int
)

@Serializable
data class AddToCartRequestDto(
    val productId: Int,
    val quantity: Int
)

@Serializable
data class UpdateCartRequestDto(
    val quantity: Int
)

fun CartDto.toDomain(): Cart = Cart(
    items = items.map { it.toDomain() },
)

fun CartItemDto.toDomain(): CartItem = CartItem(
    id = id,
    product = product.toDomain(),
    quantity = quantity,
)
