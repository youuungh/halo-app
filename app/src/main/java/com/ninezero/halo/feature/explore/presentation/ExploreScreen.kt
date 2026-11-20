
package com.ninezero.halo.feature.explore.presentation

import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.DeepLinks
import com.ninezero.halo.core.model.ReportTargetType
import com.ninezero.halo.core.ui.components.bottomsheet.PostOptionBottomSheet
import com.ninezero.halo.core.ui.components.layout.CollapsingGlassTopBar
import com.ninezero.halo.core.ui.components.layout.collapsingGlassTopBarHeight
import com.ninezero.halo.core.ui.components.layout.jumpOrAnimateToTop
import com.ninezero.halo.core.ui.components.list.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.list.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.navigation.BottomBarDefaults
import com.ninezero.halo.core.ui.components.popup.ProfilePopupData
import com.ninezero.halo.core.ui.components.popup.ProfilePopupHost
import com.ninezero.halo.core.ui.components.popup.rememberProfilePopupHostState
import com.ninezero.halo.core.ui.components.post.LocalReportRequester
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.core.ui.components.viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.viewer.ViewerOverlayHost
import com.ninezero.halo.core.ui.components.viewer.rememberViewerOverlayHostState
import com.ninezero.halo.core.util.launchCustomTab
import com.ninezero.halo.feature.explore.presentation.components.ExploreHeaderLayout
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.share.presentation.ShareIntent
import com.ninezero.halo.feature.share.presentation.ShareSheetHost
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ExploreScreen(
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToEditPost: (Int) -> Unit = {},
    onNavigateToChatRoom: (Int) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    tabReselectFlow: Flow<Unit>? = null,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    // 상위 호스트 있으면 위임, BottomNavBar 위 풀스크린
    val parentViewerHost = LocalViewerOverlayHostState.current
    val localViewerHost = rememberViewerOverlayHostState()
    val viewerHostState = parentViewerHost ?: localViewerHost

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ExploreSideEffect.NavigateToPostDetail -> onNavigateToPostDetail(sideEffect.postId)
            is ExploreSideEffect.NavigateToUserProfile -> onNavigateToProfile(sideEffect.userId)
            is ExploreSideEffect.NavigateToBannerLink -> {
                val link = sideEffect.link
                if (link.isNotBlank()) context.launchCustomTab(link)
            }
            is ExploreSideEffect.NavigateToEditPost -> onNavigateToEditPost(sideEffect.postId)
            is ExploreSideEffect.NavigateToChatRoom -> onNavigateToChatRoom(sideEffect.roomId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ExploreScreenContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onNavigateToSearch = onNavigateToSearch,
            onShowMediaViewer = { items, index ->
                viewerHostState.showMediaViewer(items, index)
            },
            onIntent = viewModel::onIntent,
            tabReselectFlow = tabReselectFlow
        )

        // 상위 호스트가 없을 때만 자체 렌더
        if (parentViewerHost == null) {
            ViewerOverlayHost(state = viewerHostState)
        }
    }

    ShareSheetHost(
        visible = state.showShareSheet && state.sharePostId != null,
        shareAction = { shareViewModel -> state.sharePostId?.let { shareViewModel.onIntent(ShareIntent.SharePost(it)) } },
        onDismiss = { viewModel.onIntent(ExploreIntent.DismissShareSheet) },
        onShareSuccess = { roomIds, count -> viewModel.onIntent(ExploreIntent.ShareCompleted(roomIds, count)) },
        externalLink = state.sharePostId?.let(DeepLinks::postLink)
    )

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onAction = {
            if (state.snackbarState?.actionLabel == "채팅 열기") {
                state.navigateRoomId?.let { roomId ->
                    viewModel.onIntent(ExploreIntent.NavigateToChatRoom(roomId))
                }
            }
        },
        onDismiss = { viewModel.onIntent(ExploreIntent.SnackbarDismissed) }
    )
}

@Composable
internal fun ExploreScreenContent(
    state: ExploreState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateToSearch: () -> Unit = {},
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit = { _, _ -> },
    tabReselectFlow: Flow<Unit>? = null,
    onIntent: (ExploreIntent) -> Unit
) {
    val hazeState = remember { HazeState() }
    val topBarBlurColor = MaterialTheme.colorScheme.background

    val bannerHeight = remember { mutableFloatStateOf(0f) }
    val scrollOffset = rememberSaveable { mutableFloatStateOf(0f) }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val trendingListState = rememberLazyListState()
    val popularListState = rememberLazyListState()

    val profilePopupState = rememberProfilePopupHostState()

    val topBarHeight = collapsingGlassTopBarHeight()
    val coroutineScope = rememberCoroutineScope()
    val onTabClick: (ExploreTab) -> Unit = { tab ->
        val tappedPage = if (tab == ExploreTab.TRENDING) 0 else 1
        if (tappedPage == pagerState.currentPage) {
            // 같은 탭 재탭은 리스트만 맨 위로
            val listState = if (tappedPage == 0) trendingListState else popularListState
            coroutineScope.launch { listState.animateScrollToItem(0) }
        } else {
            onIntent(ExploreIntent.SelectTab(tab))
        }
    }

    LaunchedEffect(state.selectedTab) {
        val targetPage = when (state.selectedTab) {
            ExploreTab.TRENDING -> 0
            ExploreTab.POPULAR -> 1
        }
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val newTab = when (pagerState.currentPage) {
            0 -> ExploreTab.TRENDING
            1 -> ExploreTab.POPULAR
            else -> ExploreTab.TRENDING
        }
        if (state.selectedTab != newTab) {
            onIntent(ExploreIntent.SelectTab(newTab))
        }
    }

    InfiniteScrollTrigger(
        listState = trendingListState,
        isLoading = state.isTrendingLoading,
        hasMore = state.hasMoreTrendingPosts && state.selectedTab == ExploreTab.TRENDING
    ) {
        onIntent(ExploreIntent.LoadMoreTrending)
    }

    InfiniteScrollTrigger(
        listState = popularListState,
        isLoading = state.isPopularLoading,
        hasMore = state.hasMorePopularPosts && state.selectedTab == ExploreTab.POPULAR
    ) {
        onIntent(ExploreIntent.LoadMorePopular)
    }

    // 같은 탭 재선택은 배너 펼치기와 맨 위, 새로고침 없음
    // 완전 펼침에서 한 번 더 누르면 새로고침
    LaunchedEffect(tabReselectFlow) {
        tabReselectFlow?.collect {
            val activeListState = if (pagerState.currentPage == 0) trendingListState else popularListState
            val listAtTop = activeListState.firstVisibleItemIndex == 0 &&
                    activeListState.firstVisibleItemScrollOffset == 0
            val bannerExpanded = scrollOffset.floatValue >= -0.5f
            if (listAtTop && bannerExpanded) {
                onIntent(ExploreIntent.RefreshCurrentTab)
            } else {
                this@LaunchedEffect.launch {
                    activeListState.jumpOrAnimateToTop()
                    animate(scrollOffset.floatValue, 0f) { value, _ ->
                        scrollOffset.floatValue = value
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            DefaultSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = BottomBarDefaults.PillSpace)
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(ExploreIntent.RefreshCurrentTab) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            indicatorPadding = PaddingValues(top = topBarHeight)
        ) {
            ExploreHeaderLayout(
                state = state,
                pagerState = pagerState,
                trendingListState = trendingListState,
                popularListState = popularListState,
                hazeState = hazeState,
                bannerHeightState = bannerHeight,
                scrollOffsetState = scrollOffset,
                topBarHeight = topBarHeight,
                onTabClick = onTabClick,
                topBar = { collapseProgress ->
                    CollapsingGlassTopBar(
                        title = "탐색",
                        actionIcon = R.drawable.ic_search,
                        actionDescription = "검색",
                        collapseProgress = collapseProgress,
                        hazeState = hazeState,
                        backgroundColor = topBarBlurColor,
                        onActionClick = onNavigateToSearch
                    )
                },
                onShowMediaViewer = onShowMediaViewer,
                onProfileClick = { author, offset ->
                    if (state.currentUserId == author.id) {
                        onIntent(ExploreIntent.NavigateToUserProfile(author.id))
                    } else {
                        profilePopupState.show(author.id, offset)
                    }
                },
                onIntent = onIntent
            )
        }
    }

    if (state.showPostOptionsSheet && state.selectedPost != null) {
        val isOwnPost = state.currentUserId == state.selectedPost.author.id
        val reportRequester = LocalReportRequester.current
        PostOptionBottomSheet(
            isOwnPost = isOwnPost,
            isBookmarked = state.selectedPost.isBookmarked,
            copyLink = state.selectedPost.takeUnless { it.isSecret }?.let { DeepLinks.postLink(it.id) },
            onDismiss = { onIntent(ExploreIntent.DismissPostOptions) },
            onEdit = { onIntent(ExploreIntent.EditPost) },
            onDelete = { onIntent(ExploreIntent.DeletePost) },
            onBookmark = { onIntent(ExploreIntent.BookmarkPost(state.selectedPost.id)) },
            onNotInterested = { onIntent(ExploreIntent.HidePost(state.selectedPost.id)) },
            onBlock = { onIntent(ExploreIntent.BlockUser(state.selectedPost.author.id)) },
            onReport = { reportRequester(ReportTargetType.POST, state.selectedPost.id) },
            // 피드 고정은 크리에이터 피드의 내 글만
            canPin = isOwnPost && state.isCurrentUserCreator &&
                state.selectedPost.contextType == PostContextType.CREATOR_FEED,
            isPinned = state.selectedPost.isPinned,
            onPin = { onIntent(ExploreIntent.PinPost) }
        )
    }

    ProfilePopupHost(
        state = profilePopupState,
        hazeState = hazeState,
        resolveAuthor = { authorId ->
            (state.trendingPosts + state.popularPosts).find { it.author.id == authorId }?.author?.let { ProfilePopupData(it.id, it.isFollowing) }
        },
        onNavigateToProfile = { onIntent(ExploreIntent.NavigateToUserProfile(it)) },
        onFollow = { onIntent(ExploreIntent.FollowUser(it)) },
        onUnfollow = { onIntent(ExploreIntent.UnfollowUser(it)) }
    )
}
