package com.ninezero.halo.feature.explore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.feature.explore.presentation.ExploreTab
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt

val ExploreTabRowHeight = 56.dp

@Composable
fun ExploreTabRow(
    selectedTab: ExploreTab,
    onTabSelected: (ExploreTab) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    hazeBackgroundColor: Color = MaterialTheme.colorScheme.background,
    scrollFraction: Float = if (selectedTab == ExploreTab.POPULAR) 1f else 0f
) {
    val tabs = listOf(ExploreTab.TRENDING, ExploreTab.POPULAR)
    val density = LocalDensity.current

    val tabSize = remember { mutableStateOf(IntSize.Zero) }

    // pager 실제 위치를 따라 양방향 전환 대칭
    val indicatorOffset = with(density) { (tabSize.value.width * scrollFraction).toDp() }

    // haloBlur 블록은 컴포저블 아님
    val glassTint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val indicatorGlassTint = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        // 배경 컨테이너
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(CircleShape)
                .then(
                    if (hazeState != null) {
                        Modifier.haloBlur(state = hazeState) {
                            style = HaloBlurStyle(
                                backgroundColor = hazeBackgroundColor,
                                tint = HaloBlurTint(glassTint),
                                blurRadius = 20.dp,
                                noiseFactor = 0f
                            )
                        }
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    }
                )
        ) {
            // 선택 인디케이터
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset + 2.dp)
                    .padding(vertical = 2.dp)
                    .width(with(density) { tabSize.value.width.toDp() } - 4.dp)
                    .height(36.dp)
                    .clip(CircleShape)
                    .then(
                        if (hazeState != null) {
                            Modifier.haloBlur(state = hazeState) {
                                style = HaloBlurStyle(
                                    backgroundColor = hazeBackgroundColor,
                                    tint = HaloBlurTint(indicatorGlassTint),
                                    blurRadius = 20.dp,
                                    noiseFactor = 0f
                                )
                            }
                        } else {
                            Modifier.background(MaterialTheme.colorScheme.surface)
                        }
                    )
            )

            // 탭 Row
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 가까운 탭 기준으로 굵기·색 전환
                val currentSelectedIndex = scrollFraction.roundToInt().coerceIn(0, tabs.lastIndex)

                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == currentSelectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .onSizeChanged { size ->
                                if (tabSize.value.width == 0) {
                                    tabSize.value = size
                                }
                            }
                            .pointerInput(tab) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val startTime = System.currentTimeMillis()
                                    val startPosition = down.position

                                    var up: PointerInputChange? = null
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: break

                                        if (!change.pressed) {
                                            up = change
                                            break
                                        }

                                        val distance = (change.position - startPosition).getDistance()
                                        if (distance > viewConfiguration.touchSlop) {
                                            break
                                        }
                                    }

                                    if (up != null) {
                                        val duration = System.currentTimeMillis() - startTime
                                        val distance = (up.position - startPosition).getDistance()
                                        if (duration < 300 && distance <= viewConfiguration.touchSlop) {
                                            onTabSelected(tab)
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (tab) {
                                ExploreTab.TRENDING -> "실시간"
                                ExploreTab.POPULAR -> "인기"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreTabRowPreview() {
    HaloTheme {
        Column {
            ExploreTabRow(
                selectedTab = ExploreTab.TRENDING,
                onTabSelected = {}
            )
            ExploreTabRow(
                selectedTab = ExploreTab.POPULAR,
                onTabSelected = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreTabRowDarkPreview() {
    HaloTheme(darkTheme = true) {
        Column {
            ExploreTabRow(
                selectedTab = ExploreTab.TRENDING,
                onTabSelected = {}
            )
            ExploreTabRow(
                selectedTab = ExploreTab.POPULAR,
                onTabSelected = {}
            )
        }
    }
}
