package com.ninezero.halo.feature.store.presentation.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.checkbox.HaloCheckbox
import com.ninezero.halo.core.ui.components.selector.QuantitySelector
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.store.domain.model.CartItem
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.ui.theme.HaloTheme
import java.text.NumberFormat

@Composable
fun CartItemRow(
    cartItem: CartItem,
    numberFormat: NumberFormat,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleSelection: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    bottomReservedSpace: Dp = 0.dp
) {
    val product = cartItem.product
    val price = product.price.toIntOrNull() ?: 0
    val hasDiscount = product.originalPrice != null && product.discountRate != null && product.discountRate > 0
    val isUnavailable = product.canAccess == false || !product.isActive || product.stock == 0 ||
        product.status == "SOLD_OUT" || product.status == "DISCONTINUED"
    val statusLabel = when {
        product.canAccess == false -> "멤버십 전용"
        !product.isActive || product.status == "DISCONTINUED" -> "판매종료"
        product.stock == 0 || product.status == "SOLD_OUT" -> "품절"
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // 체크박스
            if (!isUnavailable) {
                HaloCheckbox(
                    checked = isSelected,
                    onClick = onToggleSelection,
                    modifier = Modifier.offset(y = (-12).dp)
                )
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }

            // 상품 이미지
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onClick)
                    .alpha(if (isUnavailable) 0.6f else 1f)
            ) {
                AsyncImage(
                    model = product.imageUrls.firstOrNull(),
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_img_placeholder),
                    error = painterResource(R.drawable.ic_img_placeholder),
                    fallback = painterResource(R.drawable.ic_img_placeholder)
                )
                if (statusLabel != null) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 상품명 + 가격
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (isUnavailable) 0.5f else 1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (hasDiscount) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "${product.discountRate}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.alignByBaseline()
                        )
                        Text(
                            text = "${numberFormat.format(price)}원",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.alignByBaseline()
                        )
                        Text(
                            text = "${product.originalPrice.toBigDecimalOrNull()?.let { numberFormat.format(it) } ?: product.originalPrice}원",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                } else {
                    Text(
                        text = "${numberFormat.format(price)}원",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // X 버튼
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "삭제",
                modifier = Modifier
                    .size(18.dp)
                    .offset(y = 2.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRemove
                    ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!isUnavailable) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.width(140.dp))
                QuantitySelector(
                    quantity = cartItem.quantity,
                    maxStock = minOf(product.stock, 99),
                    onQuantityChange = onUpdateQuantity,
                    showLabel = false,
                    bottomReservedSpace = bottomReservedSpace
                )
            }
        }
    }
}

private fun previewCartItem(
    id: Int,
    name: String,
    quantity: Int,
    originalPrice: String? = null,
    discountRate: Int? = null,
    stock: Int = 50
) = CartItem(
    id = id,
    product = Product(
        id = id,
        name = name,
        description = "",
        price = "89000",
        originalPrice = originalPrice,
        discountRate = discountRate,
        imageUrls = emptyList(),
        brandName = null,
        tags = emptyList(),
        stock = stock,
        status = "ACTIVE",
        isActive = true,
        viewCount = 0,
        likeCount = 0,
        salesCount = 0,
        creator = previewUserSummary(displayName = "크리에이터 샵"),
        requiredTier = "FREE",
        canAccess = true
    ),
    quantity = quantity
)

@HaloLightDarkPreview
@Composable
private fun CartItemRowPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                CartItemRow(
                    cartItem = previewCartItem(1, "프리미엄 무선 이어폰 Pro Max", 1, "129000", 31),
                    numberFormat = rememberKoreanNumberFormat(),
                    isSelected = true,
                    onClick = {},
                    onToggleSelection = {},
                    onUpdateQuantity = {},
                    onRemove = {}
                )
                CartItemRow(
                    cartItem = previewCartItem(2, "기계식 키보드 청축", 3),
                    numberFormat = rememberKoreanNumberFormat(),
                    isSelected = false,
                    onClick = {},
                    onToggleSelection = {},
                    onUpdateQuantity = {},
                    onRemove = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CartItemRowLowStockPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                CartItemRow(
                    cartItem = previewCartItem(3, "재고 1개 남은 상품", quantity = 1, stock = 1),
                    numberFormat = rememberKoreanNumberFormat(),
                    isSelected = true,
                    onClick = {},
                    onToggleSelection = {},
                    onUpdateQuantity = {},
                    onRemove = {}
                )
            }
        }
    }
}
