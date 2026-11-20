package com.ninezero.halo.core.ui.components.selector

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme

@Composable
fun SegmentedTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var tabSize by remember { mutableStateOf(IntSize.Zero) }

    val indicatorOffset by animateDpAsState(
        targetValue = with(density) { (tabSize.width * selectedIndex).toDp() },
        label = "indicatorOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset((indicatorOffset + 2.dp).roundToPx(), 0) }
                .padding(vertical = 2.dp)
                .width(with(density) { tabSize.width.toDp() } - 4.dp)
                .height(36.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surface)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .onSizeChanged { size ->
                            if (tabSize.width == 0) {
                                tabSize = size
                            }
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** 탭 페이지 전환, 자리에서 짧게 겹쳐 바뀜 */
fun segmentedTabTransition(): ContentTransform =
    fadeIn(tween(durationMillis = 180)) togetherWith fadeOut(tween(durationMillis = 120))

@HaloLightDarkPreview
@Composable
private fun SegmentedTabRowPreview() {
    HaloTheme {
        Surface {
            var selected by remember { mutableIntStateOf(0) }
            SegmentedTabRow(
                tabs = listOf("일반태그", "섹션태그"),
                selectedIndex = selected,
                onTabSelected = { selected = it },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
