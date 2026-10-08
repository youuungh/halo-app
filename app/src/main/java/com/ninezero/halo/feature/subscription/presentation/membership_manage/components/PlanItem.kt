package com.ninezero.halo.feature.subscription.presentation.membership_manage.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toFormattedCount
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
internal fun PlanItem(
    plan: SubscriptionPlan,
    onMoreOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentAlpha = if (plan.isActive) 1f else 0.5f
    val numberFormat = rememberKoreanNumberFormat()

    val tierText = when (plan.tier) {
        SubscriptionPlanTier.FREE -> "무료"
        SubscriptionPlanTier.TIER1 -> "Tier 1"
        SubscriptionPlanTier.TIER2 -> "Tier 2"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 플랜명 + 옵션
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = plan.name,
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

        // 가격
        Text(
            text = "₩${plan.price.toBigDecimalOrNull()?.let { numberFormat.format(it) } ?: plan.price}/월",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Tier · 설명
        val infoText = buildString {
            append(tierText)
            if (plan.description.isNotBlank()) {
                append(" · ${plan.description}")
            }
        }

        Text(
            text = infoText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${plan.subscriberCount.toFormattedCount()}명 구독중",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PlanItemPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                PlanItem(
                    plan = SubscriptionPlan(
                        id = 1,
                        name = "Basic",
                        tier = SubscriptionPlanTier.TIER1,
                        description = "기본 혜택을 누려보세요",
                        price = "4,900",
                        benefits = listOf("전용 콘텐츠 열람", "커뮤니티 참여"),
                        subscriberCount = 128,
                        isActive = true
                    ),
                    onMoreOptions = {}
                )
                PlanItem(
                    plan = SubscriptionPlan(
                        id = 2,
                        name = "Premium",
                        tier = SubscriptionPlanTier.TIER2,
                        description = "프리미엄 혜택을 누려보세요. 모든 기본 혜택 포함.",
                        price = "9,900",
                        benefits = listOf("전용 콘텐츠 열람", "커뮤니티 참여", "1:1 메시지"),
                        subscriberCount = 45,
                        isActive = false
                    ),
                    onMoreOptions = {}
                )
            }
        }
    }
}
