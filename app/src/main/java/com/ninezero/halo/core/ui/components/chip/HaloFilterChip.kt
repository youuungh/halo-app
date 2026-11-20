package com.ninezero.halo.core.ui.components.chip

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview
import dev.chrisbanes.haze.HazeState

/** 필터 칩, 고르면 파란 85% · 아니면 흰 50% */
@Composable
fun HaloFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    hazeBackgroundColor: Color = MaterialTheme.colorScheme.background
) {
    Box(modifier = modifier.bounceClick()) {
        // 칩 뒤 유리 효과, 목록이 살짝 비침
        if (hazeState != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .haloBlur(
                        hazeState,
                        // 고른 칩은 회색 덧칠을 빼고 파랑만
                        HaloBlurs.glassField(hazeBackgroundColor).let { if (selected) it.copy(tint = Color.Transparent) else it }
                    )
            )
        }

        FilterChip(
            selected = selected,
            onClick = onClick,
            label = {
                Text(
                    text = label,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            shape = RoundedCornerShape(50),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (hazeState != null) 0.5f else 0.7f),
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = if (hazeState != null) 0.85f else 1f),
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                borderWidth = 0.5.dp,
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

@HaloLightDarkPreview
@Composable
private fun HaloFilterChipPreview() {
    HaloSurfacePreview {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HaloFilterChipsRow(labels = listOf("전체", "사용 가능", "사용 완료"), selectedIndex = 0, onSelect = {})
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HaloFilterChip(selected = true, label = "고름", onClick = {})
                HaloFilterChip(selected = false, label = "안 고름", onClick = {})
            }
        }
    }
}
