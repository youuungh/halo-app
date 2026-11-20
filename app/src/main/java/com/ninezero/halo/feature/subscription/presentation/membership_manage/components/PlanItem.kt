package com.ninezero.halo.feature.subscription.presentation.membership_manage.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.CardShape
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.util.toFormattedCount
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier

@Composable
internal fun PlanItem(
    plan: SubscriptionPlan,
    onMoreOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = rememberKoreanNumberFormat()
    var isBenefitsExpanded by rememberSaveable(plan.id) { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isBenefitsExpanded) 180f else 0f,
        label = "benefits_chevron"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .cardBorder(CardShape)
            .clip(CardShape)
            .alpha(if (plan.isActive) 1f else 0.5f)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 14.dp)
    ) {
        // 플랜명 + 옵션
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
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
            // 가격
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${numberFormat.format(plan.price)}원",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "/월",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alignByBaseline()
                )
            }

            if (plan.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 혜택 펼침
            if (plan.benefits.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isBenefitsExpanded = !isBenefitsExpanded }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "혜택 ${plan.benefits.size}개",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_down),
                        contentDescription = if (isBenefitsExpanded) "혜택 접기" else "혜택 펼치기",
                        modifier = Modifier
                            .size(14.dp)
                            .graphicsLayer { rotationZ = chevronRotation },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(
                    visible = isBenefitsExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    BenefitGrid(
                        benefits = plan.benefits,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 10.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // 구독자 수
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_users),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "구독자 ${plan.subscriberCount.toFormattedCount()}명",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 혜택을 두 열 체크 목록으로 */
@Composable
private fun BenefitGrid(
    benefits: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        benefits.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { benefit ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = benefit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PlanItemPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PlanItem(
                    plan = SubscriptionPlan(
                        id = 1,
                        name = "Basic",
                        tier = SubscriptionPlanTier.TIER1,
                        description = "기본 혜택을 누려보세요",
                        price = 4900L,
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
                        price = 9900L,
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
