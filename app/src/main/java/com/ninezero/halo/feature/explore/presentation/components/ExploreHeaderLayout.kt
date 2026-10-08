package com.ninezero.halo.feature.explore.presentation.components

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.feature.explore.presentation.ExploreIntent
import com.ninezero.halo.feature.explore.presentation.ExploreState
import com.ninezero.halo.feature.explore.presentation.ExploreTab
import com.ninezero.halo.feature.feed.domain.model.Author
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
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
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit,
    onProfileClick: (Author, IntOffset) -> Unit,
    onIntent: (ExploreIntent) -> Unit
) {
    // 배너 높이·오프셋은 상위 소유, 상단바와 공유
    var bannerHeight by bannerHeightState
    var scrollOffset by scrollOffsetState

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val tabHazeState = remember { HazeState() }

    val bannerPresent = state.isBannersLoading || state.banners.isNotEmpty()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val targetBannerHeightDp = if (bannerPresent) screenWidthDp + 16.dp else 0.dp
    val bannerHeightDp by animateDpAsState(targetValue = targetBannerHeightDp, label = "exploreBannerHeight")
    val bannerPx = with(density) { bannerHeightDp.toPx() }

    // 상단바 blur·스크롤 클램프 공유 상태에 동기화
    SideEffect { bannerHeight = bannerPx }

    val bannerScrollableState = rememberScrollableState { delta ->
        if (bannerHeight == 0f) return@rememberScrollableState 0f

        // 위로 스크롤: 배너 접기
        if (delta < 0) {
            val newOffset = (scrollOffset + delta).coerceIn(-bannerHeight, 0f)
            val consumed = newOffset - scrollOffset
            scrollOffset = newOffset
            return@rememberScrollableState consumed
        }

        // 아래로 스크롤: 리스트가 맨 위일 때만 배너 펼치기
        val listState = if (pagerState.currentPage == 0) trendingListState else popularListState
        if (delta > 0 && listState.firstVisibleItemIndex == 0 &&
            listState.firstVisibleItemScrollOffset == 0
        ) {
            val newOffset = (scrollOffset + delta).coerceIn(-bannerHeight, 0f)
            val consumed = newOffset - scrollOffset
            scrollOffset = newOffset
            return@rememberScrollableState consumed
        }

        0f
    }

    // 자식 리스트 스크롤 연동, 배너 먼저 소비
    val nestedScrollConnection = remember(bannerHeight) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (bannerHeight == 0f) return Offset.Zero

                val delta = available.y
                if (delta == 0f) return Offset.Zero

                // 위로 스크롤: 배너 접기
                if (delta < 0) {
                    val newOffset = (scrollOffset + delta).coerceIn(-bannerHeight, 0f)
                    val consumed = newOffset - scrollOffset
                    scrollOffset = newOffset
                    return Offset(0f, consumed)
                }

                // 아래로 스크롤: 리스트가 맨 위일 때만 배너 펼치기
                val listState = if (pagerState.currentPage == 0) trendingListState else popularListState
                if (delta > 0 && listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0
                ) {
                    val newOffset = (scrollOffset + delta).coerceIn(-bannerHeight, 0f)
                    val consumed = newOffset - scrollOffset
                    scrollOffset = newOffset
                    return Offset(0f, consumed)
                }

                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
            .nestedScroll(nestedScrollConnection)
    ) {
        // 배너 섹션
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = scrollOffset }
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

        // 탭 + 페이저
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(0, (bannerPx + scrollOffset).coerceAtLeast(0f).roundToInt())
                }
        ) {
            // 탭 - 배너 스크롤과 연결, 페이저 위에 플로팅
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .scrollable(
                        state = bannerScrollableState,
                        orientation = Orientation.Vertical
                    )
            ) {
                ExploreTabRow(
                    selectedTab = state.selectedTab,
                    onTabSelected = { tab ->
                        val currentPage = pagerState.currentPage
                        val tappedSamePage = (tab == ExploreTab.TRENDING && currentPage == 0) ||
                                (tab == ExploreTab.POPULAR && currentPage == 1)
                        if (tappedSamePage) {
                            // 같은 탭 재탭은 리스트만 맨 위로
                            val listState = if (currentPage == 0) trendingListState else popularListState
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        } else {
                            onIntent(ExploreIntent.SelectTab(tab))
                        }
                    },
                    hazeState = tabHazeState,
                    scrollFraction = pagerState.currentPage + pagerState.currentPageOffsetFraction
                )
            }

            // 탭 뒤 전체를 채우고 hazeSource로 캡처
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(tabHazeState)
            ) { page ->
                when (page) {
                    0 -> TrendingPostsContent(
                        state = state,
                        listState = trendingListState,
                        onShowMediaViewer = onShowMediaViewer,
                        onProfileClick = onProfileClick,
                        onIntent = onIntent
                    )
                    1 -> PopularPostsContent(
                        state = state,
                        listState = popularListState,
                        onShowMediaViewer = onShowMediaViewer,
                        onProfileClick = onProfileClick,
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}
