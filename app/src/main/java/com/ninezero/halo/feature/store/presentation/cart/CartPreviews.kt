package com.ninezero.halo.feature.store.presentation.cart

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.store.domain.model.Cart
import com.ninezero.halo.feature.store.domain.model.CartItem
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo

@HaloLightDarkPreview
@Composable
private fun CartScreenPreview() {
    val creatorA = previewUserSummary(
        id = 1, username = "creator_a", displayName = "크리에이터A"
    )
    val creatorB = previewUserSummary(
        id = 2, username = "creator_b", displayName = "크리에이터B"
    )
    val sampleItems = listOf(
        CartItem(
            id = 1,
            product = Product(
                id = 1,
                name = "프리미엄 에코백 토트백 내추럴 베이지 대용량 숄더백",
                description = "",
                price = "29000",
                originalPrice = "35000",
                discountRate = 17,
                imageUrls = emptyList(),
                brandName = null,
                tags = emptyList(),
                stock = 10,
                status = "ACTIVE",
                isActive = true,
                viewCount = 0,
                likeCount = 0,
                salesCount = 0,
                creator = creatorA,
                requiredTier = "NONE",
                canAccess = true,
                shippingFee = "3000",
            ),
            quantity = 2,
        ),
        CartItem(
            id = 2,
            product = Product(
                id = 2,
                name = "한정판 아트 포스터 A3 인테리어 감성 일러스트 벽장식",
                description = "",
                price = "15000",
                originalPrice = null,
                discountRate = null,
                imageUrls = emptyList(),
                brandName = null,
                tags = emptyList(),
                stock = 5,
                status = "ACTIVE",
                isActive = true,
                viewCount = 0,
                likeCount = 0,
                salesCount = 0,
                creator = creatorA,
                requiredTier = "NONE",
                canAccess = true,
                shippingFee = "3000",
            ),
            quantity = 1,
        ),
        CartItem(
            id = 3,
            product = Product(
                id = 3,
                name = "핸드메이드 소이왁스 향초 세트 라벤더 & 바닐라 3종 기프트박스",
                description = "",
                price = "25000",
                originalPrice = "30000",
                discountRate = 17,
                imageUrls = emptyList(),
                brandName = null,
                tags = emptyList(),
                stock = 20,
                status = "ACTIVE",
                isActive = true,
                viewCount = 0,
                likeCount = 0,
                salesCount = 0,
                creator = creatorB,
                requiredTier = "NONE",
                canAccess = true,
                shippingFee = "2500",
            ),
            quantity = 1,
        )
    )
    HaloScreenPreview {
        CartScreenContent(
            state = CartState(
                isLoading = false,
                cart = Cart(
                    items = sampleItems,
                ),
                selectedItemIds = setOf(1, 2, 3),
                creatorTierMap = mapOf(
                    1 to UserTierInfo(
                        tier = SubscriptionPlanTier.TIER1,
                        isSubscribed = true
                    )
                )
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CartOutOfStockPreview() {
    val creator = previewUserSummary(
        id = 1, username = "creator_a", displayName = "크리에이터A"
    )
    val sampleItems = listOf(
        CartItem(
            id = 1,
            product = Product(
                id = 1,
                name = "정상 판매 상품 에코백 토트백 내추럴 베이지",
                description = "",
                price = "29000",
                originalPrice = "35000",
                discountRate = 17,
                imageUrls = emptyList(),
                brandName = null,
                tags = emptyList(),
                stock = 10,
                status = "ACTIVE",
                isActive = true,
                viewCount = 0,
                likeCount = 0,
                salesCount = 0,
                creator = creator,
                requiredTier = "NONE",
                canAccess = true,
                shippingFee = "3000",
            ),
            quantity = 2,
        ),
        CartItem(
            id = 2,
            product = Product(
                id = 2,
                name = "품절된 상품 한정판 아트 포스터 A3 인테리어 감성",
                description = "",
                price = "15000",
                originalPrice = null,
                discountRate = null,
                imageUrls = emptyList(),
                brandName = null,
                tags = emptyList(),
                stock = 0,
                status = "SOLD_OUT",
                isActive = true,
                viewCount = 0,
                likeCount = 0,
                salesCount = 0,
                creator = creator,
                requiredTier = "NONE",
                canAccess = true,
                shippingFee = "3000",
            ),
            quantity = 1,
        )
    )
    HaloScreenPreview {
        CartScreenContent(
            state = CartState(
                isLoading = false,
                cart = Cart(
                    items = sampleItems,
                ),
                selectedItemIds = setOf(1, 2)
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CartDiscontinuedPreview() {
    val creator = previewUserSummary(
        id = 1, username = "creator_a", displayName = "크리에이터A"
    )
    val sampleItems = listOf(
        CartItem(
            id = 1,
            product = Product(
                id = 1,
                name = "정상 판매 상품 핸드메이드 소이왁스 향초 세트",
                description = "",
                price = "25000",
                originalPrice = "30000",
                discountRate = 17,
                imageUrls = emptyList(),
                brandName = null,
                tags = emptyList(),
                stock = 20,
                status = "ACTIVE",
                isActive = true,
                viewCount = 0,
                likeCount = 0,
                salesCount = 0,
                creator = creator,
                requiredTier = "NONE",
                canAccess = true,
                shippingFee = "2500",
            ),
            quantity = 1,
        ),
        CartItem(
            id = 2,
            product = Product(
                id = 2,
                name = "판매종료된 상품 프리미엄 무선 이어폰 Pro Max",
                description = "",
                price = "89000",
                originalPrice = "129000",
                discountRate = 31,
                imageUrls = emptyList(),
                brandName = null,
                tags = emptyList(),
                stock = 0,
                status = "DISCONTINUED",
                isActive = false,
                viewCount = 0,
                likeCount = 0,
                salesCount = 0,
                creator = creator,
                requiredTier = "NONE",
                canAccess = true,
                shippingFee = "0",
            ),
            quantity = 2,
        )
    )
    HaloScreenPreview {
        CartScreenContent(
            state = CartState(
                isLoading = false,
                cart = Cart(
                    items = sampleItems,
                ),
                selectedItemIds = setOf(1, 2)
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CartMembershipRestrictedPreview() {
    val creator = previewUserSummary(
        id = 1, username = "creator_a", displayName = "크리에이터A"
    )
    val sampleItems = listOf(
        CartItem(
            id = 1,
            product = Product(
                id = 1,
                name = "일반 상품 커스텀 머그컵 세트",
                description = "",
                price = "22000",
                originalPrice = "28000",
                discountRate = 21,
                imageUrls = emptyList(),
                brandName = null,
                tags = emptyList(),
                stock = 12,
                status = "ACTIVE",
                isActive = true,
                viewCount = 0,
                likeCount = 0,
                salesCount = 0,
                creator = creator,
                requiredTier = "NONE",
                canAccess = true,
                shippingFee = "3000",
            ),
            quantity = 1,
        ),
        CartItem(
            id = 2,
            product = Product(
                id = 2,
                name = "멤버십 전용 상품 한정판 후드 집업",
                description = "",
                price = "79000",
                originalPrice = null,
                discountRate = null,
                imageUrls = emptyList(),
                brandName = null,
                tags = emptyList(),
                stock = 8,
                status = "ACTIVE",
                isActive = true,
                viewCount = 0,
                likeCount = 0,
                salesCount = 0,
                creator = creator,
                requiredTier = "TIER1",
                canAccess = false,
                shippingFee = "3000",
            ),
            quantity = 1,
        )
    )
    HaloScreenPreview {
        CartScreenContent(
            state = CartState(
                isLoading = false,
                cart = Cart(
                    items = sampleItems,
                ),
                selectedItemIds = setOf(1)
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CartLoadingPreview() {
    HaloScreenPreview {
        CartScreenContent(state = CartState(isLoading = true, cart = null))
    }
}

@HaloLightDarkPreview
@Composable
private fun CartEmptyPreview() {
    HaloScreenPreview {
        CartScreenContent(
            state = CartState(
                isLoading = false,
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CartErrorPreview() {
    HaloScreenPreview {
        CartScreenContent(
            state = CartState(
                isLoading = false,
                error = "장바구니를 불러올 수 없습니다",
                cart = null
            )
        )
    }
}
