package com.ninezero.halo.feature.coupon.presentation.my_coupons.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.core.util.toFormattedPrice
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.LocalIsDarkTheme

@Composable
fun CouponCard(
    coupon: Coupon,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalIsDarkTheme.current
    val isUsable = coupon.status == UserCouponStatus.AVAILABLE
    val contentAlpha = if (isUsable) 1f else 0.5f

    val statusText: String
    val statusTextColor: Color
    val gradientBrush: Brush

    when (coupon.status) {
        UserCouponStatus.AVAILABLE -> {
            statusText = "사용 가능"
            statusTextColor = if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF1976D2)
            gradientBrush = Brush.verticalGradient(
                colorStops = if (isDarkTheme) arrayOf(
                    0.0f to Color(0xFF1E3A5F),
                    0.5f to Color(0xFF2D4A6F),
                    1.0f to Color(0xFF1E4A5F)
                ) else arrayOf(
                    0.0f to Color(0xFFE8F4FD),
                    0.5f to Color(0xFFE0F0FF),
                    1.0f to Color(0xFFE8F8F4)
                )
            )
        }
        UserCouponStatus.USED -> {
            statusText = "사용완료"
            statusTextColor = if (isDarkTheme) Color(0xFF888888) else Color(0xFF999999)
            gradientBrush = Brush.verticalGradient(
                if (isDarkTheme) listOf(Color(0xFF2A2A2A), Color(0xFF333333))
                else listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE))
            )
        }
        UserCouponStatus.EXPIRED -> {
            statusText = "만료"
            statusTextColor = if (isDarkTheme) Color(0xFFFF8A80) else Color(0xFFD32F2F)
            gradientBrush = Brush.verticalGradient(
                if (isDarkTheme) listOf(Color(0xFF3D1E1E), Color(0xFF4D2828))
                else listOf(Color(0xFFFFF0F0), Color(0xFFFFE8E8))
            )
        }
    }

    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(
        alpha = if (isUsable) 0.6f else 0.3f
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .border(0.3.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        // 좌측: 할인 금액 + 쿠폰 정보
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            // 할인 금액
            Row {
                Text(
                    text = coupon.discountText,
                    modifier = Modifier.alignByBaseline(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                )
                coupon.maxDiscountAmount?.let { maxAmount ->
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(최대 ${maxAmount.toBigDecimal().toInt().toFormattedPrice()})",
                        modifier = Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                    )
                }
            }

            // 쿠폰명 + 유효기간
            if (coupon.type != CouponType.FREE_SHIPPING) {
                Text(
                    text = coupon.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            val creatorBadgeText = coupon.creatorName
                ?.takeIf { it.isNotBlank() }
                ?.let { "$it 상품 전용" }
                ?: "특정 크리에이터 상품 전용"
            CreatorExclusiveBadge(
                text = creatorBadgeText,
                contentAlpha = contentAlpha
            )
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${coupon.expiresAt.toFormattedDate("yyyy.MM.dd")}까지",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
            )
            // 최소 주문 금액
            coupon.minOrderAmount?.let { minAmount ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${minAmount.toBigDecimal().toInt().toFormattedPrice()} 이상 주문 시",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }
        }

        // 우측: 그라데이션 상태 영역
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(84.dp)
                .background(gradientBrush),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        lineHeight = 16.sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = statusTextColor
                )
                if (isUsable) {
                    Text(
                        text = "${coupon.daysRemaining}일 남음",
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = if (coupon.daysRemaining <= 3) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatorExclusiveBadge(
    text: String,
    contentAlpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f * contentAlpha)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
        )
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
                    discountValue = "10",
                    minOrderAmount = "10000",
                    maxDiscountAmount = "5000",
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
                    discountValue = "5000",
                    minOrderAmount = "30000",
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
                    discountValue = "3000",
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
                    discountValue = "15",
                    minOrderAmount = "20000",
                    maxDiscountAmount = null,
                    status = UserCouponStatus.EXPIRED,
                    expiresAt = System.currentTimeMillis() - 1 * 86_400_000L,
                    daysRemaining = 0
                )
            )
        }
    }
}
