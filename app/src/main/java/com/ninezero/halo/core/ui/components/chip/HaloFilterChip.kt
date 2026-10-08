package com.ninezero.halo.core.ui.components.chip

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/** 필터 칩 */
@Composable
fun HaloFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    hazeBackgroundColor: Color = MaterialTheme.colorScheme.background
) {
    val glassTint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val glassSelected = selected && hazeState != null

    Box(modifier = modifier.bounceClick()) {
        if (selected && hazeState != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .hazeEffect(state = hazeState) {
                        style = HazeStyle(
                            backgroundColor = hazeBackgroundColor,
                            tint = HazeTint(glassTint),
                            blurRadius = 20.dp,
                            noiseFactor = 0f
                        )
                    }
            )
        }

        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(label) },
            shape = RoundedCornerShape(50),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                selectedContainerColor = if (glassSelected) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                selectedLabelColor = MaterialTheme.colorScheme.onSurface
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                selectedBorderColor = Color.Transparent
            )
        )
    }
}

/** 필터 칩 행 */
@Composable
fun HaloFilterChipsRow(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    hazeBackgroundColor: Color = MaterialTheme.colorScheme.background
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEachIndexed { index, label ->
            HaloFilterChip(
                selected = selectedIndex == index,
                label = label,
                onClick = { onSelect(index) },
                hazeState = hazeState,
                hazeBackgroundColor = hazeBackgroundColor
            )
        }
    }
}
