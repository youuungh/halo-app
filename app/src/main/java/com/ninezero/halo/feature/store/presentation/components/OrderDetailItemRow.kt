package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.store.domain.model.OrderItem
import java.text.NumberFormat

/** 주문 상품 한 줄, 오른쪽에 trailing */
@Composable
fun OrderDetailItemRow(
    item: OrderItem,
    numberFormat: NumberFormat,
    modifier: Modifier = Modifier,
    showPrice: Boolean = true,
    showLineTotal: Boolean = false,
    onImageClick: (() -> Unit)? = null,
    imageSize: Dp = 56.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.productImageUrl,
            contentDescription = item.productName,
            modifier = Modifier
                .size(imageSize)
                .clip(MaterialTheme.shapes.small)
                .then(if (onImageClick != null) Modifier.clickable(onClick = onImageClick) else Modifier),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_image_placeholder),
            error = painterResource(R.drawable.ic_image_placeholder),
            fallback = painterResource(R.drawable.ic_image_placeholder)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (item.isProductActive) "${item.quantity}개" else "${item.quantity}개 · 판매종료",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (showLineTotal) {
                val lineTotal = item.price * item.quantity
                Text(
                    text = "${numberFormat.format(lineTotal)}원",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else if (showPrice) {
                val originalPrice = item.originalPrice ?: 0L
                val currentPrice = item.price
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (originalPrice > currentPrice) {
                        Text(
                            text = "${((originalPrice - currentPrice) * 100 / originalPrice)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                    Text(
                        text = "${numberFormat.format(currentPrice)}원",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.alignByBaseline()
                    )
                    if (originalPrice > currentPrice) {
                        Text(
                            text = "${numberFormat.format(originalPrice)}원",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }
            }
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailing()
        }
    }
}

private fun previewItem(name: String, quantity: Int = 1, originalPrice: Long? = null) = OrderItem(
    id = 1,
    productId = 1,
    productName = name,
    productImageUrl = null,
    price = 89000L,
    originalPrice = originalPrice,
    quantity = quantity,
    subtotal = 89000L * quantity
)

@HaloLightDarkPreview
@Composable
private fun OrderDetailItemRowPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                OrderDetailItemRow(
                    item = previewItem("프리미엄 무선 이어폰 Pro Max", originalPrice = 129000L),
                    numberFormat = rememberKoreanNumberFormat()
                )
                OrderDetailItemRow(
                    item = previewItem("수량 3개 상품", quantity = 3),
                    numberFormat = rememberKoreanNumberFormat(),
                    showLineTotal = true
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderDetailItemRowNoPricePreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                OrderDetailItemRow(
                    item = previewItem("읽기 전용 상품"),
                    numberFormat = rememberKoreanNumberFormat(),
                    showPrice = false
                )
            }
        }
    }
}
