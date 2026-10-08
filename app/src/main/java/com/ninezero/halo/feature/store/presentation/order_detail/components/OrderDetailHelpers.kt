package com.ninezero.halo.feature.store.presentation.order_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.feature.store.domain.model.OrderCreatorGroup
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import java.text.NumberFormat

internal fun OrderCreatorGroup.isCancellable(): Boolean {
    return creatorId != null &&
        status in setOf(OrderStatus.PENDING, OrderStatus.CONFIRMED) &&
        trackingNumber.isNullOrBlank() &&
        shippingStatus == ShippingStatus.PREPARING
}

@Composable
internal fun OrderEarnPointsSection(
    creatorGroups: List<OrderCreatorGroup>,
    numberFormat: NumberFormat
) {
    data class EarnDetail(
        val creatorName: String,
        val tier: SubscriptionPlanTier,
        val ratePercent: Int,
        val earnPoints: Long
    )

    val details = remember(creatorGroups) {
        // 적립은 서버 계산값
        creatorGroups.map { group ->
            EarnDetail(
                creatorName = group.creatorName ?: "Unknown",
                tier = group.earnTier,
                ratePercent = (group.earnRate * 100).toInt(),
                earnPoints = group.earnPoints
            )
        }
    }

    val totalEarnPoints = remember(details) { details.sumOf { it.earnPoints } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "적립 포인트",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${numberFormat.format(totalEarnPoints)}P",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        details.forEach { detail ->
            // 요율은 서버값
            val tierPrefix = if (detail.tier == SubscriptionPlanTier.FREE) "기본" else "멤버십"
            val tierLabel = "$tierPrefix ${detail.ratePercent}%"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${detail.creatorName} ($tierLabel)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${numberFormat.format(detail.earnPoints)}P",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

internal fun getPaymentProviderText(provider: String): String = when (provider) {
    "MOCK" -> "테스트 결제"
    "TOSS_PAYMENTS" -> "토스페이"
    "TOSS_BILLING" -> "저장카드 결제"
    "NONE" -> "포인트·쿠폰"
    else -> provider
}
