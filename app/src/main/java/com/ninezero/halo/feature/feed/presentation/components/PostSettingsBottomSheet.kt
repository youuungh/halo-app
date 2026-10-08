package com.ninezero.halo.feature.feed.presentation.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.switch.HaloSwitch
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun PostSettingsBottomSheet(
    sheetState: SheetState,
    isCreator: Boolean,
    isCommunityMode: Boolean,
    isSecret: Boolean,
    requiredTier: String,
    planTiers: Set<SubscriptionPlanTier>,
    onSecretChange: (Boolean) -> Unit,
    onTierChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        BottomSheetDragHandle()
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
    requiredTier: String,
    planTiers: Set<SubscriptionPlanTier>,
    onSecretChange: (Boolean) -> Unit,
    onTierChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val hazeState = remember { HazeState() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        // 헤더: 타이틀 + X 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "상세설정",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            GlassIconButton(
                onClick = onDismiss,
                icon = painterResource(R.drawable.ic_close),
                contentDescription = "닫기",
                hazeState = hazeState,
                backgroundColor = MaterialTheme.colorScheme.surface,
                size = 36.dp,
                iconSize = 20.dp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.hazeSource(hazeState)) {
            // 팔로워의 커뮤니티 글은 비밀글 설정
            // 크리에이터가 커뮤니티에 글 쓸 때는 비밀글 의미 없음
            if (isCommunityMode && !isCreator) {
                SettingsCard {
                    SettingsSwitchItem(
                        title = "비밀글",
                        description = "크리에이터만 이 글을 볼 수 있습니다",
                        checked = isSecret,
                        onCheckedChange = onSecretChange
                    )
                }
            }

            // 크리에이터는 멤버십 티어 설정
            if (isCreator && !isCommunityMode) {
                SettingsCard {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "공개 범위",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "이 포스트를 볼 수 있는 멤버십 티어를 설정합니다",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        TierSelector(
                            selectedTier = requiredTier,
                            planTiers = planTiers,
                            onTierSelect = onTierChange,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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

@Composable
private fun TierSelector(
    selectedTier: String,
    planTiers: Set<SubscriptionPlanTier>,
    onTierSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 전체 공개와 플랜이 있는 등급만, 이미 고른 등급은 유지
    val tiers = listOf(
        TierOption(SubscriptionPlanTier.FREE.name, "전체 공개", "모든 사용자가 볼 수 있습니다"),
        TierOption(SubscriptionPlanTier.TIER1.name, "TIER 1", "TIER 1 이상 구독자만"),
        TierOption(SubscriptionPlanTier.TIER2.name, "TIER 2", "TIER 2 구독자만")
    ).filter { option ->
        option.value == SubscriptionPlanTier.FREE.name || option.value == selectedTier ||
            planTiers.any { it.name == option.value }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tiers.forEach { tier ->
            TierOptionItem(
                tier = tier,
                isSelected = selectedTier == tier.value,
                onClick = { onTierSelect(tier.value) }
            )
        }
    }
}

private data class TierOption(
    val value: String,
    val title: String,
    val description: String
)

@Composable
private fun TierOptionItem(
    tier: TierOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 체크 아이콘
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = "선택됨",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tier.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = tier.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                requiredTier = SubscriptionPlanTier.FREE.name,
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
                requiredTier = SubscriptionPlanTier.TIER1.name,
                planTiers = setOf(SubscriptionPlanTier.TIER1),
                onSecretChange = {},
                onTierChange = {},
                onDismiss = {}
            )
        }
    }
}
