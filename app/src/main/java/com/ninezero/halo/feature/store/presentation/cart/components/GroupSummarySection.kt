package com.ninezero.halo.feature.store.presentation.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.store.domain.model.CartItem
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.calcStorePricing
import com.ninezero.halo.feature.store.domain.model.toOrderLine
import com.ninezero.halo.ui.theme.HaloTheme
import java.text.NumberFormat

@Composable
fun GroupSummarySection(
    selectedItems: List<CartItem>,
    numberFormat: NumberFormat,
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
    val groupTotalPrice = pricing.subtotal
    val originalShippingFee = pricing.shipping.membershipFreeShipFee
    val effectiveShippingFee = pricing.shipping.finalShippingFee
    val totalDiscount = pricing.productDiscount
    val orderTotal = pricing.totalBeforePoints
    val isThresholdFreeShipping = pricing.shipping.freeShipByAmountIds.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        SummaryRow(
            label = "선택상품금액",
            value = "${numberFormat.format(groupTotalPrice)}원"
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isCreatorSubscribed && originalShippingFee > 0) {
            // "무료배송" + 취소선
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "배송비",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "무료배송",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${numberFormat.format(originalShippingFee)}원",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }
        } else if (isThresholdFreeShipping) {
            SummaryRow(
                label = "배송비",
                value = "무료배송"
            )
        } else {
            SummaryRow(
                label = "배송비",
                value = "${numberFormat.format(effectiveShippingFee)}원"
            )
        }
        if (totalDiscount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            SummaryRow(
                label = "할인예상금액",
                value = "-${numberFormat.format(totalDiscount)}원",
                valueColor = MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
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
                text = "${numberFormat.format(orderTotal)}원",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    shadow: Shadow? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.let {
                if (shadow != null) it.copy(shadow = shadow) else it
            },
            fontWeight = FontWeight.Normal,
            color = labelColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.let {
                if (shadow != null) it.copy(shadow = shadow) else it
            },
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

private fun previewCartItem(id: Int, price: String, quantity: Int) = CartItem(
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
        status = "ACTIVE",
        isActive = true,
        viewCount = 0,
        likeCount = 0,
        salesCount = 0,
        creator = previewUserSummary(),
        requiredTier = "FREE",
        canAccess = true
    ),
    quantity = quantity
)

@HaloLightDarkPreview
@Composable
private fun GroupSummarySectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                GroupSummarySection(
                    selectedItems = listOf(
                        previewCartItem(1, "89000", 1),
                        previewCartItem(2, "45000", 2)
                    ),
                    numberFormat = rememberKoreanNumberFormat()
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun GroupSummarySectionSubscribedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                GroupSummarySection(
                    selectedItems = listOf(previewCartItem(1, "89000", 1)),
                    numberFormat = rememberKoreanNumberFormat(),
                    isCreatorSubscribed = true
                )
            }
        }
    }
}
