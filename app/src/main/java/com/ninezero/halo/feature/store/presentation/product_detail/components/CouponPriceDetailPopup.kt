package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.feature.store.domain.model.calcSingleCouponDiscount
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun CouponPriceDetailPopup(
    originalPrice: Int?,
    price: Int,
    coupon: Coupon,
    onDismiss: () -> Unit
) {
    Popup(
        alignment = Alignment.TopStart,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        CouponPriceDetailContent(
            originalPrice = originalPrice,
            price = price,
            coupon = coupon,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun CouponPriceDetailContent(
    originalPrice: Int?,
    price: Int,
    coupon: Coupon,
    onDismiss: (() -> Unit)? = null
) {
    val numberFormat = rememberKoreanNumberFormat()
    val creatorDiscount = if (originalPrice != null && originalPrice > price) originalPrice - price else 0
    val couponDiscount = calcSingleCouponDiscount(coupon, price.toLong()).toInt()
    val couponAppliedPrice = price - couponDiscount

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 14.dp, bottom = 16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "상세 내역",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss?.invoke() },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PriceRow(
                label = "상품가격",
                amount = "${numberFormat.format(originalPrice ?: price)}원",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (creatorDiscount > 0) {
                PriceRow(
                    label = "크리에이터 할인",
                    amount = "-${numberFormat.format(creatorDiscount)}원",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            PriceRow(
                label = coupon.discountText + " 쿠폰",
                amount = "-${numberFormat.format(couponDiscount)}원",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            PriceRow(
                label = "쿠폰적용가",
                amount = "${numberFormat.format(couponAppliedPrice)}원",
                labelFontWeight = FontWeight.Bold,
                amountFontWeight = FontWeight.Bold,
                labelColor = MaterialTheme.colorScheme.error,
                amountColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onBackground,
    amountColor: Color = MaterialTheme.colorScheme.onBackground,
    labelFontWeight: FontWeight = FontWeight.Normal,
    amountFontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = labelFontWeight,
            color = labelColor
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = amountFontWeight,
            color = amountColor
        )
    }
}

private val previewCouponPercentage = Coupon(
    id = 1, creatorId = 1, code = "SAVE10", name = "10% 할인 쿠폰",
    type = CouponType.PERCENTAGE, discountValue = "10",
    minOrderAmount = null, maxDiscountAmount = null,
    status = UserCouponStatus.AVAILABLE,
    expiresAt = System.currentTimeMillis() + 30 * 86_400_000L, daysRemaining = 30
)

private val previewCouponFixed = Coupon(
    id = 2, creatorId = 1, code = "FIXED5000", name = "5,000원 할인 쿠폰",
    type = CouponType.FIXED_AMOUNT, discountValue = "5000",
    minOrderAmount = null, maxDiscountAmount = null,
    status = UserCouponStatus.AVAILABLE,
    expiresAt = System.currentTimeMillis() + 30 * 86_400_000L, daysRemaining = 30
)

@HaloLightDarkPreview
@Composable
private fun CouponPriceDetailContentPreview() {
    HaloTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CouponPriceDetailContent(
                originalPrice = 129000,
                price = 89000,
                coupon = previewCouponPercentage
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponPriceDetailContentNoDiscountPreview() {
    HaloTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CouponPriceDetailContent(
                originalPrice = null,
                price = 89000,
                coupon = previewCouponFixed
            )
        }
    }
}
