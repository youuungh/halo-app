package com.ninezero.halo.feature.point.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.chip.HaloFilterChip
import com.ninezero.halo.feature.point.domain.model.PointType
import dev.chrisbanes.haze.HazeState

@Composable
internal fun PointFilterChips(
    selectedType: PointType?,
    onFilterByType: (PointType?) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HaloFilterChip(
            selected = selectedType == null,
            label = "전체",
            onClick = { onFilterByType(null) },
            hazeState = hazeState
        )
        HaloFilterChip(
            selected = selectedType == PointType.EARN,
            label = "적립",
            onClick = { onFilterByType(PointType.EARN) },
            hazeState = hazeState
        )
        HaloFilterChip(
            selected = selectedType == PointType.USE,
            label = "사용",
            onClick = { onFilterByType(PointType.USE) },
            hazeState = hazeState
        )
        HaloFilterChip(
            selected = selectedType == PointType.EXPIRE,
            label = "만료",
            onClick = { onFilterByType(PointType.EXPIRE) },
            hazeState = hazeState
        )
    }
}
