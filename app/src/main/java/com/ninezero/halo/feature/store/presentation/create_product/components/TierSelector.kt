package com.ninezero.halo.feature.store.presentation.create_product.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun TierSelector(
    selectedTier: String,
    planTiers: Set<SubscriptionPlanTier>,
    onTierSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 전체 공개와 플랜이 있는 등급만, 이미 고른 등급은 유지
    val tiers = listOf(
        SubscriptionPlanTier.FREE.name to "전체 공개",
        SubscriptionPlanTier.TIER1.name to "Tier 1",
        SubscriptionPlanTier.TIER2.name to "Tier 2"
    ).filter { (tier, _) ->
        tier == SubscriptionPlanTier.FREE.name || tier == selectedTier || planTiers.any { it.name == tier }
    }

    Column(modifier = modifier) {
        Text(
            text = "구매 가능 등급",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tiers.forEach { (tier, label) ->
                FilterChip(
                    selected = selectedTier == tier,
                    onClick = { onTierSelected(tier) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun TierSelectorPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                TierSelector(selectedTier = "FREE", planTiers = setOf(SubscriptionPlanTier.TIER1), onTierSelected = {})
                TierSelector(selectedTier = "TIER2", planTiers = setOf(SubscriptionPlanTier.TIER1, SubscriptionPlanTier.TIER2), onTierSelected = {})
            }
        }
    }
}
