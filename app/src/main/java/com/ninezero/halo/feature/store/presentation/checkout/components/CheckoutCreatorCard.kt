package com.ninezero.halo.feature.store.presentation.checkout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.badge.MembershipBadge
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.media.Avatar
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.theme.CardShape
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.core.ui.theme.MembershipColors
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderLine
import com.ninezero.halo.feature.store.domain.model.calcEarnPoints
import com.ninezero.halo.feature.store.presentation.components.CardInfoRow
import com.ninezero.halo.feature.store.presentation.components.OrderDetailItemRow
import java.text.NumberFormat

/** 크리에이터 카드 안 쿠폰 칸 상태 */
@Immutable
sealed interface CouponBoxState {
    data object Loading : CouponBoxState
    data object Failed : CouponBoxState
    data object None : CouponBoxState
    data class Available(val count: Int) : CouponBoxState
    data class Applied(val couponName: String, val amount: Long) : CouponBoxState
}

@Composable
fun CheckoutCreatorCard(
    creator: UserSummary,
    items: List<OrderLine>,
    isSubscribed: Boolean,
    earnRatePercent: Int,
    shippingFee: Long,
    couponDiscount: Long,
    couponState: CouponBoxState,
    numberFormat: NumberFormat,
    onCouponClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subtotal = items.sumOf { it.subtotal }
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .then(
                if (isSubscribed) {
                    Modifier
                        .background(MembershipColors.groupCardBrush, CardShape)
                        .border(0.5.dp, MembershipColors.groupCardBorder, CardShape)
                } else {
                    Modifier.border(0.5.dp, dividerColor, CardShape)
                }
            )
            .clip(CardShape)
            .padding(bottom = 16.dp)
    ) {
        CreatorHeader(creator = creator, isSubscribed = isSubscribed)
        HorizontalDivider(
            thickness = 0.5.dp,
            color = dividerColor,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        items.forEach { line ->
            OrderDetailItemRow(
                item = OrderItem(
                    id = line.product.id,
                    productId = line.product.id,
                    productName = line.product.name,
                    productImageUrl = line.product.imageUrls.firstOrNull(),
                    price = line.product.price,
                    originalPrice = line.product.originalPrice,
                    quantity = line.quantity,
                    subtotal = line.subtotal
                ),
                numberFormat = numberFormat,
                showPrice = false,
                showLineTotal = true
            )
        }

        CouponBox(
            state = couponState,
            isGlass = isSubscribed,
            numberFormat = numberFormat,
            onClick = onCouponClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        HorizontalDivider(
            thickness = 0.5.dp,
            color = dividerColor,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CardInfoRow(label = "상품금액", value = "${numberFormat.format(subtotal)}원")
            CardInfoRow(
                label = "배송비",
                value = if (shippingFee == 0L) "무료배송" else "${numberFormat.format(shippingFee)}원",
                valueColor = when {
                    isSubscribed -> MembershipColors.label
                    shippingFee == 0L -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                isHighlighted = shippingFee == 0L
            )
            CardInfoRow(
                label = "적립 예정",
                value = "${numberFormat.format(calcEarnPoints(subtotal, earnRatePercent))}P (${earnRatePercent}%)",
                valueColor = if (isSubscribed) MembershipColors.label else MaterialTheme.colorScheme.onSurfaceVariant,
                isHighlighted = isSubscribed
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "예상 결제금액",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${numberFormat.format((subtotal + shippingFee - couponDiscount).coerceAtLeast(0L))}원",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun CreatorHeader(creator: UserSummary, isSubscribed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            model = creator.avatarThumbUrl ?: creator.avatarUrl,
            size = 24.dp,
            contentDescription = "${creator.displayName} 프로필"
        )
        Spacer(modifier = Modifier.width(8.dp))
        // 남는 폭을 이름 줄이 가져가 멤버십 표시는 맨 오른쪽에 붙음
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = creator.displayName ?: creator.username,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            VerifiedBadge(role = creator.role, size = 14.dp)
        }
        if (isSubscribed) {
            Spacer(modifier = Modifier.width(8.dp))
            MembershipBadge()
        }
    }
}

@Composable
private fun CouponBox(
    state: CouponBoxState,
    isGlass: Boolean,
    numberFormat: NumberFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val shape = MaterialTheme.shapes.medium
    // 멤버십 카드 위에선 회색 대신 유리 칸
    val boxModifier = if (isGlass) {
        Modifier
            .background(Color.White.copy(alpha = if (isDark) 0.08f else 0.55f), shape)
            .border(1.dp, Color.White.copy(alpha = if (isDark) 0.14f else 0.9f), shape)
    } else {
        Modifier.background(MaterialTheme.colorScheme.surfaceVariant, shape)
    }

    Row(
        modifier = modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .bounceClick()  // 바탕까지 함께 작아지도록 먼저 적용
            .then(boxModifier)
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "쿠폰",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            val (text, color) = when (state) {
                CouponBoxState.Loading -> "불러오는 중" to MaterialTheme.colorScheme.onSurfaceVariant
                CouponBoxState.Failed -> "쿠폰을 불러오지 못했습니다" to MaterialTheme.colorScheme.onSurfaceVariant
                CouponBoxState.None -> "사용 가능한 쿠폰 없음" to MaterialTheme.colorScheme.onSurfaceVariant
                is CouponBoxState.Available -> "사용 가능 ${state.count}장" to MaterialTheme.colorScheme.primary
                is CouponBoxState.Applied ->
                    "-${numberFormat.format(state.amount)}원" to MaterialTheme.colorScheme.error
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (state is CouponBoxState.Applied) FontWeight.SemiBold else FontWeight.Normal,
                color = color
            )
            if (state is CouponBoxState.Applied) {
                Text(
                    text = state.couponName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
