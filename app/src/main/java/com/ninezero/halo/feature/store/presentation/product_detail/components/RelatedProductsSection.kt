package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.section.ProductThumbnailRow
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun RelatedProductsSection(
    title: String,
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProductThumbnailRow(
            products = products,
            onProductClick = onProductClick
        )
    }
}

private fun previewProduct(id: Int, name: String, price: String, discount: Int? = null) = Product(
    id = id,
    name = name,
    description = "",
    price = price,
    originalPrice = discount?.let { "129000" },
    discountRate = discount,
    imageUrls = emptyList(),
    brandName = null,
    tags = emptyList(),
    stock = 10,
    status = "ACTIVE",
    isActive = true,
    viewCount = 0,
    likeCount = 0,
    salesCount = 0,
    creator = previewUserSummary(displayName = "크리에이터 샵"),
    requiredTier = "FREE",
    canAccess = true
)

@HaloLightDarkPreview
@Composable
private fun RelatedProductsSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                RelatedProductsSection(
                    title = "이 상품과 함께 보면 좋아요",
                    products = listOf(
                        previewProduct(1, "블루투스 스피커 미니", "45000"),
                        previewProduct(2, "노이즈캔슬링 헤드폰", "159000", discount = 20),
                        previewProduct(3, "이름이 아주 긴 상품입니다 줄바꿈 확인용", "29000")
                    ),
                    onProductClick = {}
                )
            }
        }
    }
}
