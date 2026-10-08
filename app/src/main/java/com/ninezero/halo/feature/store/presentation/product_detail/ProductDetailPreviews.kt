package com.ninezero.halo.feature.store.presentation.product_detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.domain.model.ReviewSummary
import com.ninezero.halo.feature.store.presentation.product_detail.components.ProductDetailSkeleton
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo

private val previewCreator = previewUserSummary(
    username = "creator_shop",
    displayName = "크리에이터 샵",
    role = "CREATOR",
)

// 프리뷰 전용 이미지
private val testImageUrl = "android.resource://com.ninezero.halo/${R.drawable.ic_img_placeholder}"

private val previewProduct = Product(
    id = 1,
    name = "프리미엄 무선 이어폰 Pro Max",
    description = "최고급 사운드 품질의 무선 이어폰입니다.\n노이즈 캔슬링 기능과 30시간 배터리를 지원합니다.\n인체공학적 설계로 편안한 착용감을 제공합니다.\n최신 블루투스 5.3 기술을 적용하여 안정적인 연결을 제공합니다.\nIPX5 방수 등급으로 운동 시에도 안심하고 사용할 수 있습니다.",
    price = "89000",
    originalPrice = "129000",
    discountRate = 31,
    imageUrls = listOf(testImageUrl, testImageUrl, testImageUrl),
    detailContent = emptyList(),
    brandName = "SoundWave",
    tags = listOf("이어폰", "무선", "노이즈캔슬링"),
    stock = 50,
    status = "ACTIVE",
    isActive = true,
    viewCount = 1234,
    likeCount = 89,
    salesCount = 567,
    creator = previewCreator,
    requiredTier = "FREE",
    canAccess = true,
)

private val previewReviewSummary = ReviewSummary(
    avgRating = 4.5,
    totalReviews = 128,
    ratingDistribution = mapOf(5 to 72, 4 to 35, 3 to 12, 2 to 6, 1 to 3)
)

private val previewReviews = listOf(
    Review(
        id = 1,
        productId = 1,
        orderId = 1,
        user = previewUserSummary(id = 10, username = "user1", displayName = "음악러버"),
        rating = 5,
        content = "음질이 정말 뛰어납니다! 노이즈 캔슬링도 완벽하고 배터리도 오래가요.",
        images = listOf(
            "https://picsum.photos/200/200?random=1",
            "https://picsum.photos/200/200?random=2",
            "https://picsum.photos/200/200?random=3"
        ),
        createdAt = System.currentTimeMillis() - 86400000,
    ),
    Review(
        id = 2,
        productId = 1,
        orderId = 2,
        user = previewUserSummary(id = 11, username = "user2", displayName = "테크리뷰어"),
        rating = 4,
        content = "가성비 좋은 이어폰입니다. 착용감도 편하네요.",
        images = emptyList(),
        createdAt = System.currentTimeMillis() - 172800000,
    )
)

private val previewCreatorProducts = listOf(
    Product(
        id = 11,
        name = "블루투스 스피커 미니",
        description = "휴대성과 음질을 모두 잡은 컴팩트 스피커입니다.",
        price = "45000",
        originalPrice = null,
        discountRate = null,
        imageUrls = listOf(testImageUrl),
        detailContent = emptyList(),
        brandName = "SoundWave",
        tags = listOf("스피커", "휴대용"),
        stock = 30,
        status = "ACTIVE",
        isActive = true,
        viewCount = 820,
        likeCount = 41,
        salesCount = 205,
        creator = previewCreator,
        requiredTier = "FREE",
        canAccess = true,
    ),
    Product(
        id = 12,
        name = "노이즈캔슬링 헤드폰",
        description = "장시간 착용해도 편안한 오버이어 헤드폰입니다.",
        price = "159000",
        originalPrice = "199000",
        discountRate = 20,
        imageUrls = listOf(testImageUrl),
        detailContent = emptyList(),
        brandName = "SoundWave",
        tags = listOf("헤드폰", "노이즈캔슬링"),
        stock = 12,
        status = "ACTIVE",
        isActive = true,
        viewCount = 2140,
        likeCount = 132,
        salesCount = 88,
        creator = previewCreator,
        requiredTier = "FREE",
        canAccess = true,
    ),
    Product(
        id = 13,
        name = "USB-C 고속 충전기",
        description = "65W 출력으로 노트북까지 충전 가능합니다.",
        price = "29000",
        originalPrice = null,
        discountRate = null,
        imageUrls = listOf(testImageUrl),
        detailContent = emptyList(),
        brandName = "SoundWave",
        tags = listOf("충전기"),
        stock = 0,
        status = "ACTIVE",
        isActive = true,
        viewCount = 640,
        likeCount = 27,
        salesCount = 310,
        creator = previewCreator,
        requiredTier = "FREE",
        canAccess = true,
    )
)

private val previewRelatedProducts = listOf(
    Product(
        id = 21,
        name = "이어폰 실리콘 팁 세트",
        description = "3가지 사이즈로 구성된 교체용 팁입니다.",
        price = "9000",
        originalPrice = null,
        discountRate = null,
        imageUrls = listOf(testImageUrl),
        detailContent = emptyList(),
        brandName = "FitEar",
        tags = listOf("액세서리"),
        stock = 100,
        status = "ACTIVE",
        isActive = true,
        viewCount = 310,
        likeCount = 12,
        salesCount = 150,
        creator = previewUserSummary(id = 2, username = "fitear", displayName = "핏이어"),
        requiredTier = "FREE",
        canAccess = true,
    ),
    Product(
        id = 22,
        name = "휴대용 이어폰 케이스",
        description = "충격에 강한 하드 케이스입니다.",
        price = "15000",
        originalPrice = "19000",
        discountRate = 21,
        imageUrls = listOf(testImageUrl),
        detailContent = emptyList(),
        brandName = "CarryOn",
        tags = listOf("케이스", "휴대용"),
        stock = 45,
        status = "ACTIVE",
        isActive = true,
        viewCount = 520,
        likeCount = 33,
        salesCount = 96,
        creator = previewUserSummary(id = 3, username = "carryon", displayName = "캐리온"),
        requiredTier = "FREE",
        canAccess = true,
    ),
    Product(
        id = 23,
        name = "멤버십 전용 오디오 케이블",
        description = "구독자만 구매할 수 있는 한정 케이블입니다.",
        price = "39000",
        originalPrice = null,
        discountRate = null,
        imageUrls = listOf(testImageUrl),
        detailContent = emptyList(),
        brandName = "SoundWave",
        tags = listOf("케이블", "한정"),
        stock = 8,
        status = "ACTIVE",
        isActive = true,
        viewCount = 180,
        likeCount = 21,
        salesCount = 14,
        creator = previewCreator,
        requiredTier = "TIER2",
        canAccess = false,
    )
)

private val previewCoupon = Coupon(
    id = 1,
    creatorId = 1,
    code = "SAVE10",
    name = "10% 할인 쿠폰",
    type = CouponType.PERCENTAGE,
    discountValue = "10",
    minOrderAmount = null,
    maxDiscountAmount = null,
    status = UserCouponStatus.AVAILABLE,
    expiresAt = System.currentTimeMillis() + 30 * 86_400_000L,
    daysRemaining = 30
)

@HaloLightDarkPreview
@Composable
private fun ProductDetailDefaultPreview() {
    HaloScreenPreview {
        ProductDetailScreenContent(
            state = ProductDetailState(
                product = previewProduct.copy(
                    stock = 50,
                    originalPrice = null,
                    discountRate = null
                ),
                reviewSummary = previewReviewSummary,
                recentReviews = previewReviews,
                creatorProducts = previewCreatorProducts,
                relatedProducts = previewRelatedProducts,
                cartItemCount = 3
            ),
            onIntent = {},
            onNavigateToUserProfile = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductDetailScreenContentPreview() {
    HaloScreenPreview {
        var showCouponPopup by remember { mutableStateOf(false) }
        ProductDetailScreenContent(
            state = ProductDetailState(
                product = previewProduct.copy(stock = 3),
                reviewSummary = previewReviewSummary,
                recentReviews = previewReviews,
                creatorProducts = previewCreatorProducts,
                relatedProducts = previewRelatedProducts,
                selectedDetailTab = 0,
                isDetailExpanded = false,
                bestCoupon = previewCoupon,
                showCouponDetailDialog = showCouponPopup,
                userTierInfo = UserTierInfo(
                    tier = SubscriptionPlanTier.TIER1,
                    isSubscribed = true
                )
            ),
            onIntent = { intent ->
                when (intent) {
                    is ProductDetailIntent.ShowCouponDetailDialog -> showCouponPopup = true
                    is ProductDetailIntent.DismissCouponDetailDialog -> showCouponPopup = false
                    else -> {}
                }
            },
            onNavigateToUserProfile = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductDetailOutOfStockPreview() {
    HaloScreenPreview {
        ProductDetailScreenContent(
            state = ProductDetailState(
                product = previewProduct.copy(
                    stock = 0,
                    status = "SOLD_OUT",
                    isActive = true
                ),
                reviewSummary = previewReviewSummary,
                recentReviews = previewReviews,
                creatorProducts = previewCreatorProducts,
                relatedProducts = previewRelatedProducts
            ),
            onIntent = {},
            onNavigateToUserProfile = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductDetailDiscontinuedPreview() {
    HaloScreenPreview {
        ProductDetailScreenContent(
            state = ProductDetailState(
                product = previewProduct.copy(
                    stock = 0,
                    status = "DISCONTINUED",
                    isActive = false
                ),
                reviewSummary = previewReviewSummary,
                recentReviews = previewReviews,
                creatorProducts = previewCreatorProducts,
                relatedProducts = previewRelatedProducts
            ),
            onIntent = {},
            onNavigateToUserProfile = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductDetailLoadingPreview() {
    HaloScreenPreview {
        ProductDetailSkeleton()
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductDetailErrorPreview() {
    HaloScreenPreview {
        Box(Modifier.fillMaxSize()) {
            ProductDetailSkeleton()
            DefaultSnackbarHost(
                hostState = SnackbarHostState(),
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
