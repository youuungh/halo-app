package com.ninezero.halo.feature.store.presentation.my_products

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.ProductStatus
import com.ninezero.halo.feature.store.presentation.my_products.components.GroupColors
import com.ninezero.halo.feature.tag.domain.model.Tag

private val previewCreator = previewUserSummary(
    username = "creator", displayName = "MyStore",
    role = "CREATOR"
)

private val previewSectionTag1 = Tag(
    id = 1, name = "전자기기", isSectionEnabled = true, itemCount = 2
)

private val previewSectionTag2 = Tag(
    id = 2, name = "패션", isSectionEnabled = true, itemCount = 1
)

private val previewProducts = listOf(
    Product(
        id = 1, name = "프리미엄 블루투스 이어폰 Pro Max", description = "고음질 블루투스 이어폰",
        price = "89000", originalPrice = "110000", discountRate = 19,
        brandName = "TechBrand", tags = listOf("음향", "프리미엄"), stock = 25,
        sectionTag = previewSectionTag1,
        status = ProductStatus.ACTIVE,
        isActive = true, viewCount = 1520, likeCount = 89, salesCount = 45,
        requiredTier = "FREE", canAccess = true,
        imageUrls = emptyList(), creator = previewCreator,
    ),
    Product(
        id = 2, name = "기계식 키보드 청축", description = "기계식 키보드",
        price = "65000", originalPrice = null, discountRate = null,
        brandName = null, tags = listOf("키보드"), stock = 3,
        sectionTag = previewSectionTag1,
        status = ProductStatus.ACTIVE,
        isActive = true, viewCount = 830, likeCount = 42, salesCount = 18,
        requiredTier = "FREE", canAccess = true,
        imageUrls = emptyList(), creator = previewCreator,
    ),
    Product(
        id = 3, name = "오가닉 코튼 티셔츠", description = "유기농 면 티셔츠",
        price = "39000", originalPrice = "49000", discountRate = 20,
        brandName = "EcoWear", tags = listOf("의류"), stock = 0,
        sectionTag = previewSectionTag2,
        status = ProductStatus.SOLD_OUT,
        isActive = false, viewCount = 2100, likeCount = 150, salesCount = 200,
        requiredTier = "FREE", canAccess = true,
        imageUrls = emptyList(), creator = previewCreator,
    ),
    Product(
        id = 4, name = "스테인리스 텀블러", description = "보온 텀블러",
        price = "25000", originalPrice = null, discountRate = null,
        brandName = null, tags = emptyList(), stock = 50,
        status = ProductStatus.ACTIVE,
        isActive = true, viewCount = 300, likeCount = 10, salesCount = 5,
        requiredTier = "FREE", canAccess = true,
        imageUrls = emptyList(), creator = previewCreator,
    )
)

@HaloLightDarkPreview
@Composable
private fun MyProductsListPreview() {
    HaloScreenPreview {
        MyProductsScreenContent(
            state = MyProductsState(
                isLoading = false,
                products = previewProducts
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyProductsWithColorsPreview() {
    HaloScreenPreview {
        MyProductsScreenContent(
            state = MyProductsState(
                isLoading = false,
                products = previewProducts,
                groupColors = mapOf(
                    1 to GroupColors[5],
                    2 to GroupColors[0]
                )
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyProductsLoadingPreview() {
    HaloScreenPreview {
        MyProductsScreenContent(
            state = MyProductsState(isLoading = true)
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyProductsEmptyPreview() {
    HaloScreenPreview {
        MyProductsScreenContent(
            state = MyProductsState(
                isLoading = false,
                products = emptyList()
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyProductsErrorPreview() {
    HaloScreenPreview {
        MyProductsScreenContent(
            state = MyProductsState(
                isLoading = false,
                error = "네트워크 연결을 확인해주세요"
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyProductsDeleteDialogPreview() {
    HaloScreenPreview {
        MyProductsScreenContent(
            state = MyProductsState(
                isLoading = false,
                products = previewProducts,
                showDeleteDialog = true,
                deletingProduct = previewProducts.first()
            )
        )
    }
}
