package com.ninezero.halo.feature.store.presentation.wishlist

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.store.domain.model.Wishlist
import com.ninezero.halo.feature.store.domain.model.WishlistProduct
import com.ninezero.halo.feature.store.domain.model.WishlistPurchaseState

internal fun previewWishlistState(): WishlistState {
    return WishlistState(
        isLoading = false,
        items = previewWishlist,
        hasMore = false
    )
}

internal fun previewWishlistEmptyState(): WishlistState {
    return WishlistState(
        isLoading = false,
        items = emptyList(),
        hasMore = false
    )
}

internal fun previewWishlistLoadingState(): WishlistState {
    return WishlistState(isLoading = true)
}

internal fun previewWishlistErrorState(): WishlistState {
    return WishlistState(
        isLoading = false,
        error = "네트워크 연결을 확인해주세요"
    )
}

private val previewWishlist = listOf(
    Wishlist(
        id = 1,
        product = WishlistProduct(
            id = 1,
            name = "프리미엄 블루투스 이어폰 Pro Max",
            price = "89000",
            originalPrice = "129000",
            discountRate = 31,
            imageUrl = null,
            purchaseState = WishlistPurchaseState.PURCHASABLE,
        ),
    ),
    Wishlist(
        id = 2,
        product = WishlistProduct(
            id = 2,
            name = "기계식 키보드 청축",
            price = "65000",
            originalPrice = null,
            discountRate = null,
            imageUrl = null,
            purchaseState = WishlistPurchaseState.SOLD_OUT,
        ),
    ),
    Wishlist(
        id = 3,
        product = WishlistProduct(
            id = 3,
            name = "무선 충전 패드 슬림",
            price = "35000",
            originalPrice = "45000",
            discountRate = 22,
            imageUrl = null,
            purchaseState = WishlistPurchaseState.DISCONTINUED,
        ),
    )
)

@HaloLightDarkPreview
@Composable
private fun WishlistScreenPreview() {
    HaloScreenPreview {
        WishlistScreenContent(
            state = previewWishlistState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun WishlistScreenLoadingPreview() {
    HaloScreenPreview {
        WishlistScreenContent(
            state = previewWishlistLoadingState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun WishlistScreenEmptyPreview() {
    HaloScreenPreview {
        WishlistScreenContent(
            state = previewWishlistEmptyState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun WishlistScreenErrorPreview() {
    HaloScreenPreview {
        WishlistScreenContent(
            state = previewWishlistErrorState()
        )
    }
}
