package com.ninezero.halo.feature.profile.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.selector.CapsuleTabDefaults
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs
import kotlin.math.roundToInt

enum class ProfileTab(val displayName: String) {
    // 기본 유저 탭
    POSTS("포스트"),
    REPLIES("답글"),
    // Creator 전용 탭
    HOME("홈"),
    COMMUNITY("커뮤니티"),
    STORE("스토어")
}

val ProfileTabRowHeight = CapsuleTabDefaults.Height + 16.dp

private const val INDICATOR_JUMP_THRESHOLD = 0.5f

@Composable
fun ProfileTabRow(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit,
    modifier: Modifier = Modifier,
    tabs: List<ProfileTab> = listOf(ProfileTab.POSTS, ProfileTab.REPLIES),
    hazeState: HazeState? = null,
    hazeBackgroundColor: Color = MaterialTheme.colorScheme.background,
    scrollFraction: () -> Float = { tabs.indexOf(selectedTab).toFloat().coerceAtLeast(0f) }
) {
    val density = LocalDensity.current

    val tabSize = remember { mutableStateOf(IntSize.Zero) }

    // 탭을 눌러 건너뛸 때만 미끄러짐
    val currentFraction by rememberUpdatedState(scrollFraction)
    val indicatorFraction = remember { Animatable(scrollFraction()) }
    LaunchedEffect(Unit) {
        snapshotFlow { currentFraction() }.collectLatest { fraction ->
            if (abs(fraction - indicatorFraction.targetValue) >= INDICATOR_JUMP_THRESHOLD) {
                indicatorFraction.animateTo(fraction, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow))
            } else {
                indicatorFraction.snapTo(fraction)
            }
        }
    }
    val currentSelectedIndex by remember(tabs) {
        derivedStateOf { currentFraction().roundToInt().coerceIn(0, tabs.lastIndex) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // 배경 컨테이너
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CapsuleTabDefaults.Height)
                .then(
                    if (hazeState != null) {
                        Modifier
                            .clip(CircleShape)
                            .haloBlur(hazeState, HaloBlurs.glassField(hazeBackgroundColor))
                    } else {
                        Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    }
                )
        ) {
            // 선택 인디케이터
            Box(
                modifier = Modifier
                    .offset { IntOffset((tabSize.value.width * indicatorFraction.value).roundToInt() + 2.dp.roundToPx(), 0) }
                    .padding(vertical = 2.dp)
                    .width(with(density) { tabSize.value.width.toDp() } - 4.dp)
                    .height(CapsuleTabDefaults.Height - 4.dp)
                    .then(
                        // 밝은 블러 위에서 경계가 보이게 살짝 띄움
                        if (!LocalIsDarkTheme.current) Modifier.dropShadow(CircleShape, CapsuleTabDefaults.IndicatorShadow) else Modifier
                    )
                    .clip(CircleShape)
                    .then(
                        if (hazeState != null) {
                            if (LocalIsDarkTheme.current) {
                                Modifier.background(CapsuleTabDefaults.DarkIndicatorColor)
                            } else {
                                Modifier.haloBlur(hazeState, HaloBlurs.glassIndicator(hazeBackgroundColor))
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

                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == currentSelectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(CapsuleTabDefaults.Height)
                            .onSizeChanged { size ->
                                tabSize.value = size
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
                            text = tab.displayName,
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

@HaloLightDarkPreview
@Composable
private fun ProfileTabRowPreview() {
    HaloTheme {
        Column {
            ProfileTabRow(
                selectedTab = ProfileTab.POSTS,
                onTabSelected = {}
            )
            ProfileTabRow(
                selectedTab = ProfileTab.REPLIES,
                onTabSelected = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorProfileTabRowPreview() {
    val creatorTabs = listOf(
        ProfileTab.HOME,
        ProfileTab.POSTS,
        ProfileTab.COMMUNITY,
        ProfileTab.STORE
    )
    HaloTheme {
        Column {
            ProfileTabRow(
                selectedTab = ProfileTab.HOME,
                onTabSelected = {},
                tabs = creatorTabs
            )
            ProfileTabRow(
                selectedTab = ProfileTab.POSTS,
                onTabSelected = {},
                tabs = creatorTabs
            )
        }
    }
}
