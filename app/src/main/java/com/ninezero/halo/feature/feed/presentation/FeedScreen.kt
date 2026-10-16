
package com.ninezero.halo.feature.feed.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.ninezero.halo.core.ui.components.media.PrefetchUpcomingPostImages
import com.ninezero.halo.core.ui.components.media.rememberSettledVideoPostId
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.navigation.BottomBarDefaults
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.popup.ProfilePopupData
import com.ninezero.halo.core.ui.components.popup.ProfilePopupHost
import com.ninezero.halo.core.ui.components.popup.rememberProfilePopupHostState
import com.ninezero.halo.core.ui.components.post.LocalReportRequester
import com.ninezero.halo.core.ui.components.post.PostCard
import com.ninezero.halo.core.ui.components.post.PostCardSkeleton
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.video_player.VideoPreloader
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.core.ui.components.viewer.ViewerOverlayHost
import com.ninezero.halo.core.ui.components.viewer.rememberViewerOverlayHostState
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.feed.domain.model.nextVideoUrlsAfter
import com.ninezero.halo.feature.feed.presentation.components.RecommendedCreatorsSection
import com.ninezero.halo.feature.share.presentation.ShareIntent
import com.ninezero.halo.feature.share.presentation.ShareSheetHost
import com.ninezero.halo.ui.theme.GodoRounded
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.Flow
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun FeedScreen(
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToEditPost: ((Int) -> Unit)? = null,
    onNavigateToChatRoom: ((Int) -> Unit)? = null,
    onNavigateToNotifications: (() -> Unit)? = null,
    hasUnreadNotification: Boolean = false,
    tabReselectFlow: Flow<Unit>? = null,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val parentViewerHost = LocalViewerOverlayHostState.current  // 상위 호스트 있으면 위임
    val localViewerHost = rememberViewerOverlayHostState()
    val viewerHostState = parentViewerHost ?: localViewerHost

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is FeedSideEffect.NavigateToPostDetail -> onNavigateToPostDetail(sideEffect.postId)
            is FeedSideEffect.NavigateToUserProfile -> onNavigateToProfile(sideEffect.userId)
            is FeedSideEffect.NavigateToCreatorProfile -> onNavigateToProfile(sideEffect.creatorId)
            is FeedSideEffect.NavigateToEditPost -> onNavigateToEditPost?.invoke(sideEffect.postId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FeedScreenContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onNavigateToNotifications = onNavigateToNotifications,
            hasUnreadNotification = hasUnreadNotification,
            onShowMediaViewer = { items, index ->
                viewerHostState.showMediaViewer(items, index)
            },
            tabReselectFlow = tabReselectFlow,
            onIntent = viewModel::onIntent
        )

        // 상위 호스트 없을 때만 자체 렌더
        if (parentViewerHost == null) {
            ViewerOverlayHost(state = viewerHostState)
        }
    }

    ShareSheetHost(
        visible = state.showShareSheet && state.sharePostId != null,
        shareAction = { vm -> vm.onIntent(ShareIntent.SharePost(state.sharePostId!!)) },
        onDismiss = { viewModel.onIntent(FeedIntent.DismissShareSheet) },
        onShareSuccess = { roomIds, count -> viewModel.onIntent(FeedIntent.ShareCompleted(roomIds, count)) },
        externalLink = state.sharePostId?.let(DeepLinks::postLink)
    )

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onAction = {
            state.navigateRoomId?.let { roomId ->
                viewModel.onIntent(FeedIntent.SnackbarDismissed)
                onNavigateToChatRoom?.invoke(roomId)
            }
        },
        onDismiss = { viewModel.onIntent(FeedIntent.SnackbarDismissed) }
    )
}

@Composable
internal fun FeedScreenContent(
    state: FeedState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateToNotifications: (() -> Unit)? = null,
    hasUnreadNotification: Boolean = false,
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit = { _, _ -> },
    tabReselectFlow: Flow<Unit>? = null,
    onIntent: (FeedIntent) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(tabReselectFlow, listState) {
        tabReselectFlow?.collect {
            val atTop = listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0
            if (atTop) {
                onIntent(FeedIntent.RefreshFeed)
            } else {
                listState.animateScrollToItem(0)
            }
        }
    }

    // 자동재생 후보는 영상 포스트만
    val videoPostIds = remember(state.posts) {
        state.posts
            .filter { post -> post.mediaAttachments.any { it.type == MediaType.VIDEO } }
            .mapTo(HashSet()) { it.id }
    }
    // 중앙 영상 1개, 정착 후 확정
    val visiblePostId by rememberSettledVideoPostId(listState, videoPostIds) {
        (it as? String)?.removePrefix("post_")?.toIntOrNull()
    }
    // 다음 영상 포스트 앞부분 미리 캐시
    val context = LocalContext.current
    LaunchedEffect(visiblePostId, state.posts) {
        VideoPreloader.prefetch(context, state.posts.nextVideoUrlsAfter(visiblePostId, count = 2))
    }
    // 곧 보일 아래쪽 포스트들의 이미지를 Coil 캐시에 미리 로드
    PrefetchUpcomingPostImages(listState = listState, posts = state.posts) {
        (it as? String)?.removePrefix("post_")?.toIntOrNull()
    }
    val hazeState = remember { HazeState() }
    val topBarBlurColor = MaterialTheme.colorScheme.background

    val profilePopupState = rememberProfilePopupHostState()
    // 필요한 필드만 hoist해 PostCard 리컴포즈 방지
    val currentUserId = state.currentUserId

    InfiniteScrollTrigger(
        listState = listState,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        onLoadMore = { onIntent(FeedIntent.LoadMorePosts) }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = {
                DefaultSnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = BottomBarDefaults.PillFootprint)
                )
            },
            contentWindowInsets = WindowInsets(0),
            topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBarsIgnoringVisibility,
                title = {
                    Text(
                        text = "Halo",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = GodoRounded,
                        fontWeight = FontWeight.Normal
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
                            ) { onNavigateToNotifications?.invoke() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_notification),
                            contentDescription = "알림"
                        )
                        if (hasUnreadNotification) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 6.dp)
                                    .size(8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.error,
                                        shape = CircleShape
                                    )
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
                    .haloBlur(state = hazeState) {
                        style = HaloBlurStyle(
                            backgroundColor = topBarBlurColor,
                            tint = HaloBlurTint(topBarBlurColor),
                            noiseFactor = 0f
                        )
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0.5f,
                            endIntensity = 0f
                        )
                    }
            )
        }
    ) { paddingValues ->
        val topPadding = paddingValues.calculateTopPadding()

        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(FeedIntent.RefreshFeed) },
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = topPadding)
        ) {
            when {
                state.isLoading && state.posts.isEmpty() && state.recommendedCreators.isEmpty() -> {
                    // 확정 후에만 렌더
                    if (topPadding > 0.dp) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = topPadding, bottom = LocalBottomBarPadding.current)
                        ) {
                            items(5) {
                                PostCardSkeleton()
                            }
                        }
                    }
                }

                state.error != null && state.posts.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = topPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { onIntent(FeedIntent.LoadFeed) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }

                else -> {
                    val allPosts = remember(state.pendingPosts, state.posts) {
                        state.pendingPosts + state.posts
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = topPadding, bottom = LocalBottomBarPadding.current)
                    ) {
                        item(key = "creators_section") {
                            RecommendedCreatorsSection(
                                creators = state.recommendedCreators,
                                isLoading = state.isCreatorsLoading,
                                onCreatorClick = { creatorId ->
                                    onIntent(FeedIntent.NavigateToCreatorProfile(creatorId))
                                }
                            )
                        }

                        if (state.posts.isEmpty() && state.pendingPosts.isEmpty() && !state.isLoading) {
                            item(key = "empty_posts") {
                                Column(
                                    modifier = Modifier
                                        .fillParentMaxSize()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_no_data),
                                        contentDescription = null,
                                        modifier = Modifier.size(120.dp),
                                        tint = Color.Unspecified
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "아직 포스트가 없어요",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(
                                items = allPosts,
                                key = { _, post -> post.pendingId?.let { "pending_$it" } ?: "post_${post.id}" }
                            ) { index, post ->
                                Column(modifier = Modifier.animateItem()) {
                                    if (index == 0) {
                                        HorizontalDivider(
                                            thickness = 0.25.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                    PostCard(
                                        post = post,
                                        isUploading = post.isUploading,
                                        isVideoPlaying = visiblePostId == post.id,
                                        onPostClick = {
                                            if (!post.isUploading) {
                                                onIntent(FeedIntent.NavigateToPostDetail(post.id))
                                            }
                                        },
                                        onProfileClick = { offset ->
                                            if (!post.isUploading) {
                                                if (currentUserId == post.author.id) {
                                                    onIntent(FeedIntent.NavigateToUserProfile(post.author.id))
                                                } else {
                                                    profilePopupState.show(post.author.id, offset)
                                                }
                                            }
                                        },
                                        onLikeClick = {
                                            if (!post.isUploading) {
                                                if (post.isLiked) {
                                                    onIntent(FeedIntent.UnlikePost(post.id))
                                                } else {
                                                    onIntent(FeedIntent.LikePost(post.id))
                                                }
                                            }
                                        },
                                        onCommentClick = {
                                            if (!post.isUploading) {
                                                onIntent(FeedIntent.NavigateToPostDetail(post.id))
                                            }
                                        },
                                        onShareClick = {
                                            if (!post.isUploading) {
                                                onIntent(FeedIntent.SharePost(post.id))
                                            }
                                        },
                                        onMoreClick = {
                                            if (!post.isUploading) {
                                                onIntent(FeedIntent.ShowPostOptions(post))
                                            }
                                        },
                                        onMediaClick = { index ->
                                            if (!post.isUploading) {
                                                onShowMediaViewer(post.mediaAttachments.toViewerItems(), index)
                                            }
                                        }
                                    )
                                    HorizontalDivider(
                                        thickness = 0.25.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }

                        if (state.isLoading && state.posts.isNotEmpty()) {
                            items(3) { _ ->
                                PostCardSkeleton()
                            }
                        }
                    }
                }
            }
        }
        }
    }

    ProfilePopupHost(
        state = profilePopupState,
        resolveAuthor = { authorId -> state.posts.find { it.author.id == authorId }?.author?.let { ProfilePopupData(it.id, it.isFollowing) } },
        onNavigateToProfile = { onIntent(FeedIntent.NavigateToUserProfile(it)) },
        onFollow = { onIntent(FeedIntent.FollowUser(it)) },
        onUnfollow = { onIntent(FeedIntent.UnfollowUser(it)) }
    )

    CenterToast(
        visible = state.centerToastMessage != null,
        message = state.centerToastMessage ?: "",
        onDismiss = { onIntent(FeedIntent.DismissCenterToast) }
    )

    if (state.showPostOptionsSheet && state.selectedPost != null) {
        val isOwnPost = state.currentUserId == state.selectedPost.author.id
        val reportRequester = LocalReportRequester.current
        PostOptionBottomSheet(
            isOwnPost = isOwnPost,
            isBookmarked = state.selectedPost.isBookmarked,
            copyLink = state.selectedPost.takeUnless { it.isSecret }?.let { DeepLinks.postLink(it.id) },
            onDismiss = { onIntent(FeedIntent.DismissPostOptions) },
            onEdit = { onIntent(FeedIntent.EditPost) },
            onDelete = { onIntent(FeedIntent.DeletePost) },
            onBookmark = { onIntent(FeedIntent.BookmarkPost(state.selectedPost.id)) },
            onNotInterested = { onIntent(FeedIntent.HidePost(state.selectedPost.id)) },
            onBlock = { onIntent(FeedIntent.BlockUser(state.selectedPost.author.id)) },
            onReport = { reportRequester(ReportTargetType.POST, state.selectedPost.id) },
            // 피드 고정은 크리에이터 피드의 내 글만
            canPin = isOwnPost && state.isCurrentUserCreator &&
                state.selectedPost.contextType == PostContextType.CREATOR_FEED,
            isPinned = state.selectedPost.isPinned,
            onPin = { onIntent(FeedIntent.PinPost) }
        )
    }
}
