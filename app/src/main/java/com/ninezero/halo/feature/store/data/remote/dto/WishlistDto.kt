package com.ninezero.halo.feature.store.data.remote.dto

import com.ninezero.halo.core.network.CursorDto
import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.core.util.toEnumOrDefault
import com.ninezero.halo.feature.store.domain.model.Wishlist
import com.ninezero.halo.feature.store.domain.model.WishlistProduct
import com.ninezero.halo.feature.store.domain.model.WishlistPurchaseState
import kotlinx.serialization.Serializable

@Serializable
data class WishlistDto(
    val id: Int,
    val userId: Int,
    val product: WishlistProductDto,
    val createdAt: String
)

@Serializable
data class WishlistProductDto(
    val id: Int,
    val name: String,
    val description: String,
    val price: String,
    val originalPrice: String? = null,
    val discountRate: Int? = null,
    val imageUrl: String? = null,
    val stock: Int,
    val status: String,
    val isActive: Boolean,
    val canAccess: Boolean,
    val purchaseState: String,
    val rating: String,
    val reviewCount: Int,
    val creator: UserSummaryDto
)

typealias WishlistListResponseDto = CursorDto<WishlistDto>

@Serializable
data class WishlistToggleResponseDto(
    val isWishlisted: Boolean,
    val wishlistCount: Int,
    val message: String
)

@Serializable
data class WishlistStatusResponseDto(
    val isWishlisted: Boolean
)

fun WishlistDto.toDomain(): Wishlist = Wishlist(
    id = id,
    product = product.toDomain(),
)

fun WishlistProductDto.toDomain(): WishlistProduct = WishlistProduct(
    id = id,
    name = name,
    price = price,
    originalPrice = originalPrice,
    discountRate = discountRate,
    imageUrl = imageUrl,
    purchaseState = purchaseState.toWishlistPurchaseState(),
)

private fun String.toWishlistPurchaseState(): WishlistPurchaseState =
    toEnumOrDefault(WishlistPurchaseState.PURCHASABLE)
