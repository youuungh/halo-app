package com.ninezero.halo.core.ui.components.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.filter

/** 탭이 아래 줄에서 톱바 안으로 올라가는 거리 */
val CollapsingTabTravel = 60.dp

private const val FAR_SCROLL_ITEM_COUNT = 2
private const val TITLE_FADE_START = 0.45f
private const val TITLE_FADE_RANGE = 0.3f
private const val TITLE_COLLAPSED_SCALE = 0.9f
private val TitleMaxBlur = 4.dp

/** 톱바가 차지하는 높이 */
@Composable
fun collapsingGlassTopBarHeight(): Dp =
    WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding() +
        TopAppBarDefaults.TopAppBarExpandedHeight

/** 스크롤된 목록이 탭 뒤로 비치지 않게 자름 */
fun Modifier.clipAboveCollapsingTab(listTop: Dp, collapseProgress: () -> Float): Modifier =
    drawWithContent {
        clipRect(top = listTop.toPx() * (1f - collapseProgress())) {
            this@drawWithContent.drawContent()
        }
    }

/** 닿을 때마다 새 손짓으로 표시 */
class GestureStartFlag {
    private var isNew = false

    fun markNew() {
        isNew = true
    }

    /** 새 손짓의 첫 스크롤이면 한 번만 true */
    fun takeNew(): Boolean = isNew.also { isNew = false }
}

/** 자식이 터치를 먼저 가져가도 손가락이 닿은 순간을 알림 */
fun Modifier.markGestureStarts(flag: GestureStartFlag): Modifier = pointerInput(flag) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        flag.markNew()
    }
}

/** 멀면 한 번에 옮겨 헤더 펼침 하나로 끝내는 맨 위 이동 */
suspend fun LazyListState.jumpOrAnimateToTop() {
    if (firstVisibleItemIndex >= FAR_SCROLL_ITEM_COUNT) scrollToItem(0) else animateScrollToItem(0)
}

/** 헤더가 드러날 때 안 보는 목록을 맨 위로 */
@Composable
fun ResetListsWhenHeaderShows(
    lists: List<LazyListState>,
    activeList: () -> LazyListState,
    isHeaderVisible: () -> Boolean
) {
    val currentActiveList by rememberUpdatedState(activeList)
    val currentIsHeaderVisible by rememberUpdatedState(isHeaderVisible)
    LaunchedEffect(lists) {
        snapshotFlow { currentIsHeaderVisible() }
            .filter { it }
            .collect {
                val active = currentActiveList()
                lists.filter { it !== active }.forEach { it.requestScrollToItem(0) }
            }
    }
}

/** 스크롤을 따라 제목이 옅어지고 동작 버튼이 유리로 바뀌는 톱바 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingGlassTopBar(
    title: String,
    @DrawableRes actionIcon: Int,
    actionDescription: String,
    collapseProgress: () -> Float,
    hazeState: HazeState,
    backgroundColor: Color,
    onActionClick: () -> Unit
) {
    val showGlassAction by remember { derivedStateOf { collapseProgress() > 0f } }

    TopAppBar(
        windowInsets = WindowInsets.statusBarsIgnoringVisibility,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                // 캡슐이 글자 자리를 비킨 뒤에야 나타남
                modifier = Modifier.graphicsLayer {
                    val reveal = ((TITLE_FADE_START - collapseProgress()) / TITLE_FADE_RANGE).coerceIn(0f, 1f)
                    alpha = reveal
                    // 왼쪽을 기준으로 커지며 흐림이 걷힘
                    scaleX = lerp(TITLE_COLLAPSED_SCALE, 1f, reveal)
                    scaleY = scaleX
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    val blur = TitleMaxBlur.toPx() * (1f - reveal)
                    renderEffect = if (blur > 0f) BlurEffect(blur, blur, TileMode.Decal) else null
                }
            )
        },
        actions = {
            Box(modifier = Modifier.padding(end = 8.dp)) {
                Box(
                    modifier = Modifier
                        .graphicsLayer { alpha = 1f - collapseProgress() }
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .bounceClick()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onActionClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(actionIcon),
                        contentDescription = actionDescription
                    )
                }
                if (showGlassAction) {
                    GlassIconButton(
                        onClick = onActionClick,
                        icon = painterResource(actionIcon),
                        contentDescription = actionDescription,
                        hazeState = hazeState,
                        backgroundColor = backgroundColor,
                        modifier = Modifier.graphicsLayer { alpha = collapseProgress() }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures { /* 터치 이벤트 소비 */ }
            }
            .haloBlur(hazeState, HaloBlurs.fadeDown(backgroundColor))
    )
}
