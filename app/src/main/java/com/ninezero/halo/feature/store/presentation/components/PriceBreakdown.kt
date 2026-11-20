package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.feature.store.domain.model.OrderLine
import com.ninezero.halo.feature.store.domain.model.StorePricingSummary
import com.ninezero.halo.feature.store.domain.model.calcEarnPoints
import com.ninezero.halo.feature.store.domain.model.earnRatePercent
import com.ninezero.halo.feature.store.domain.model.isFreeShippingReached
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier

/** 멤버십 혜택 하위 줄 하나 */
@Immutable
data class MembershipLine(
    val creatorName: String,
    val amount: Long,
    val ratePercent: Int = 0
)

/** 배송비·적립에서 멤버십 몫을 따로 뺀 내역 */
@Immutable
data class MembershipBreakdown(
    val shippingLines: List<MembershipLine> = emptyList(),
    val baseEarnPoints: Long = 0L,
    val earnLines: List<MembershipLine> = emptyList()
) {
    val freeShippingAmount: Long
        get() = shippingLines.sumOf { it.amount }

    val totalEarnPoints: Long
        get() = baseEarnPoints + earnLines.sumOf { it.amount }
}

fun calcMembershipBreakdown(
    items: List<OrderLine>,
    pricing: StorePricingSummary,
    subscribedCreatorIds: Set<Int>,
    tierOf: (creatorId: Int) -> SubscriptionPlanTier?
): MembershipBreakdown {
    val groups = items.groupBy { it.product.creator.id }
    val nameOf = { creatorId: Int ->
        groups.getValue(creatorId).first().product.creator.let { it.displayName ?: it.username }
    }
    val subtotalOf = { creatorId: Int -> groups.getValue(creatorId).sumOf { it.subtotal } }

    // 금액 기준을 넘긴 크리에이터는 멤버십 없이도 무료라 빼지 않음
    val shippingLines = pricing.shipping.membershipFreeShipIds
        .filter { it in groups && !isFreeShippingReached(subtotalOf(it)) }
        .map { creatorId -> MembershipLine(nameOf(creatorId), pricing.shipping.baseFeeByCreator[creatorId] ?: 0L) }

    var baseEarnPoints = 0L
    val earnLines = mutableListOf<MembershipLine>()
    groups.keys.forEach { creatorId ->
        val rate = earnRatePercent(tierOf(creatorId))
        val points = calcEarnPoints(subtotalOf(creatorId), rate)
        if (creatorId in subscribedCreatorIds) {
            earnLines += MembershipLine(nameOf(creatorId), points, rate)
        } else {
            baseEarnPoints += points
        }
    }
    return MembershipBreakdown(shippingLines, baseEarnPoints, earnLines)
}

/** 결제 금액 내역 한 줄 */
@Composable
fun PriceBreakdownRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    small: Boolean = false
) {
    val style = if (small) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = style, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = style, color = valueColor)
    }
}

/** 왼쪽 세로선으로 묶은 하위 줄 */
@Composable
fun PriceSubLines(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(start = 4.dp, top = 3.dp, bottom = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), CircleShape)
        )
        Column(
            modifier = Modifier.padding(start = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            content()
        }
    }
}

@Composable
fun PriceSubLine(
    label: String,
    value: String,
    color: Color,
    showSparkle: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showSparkle) {
            Icon(
                painter = painterResource(R.drawable.ic_sparkle),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(3.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.weight(1f)
        )
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

/** 크리에이터 카드 아래 금액 정보 한 줄 */
@Composable
fun CardInfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
            color = valueColor
        )
    }
}
