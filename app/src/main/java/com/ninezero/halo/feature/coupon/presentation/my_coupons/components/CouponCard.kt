package com.ninezero.halo.feature.coupon.presentation.my_coupons.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.core.util.toFormattedPrice
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus

/** 내 쿠폰 줄: 이름 → 할인 → 조건 → 기간 */
@Composable
fun CouponCard(
    coupon: Coupon,
    modifier: Modifier = Modifier
) {
    val isUsable = coupon.status == UserCouponStatus.AVAILABLE
    val creatorLabel = coupon.creatorName?.takeIf { it.isNotBlank() }?.let { "$it 상품 전용" } ?: "크리에이터 상품 전용"
    val minOrderText = coupon.minOrderAmount?.let { "${it.toFormattedPrice()} 이상 구매 시" }
    val subColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isUsable) 1f else 0.5f)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = coupon.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (isUsable) {
                Text(text = creatorLabel, style = MaterialTheme.typography.bodySmall, color = subColor)
            } else {
                Text(
                    text = if (coupon.status == UserCouponStatus.USED) "사용 완료" else "만료",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = subColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(subColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Row {
            Text(
                text = if (coupon.type == CouponType.FREE_SHIPPING) "무료 배송" else coupon.discountText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alignByBaseline()
            )
            if (coupon.type != CouponType.FREE_SHIPPING) {
                Text(
                    text = " 할인",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alignByBaseline()
                )
            }
            coupon.maxDiscountAmount?.let { maxAmount ->
                Text(
                    text = " · 최대 ${maxAmount.toFormattedPrice()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = subColor,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }

        // 끝난 쿠폰은 크리에이터를 조건 줄로
        val conditionText = if (isUsable) minOrderText else listOfNotNull(creatorLabel, minOrderText).joinToString(" · ")
        if (!conditionText.isNullOrEmpty()) {
            Text(text = conditionText, style = MaterialTheme.typography.bodySmall, color = subColor)
        }

        Text(text = periodText(coupon), style = MaterialTheme.typography.bodySmall, color = subColor)
    }
}

@Composable
private fun periodText(coupon: Coupon): AnnotatedString {
    val errorColor = MaterialTheme.colorScheme.error
    return when (coupon.status) {
        UserCouponStatus.USED -> AnnotatedString(
            coupon.usedAt?.let { "${it.toFormattedDate("yyyy.MM.dd")} 사용" } ?: "사용 완료"
        )
        UserCouponStatus.EXPIRED -> AnnotatedString("${coupon.expiresAt.toFormattedDate("yyyy.MM.dd")} 만료")
        UserCouponStatus.AVAILABLE -> buildAnnotatedString {
            append("${coupon.expiresAt.toFormattedDate("yyyy.MM.dd")}까지 · ")
            val left = if (coupon.daysRemaining <= 0) "오늘 종료" else "${coupon.daysRemaining}일 남음"
            if (coupon.daysRemaining <= Constants.Commerce.COUPON_EXPIRING_SOON_DAYS) {
                withStyle(SpanStyle(color = errorColor, fontWeight = FontWeight.SemiBold)) { append(left) }
            } else {
                append(left)
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponCardPreview() {
    HaloTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CouponCard(
                coupon = Coupon(
                    id = 1,
                    creatorId = 1,
                    code = "WELCOME10",
                    name = "신규 가입 할인 쿠폰",
                    type = CouponType.PERCENTAGE,
                    discountValue = 10L,
                    minOrderAmount = 10000L,
                    maxDiscountAmount = 5000L,
                    status = UserCouponStatus.AVAILABLE,
                    expiresAt = System.currentTimeMillis() + 30 * 86_400_000L,
                    daysRemaining = 30
                )
            )
            CouponCard(
                coupon = Coupon(
                    id = 2,
                    creatorId = 1,
                    code = "SAVE5000",
                    name = "5천원 할인 쿠폰",
                    type = CouponType.FIXED_AMOUNT,
                    discountValue = 5000L,
                    minOrderAmount = 30000L,
                    maxDiscountAmount = null,
                    status = UserCouponStatus.AVAILABLE,
                    expiresAt = System.currentTimeMillis() + 3 * 86_400_000L,
                    daysRemaining = 2
                )
            )
            CouponCard(
                coupon = Coupon(
                    id = 3,
                    creatorId = 1,
                    code = "USED",
                    name = "사용된 쿠폰",
                    type = CouponType.FIXED_AMOUNT,
                    discountValue = 3000L,
                    minOrderAmount = null,
                    maxDiscountAmount = null,
                    status = UserCouponStatus.USED,
                    expiresAt = System.currentTimeMillis() + 10 * 86_400_000L,
                    daysRemaining = 10
                )
            )
            CouponCard(
                coupon = Coupon(
                    id = 4,
                    creatorId = 1,
                    code = "EXPIRED1",
                    name = "만료된 할인 쿠폰",
                    type = CouponType.PERCENTAGE,
                    discountValue = 15L,
                    minOrderAmount = 20000L,
                    maxDiscountAmount = null,
                    status = UserCouponStatus.EXPIRED,
                    expiresAt = System.currentTimeMillis() - 1 * 86_400_000L,
                    daysRemaining = 0
                )
            )
        }
    }
}
