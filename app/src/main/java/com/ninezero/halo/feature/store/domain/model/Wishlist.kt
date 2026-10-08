package com.ninezero.halo.feature.store.domain.model

data class Wishlist(
    val id: Int,
    val product: WishlistProduct,
)

data class WishlistProduct(
    val id: Int,
    val name: String,
    val price: String,
    val originalPrice: String?,
    val discountRate: Int?,
    val imageUrl: String?,
    val purchaseState: WishlistPurchaseState,
)

enum class WishlistPurchaseState {
    PURCHASABLE,
    NO_ACCESS,
    DISCONTINUED,
    SOLD_OUT
}
