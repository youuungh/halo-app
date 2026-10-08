package com.ninezero.halo.feature.coupon.presentation.coupon_manage.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.core.util.toFormattedPrice
import com.ninezero.halo.feature.coupon.domain.model.CouponDiscountTarget
import com.ninezero.halo.feature.coupon.domain.model.CouponStatus
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.CreatorCoupon
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun CreatorCouponCard(
    coupon: CreatorCoupon,
    onMoreOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentAlpha = if (coupon.isActive && !coupon.isExpired) 1f else 0.5f
    val targetProgress = if (coupon.totalQuantity > 0) {
        coupon.issuedQuantity.toFloat() / coupon.totalQuantity.toFloat()
    } else 0f
    var animatedTarget by rememberSaveable { mutableFloatStateOf(0f) }
    LaunchedEffect(targetProgress) { animatedTarget = targetProgress }
    val progress by animateFloatAsState(
        targetValue = animatedTarget,
        animationSpec = tween(durationMillis = 800),
        label = "progressAnim"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 옵션
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = coupon.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onMoreOptions() }
                    .bounceClick(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ellipsis_horizontal),
                    contentDescription = "옵션",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 할인 금액
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = coupon.discountText,
                modifier = Modifier.alignByBaseline(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
            )
            coupon.maxDiscountAmount?.let { maxAmount ->
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(최대 ${maxAmount.toBigDecimal().toInt().toFormattedPrice()})",
                    modifier = Modifier.alignByBaseline(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 코드 + 기간 + 조건
        val infoText = buildString {
            append(coupon.code)
            append(" · ${coupon.endDate.toFormattedDate("yyyy.MM.dd")}까지")
            if (coupon.isExpired) append(" (만료됨)")
            val minAmount = coupon.minOrderAmount
            if (minAmount != "0" && minAmount.isNotBlank()) {
                append(" · 최소 ${minAmount.toBigDecimal().toInt().toFormattedPrice()}")
            }
        }

        Text(
            text = infoText,
            style = MaterialTheme.typography.bodySmall,
            color = if (coupon.isExpired)
                MaterialTheme.colorScheme.error.copy(alpha = contentAlpha)
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 발급 현황 프로그레스 바
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (coupon.isActive && !coupon.isExpired) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "${coupon.issuedQuantity}/${coupon.totalQuantity} 발급",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreatorCouponCardPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                CreatorCouponCard(
                    coupon = CreatorCoupon(
                        id = 1,
                        code = "WELCOME10",
                        name = "신규 팬 할인 쿠폰",
                        description = null,
                        type = CouponType.PERCENTAGE,
                        discountTarget = CouponDiscountTarget.ALL,
                        discountValue = "10",
                        minOrderAmount = "10000",
                        maxDiscountAmount = "5000",
                        totalQuantity = 100,
                        issuedQuantity = 23,
                        maxUseCount = 1,
                        status = CouponStatus.ACTIVE,
                        startDate = System.currentTimeMillis(),
                        endDate = System.currentTimeMillis() + 30 * 86_400_000L
                    ),
                    onMoreOptions = {}
                )
                CreatorCouponCard(
                    coupon = CreatorCoupon(
                        id = 2,
                        code = "SAVE5000",
                        name = "5천원 할인 쿠폰",
                        description = null,
                        type = CouponType.FIXED_AMOUNT,
                        discountTarget = CouponDiscountTarget.ALL,
                        discountValue = "5000",
                        minOrderAmount = "30000",
                        maxDiscountAmount = null,
                        totalQuantity = 200,
                        issuedQuantity = 45,
                        maxUseCount = 1,
                        status = CouponStatus.ACTIVE,
                        startDate = System.currentTimeMillis(),
                        endDate = System.currentTimeMillis() + 7 * 86_400_000L
                    ),
                    onMoreOptions = {}
                )
                CreatorCouponCard(
                    coupon = CreatorCoupon(
                        id = 3,
                        code = "EXPIRED",
                        name = "만료된 쿠폰",
                        description = null,
                        type = CouponType.FIXED_AMOUNT,
                        discountTarget = CouponDiscountTarget.ALL,
                        discountValue = "3000",
                        minOrderAmount = "0",
                        maxDiscountAmount = null,
                        totalQuantity = 50,
                        issuedQuantity = 50,
                        maxUseCount = 1,
                        status = CouponStatus.DISABLED,
                        startDate = System.currentTimeMillis() - 30 * 86_400_000L,
                        endDate = System.currentTimeMillis() - 1 * 86_400_000L
                    ),
                    onMoreOptions = {}
                )
            }
        }
    }
}
