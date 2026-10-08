package com.ninezero.halo.feature.profile.presentation.follow_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.util.toFormattedCount
import com.ninezero.halo.feature.profile.presentation.follow_list.FollowTab
import kotlin.math.roundToInt

data class FollowTabSpec(
    val tab: FollowTab,
    val label: String,
    val count: Int
)

@Composable
fun FollowTabRow(
    tabs: List<FollowTabSpec>,
    onTabSelected: (FollowTab) -> Unit,
    modifier: Modifier = Modifier,
    scrollFraction: Float = 0f
) {
    val density = LocalDensity.current
    var tabWidth by remember { mutableIntStateOf(0) }

    // scrollFraction 기반 인디케이터 위치 계산
    val indicatorOffset = with(density) { (tabWidth * scrollFraction).toDp() }
    val activeIndex = scrollFraction.roundToInt().coerceIn(0, (tabs.size - 1).coerceAtLeast(0))

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, spec ->
                FollowTabItem(
                    label = spec.label,
                    count = spec.count,
                    isSelected = index == activeIndex,
                    onClick = { onTabSelected(spec.tab) },
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            // 첫 탭에서만 탭 너비 측정
                            if (index == 0) {
                                Modifier.onSizeChanged { size ->
                                    if (tabWidth == 0) tabWidth = size.width
                                }
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }

        // 인디케이터 라인
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = indicatorOffset)
                .width(with(density) { tabWidth.toDp() })
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
private fun FollowTabItem(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = count.toFormattedCount(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
