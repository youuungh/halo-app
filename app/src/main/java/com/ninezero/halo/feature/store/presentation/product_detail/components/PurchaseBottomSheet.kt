package com.ninezero.halo.feature.store.presentation.product_detail.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.button.SheetActionButton
import com.ninezero.halo.core.ui.components.button.SheetActionButtonStyle
import com.ninezero.halo.core.ui.components.counter.AnimatedCounter
import com.ninezero.halo.core.ui.components.selector.QuantitySelector
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.isPurchasable
import com.ninezero.halo.feature.store.domain.model.purchaseBlockedMessage
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun PurchaseBottomSheet(
    product: Product,
    quantity: Int,
    isAddingToCart: Boolean,
    onDismiss: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val sheetColor = MaterialTheme.colorScheme.surface
    val price = product.price.toIntOrNull() ?: 0
    val totalPrice = price * quantity
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val maxSelectableQuantity = maxOf(1, minOf(product.stock, Constants.Commerce.MAX_CART_QUANTITY))
    val blockedMessage = product.purchaseBlockedMessage()
    val isPurchasable = product.isPurchasable()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetColor,
        dragHandle = null,
        modifier = modifier
    ) {
        BottomSheetDragHandle()
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        ) {
            // 수량 선택
            QuantitySelector(
                quantity = quantity,
                maxStock = maxSelectableQuantity,
                onQuantityChange = onQuantityChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 총 금액
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "합계",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AnimatedCounter(
                    count = totalPrice,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    suffix = "원"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 액션 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SheetActionButton(
                    text = "장바구니",
                    onClick = {
                        focusManager.clearFocus()  // 수량 칸 입력을 먼저 확정
                        if (!isPurchasable) {
                            Toast.makeText(context, blockedMessage ?: "구매할 수 없는 상품입니다", Toast.LENGTH_SHORT).show()
                            return@SheetActionButton
                        }
                        onAddToCart()
                    },
                    modifier = Modifier.weight(1f),
                    style = SheetActionButtonStyle.Outlined,
                    enabled = !isAddingToCart && isPurchasable
                )

                SheetActionButton(
                    text = "구매하기",
                    onClick = {
                        focusManager.clearFocus()  // 수량 칸 입력을 먼저 확정
                        if (!isPurchasable) {
                            Toast.makeText(context, blockedMessage ?: "구매할 수 없는 상품입니다", Toast.LENGTH_SHORT).show()
                            return@SheetActionButton
                        }
                        onBuyNow()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isAddingToCart && isPurchasable
                )
            }
        }
    }
}

private val previewCreator = UserSummary(
    id = 1,
    username = "creator_shop",
    displayName = "크리에이터 샵",
    avatarUrl = null,
    role = "CREATOR",
)

private val previewProduct = Product(
    id = 1,
    name = "프리미엄 무선 이어폰 Pro Max",
    description = "최고급 사운드 품질의 무선 이어폰",
    price = "89000",
    originalPrice = "129000",
    discountRate = 31,
    imageUrls = listOf("https://example.com/img1.jpg"),
    detailContent = emptyList(),
    brandName = "SoundWave",
    tags = listOf("이어폰"),
    stock = 50,
    status = "ACTIVE",
    isActive = true,
    viewCount = 1234,
    likeCount = 89,
    salesCount = 567,
    creator = previewCreator,
    requiredTier = "FREE",
    canAccess = true,
)

@HaloLightDarkPreview
@Composable
private fun PurchaseBottomSheetPreview() {
    HaloTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            var quantity by remember { mutableIntStateOf(2) }
            val price = previewProduct.price.toIntOrNull() ?: 0
            val totalPrice = price * quantity

            QuantitySelector(
                quantity = quantity,
                maxStock = previewProduct.stock,
                onQuantityChange = { quantity = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "합계",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AnimatedCounter(
                    count = totalPrice,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    suffix = "원"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SheetActionButton(
                    text = "장바구니",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    style = SheetActionButtonStyle.Outlined
                )

                SheetActionButton(
                    text = "구매하기",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
