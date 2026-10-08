package com.ninezero.halo.feature.profile.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

data class ProfileMenuItemData(
    val iconRes: Int,
    val label: String,
    val isCreatorMenu: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun ProfileMenuGrid(
    isCreator: Boolean,
    onOrderClick: () -> Unit,
    onCartClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onReviewClick: () -> Unit,
    onPointClick: () -> Unit,
    onCouponClick: () -> Unit,
    onMembershipClick: () -> Unit,
    onAddressClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMembershipManageClick: () -> Unit = {},
    onStoreClick: () -> Unit = {},
    onSalesClick: () -> Unit = {},
    onCouponManageClick: () -> Unit = {}
) {
    val commonMenuItems = listOf(
        ProfileMenuItemData(R.drawable.ic_order, "주문/배송", onClick = onOrderClick),
        ProfileMenuItemData(R.drawable.ic_cart, "장바구니", onClick = onCartClick),
        ProfileMenuItemData(R.drawable.ic_wishlist, "위시리스트", onClick = onWishlistClick),
        ProfileMenuItemData(R.drawable.ic_review, "리뷰", onClick = onReviewClick),
        ProfileMenuItemData(R.drawable.ic_map_pin, "주소 관리", onClick = onAddressClick),
        ProfileMenuItemData(R.drawable.ic_point, "포인트", onClick = onPointClick),
        ProfileMenuItemData(R.drawable.ic_membership, "멤버십", onClick = onMembershipClick),
        ProfileMenuItemData(R.drawable.ic_coupon, "쿠폰", onClick = onCouponClick)
    )

    val creatorMenuItems = listOf(
        ProfileMenuItemData(R.drawable.ic_store, "스토어", isCreatorMenu = true, onClick = onStoreClick),
        ProfileMenuItemData(R.drawable.ic_sales, "판매 관리", isCreatorMenu = true, onClick = onSalesClick),
        ProfileMenuItemData(R.drawable.ic_square_m, "멤버십 관리", isCreatorMenu = true, onClick = onMembershipManageClick),
        ProfileMenuItemData(R.drawable.ic_coupon_manage, "쿠폰 관리", isCreatorMenu = true, onClick = onCouponManageClick)
    )

    val allItems = if (isCreator) {
        commonMenuItems + creatorMenuItems
    } else {
        commonMenuItems
    }

    val rows = allItems.chunked(4)

    Column(modifier = modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    ProfileMenuItem(
                        iconRes = item.iconRes,
                        label = item.label,
                        onClick = item.onClick,
                        isCreatorMenu = item.isCreatorMenu,
                        modifier = Modifier.weight(1f)
                    )
                }
                // 4열 맞춤 빈 칸
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (index < rows.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProfileMenuGridPreview() {
    HaloTheme {
        ProfileMenuGrid(
            isCreator = false,
            onOrderClick = {},
            onCartClick = {},
            onWishlistClick = {},
            onReviewClick = {},
            onPointClick = {},
            onCouponClick = {},
            onMembershipClick = {},
            onAddressClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ProfileMenuGridCreatorPreview() {
    HaloTheme {
        ProfileMenuGrid(
            isCreator = true,
            onOrderClick = {},
            onCartClick = {},
            onWishlistClick = {},
            onReviewClick = {},
            onPointClick = {},
            onCouponClick = {},
            onMembershipClick = {},
            onAddressClick = {},
            onMembershipManageClick = {},
            onStoreClick = {},
            onSalesClick = {},
            onCouponManageClick = {}
        )
    }
}
