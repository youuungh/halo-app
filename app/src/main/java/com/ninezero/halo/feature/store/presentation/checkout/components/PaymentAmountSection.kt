package com.ninezero.halo.feature.store.presentation.checkout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.MembershipColors
import com.ninezero.halo.feature.store.presentation.components.MembershipBreakdown
import com.ninezero.halo.feature.store.presentation.components.MembershipLine
import com.ninezero.halo.feature.store.presentation.components.PriceBreakdownRow
import com.ninezero.halo.feature.store.presentation.components.PriceSubLine
import com.ninezero.halo.feature.store.presentation.components.PriceSubLines
import java.text.NumberFormat

/** 결제 금액 내역, 장바구니 바 펼침과 같은 구성 */
@Composable
fun PaymentAmountSection(
    productAmount: Long,
    productDiscount: Long,
    couponDiscount: Long,
    shippingFee: Long,
    shippingCouponDiscount: Long,
    membership: MembershipBreakdown,
    usedPoints: Int,
    paymentAmount: Long,
    numberFormat: NumberFormat
) {
    val membershipColor = MembershipColors.label
    val discountColor = MaterialTheme.colorScheme.error

    CheckoutCard {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            PriceBreakdownRow("상품 금액", "${numberFormat.format(productAmount)}원")
            if (productDiscount > 0L) {
                PriceBreakdownRow("상품 할인", "-${numberFormat.format(productDiscount)}원", discountColor)
            }
            if (couponDiscount > 0L) {
                PriceBreakdownRow("쿠폰 할인", "-${numberFormat.format(couponDiscount)}원", discountColor)
            }
            PriceBreakdownRow("배송비", "${numberFormat.format(shippingFee)}원")
            if (membership.shippingLines.isNotEmpty() || shippingCouponDiscount > 0L) {
                PriceSubLines {
                    membership.shippingLines.forEach { line ->
                        PriceSubLine(
                            label = "${line.creatorName} 멤버십 무료배송",
                            value = "-${numberFormat.format(line.amount)}원",
                            color = membershipColor,
                            showSparkle = true
                        )
                    }
                    if (shippingCouponDiscount > 0L) {
                        PriceSubLine(
                            label = "무료배송 쿠폰",
                            value = "-${numberFormat.format(shippingCouponDiscount)}원",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (usedPoints > 0) {
                PriceBreakdownRow("포인트 사용", "-${numberFormat.format(usedPoints)}원", discountColor)
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "최종 결제 금액",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${numberFormat.format(paymentAmount.coerceAtLeast(0L))}원",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            PriceBreakdownRow(
                label = "구매 시 적립",
                value = "${numberFormat.format(membership.totalEarnPoints)}P",
                small = true
            )
            // 멤버십 적립이 섞일 때만 나눠 보여 줌
            if (membership.earnLines.isNotEmpty()) {
                PriceSubLines {
                    if (membership.baseEarnPoints > 0L) {
                        PriceSubLine(
                            label = "기본 적립 1%",
                            value = "${numberFormat.format(membership.baseEarnPoints)}P",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    membership.earnLines.forEach { line ->
                        PriceSubLine(
                            label = "${line.creatorName} 멤버십 적립 ${line.ratePercent}%",
                            value = "${numberFormat.format(line.amount)}P",
                            color = membershipColor,
                            showSparkle = true
                        )
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PaymentAmountSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                PaymentAmountSection(
                    productAmount = 83_000L,
                    productDiscount = 6_000L,
                    couponDiscount = 3_000L,
                    shippingFee = 9_000L,
                    shippingCouponDiscount = 0L,
                    membership = MembershipBreakdown(
                        shippingLines = listOf(MembershipLine("오닉스", 3_000L)),
                        baseEarnPoints = 320L,
                        earnLines = listOf(MembershipLine("오닉스", 2_250L, 5))
                    ),
                    usedPoints = 0,
                    paymentAmount = 80_000L,
                    numberFormat = rememberKoreanNumberFormat()
                )
            }
        }
    }
}
