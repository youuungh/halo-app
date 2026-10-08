package com.ninezero.halo.feature.store.presentation.checkout

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.address.domain.model.Address
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.feature.point.domain.model.PointBalance
import com.ninezero.halo.feature.store.domain.model.OrderLine
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier

private val previewAddresses = listOf(
    Address(
        id = 1, recipientName = "홍길동", recipientPhone = "010-1234-5678",
        zipCode = "06236", address = "서울특별시 강남구 테헤란로 123 (역삼동)",
        addressDetail = "101동 202호", memo = "부재시 문앞에 놓아주세요"
    ),
    Address(
        id = 2, recipientName = "김철수", recipientPhone = "010-9876-5432",
        zipCode = "04158", address = "서울특별시 마포구 월드컵북로 396",
        addressDetail = "상암동 누리꿈스퀘어", memo = null
    )
)

private val previewCreator = previewUserSummary(
    id = 1, username = "creator", displayName = "크리에이터"
)

private val previewOrderLines = listOf(
    OrderLine(
        product = Product(
            id = 1, name = "프리미엄 블루투스 이어폰", description = "",
            price = "89000", originalPrice = null, discountRate = null,
            tags = emptyList(), stock = 10, status = "ACTIVE", isActive = true,
            imageUrls = emptyList(), brandName = null, viewCount = 0, likeCount = 0, salesCount = 0,
            creator = previewCreator, requiredTier = "FREE", canAccess = true,
        ),
        quantity = 1
    ),
    OrderLine(
        product = Product(
            id = 2, name = "기계식 키보드 청축", description = "",
            price = "65000", originalPrice = null, discountRate = null,
            tags = emptyList(), stock = 5, status = "ACTIVE", isActive = true,
            imageUrls = emptyList(), brandName = null, viewCount = 0, likeCount = 0, salesCount = 0,
            creator = previewCreator, requiredTier = "FREE", canAccess = true,
        ),
        quantity = 1
    ),
    OrderLine(
        product = Product(
            id = 3, name = "무선 충전 마우스", description = "",
            price = "45000", originalPrice = null, discountRate = null,
            tags = emptyList(), stock = 15, status = "ACTIVE", isActive = true,
            imageUrls = emptyList(), brandName = null, viewCount = 0, likeCount = 0, salesCount = 0,
            creator = previewCreator, requiredTier = "FREE", canAccess = true,
        ),
        quantity = 1
    )
)

private val previewCoupons = listOf(
    Coupon(
        id = 1, creatorId = 1, code = "WELCOME10", name = "신규 가입 할인 쿠폰",
        type = CouponType.PERCENTAGE, discountValue = "10", minOrderAmount = "10000",
        maxDiscountAmount = "5000", status = UserCouponStatus.AVAILABLE,
        expiresAt = System.currentTimeMillis() + 30 * 86_400_000L, daysRemaining = 30
    )
)

@HaloLightDarkPreview
@Composable
private fun CheckoutScreenWithAddressPreview() {
    HaloScreenPreview {
        CheckoutScreenContent(
            state = CheckoutState(
                isLoading = false,
                orderLines = previewOrderLines,
                selectedAddress = previewAddresses.first(),
                addresses = previewAddresses,
                shippingName = previewAddresses.first().recipientName,
                shippingPhone = previewAddresses.first().recipientPhone,
                shippingAddress = "[${previewAddresses.first().zipCode}] ${previewAddresses.first().address} ${previewAddresses.first().addressDetail}",
                memo = previewAddresses.first().memo ?: "",
                availableCoupons = previewCoupons,
                pointBalance = PointBalance(totalPoints = 5000),
                subscribedCreatorIds = setOf(1),
                creatorTiers = mapOf(
                    1 to SubscriptionPlanTier.TIER1,
                    2 to SubscriptionPlanTier.FREE
                )
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CheckoutScreenWithDiscountPreview() {
    HaloScreenPreview {
        CheckoutScreenContent(
            state = CheckoutState(
                isLoading = false,
                isProcessing = true,
                orderLines = previewOrderLines,
                selectedAddress = previewAddresses.first(),
                addresses = previewAddresses,
                shippingName = previewAddresses.first().recipientName,
                shippingPhone = previewAddresses.first().recipientPhone,
                shippingAddress = "[${previewAddresses.first().zipCode}] ${previewAddresses.first().address} ${previewAddresses.first().addressDetail}",
                memo = previewAddresses.first().memo ?: "",
                availableCoupons = previewCoupons,
                selectedCoupons = mapOf(1 to previewCoupons[0]),
                couponDiscount = 5000,
                pointBalance = PointBalance(totalPoints = 5000),
                usedPoints = 2000
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CheckoutScreenNoAddressPreview() {
    HaloScreenPreview {
        CheckoutScreenContent(
            state = CheckoutState(
                isLoading = false,
                orderLines = previewOrderLines,
                pointBalance = PointBalance(totalPoints = 0)
            )
        )
    }
}

@HaloLightDarkPreview

@Composable
private fun CheckoutScreenLoadingPreview() {
    HaloScreenPreview {
        CheckoutScreenContent(
            state = CheckoutState(
                isLoading = true,
                orderLines = null
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CheckoutScreenErrorPreview() {
    HaloScreenPreview {
        CheckoutScreenContent(
            state = CheckoutState(
                isLoading = false,
                orderLines = null,
                error = "네트워크 연결을 확인해주세요"
            )
        )
    }
}
