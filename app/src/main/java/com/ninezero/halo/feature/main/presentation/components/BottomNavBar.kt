package com.ninezero.halo.feature.main.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.popClick
import com.ninezero.halo.core.ui.components.navigation.BottomBarAccessory
import com.ninezero.halo.core.ui.components.navigation.BottomBarDefaults
import com.ninezero.halo.core.ui.components.navigation.liquidtabs.DampedDragAnimation
import com.ninezero.halo.core.ui.components.navigation.liquidtabs.InteractiveHighlight
import com.ninezero.halo.core.ui.components.navigation.liquidtabs.LiquidNavTab
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.main.presentation.MainTab
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.LocalIsDarkTheme

data class BottomNavItem(
    val tab: MainTab,
    val title: String,
    @param:DrawableRes val selectedIconRes: Int,
    @param:DrawableRes val unselectedIconRes: Int,
    val showBadge: Boolean = false
)

private fun navItems(hasUnreadMessage: Boolean) = listOf(
    BottomNavItem(MainTab.FEED, "피드", R.drawable.ic_home_filled, R.drawable.ic_home),
    BottomNavItem(MainTab.EXPLORE, "탐색", R.drawable.ic_explore_filled, R.drawable.ic_explore),
    BottomNavItem(MainTab.CREATE, "글쓰기", R.drawable.ic_add, R.drawable.ic_add),
    BottomNavItem(MainTab.CHAT, "메시지", R.drawable.ic_message_filled, R.drawable.ic_message, hasUnreadMessage),
    BottomNavItem(MainTab.PROFILE, "프로필", R.drawable.ic_profile_filled, R.drawable.ic_profile)
)

@Composable
fun BottomNavBar(  // Kyant liquid glass
    selectedTab: MainTab,
    hasUnreadMessage: Boolean,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    accessory: BottomBarAccessory? = null
) {
    val items = navItems(hasUnreadMessage)
    val tabsCount = items.size
    val selectedIndex = items.indexOfFirst { it.tab == selectedTab }.coerceAtLeast(0)
    val shape = RoundedCornerShape(percent = 50)

    if (backdrop == null) {
        SolidNavBarFallback(items, selectedTab, onTabSelected, modifier)
        return
    }

    val surface = MaterialTheme.colorScheme.surface
    val iconTint = MaterialTheme.colorScheme.onSurface
    val isDark = LocalIsDarkTheme.current

    val animationScope = rememberCoroutineScope()
    val tabsBackdrop = rememberLayerBackdrop()

    val latestOnTabSelected = rememberUpdatedState(onTabSelected)
    val latestSelectedIndex = rememberUpdatedState(selectedIndex)

    val tabsContent: @Composable RowScope.() -> Unit = {
        items.forEach { item ->
            LiquidNavTab(onClick = { onTabSelected(item.tab) }) {
                Box(modifier = Modifier.popClick()) {
                    val res = if (item.tab == selectedTab) item.selectedIconRes else item.unselectedIconRes
                    if (item.showBadge) {
                        BadgedBox(badge = { Badge() }) {
                            Icon(painterResource(res), contentDescription = item.title, tint = iconTint)
                        }
                    } else {
                        Icon(painterResource(res), contentDescription = item.title, tint = iconTint)
                    }
                }
            }
        }
    }

    // 옆 버튼이 물방울처럼 떨어져 나오는 만큼 바가 비켜남
    val accessoryMotion = rememberAccessoryMotion(visible = accessory != null)

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Box(
            modifier = modifier
                .navigationBarsPadding()
                .padding(
                    horizontal = BottomBarDefaults.HorizontalMargin,
                    vertical = BottomBarDefaults.VerticalMargin
                )
                .fillMaxWidth()
                .height(BottomBarDefaults.PillHeight)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = AccessorySlot * accessoryMotion.separation.value.coerceIn(0f, 1f))
                    .shadow(elevation = 6.dp, shape = shape, clip = false),
                contentAlignment = Alignment.CenterStart
            ) {
                val density = LocalDensity.current
                val insetPx = with(density) { 8.dp.toPx() }
                val tabWidth = (constraints.maxWidth.toFloat() - insetPx) / tabsCount
                val latestTabWidth = rememberUpdatedState(tabWidth)  // 바 너비가 바뀌어도 드래그·하이라이트가 새 폭을 씀
                val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr

                val dampedDrag = remember(animationScope) {
                    DampedDragAnimation(
                        animationScope = animationScope,
                        initialValue = selectedIndex.toFloat(),
                        valueRange = 0f..(tabsCount - 1).toFloat(),
                        visibilityThreshold = 0.001f,
                        initialScale = 1f,
                        pressedScale = 64f / 56f,
                        onDragStarted = {},
                        onDragStopped = {
                            val target = targetValue.fastRoundToInt().coerceIn(0, tabsCount - 1)
                            val targetTab = items[target].tab
                            val selIdx = latestSelectedIndex.value
                            when {
                                // 다른 탭으로 드래그 → 그 탭으로 이동
                                targetTab != MainTab.CREATE && target != selIdx ->
                                    latestOnTabSelected.value(targetTab)
                                // 같은 탭 재탭은 재선택, 처리는 MainScreen
                                target == selIdx -> {
                                    animateToValue(selIdx.toFloat())
                                    latestOnTabSelected.value(items[selIdx].tab)
                                }
                                // CREATE → 현재 선택으로 복귀
                                else -> animateToValue(selIdx.toFloat())
                            }
                        },
                        onDrag = { _, dragAmount ->
                            updateValue(
                                (targetValue + dragAmount.x / latestTabWidth.value * if (isLtr) 1f else -1f)
                                    .fastCoerceIn(0f, (tabsCount - 1).toFloat())
                            )
                        }
                    )
                }
                LaunchedEffect(selectedIndex) {
                    dampedDrag.animateToValue(selectedIndex.toFloat())
                }

                val interactiveHighlight = remember(animationScope) {
                    InteractiveHighlight(
                        animationScope = animationScope,
                        position = { size, _ ->
                            val width = latestTabWidth.value
                            Offset(
                                if (isLtr) (dampedDrag.value + 0.5f) * width + insetPx / 2f
                                else size.width - (dampedDrag.value + 0.5f) * width - insetPx / 2f,
                                size.height / 2f
                            )
                        }
                    )
                }

                // 바 글래스 패널
                Row(
                    Modifier
                        .fillMaxSize()
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { shape },
                            effects = {
                                vibrancy()
                                blur(4.dp.toPx())
                                lens(14.dp.toPx(), 20.dp.toPx())
                            },
                            shadow = null,
                            onDrawSurface = { drawRect(surface.copy(alpha = 0.16f)) }
                        )
                        .then(interactiveHighlight.modifier)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = tabsContent
                )

                // accent 아이콘
                Row(
                    Modifier
                        .clearAndSetSemantics {}
                        .alpha(0f)
                        .fillMaxSize()
                        .layerBackdrop(tabsBackdrop)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            val res =
                                if (item.tab == selectedTab) item.selectedIconRes
                                else item.unselectedIconRes
                            if (item.showBadge) {
                                BadgedBox(badge = { Badge() }) {
                                    Icon(painterResource(res), contentDescription = null, tint = iconTint)
                                }
                            } else {
                                Icon(painterResource(res), contentDescription = null, tint = iconTint)
                            }
                        }
                    }
                }

                // 선택 인디케이터
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .graphicsLayer {
                            translationX =
                                if (isLtr) dampedDrag.value * tabWidth
                                else size.width - (dampedDrag.value + 1f) * tabWidth
                        }
                        .fillMaxHeight()
                        .fillMaxWidth(1f / tabsCount)
                        // 선택 칩을 슬롯보다 아주 살짝 작게
                        .padding(horizontal = 3.dp, vertical = 3.dp)
                        .then(interactiveHighlight.gestureModifier)
                        .then(dampedDrag.modifier)
                        .drawBackdrop(
                            backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),
                            shape = { shape },
                            effects = {
                                val progress = dampedDrag.pressProgress
                                vibrancy()
                                blur(6.dp.toPx() * (1f - progress))
                                lens(
                                    24.dp.toPx() * progress,
                                    34.dp.toPx() * progress,
                                    chromaticAberration = true
                                )
                            },
                            highlight = { Highlight.Default.copy(alpha = dampedDrag.pressProgress) },
                            shadow = { Shadow(alpha = dampedDrag.pressProgress) },
                            innerShadow = {
                                InnerShadow(
                                    radius = 8.dp * dampedDrag.pressProgress,
                                    alpha = dampedDrag.pressProgress
                                )
                            },
                            layerBlock = {
                                scaleX = dampedDrag.scaleX
                                scaleY = dampedDrag.scaleY
                                val velocity = dampedDrag.velocity / 10f
                                scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                                scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                            },
                            onDrawSurface = {
                                val progress = dampedDrag.pressProgress
                                drawRect(surface.copy(alpha = 0.28f))
                                drawRect((if (isDark) Color.White else Color.Black).copy(alpha = 0.14f * (1f - progress)))
                            }
                        )
                )

                // 아이콘을 인디케이터 위에 선명하게 표시
                Row(
                    Modifier
                        .clearAndSetSemantics {}
                        .fillMaxSize()
                        .padding(horizontal = 4.dp)
                        .graphicsLayer { alpha = 1f - dampedDrag.pressProgress },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            // 인디케이터에 안 묻히게 선택 슬롯만 선명하게 덮음
                            // 비선택은 비워 둬서 아래 패널 아이콘이 그대로 보임
                            if (item.tab == selectedTab) {
                                if (item.showBadge) {
                                    BadgedBox(badge = { Badge() }) {
                                        Icon(painterResource(item.selectedIconRes), contentDescription = null, tint = iconTint)
                                    }
                                } else {
                                    Icon(painterResource(item.selectedIconRes), contentDescription = null, tint = iconTint)
                                }
                            }
                        }
                    }
                }
            }

            // 바 위에 그려서 바 그림자를 덮으며 바 끝에서 떨어져 나옴
            LiquidAccessory(
                accessory = accessory,
                motion = accessoryMotion,
                backdrop = backdrop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** Preview·저사양 폴백 */
@Composable
private fun SolidNavBarFallback(
    items: List<BottomNavItem>,
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(percent = 50)
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Box(
            modifier = modifier
                .navigationBarsPadding()
                .padding(
                    horizontal = BottomBarDefaults.HorizontalMargin,
                    vertical = BottomBarDefaults.VerticalMargin
                )
                .fillMaxWidth()
                .height(BottomBarDefaults.PillHeight)
                .shadow(elevation = 6.dp, shape = shape, clip = false)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = item.tab == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onTabSelected(item.tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        val res = if (selected) item.selectedIconRes else item.unselectedIconRes
                        val tint =
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        if (item.showBadge) {
                            BadgedBox(badge = { Badge() }) {
                                Icon(painterResource(res), contentDescription = item.title, tint = tint)
                            }
                        } else {
                            Icon(painterResource(res), contentDescription = item.title, tint = tint)
                        }
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun BottomNavBarPreview() {
    HaloTheme {
        BottomNavBar(
            selectedTab = MainTab.FEED,
            hasUnreadMessage = false,
            onTabSelected = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun BottomNavBarWithBadgePreview() {
    HaloTheme {
        BottomNavBar(
            selectedTab = MainTab.CHAT,
            hasUnreadMessage = true,
            onTabSelected = {}
        )
    }
}
