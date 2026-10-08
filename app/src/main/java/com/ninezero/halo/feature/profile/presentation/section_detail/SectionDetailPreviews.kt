package com.ninezero.halo.feature.profile.presentation.section_detail

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewAuthor
import com.ninezero.halo.core.ui.preview.previewMediaItem
import com.ninezero.halo.core.ui.preview.previewPost
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.store.domain.model.Product

private val previewAuthor = previewAuthor(
    username = "creator",
    displayName = "크리에이터"
)

private val previewPosts = List(12) { index ->
    previewPost(
        id = index + 1,
        content = "테스트 포스트 ${index + 1}입니다. 이것은 프리뷰용 더미 컨텐츠입니다.",
        author = previewAuthor,
        mediaAttachments = if (index % 3 != 0) {
            listOf(
                previewMediaItem(
                    type = if (index % 4 == 0) MediaType.VIDEO else MediaType.IMAGE,
                    url = "https://example.com/img$index.jpg"
                )
            )
        } else emptyList(),
        likeCount = 10 + index * 5,
        commentCount = 5 + index,
        createdAtOffset = index * 3600000L
    )
}

private val previewCreator = previewUserSummary(
    username = "creator",
    displayName = "크리에이터"
)

private val previewProducts = listOf(
    Product(
        id = 1, name = "기본 상품입니다", description = "상품 설명입니다.",
        price = "29000.00", originalPrice = null, discountRate = null,
        tags = listOf("인기", "추천"), stock = 100, status = "ACTIVE", isActive = true,
        imageUrls = emptyList(), brandName = null, viewCount = 0, likeCount = 0, salesCount = 0,
        creator = previewCreator, requiredTier = "FREE", canAccess = true,
    ),
    Product(
        id = 2, name = "할인 상품", description = "상품 설명입니다.",
        price = "19000.00", originalPrice = "29000.00", discountRate = 34,
        tags = listOf("세일"), stock = 50, status = "ACTIVE", isActive = true,
        imageUrls = emptyList(), brandName = null, viewCount = 0, likeCount = 0, salesCount = 0,
        creator = previewCreator, requiredTier = "FREE", canAccess = true,
    ),
    Product(
        id = 3, name = "멤버십 전용 상품", description = "상품 설명입니다.",
        price = "50000.00", originalPrice = null, discountRate = null,
        tags = listOf("프리미엄"), stock = 10, status = "ACTIVE", isActive = true,
        imageUrls = emptyList(), brandName = null, viewCount = 0, likeCount = 0, salesCount = 0,
        creator = previewCreator, requiredTier = "TIER2", canAccess = false,
    ),
    Product(
        id = 4, name = "품절 상품", description = "상품 설명입니다.",
        price = "35000.00", originalPrice = null, discountRate = null,
        tags = listOf("인기"), stock = 0, status = "ACTIVE", isActive = true,
        imageUrls = emptyList(), brandName = null, viewCount = 0, likeCount = 0, salesCount = 0,
        creator = previewCreator, requiredTier = "FREE", canAccess = true,
    ),
    Product(
        id = 5, name = "멤버십 할인 상품", description = "상품 설명입니다.",
        price = "40000.00", originalPrice = "50000.00", discountRate = 20,
        tags = listOf("한정"), stock = 30, status = "ACTIVE", isActive = true,
        imageUrls = emptyList(), brandName = null, viewCount = 0, likeCount = 0, salesCount = 0,
        creator = previewCreator, requiredTier = "TIER1", canAccess = true,
    ),
    Product(
        id = 6, name = "긴 이름의 상품입니다 아주 긴 이름", description = "상품 설명입니다.",
        price = "15000.00", originalPrice = null, discountRate = null,
        tags = listOf("신상", "베스트"), stock = 200, status = "ACTIVE", isActive = true,
        imageUrls = emptyList(), brandName = null, viewCount = 0, likeCount = 0, salesCount = 0,
        creator = previewCreator, requiredTier = "FREE", canAccess = true,
    )
)

@HaloLightDarkPreview
@Composable
private fun SectionDetailPostsPreview() {
    HaloScreenPreview {
        SectionDetailScreenContent(
            state = SectionDetailState(
                sectionType = SectionType.POST_TAG,
                title = "일상 브이로그",
                posts = previewPosts,
                hasMore = true
            ),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SectionDetailProductsPreview() {
    HaloScreenPreview {
        SectionDetailScreenContent(
            state = SectionDetailState(
                sectionType = SectionType.PRODUCT_TAG,
                title = "인기 상품",
                products = previewProducts,
                hasMore = true
            ),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SectionDetailLoadingPreview() {
    HaloScreenPreview {
        SectionDetailScreenContent(
            state = SectionDetailState(
                sectionType = SectionType.POST_TAG,
                title = "여행",
                isLoading = true
            ),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SectionDetailErrorPreview() {
    HaloScreenPreview {
        SectionDetailScreenContent(
            state = SectionDetailState(
                sectionType = SectionType.POST_TAG,
                title = "일상",
                error = "데이터를 불러올 수 없습니다"
            ),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}
