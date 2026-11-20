package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.feature.store.domain.model.calcSingleCouponDiscount

/** 쿠폰 적용가 펼침 내역 */
@Composable
fun CouponPriceDetail(
    originalPrice: Int?,
    price: Int,
    coupon: Coupon,
    modifier: Modifier = Modifier
) {
    val numberFormat = rememberKoreanNumberFormat()
    val creatorDiscount = if (originalPrice != null && originalPrice > price) originalPrice - price else 0
    val couponDiscount = calcSingleCouponDiscount(coupon, price.toLong()).toInt()
    val couponAppliedPrice = price - couponDiscount

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PriceRow(
            label = "상품가격",
            amount = "${numberFormat.format(originalPrice ?: price)}원"
        )
        if (creatorDiscount > 0) {
            PriceRow(
                label = "크리에이터 할인",
                amount = "-${numberFormat.format(creatorDiscount)}원"
            )
        }
        PriceRow(
            label = coupon.discountText + " 쿠폰",
            amount = "-${numberFormat.format(couponDiscount)}원"
        )

        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(2.dp))

        PriceRow(
            label = "쿠폰적용가",
            amount = "${numberFormat.format(couponAppliedPrice)}원",
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PriceRow(
    label: String,
    amount: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = fontWeight,
            color = color
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = fontWeight,
            color = color
        )
    }
}

private val previewCoupon = Coupon(
    id = 1, creatorId = 1, code = "SAVE10", name = "10% 할인 쿠폰",
    type = CouponType.PERCENTAGE, discountValue = 10L,
    minOrderAmount = null, maxDiscountAmount = null,
    status = UserCouponStatus.AVAILABLE,
    expiresAt = System.currentTimeMillis() + 30 * 86_400_000L, daysRemaining = 30
)

@HaloLightDarkPreview
@Composable
private fun CouponPriceDetailPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CouponPriceDetail(
                originalPrice = 129000,
                price = 89000,
                coupon = previewCoupon,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponPriceDetailNoDiscountPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CouponPriceDetail(
                originalPrice = null,
                price = 89000,
                coupon = previewCoupon,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
