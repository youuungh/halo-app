package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.ui.theme.HaloTheme
import java.text.NumberFormat

@Composable
fun OrderDetailItemRow(
    item: OrderItem,
    numberFormat: NumberFormat,
    modifier: Modifier = Modifier,
    canWriteReview: Boolean = false,
    reviewDeadlineLabel: String? = null,
    onWriteReview: () -> Unit = {},
    onShippingInquiry: () -> Unit = {},
    onAddToCart: () -> Unit = {},
    showActionButtons: Boolean = true,
    showPrice: Boolean = true,
    showLineTotal: Boolean = false,
    onImageClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = item.productImageUrl,
                contentDescription = item.productName,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(if (onImageClick != null) Modifier.clickable(onClick = onImageClick) else Modifier),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_img_placeholder),
                error = painterResource(R.drawable.ic_img_placeholder),
                fallback = painterResource(R.drawable.ic_img_placeholder)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${item.quantity}개",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!item.isProductActive) {
                            Text(
                                text = "· 판매종료",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (showLineTotal) {
                    val lineTotal = (item.price.toLongOrNull() ?: 0L) * item.quantity
                    Text(
                        text = "${numberFormat.format(lineTotal)}원",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else if (showPrice) {
                    val hasDiscount = item.originalPrice != null &&
                            item.originalPrice != item.price &&
                            (item.originalPrice.toLongOrNull() ?: 0) > (item.price.toLongOrNull() ?: 0)
                    if (hasDiscount) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val originalPrice = item.originalPrice.toLongOrNull() ?: 0L
                            val currentPrice = item.price.toLongOrNull() ?: 0L
                            val discountRate = if (originalPrice > 0 && originalPrice > currentPrice) {
                                (((originalPrice - currentPrice).toDouble() / originalPrice.toDouble()) * 100).toInt()
                            } else {
                                0
                            }
                            if (discountRate > 0) {
                                Text(
                                    text = "$discountRate%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.alignByBaseline()
                                )
                            }
                            Text(
                                text = "${numberFormat.format(currentPrice)}원",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.alignByBaseline()
                            )
                            Text(
                                text = "${numberFormat.format(originalPrice)}원",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                    } else {
                        Text(
                            text = "${numberFormat.format(item.price.toLongOrNull() ?: 0)}원",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (showActionButtons) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canWriteReview) {
                    OutlinedButton(
                        onClick = onWriteReview,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .bounceClick()
                    ) {
                        Text(
                            text = "리뷰 작성",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        reviewDeadlineLabel?.let { deadlineLabel ->
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = deadlineLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = onShippingInquiry,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .bounceClick()
                ) {
                    Text(
                        text = "배송조회",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                // 삭제된 상품은 재구매 불가
                if (item.isProductActive) {
                    OutlinedButton(
                        onClick = onAddToCart,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .bounceClick()
                    ) {
                        Text(
                            text = "장바구니 담기",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun previewItem(name: String, quantity: Int = 1, originalPrice: String? = null) = OrderItem(
    id = 1,
    productId = 1,
    productName = name,
    productImageUrl = null,
    price = "89000",
    originalPrice = originalPrice,
    quantity = quantity,
    subtotal = (89000 * quantity).toString()
)

@HaloLightDarkPreview
@Composable
private fun OrderDetailItemRowPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                OrderDetailItemRow(
                    item = previewItem("프리미엄 무선 이어폰 Pro Max", originalPrice = "129000"),
                    numberFormat = rememberKoreanNumberFormat(),
                    canWriteReview = true,
                    reviewDeadlineLabel = "D-25",
                    onWriteReview = {},
                    onShippingInquiry = {},
                    onAddToCart = {}
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
private fun OrderDetailItemRowNoActionsPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                OrderDetailItemRow(
                    item = previewItem("읽기 전용 상품"),
                    numberFormat = rememberKoreanNumberFormat(),
                    showActionButtons = false
                )
            }
        }
    }
}
