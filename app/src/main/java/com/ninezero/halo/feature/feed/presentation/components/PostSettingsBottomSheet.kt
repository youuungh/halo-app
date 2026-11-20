package com.ninezero.halo.feature.feed.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.components.toggle.HaloSwitch
import com.ninezero.halo.core.ui.components.toggle.SelectionRadio
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.GroupShape
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun PostSettingsBottomSheet(
    sheetState: SheetState,
    isCreator: Boolean,
    isCommunityMode: Boolean,
    isSecret: Boolean,
    requiredTier: SubscriptionPlanTier,
    planTiers: Set<SubscriptionPlanTier>,
    onSecretChange: (Boolean) -> Unit,
    onTierChange: (SubscriptionPlanTier) -> Unit,
    onDismiss: () -> Unit
) {
    FloatingBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        PostSettingsContent(
            isCreator = isCreator,
            isCommunityMode = isCommunityMode,
            isSecret = isSecret,
            requiredTier = requiredTier,
            planTiers = planTiers,
            onSecretChange = onSecretChange,
            onTierChange = onTierChange,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun PostSettingsContent(
    isCreator: Boolean,
    isCommunityMode: Boolean,
    isSecret: Boolean,
    requiredTier: SubscriptionPlanTier,
    planTiers: Set<SubscriptionPlanTier>,
    onSecretChange: (Boolean) -> Unit,
    onTierChange: (SubscriptionPlanTier) -> Unit,
    onDismiss: () -> Unit
) {
    val hazeState = remember { HazeState() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        // 헤더: X 버튼 + 타이틀
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            GlassIconButton(
                onClick = onDismiss,
                icon = painterResource(R.drawable.ic_close),
                contentDescription = "닫기",
                hazeState = hazeState,
                backgroundColor = MaterialTheme.colorScheme.surface,
                size = 36.dp,
                iconSize = 20.dp,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(
                text = "상세설정",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 두 경우는 동시에 나오지 않음
        Column(modifier = Modifier.hazeSource(hazeState)) {
            if (isCommunityMode && !isCreator) {
                SettingsGroup {
                    SettingsSwitchItem(
                        title = "비밀글",
                        description = "크리에이터만 이 글을 볼 수 있습니다",
                        checked = isSecret,
                        onCheckedChange = onSecretChange
                    )
                }
            }

            if (isCreator && !isCommunityMode) {
                Text(
                    text = "공개 범위",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TierOptionList(
                    selectedTier = requiredTier,
                    planTiers = planTiers,
                    onTierSelect = onTierChange
                )
            }
        }
    }
}

/** 테두리 한 묶음, 줄 사이 가는 선은 안에서 */
@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GroupShape)
            .cardBorder(GroupShape)
    ) {
        content()
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        HaloSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/** 공개 범위 동그라미 목록 */
@Composable
private fun TierOptionList(
    selectedTier: SubscriptionPlanTier,
    planTiers: Set<SubscriptionPlanTier>,
    onTierSelect: (SubscriptionPlanTier) -> Unit
) {
    // 전체 공개와 플랜이 있는 등급만, 이미 고른 등급은 유지
    val tiers = listOf(
        TierOption(SubscriptionPlanTier.FREE, "전체 공개", "모든 사용자가 볼 수 있습니다"),
        TierOption(SubscriptionPlanTier.TIER1, "${SubscriptionPlanTier.TIER1.label} 이상", "${SubscriptionPlanTier.TIER1.label} · ${SubscriptionPlanTier.TIER2.label} 구독자만"),
        TierOption(SubscriptionPlanTier.TIER2, SubscriptionPlanTier.TIER2.label, "${SubscriptionPlanTier.TIER2.label} 구독자만")
    ).filter { option ->
        option.value == SubscriptionPlanTier.FREE || option.value == selectedTier ||
            option.value in planTiers
    }
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    SettingsGroup {
        tiers.forEachIndexed { index, tier ->
            if (index > 0) HorizontalDivider(thickness = 0.5.dp, color = lineColor)
            TierOptionItem(
                tier = tier,
                isSelected = selectedTier == tier.value,
                onClick = { onTierSelect(tier.value) }
            )
        }
    }
}

private data class TierOption(
    val value: SubscriptionPlanTier,
    val title: String,
    val description: String
)

@Composable
private fun TierOptionItem(
    tier: TierOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectionRadio(selected = isSelected)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tier.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = tier.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun hasSettingsOptions(isCreator: Boolean, isCommunityMode: Boolean): Boolean {
    return when {
        isCommunityMode && !isCreator -> true  // 팔로워 + 커뮤니티: 비밀글
        isCreator && !isCommunityMode -> true  // 크리에이터 + 본인피드: 티어
        else -> false
    }
}

@HaloLightDarkPreview
@Composable
private fun PostSettingsFollowerCommunityPreview() {
    HaloTheme {
        Surface {
            PostSettingsContent(
                isCreator = false,
                isCommunityMode = true,
                isSecret = false,
                requiredTier = SubscriptionPlanTier.FREE,
                planTiers = setOf(SubscriptionPlanTier.TIER1, SubscriptionPlanTier.TIER2),
                onSecretChange = {},
                onTierChange = {},
                onDismiss = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostSettingsCreatorFeedPreview() {
    HaloTheme {
        Surface {
            PostSettingsContent(
                isCreator = true,
                isCommunityMode = false,
                isSecret = false,
                requiredTier = SubscriptionPlanTier.TIER1,
                planTiers = setOf(SubscriptionPlanTier.TIER1),
                onSecretChange = {},
                onTierChange = {},
                onDismiss = {}
            )
        }
    }
}
