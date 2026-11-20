package com.ninezero.halo.feature.store.presentation.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.MembershipBadge
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.components.toggle.HaloCheckbox
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.core.ui.theme.SoldOutBgDark
import com.ninezero.halo.core.ui.theme.SoldOutBgLight
import com.ninezero.halo.core.ui.theme.SoldOutTextDark
import com.ninezero.halo.core.ui.theme.SoldOutTextLight
import com.ninezero.halo.feature.store.domain.model.CartItem
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.ProductStatus
import com.ninezero.halo.feature.store.presentation.components.ProductStatusImage
import com.ninezero.halo.feature.store.presentation.components.QuantitySelector
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import java.text.NumberFormat

private val ItemImageSize = 64.dp
private val StepperButtonSize = 30.dp
private val StepperFieldWidth = 32.dp
private val StepperWidth = StepperButtonSize * 2 + StepperFieldWidth + 4.dp  // 안쪽 여백 2씩
private val PriceStepperGap = 14.dp

@Composable
fun CartItemRow(
    cartItem: CartItem,
    numberFormat: NumberFormat,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleSelection: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onQuantityLimit: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    isMembershipCard: Boolean = false,
    bottomReservedSpace: Dp = 0.dp
) {
    val product = cartItem.product
    val price = product.price
    val discountRate = product.discountRate?.takeIf { it > 0 }
    val originalPrice = product.originalPrice?.takeIf { discountRate != null }
    val isDark = LocalIsDarkTheme.current
    val statusTag = when {
        product.canAccess == false -> StatusTag.MembershipOnly
        !product.isActive || product.status == ProductStatus.DISCONTINUED -> StatusTag.Discontinued
        product.stock == 0 || product.status == ProductStatus.SOLD_OUT -> StatusTag.SoldOut
        else -> null
    }
    val isUnavailable = statusTag != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 48 터치 영역의 체크 표시를 사진 윗줄에 맞춤
        if (isUnavailable) {
            DisabledCheckbox(modifier = Modifier.offset(y = (-14).dp))
        } else {
            HaloCheckbox(
                checked = isSelected,
                onClick = onToggleSelection,
                modifier = Modifier.offset(y = (-14).dp)
            )
        }

        ProductStatusImage(
            imageUrl = product.imageUrls.firstOrNull(),
            contentDescription = product.name,
            isMembersOnly = statusTag == StatusTag.MembershipOnly,
            isUnavailable = isUnavailable,
            size = ItemImageSize,
            lockIconSize = 20.dp,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 이름은 위, 금액은 사진 아랫선에
        Column(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = ItemImageSize),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isUnavailable) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onClick
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "삭제",
                        modifier = Modifier
                            .size(18.dp)
                            .offset(y = (-1).dp)  // 상품명 첫 줄 가운데에 맞춤
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onRemove
                            ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (statusTag == null && product.stock in 1..10) {
                    Text(
                        text = "재고 ${product.stock}개 남음",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (statusTag != null) {
                StatusTagLabel(
                    tag = statusTag,
                    isDark = isDark,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                PriceStepperLayout(
                    price = {
                        Column {
                            if (originalPrice != null) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${discountRate}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${numberFormat.format(originalPrice * cartItem.quantity)}원",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }
                            Text(
                                text = "${numberFormat.format(price * cartItem.quantity)}원",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    },
                    stepper = {
                        QuantitySelector(
                            quantity = cartItem.quantity,
                            maxStock = minOf(product.stock, 99),
                            onQuantityChange = onUpdateQuantity,
                            onQuantityLimit = onQuantityLimit,
                            modifier = Modifier.width(StepperWidth),
                            showLabel = false,
                            bottomReservedSpace = bottomReservedSpace,
                            buttonShape = CircleShape,
                            buttonSize = StepperButtonSize,
                            fieldWidth = StepperFieldWidth,
                            // 멤버십 카드 배경 위에선 회색 버튼이 탁해 흰색으로 띄움
                            buttonColor = when {
                                !isMembershipCard -> MaterialTheme.colorScheme.surfaceVariant
                                isDark -> Color.White.copy(alpha = 0.2f)
                                else -> Color.White
                            },
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }
    }
}

/** 금액 · 수량 한 줄, 모자라면 수량을 다음 줄로 */
@Composable
private fun PriceStepperLayout(
    price: @Composable () -> Unit,
    stepper: @Composable () -> Unit
) {
    Layout(
        contents = listOf(price, stepper),
        modifier = Modifier.fillMaxWidth()
    ) { (priceMeasurables, stepperMeasurables), constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val pricePlaceable = priceMeasurables.first().measure(loose)
        val stepperPlaceable = stepperMeasurables.first().measure(loose)
        val gap = PriceStepperGap.roundToPx()
        val rowGap = 6.dp.roundToPx()
        val width = constraints.maxWidth
        val fitsOneLine = pricePlaceable.width + gap + stepperPlaceable.width <= width

        if (fitsOneLine) {
            val height = maxOf(pricePlaceable.height, stepperPlaceable.height)
            layout(width, height) {
                // 아랫선 맞춤
                pricePlaceable.place(0, height - pricePlaceable.height)
                stepperPlaceable.place(width - stepperPlaceable.width, height - stepperPlaceable.height)
            }
        } else {
            val height = pricePlaceable.height + rowGap + stepperPlaceable.height
            layout(width, height) {
                pricePlaceable.place(0, 0)
                stepperPlaceable.place(width - stepperPlaceable.width, pricePlaceable.height + rowGap)
            }
        }
    }
}

private enum class StatusTag(val label: String) {
    MembershipOnly("멤버십 전용"),
    SoldOut("품절"),
    Discontinued("판매종료")
}

@Composable
private fun StatusTagLabel(tag: StatusTag, isDark: Boolean, modifier: Modifier = Modifier) {
    if (tag == StatusTag.MembershipOnly) {
        MembershipBadge(modifier = modifier, text = tag.label)
        return
    }
    val (textColor, backgroundColor) = if (isDark) SoldOutTextDark to SoldOutBgDark else SoldOutTextLight to SoldOutBgLight
    Text(
        text = tag.label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = textColor,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

/** 구매할 수 없는 상품의 선택 불가 체크 칸 */
@Composable
private fun DisabledCheckbox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.extraSmall)
        )
    }
}

private fun previewCartItem(
    id: Int,
    name: String,
    quantity: Int,
    originalPrice: Long? = null,
    discountRate: Int? = null,
    stock: Int = 50,
    status: ProductStatus = ProductStatus.ACTIVE,
    canAccess: Boolean = true
) = CartItem(
    id = id,
    product = Product(
        id = id,
        name = name,
        description = "",
        price = 89000L,
        originalPrice = originalPrice,
        discountRate = discountRate,
        imageUrls = emptyList(),
        brandName = null,
        tags = emptyList(),
        stock = stock,
        status = status,
        isActive = true,
        viewCount = 0,
        likeCount = 0,
        salesCount = 0,
        creator = previewUserSummary(displayName = "크리에이터 샵"),
        requiredTier = SubscriptionPlanTier.FREE,
        canAccess = canAccess
    ),
    quantity = quantity
)

@HaloLightDarkPreview
@Composable
private fun CartItemRowPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                CartItemRow(
                    cartItem = previewCartItem(1, "프리미엄 무선 이어폰 Pro Max", 1, 129000L, 31),
                    numberFormat = rememberKoreanNumberFormat(),
                    isSelected = true,
                    onClick = {},
                    onToggleSelection = {},
                    onUpdateQuantity = {},
                    onQuantityLimit = {},
                    onRemove = {}
                )
                CartItemRow(
                    cartItem = previewCartItem(2, "재고 3개 남은 상품", 3, stock = 3),
                    numberFormat = rememberKoreanNumberFormat(),
                    isSelected = false,
                    onClick = {},
                    onToggleSelection = {},
                    onUpdateQuantity = {},
                    onQuantityLimit = {},
                    onRemove = {}
                )
                CartItemRow(
                    cartItem = previewCartItem(3, "멤버십 전용 상품", 1, canAccess = false),
                    numberFormat = rememberKoreanNumberFormat(),
                    isSelected = false,
                    onClick = {},
                    onToggleSelection = {},
                    onUpdateQuantity = {},
                    onQuantityLimit = {},
                    onRemove = {}
                )
                CartItemRow(
                    cartItem = previewCartItem(4, "품절된 상품", 1, stock = 0, status = ProductStatus.SOLD_OUT),
                    numberFormat = rememberKoreanNumberFormat(),
                    isSelected = false,
                    onClick = {},
                    onToggleSelection = {},
                    onUpdateQuantity = {},
                    onQuantityLimit = {},
                    onRemove = {}
                )
            }
        }
    }
}
