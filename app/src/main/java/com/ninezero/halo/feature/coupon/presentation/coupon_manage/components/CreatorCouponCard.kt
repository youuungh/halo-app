package com.ninezero.halo.feature.coupon.presentation.coupon_manage.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.copyCodeToClipboard
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.core.util.toFormattedPrice
import com.ninezero.halo.feature.coupon.domain.model.CouponDiscountTarget
import com.ninezero.halo.feature.coupon.domain.model.CouponStatus
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.CreatorCoupon
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun CreatorCouponCard(
    coupon: CreatorCoupon,
    onMoreOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEnded = coupon.isExpired || coupon.status == CouponStatus.EXPIRED
    val isLive = coupon.isActive && !isEnded
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
            .alpha(if (isLive) 1f else 0.5f)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 14.dp)
    ) {
        // 이름 + 상태 + 옵션
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 남는 폭은 이름 칸이 가져가 옵션 버튼이 오른쪽 끝에 붙음
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = coupon.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (!isLive) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEnded) "종료" else "중지됨",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onMoreOptions() }
                    .bounceClick(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ellipsis_horizontal),
                    contentDescription = "옵션",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column {
            // 할인
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = coupon.discountText,
                    modifier = Modifier.alignByBaseline(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (coupon.type != CouponType.FREE_SHIPPING) {
                    Text(
                        text = " 할인",
                        modifier = Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                // 최대 할인은 할인 줄에
                coupon.maxDiscountAmount?.let { maxAmount ->
                    Text(
                        text = " · 최대 ${maxAmount.toFormattedPrice()}",
                        modifier = Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 최소 주문
            coupon.minOrderAmount.takeIf { it > 0L }?.let { minAmount ->
                Text(
                    text = "${minAmount.toFormattedPrice()} 이상 구매 시",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 코드 · 기간, 코드는 누르면 복사
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { context.copyCodeToClipboard(coupon.code) }
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = coupon.code,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = "코드 복사",
                        modifier = Modifier.size(11.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = buildPeriodText(coupon.endDate, isEnded),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 발급 현황
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                if (isLive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${coupon.issuedQuantity}/${coupon.totalQuantity} 발급",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 7일 안에 끝나면 남은 날을 빨강으로 덧붙임 */
@Composable
private fun buildPeriodText(endDate: Long, isEnded: Boolean): AnnotatedString {
    val dateText = endDate.toFormattedDate("yyyy.MM.dd")
    val soonColor = MaterialTheme.colorScheme.error
    if (isEnded) return AnnotatedString("$dateText 종료")
    val zone = ZoneId.systemDefault()
    val daysLeft = ChronoUnit.DAYS.between(
        LocalDate.now(zone),
        Instant.ofEpochMilli(endDate).atZone(zone).toLocalDate()
    )
    return buildAnnotatedString {
        append("${dateText}까지")
        if (daysLeft in 0..Constants.Commerce.COUPON_EXPIRING_SOON_DAYS) {
            withStyle(SpanStyle(color = soonColor, fontWeight = FontWeight.SemiBold)) {
                append(if (daysLeft == 0L) " · 오늘 종료" else " · ${daysLeft}일 남음")
            }
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
                        discountValue = 10L,
                        minOrderAmount = 10000L,
                        maxDiscountAmount = 5000L,
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
                        discountValue = 5000L,
                        minOrderAmount = 30000L,
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
                        discountValue = 3000L,
                        minOrderAmount = 0L,
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
