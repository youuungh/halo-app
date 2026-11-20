package com.ninezero.halo.feature.store.presentation.product_detail.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.core.ui.components.button.PrimaryButton
import com.ninezero.halo.core.ui.components.button.PrimaryButtonStyle
import com.ninezero.halo.core.ui.components.text.AnimatedCounter
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.canBuy
import com.ninezero.halo.feature.store.domain.model.purchaseBlockedMessage
import com.ninezero.halo.feature.store.presentation.components.QuantitySelector
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo

@Composable
fun PurchaseBottomSheet(
    product: Product,
    quantity: Int,
    isAddingToCart: Boolean,
    userTierInfo: UserTierInfo?,
    onDismiss: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val numberFormat = rememberKoreanNumberFormat()
    val price = product.price.toInt()
    val productTotal = price * quantity
    val isFreeShipping = userTierInfo?.isSubscribed == true ||
            productTotal >= Constants.Commerce.FREE_SHIPPING_THRESHOLD
    val shippingFee = if (isFreeShipping) 0 else product.shippingFee.toInt()
    val earnPoints = productTotal * userTierInfo.earnRatePercent() / 100
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val maxSelectableQuantity = maxOf(1, minOf(product.stock, Constants.Commerce.MAX_CART_QUANTITY))
    val blockedMessage = product.purchaseBlockedMessage()
    val canBuy = product.canBuy()

    FloatingBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column {

            Column(
                modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .padding(top = 6.dp, bottom = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = product.imageUrls.firstOrNull(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_image_placeholder),
                        error = painterResource(R.drawable.ic_image_placeholder),
                        fallback = painterResource(R.drawable.ic_image_placeholder)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${numberFormat.format(price)}원",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    QuantitySelector(
                        quantity = quantity,
                        maxStock = maxSelectableQuantity,
                        onQuantityChange = onQuantityChange,
                        // 시트 위라 화면 스낵바가 가려져 토스트로 안내
                        onQuantityLimit = { max ->
                            Toast.makeText(context, "최대 ${max}개까지 구매 가능합니다", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .width(QuantitySelectorWidth)
                            .offset(x = 2.dp),  // 안쪽 여백만큼 밀어 + 버튼을 금액 끝선에 맞춤
                        showLabel = false,
                        buttonShape = CircleShape
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 16.dp, bottom = 14.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryRow(label = "상품 금액", value = "${numberFormat.format(productTotal)}원")
                    SummaryRow(
                        label = "배송비",
                        value = if (shippingFee > 0) "${numberFormat.format(shippingFee)}원" else "무료"
                    )
                    SummaryRow(label = "적립 예정", value = "${numberFormat.format(earnPoints)}P")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "합계",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AnimatedCounter(
                        count = productTotal + shippingFee,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        suffix = "원"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrimaryButton(
                        text = "장바구니",
                        onClick = {
                            focusManager.clearFocus()  // 수량 칸 입력을 먼저 확정
                            if (!canBuy) {
                                Toast.makeText(context, blockedMessage ?: "구매할 수 없는 상품입니다", Toast.LENGTH_SHORT).show()
                                return@PrimaryButton
                            }
                            onAddToCart()
                        },
                        modifier = Modifier.weight(1f),
                        style = PrimaryButtonStyle.Tonal,
                        enabled = !isAddingToCart && canBuy
                    )

                    PrimaryButton(
                        text = "구매하기",
                        onClick = {
                            focusManager.clearFocus()  // 수량 칸 입력을 먼저 확정
                            if (!canBuy) {
                                Toast.makeText(context, blockedMessage ?: "구매할 수 없는 상품입니다", Toast.LENGTH_SHORT).show()
                                return@PrimaryButton
                            }
                            onBuyNow()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isAddingToCart && canBuy
                    )
                }
            }
        }
    }
}

private val QuantitySelectorWidth = 124.dp  // 버튼 36·입력 48·버튼 36 + 안쪽 여백 2씩
@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
