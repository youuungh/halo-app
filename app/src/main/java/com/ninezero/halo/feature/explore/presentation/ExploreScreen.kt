
package com.ninezero.halo.feature.explore.presentation

import androidx.compose.animation.core.animate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.DeepLinks
import com.ninezero.halo.core.model.ReportTargetType
import com.ninezero.halo.core.ui.components.bottomsheet.PostOptionBottomSheet
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.navigation.BottomBarDefaults
import com.ninezero.halo.core.ui.components.popup.ProfilePopupData
import com.ninezero.halo.core.ui.components.popup.ProfilePopupHost
import com.ninezero.halo.core.ui.components.popup.rememberProfilePopupHostState
import com.ninezero.halo.core.ui.components.post.LocalReportRequester
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.core.ui.components.viewer.ViewerOverlayHost
import com.ninezero.halo.core.ui.components.viewer.rememberViewerOverlayHostState
import com.ninezero.halo.core.util.launchCustomTab
import com.ninezero.halo.feature.explore.presentation.components.ExploreHeaderLayout
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.share.presentation.ShareIntent
import com.ninezero.halo.feature.share.presentation.ShareSheetHost
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
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
        shareAction = { vm -> vm.onIntent(ShareIntent.SharePost(state.sharePostId!!)) },
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
        hasMore = state.trendingHasMore && state.selectedTab == ExploreTab.TRENDING
    ) {
        onIntent(ExploreIntent.LoadMoreTrending)
    }

    InfiniteScrollTrigger(
        listState = popularListState,
        isLoading = state.isPopularLoading,
        hasMore = state.popularHasMore && state.selectedTab == ExploreTab.POPULAR
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
                launch { activeListState.animateScrollToItem(0) }
                launch { animate(scrollOffset.floatValue, 0f) { value, _ -> scrollOffset.floatValue = value } }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            DefaultSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = BottomBarDefaults.PillFootprint)
            )
        },
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBarsIgnoringVisibility,
                title = {
                    Text(
                        text = "탐색",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .bounceClick()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true)
                            ) { onNavigateToSearch() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "검색"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
                    .hazeEffect(state = hazeState) {
                        // 배너 접힐수록 블러 강화
                        val h = bannerHeight.floatValue
                        val collapse = if (h > 0f) {
                            (-scrollOffset.floatValue / h).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                        style = HazeStyle(
                            backgroundColor = topBarBlurColor,
                            tint = HazeTint(topBarBlurColor),
                            blurRadius = (30f + 50f * collapse).dp,
                            noiseFactor = 0f
                        )
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0.85f + 0.15f * collapse,
                            endIntensity = 0.6f * collapse
                        )
                    }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(ExploreIntent.RefreshCurrentTab) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ExploreHeaderLayout(
                state = state,
                pagerState = pagerState,
                trendingListState = trendingListState,
                popularListState = popularListState,
                hazeState = hazeState,
                bannerHeightState = bannerHeight,
                scrollOffsetState = scrollOffset,
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
        resolveAuthor = { authorId ->
            (state.trendingPosts + state.popularPosts).find { it.author.id == authorId }?.author?.let { ProfilePopupData(it.id, it.isFollowing) }
        },
        onNavigateToProfile = { onIntent(ExploreIntent.NavigateToUserProfile(it)) },
        onFollow = { onIntent(ExploreIntent.FollowUser(it)) },
        onUnfollow = { onIntent(ExploreIntent.UnfollowUser(it)) }
    )
}
