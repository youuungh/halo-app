package com.ninezero.halo.feature.store.presentation.cart.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.MembershipColors
import com.ninezero.halo.feature.store.domain.model.CartItem
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.ProductStatus
import com.ninezero.halo.feature.store.domain.model.calcEarnPoints
import com.ninezero.halo.feature.store.domain.model.calcStorePricing
import com.ninezero.halo.feature.store.domain.model.toOrderLine
import com.ninezero.halo.feature.store.presentation.components.CardInfoRow
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import java.text.NumberFormat

@Composable
fun GroupSummarySection(
    selectedItems: List<CartItem>,
    numberFormat: NumberFormat,
    earnRatePercent: Int,
    modifier: Modifier = Modifier,
    isCreatorSubscribed: Boolean = false
) {
    val pricing = remember(selectedItems, isCreatorSubscribed) {
        calcStorePricing(
            items = selectedItems.map { it.toOrderLine() },
            subscribedCreatorIds = if (isCreatorSubscribed) {
                selectedItems.mapTo(mutableSetOf()) { it.product.creator.id }
            } else {
                emptySet()
            }
        )
    }
    val earnPoints = calcEarnPoints(pricing.subtotal, earnRatePercent)
    val shippingFee = pricing.shipping.finalShippingFee

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CardInfoRow(label = "선택상품금액", value = "${numberFormat.format(pricing.subtotal)}원")
        CardInfoRow(
            label = "배송비",
            value = if (shippingFee == 0L) "무료배송" else "${numberFormat.format(shippingFee)}원",
            valueColor = when {
                isCreatorSubscribed -> MembershipColors.label
                shippingFee == 0L -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            isHighlighted = shippingFee == 0L
        )
        CardInfoRow(
            label = "적립 예정",
            value = "${numberFormat.format(earnPoints)}P (${earnRatePercent}%)",
            valueColor = if (isCreatorSubscribed) {
                MembershipColors.label
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            isHighlighted = isCreatorSubscribed
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "예상 주문금액",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${numberFormat.format(pricing.totalBeforePoints)}원",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun previewCartItem(id: Int, price: Long, quantity: Int) = CartItem(
    id = id,
    product = Product(
        id = id,
        name = "상품 $id",
        description = "",
        price = price,
        originalPrice = null,
        discountRate = null,
        imageUrls = emptyList(),
        brandName = null,
        tags = emptyList(),
        stock = 10,
        status = ProductStatus.ACTIVE,
        isActive = true,
        viewCount = 0,
        likeCount = 0,
        salesCount = 0,
        creator = previewUserSummary(),
        requiredTier = SubscriptionPlanTier.FREE,
        canAccess = true
    ),
    quantity = quantity
)

@HaloLightDarkPreview
@Composable
private fun GroupSummarySectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                GroupSummarySection(
                    selectedItems = listOf(previewCartItem(1, 14000L, 1)),
                    numberFormat = rememberKoreanNumberFormat(),
                    earnRatePercent = 1
                )
                Spacer(modifier = Modifier.height(24.dp))
                GroupSummarySection(
                    selectedItems = listOf(previewCartItem(1, 15000L, 3)),
                    numberFormat = rememberKoreanNumberFormat(),
                    earnRatePercent = 5,
                    isCreatorSubscribed = true
                )
            }
        }
    }
}
