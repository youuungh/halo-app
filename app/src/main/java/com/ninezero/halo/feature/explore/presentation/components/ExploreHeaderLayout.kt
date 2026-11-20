package com.ninezero.halo.feature.explore.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.ninezero.halo.core.ui.components.layout.CollapsingTabTravel
import com.ninezero.halo.core.ui.components.layout.GestureStartFlag
import com.ninezero.halo.core.ui.components.layout.ResetListsWhenHeaderShows
import com.ninezero.halo.core.ui.components.layout.clipAboveCollapsingTab
import com.ninezero.halo.core.ui.components.layout.markGestureStarts
import com.ninezero.halo.core.ui.components.viewer.ViewerMediaItem
import com.ninezero.halo.feature.explore.presentation.ExploreIntent
import com.ninezero.halo.feature.explore.presentation.ExploreState
import com.ninezero.halo.feature.explore.presentation.ExploreTab
import com.ninezero.halo.feature.feed.domain.model.Author
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlin.math.roundToInt

@Composable
fun ExploreHeaderLayout(
    state: ExploreState,
    pagerState: PagerState,
    trendingListState: LazyListState,
    popularListState: LazyListState,
    hazeState: HazeState,
    bannerHeightState: MutableFloatState,
    scrollOffsetState: MutableFloatState,
    topBarHeight: Dp,
    onTabClick: (ExploreTab) -> Unit,
    topBar: @Composable (collapseProgress: () -> Float) -> Unit,
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit,
    onProfileClick: (Author, IntOffset) -> Unit,
    onIntent: (ExploreIntent) -> Unit
) {
    // 배너 높이·오프셋은 상위 소유, 상단바와 공유
    var bannerHeight by bannerHeightState
    var scrollOffset by scrollOffsetState

    val density = LocalDensity.current

    val bannerPresent = state.isBannersLoading || state.banners.isNotEmpty()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val targetBannerHeightDp = if (bannerPresent) screenWidthDp + 16.dp else 0.dp
    val bannerHeightDp by animateDpAsState(targetValue = targetBannerHeightDp, label = "exploreBannerHeight")
    val bannerPx = with(density) { bannerHeightDp.toPx() }
    val tabTravelPx = with(density) { CollapsingTabTravel.toPx() }
    val minScrollOffset = { -(bannerHeight + tabTravelPx) }
    // 접힘 쪽 맞춤에서 위치는 멈추고 폭만 살짝 넘치는 몫
    // 0 = 탭이 아래 줄, 1 = 탭이 톱바 안
    val collapseProgress = remember(tabTravelPx) {
        { if (bannerHeight > 0f) ((-scrollOffset - bannerHeight) / tabTravelPx).coerceIn(0f, 1f) else 0f }
    }

    ResetListsWhenHeaderShows(
        lists = listOf(trendingListState, popularListState),
        activeList = { if (pagerState.currentPage == 0) trendingListState else popularListState },
        isHeaderVisible = { bannerHeight > 0f && collapseProgress() < 1f }
    )

    // 상단바 blur·스크롤 클램프 공유 상태에 동기화
    SideEffect { bannerHeight = bannerPx }

    val bannerScrollableState = rememberScrollableState { delta ->
        if (bannerHeight == 0f) return@rememberScrollableState 0f

        // 위로 스크롤: 배너 접기
        if (delta < 0) {
            val newOffset = (scrollOffset + delta).coerceIn(minScrollOffset(), 0f)
            val consumed = newOffset - scrollOffset
            scrollOffset = newOffset
            return@rememberScrollableState consumed
        }

        // 아래로 스크롤: 리스트가 맨 위일 때만 배너 펼치기
        val listState = if (pagerState.currentPage == 0) trendingListState else popularListState
        if (delta > 0 && listState.firstVisibleItemIndex == 0 &&
            listState.firstVisibleItemScrollOffset == 0
        ) {
            val newOffset = (scrollOffset + delta).coerceIn(minScrollOffset(), 0f)
            val consumed = newOffset - scrollOffset
            scrollOffset = newOffset
            return@rememberScrollableState consumed
        }

        0f
    }

    // 자식 리스트 스크롤 연동, 배너 먼저 소비
    val gestureStart = remember { GestureStartFlag() }
    val nestedScrollConnection = remember(bannerHeight) {
        object : NestedScrollConnection {
            // 이번 손짓을 시작한 위치
            private var gestureStartOffset = Float.NaN

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (bannerHeight == 0f) return Offset.Zero

                val delta = available.y
                if (delta == 0f) return Offset.Zero
                if (gestureStart.takeNew()) gestureStartOffset = scrollOffset

                // 위로 스크롤: 배너 접기
                if (delta < 0) {
                    // 접히기 전 손짓은 접힘에서 멈춤, 목록은 다음 손짓부터
                    val collapsedOffset = minScrollOffset()
                    val newOffset = (scrollOffset + delta).coerceIn(collapsedOffset, 0f)
                    val consumed = newOffset - scrollOffset
                    scrollOffset = newOffset
                    val staysCollapsed = collapsedOffset in newOffset..<gestureStartOffset
                    return Offset(0f, if (staysCollapsed) delta else consumed)
                }

                // 아래로 스크롤: 리스트가 맨 위일 때만 배너 펼치기
                val listState = if (pagerState.currentPage == 0) trendingListState else popularListState
                if (delta > 0 && listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0
                ) {
                    val newOffset = (scrollOffset + delta).coerceIn(minScrollOffset(), 0f)
                    val consumed = newOffset - scrollOffset
                    scrollOffset = newOffset
                    return Offset(0f, consumed)
                }

                return Offset.Zero
            }

            // 탭 전환 구간에서 멈추면 가까운 쪽으로 맞춤
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val progress = collapseProgress()
                if (progress > 0f && progress < 1f) {
                    val target = if (progress >= 0.5f) minScrollOffset() else -bannerHeight
                    animate(
                        initialValue = scrollOffset,
                        targetValue = target,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 500f)
                    ) { value, _ ->
                        scrollOffset = value.coerceIn(minScrollOffset(), -bannerHeight)
                    }
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .markGestureStarts(gestureStart)
            .nestedScroll(nestedScrollConnection)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        ) {
            // 페이저
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.layout { measurable, constraints ->
                    // 탭과 함께 올라가는 만큼 아래가 비지 않게 키움
                    val height = constraints.maxHeight + tabTravelPx.roundToInt()
                    val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(0, (bannerPx + scrollOffset).roundToInt())
                    }
                }.clipAboveCollapsingTab(topBarHeight + ExploreTabRowHeight, collapseProgress)
            ) { page ->
                when (page) {
                    0 -> TrendingPostsContent(
                        state = state,
                        topPadding = topBarHeight,
                        listState = trendingListState,
                        onShowMediaViewer = onShowMediaViewer,
                        onProfileClick = onProfileClick,
                        onIntent = onIntent
                    )
                    1 -> PopularPostsContent(
                        state = state,
                        topPadding = topBarHeight,
                        listState = popularListState,
                        onShowMediaViewer = onShowMediaViewer,
                        onProfileClick = onProfileClick,
                        onIntent = onIntent
                    )
                }
            }

            // 배너 섹션
            Box(
                modifier = Modifier
                    .offset(y = topBarHeight)
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = scrollOffset
                        alpha = 1f - collapseProgress()
                    }
                    .scrollable(
                        state = bannerScrollableState,
                        orientation = Orientation.Vertical
                    )
            ) {
                BannerSection(
                    banners = state.banners,
                    isLoading = state.isBannersLoading,
                    onBannerClick = { banner ->
                        onIntent(ExploreIntent.NavigateToBannerLink(banner))
                    }
                )
            }
        }

        topBar(collapseProgress)

        // 탭 - 배너 끝부터 스크롤을 따라 톱바 자리로 올라가며 줄어듦
        ExploreTabRow(
            selectedTab = state.selectedTab,
            onTabSelected = onTabClick,
            hazeState = hazeState,
            scrollFraction = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
            modifier = Modifier
                .layout { measurable, constraints ->
                    val progress = collapseProgress()
                    val width = lerp(constraints.maxWidth.toFloat(), TabCollapsedWidth.toPx(), progress).roundToInt()
                    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                    layout(placeable.width, placeable.height) {
                        placeable.place(
                            x = (TabCollapsedStart.toPx() * progress).roundToInt(),
                            y = (topBarHeight.toPx() + bannerPx + scrollOffset).roundToInt()
                        )
                    }
                }
                .scrollable(
                    state = bannerScrollableState,
                    orientation = Orientation.Vertical
                )
        )
    }
}

private val TabCollapsedWidth = 216.dp
private val TabCollapsedStart = 8.dp
