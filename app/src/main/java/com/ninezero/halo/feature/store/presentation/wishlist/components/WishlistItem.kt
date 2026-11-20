package com.ninezero.halo.feature.store.presentation.wishlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.MembershipBadge
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.core.ui.theme.SoldOutBgDark
import com.ninezero.halo.core.ui.theme.SoldOutBgLight
import com.ninezero.halo.core.ui.theme.SoldOutTextDark
import com.ninezero.halo.core.ui.theme.SoldOutTextLight
import com.ninezero.halo.feature.store.domain.model.Wishlist
import com.ninezero.halo.feature.store.domain.model.WishlistProduct
import com.ninezero.halo.feature.store.domain.model.WishlistPurchaseState
import com.ninezero.halo.feature.store.presentation.components.CartCircleButton
import com.ninezero.halo.feature.store.presentation.components.ProductStatusImage

private const val THUMB_SIZE_DP = 80

/** 위시 줄: 사진 · 이름 · 가격 · 버튼, 오른쪽 X */
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
    val hasDiscount = product.originalPrice != null && product.discountRate != null && product.discountRate > 0
    val formattedPrice = numberFormat.format(product.price)
    val formattedOriginalPrice = product.originalPrice?.let { numberFormat.format(it) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isEntryBlocked,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        ProductStatusImage(
            imageUrl = product.imageUrl,
            contentDescription = product.name,
            isMembersOnly = product.purchaseState == WishlistPurchaseState.NO_ACCESS,
            isUnavailable = product.purchaseState != WishlistPurchaseState.PURCHASABLE,
            size = THUMB_SIZE_DP.dp,
            lockIconSize = 22.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 글자 칸을 사진 높이에 맞춤
        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = THUMB_SIZE_DP.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (hasDiscount) {
                        Text(
                            text = "${product.discountRate}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                    Text(
                        text = "${formattedPrice}원",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.alignByBaseline()
                    )
                    if (hasDiscount && formattedOriginalPrice != null) {
                        Text(
                            text = "${formattedOriginalPrice}원",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (product.purchaseState == WishlistPurchaseState.PURCHASABLE) {
                CartCircleButton(onClick = onMoveToCart)
            } else {
                WishlistStatusTag(state = product.purchaseState)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = "삭제",
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRemove
                ),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 장바구니 상태 태그와 같은 색 */
@Composable
private fun WishlistStatusTag(state: WishlistPurchaseState) {
    val isDark = LocalIsDarkTheme.current
    val isMembersOnly = state == WishlistPurchaseState.NO_ACCESS
    val label = when (state) {
        WishlistPurchaseState.NO_ACCESS -> "멤버십 전용"
        WishlistPurchaseState.DISCONTINUED -> "판매종료"
        else -> "품절"
    }
    if (isMembersOnly) {
        MembershipBadge(text = label)
        return
    }
    val (textColor, backgroundColor) = if (isDark) SoldOutTextDark to SoldOutBgDark else SoldOutTextLight to SoldOutBgLight
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = textColor,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

private fun previewWishlist(
    id: Int,
    name: String,
    state: WishlistPurchaseState,
    originalPrice: Long? = null,
    discountRate: Int? = null
) = Wishlist(
    id = id,
    product = WishlistProduct(
        id = id,
        name = name,
        price = 89000L,
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
                        originalPrice = 129000L,
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
