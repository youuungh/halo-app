package com.ninezero.halo.feature.store.presentation.wishlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.store.domain.model.Wishlist
import com.ninezero.halo.feature.store.domain.model.WishlistProduct
import com.ninezero.halo.feature.store.domain.model.WishlistPurchaseState
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun WishlistItem(
    wishlist: Wishlist,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onMoveToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val product = wishlist.product
    val numberFormat = rememberKoreanNumberFormat()
    val isEntryBlocked = product.purchaseState == WishlistPurchaseState.NO_ACCESS ||
            product.purchaseState == WishlistPurchaseState.DISCONTINUED
    val isUnavailable = product.purchaseState != WishlistPurchaseState.PURCHASABLE
    val statusLabel = when (product.purchaseState) {
        WishlistPurchaseState.NO_ACCESS -> "멤버십 전용"
        WishlistPurchaseState.DISCONTINUED -> "판매종료"
        WishlistPurchaseState.SOLD_OUT -> "품절"
        WishlistPurchaseState.PURCHASABLE -> null
    }
    val hasDiscount = product.originalPrice != null && product.discountRate != null && product.discountRate > 0
    val formattedPrice = product.price.toBigDecimalOrNull()?.let { numberFormat.format(it) } ?: product.price
    val formattedOriginalPrice = product.originalPrice?.toBigDecimalOrNull()
        ?.let { numberFormat.format(it) } ?: product.originalPrice

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isEntryBlocked, onClick = onClick)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .alpha(if (isUnavailable) 0.6f else 1f)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_img_placeholder),
                    error = painterResource(R.drawable.ic_img_placeholder),
                    fallback = painterResource(R.drawable.ic_img_placeholder)
                )

                if (statusLabel != null) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
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
                            text = "${formattedPrice}원",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.alignByBaseline()
                        )
                        if (formattedOriginalPrice != null) {
                            Text(
                                text = "${formattedOriginalPrice}원",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                    }
                } else {
                    Text(
                        text = "${formattedPrice}원",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onMoveToCart,
                    enabled = !isUnavailable,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(32.dp)
                ) {
                    Text(
                        text = "장바구니 담기",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

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
    }
}

private fun previewWishlist(
    id: Int,
    name: String,
    state: WishlistPurchaseState,
    originalPrice: String? = null,
    discountRate: Int? = null
) = Wishlist(
    id = id,
    product = WishlistProduct(
        id = id,
        name = name,
        price = "89000",
        originalPrice = originalPrice,
        discountRate = discountRate,
        imageUrl = null,
        purchaseState = state
    )
)

@HaloLightDarkPreview
@Composable
private fun WishlistItemPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                WishlistItem(
                    wishlist = previewWishlist(
                        id = 1,
                        name = "프리미엄 무선 이어폰 Pro Max",
                        state = WishlistPurchaseState.PURCHASABLE,
                        originalPrice = "129000",
                        discountRate = 31
                    ),
                    onClick = {},
                    onRemove = {},
                    onMoveToCart = {}
                )
                WishlistItem(
                    wishlist = previewWishlist(2, "품절된 기계식 키보드", WishlistPurchaseState.SOLD_OUT),
                    onClick = {},
                    onRemove = {},
                    onMoveToCart = {}
                )
                WishlistItem(
                    wishlist = previewWishlist(3, "판매종료된 충전기", WishlistPurchaseState.DISCONTINUED),
                    onClick = {},
                    onRemove = {},
                    onMoveToCart = {}
                )
                WishlistItem(
                    wishlist = previewWishlist(4, "멤버십 전용 한정 케이블", WishlistPurchaseState.NO_ACCESS),
                    onClick = {},
                    onRemove = {},
                    onMoveToCart = {}
                )
            }
        }
    }
}
