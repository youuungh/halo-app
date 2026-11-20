package com.ninezero.halo.feature.profile.presentation.follow_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.selector.CapsuleTabDefaults
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.core.util.toFormattedCount
import com.ninezero.halo.feature.profile.presentation.follow_list.FollowTab
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt

data class FollowTabSpec(
    val tab: FollowTab,
    val count: Int
)

@Composable
fun FollowTabRow(
    tabs: List<FollowTabSpec>,
    onTabSelected: (FollowTab) -> Unit,
    hazeState: HazeState,
    hazeBackgroundColor: Color,
    modifier: Modifier = Modifier,
    scrollFraction: () -> Float = { 0f }
) {
    val density = LocalDensity.current
    val isDark = LocalIsDarkTheme.current
    var tabWidth by remember { mutableIntStateOf(0) }

    // 페이저를 따라 인디케이터 이동
    val currentFraction by rememberUpdatedState(scrollFraction)
    val activeIndex by remember(tabs.size) {
        derivedStateOf { currentFraction().roundToInt().coerceIn(0, (tabs.size - 1).coerceAtLeast(0)) }
    }

    // 캡슐 배경은 FollowTabSearchRow가 그림
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CapsuleTabDefaults.Height)
    ) {
        // 탭 폭을 재기 전 첫 프레임엔 그림자만 보이므로 그리지 않음
        if (tabWidth > 0) {
            Box(
                modifier = Modifier
                    .offset { IntOffset((tabWidth * currentFraction()).roundToInt() + 2.dp.roundToPx(), 0) }
                    .padding(vertical = 2.dp)
                    .width(with(density) { tabWidth.toDp() } - 4.dp)
                    .height(CapsuleTabDefaults.Height - 4.dp)
                    .then(if (!isDark) Modifier.dropShadow(CircleShape, CapsuleTabDefaults.IndicatorShadow) else Modifier)
                    .clip(CircleShape)
                    .then(
                        if (isDark) {
                            Modifier.background(CapsuleTabDefaults.DarkIndicatorColor)
                        } else {
                            Modifier.haloBlur(hazeState, HaloBlurs.glassIndicator(hazeBackgroundColor))
                        }
                    )
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, spec ->
                FollowTabItem(
                    label = spec.tab.displayName,
                    count = spec.count,
                    isSelected = index == activeIndex,
                    onClick = { onTabSelected(spec.tab) },
                    modifier = Modifier
                        .weight(1f)
                        .onSizeChanged { tabWidth = it.width }
                )
            }
        }
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
    val color = if (isSelected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }
    Row(
        modifier = modifier
            .height(CapsuleTabDefaults.Height)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = color
        )
        Text(
            text = count.toFormattedCount(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}
