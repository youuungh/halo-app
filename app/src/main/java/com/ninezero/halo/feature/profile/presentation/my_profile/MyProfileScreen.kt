package com.ninezero.halo.feature.profile.presentation.my_profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.DeepLinks
import com.ninezero.halo.core.model.ReportTargetType
import com.ninezero.halo.core.ui.components.bottomsheet.CommentOptionBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.PostOptionBottomSheet
import com.ninezero.halo.core.ui.components.fab.AnimatedScrollAwareFAB
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.navigation.BottomBarDefaults
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.core.ui.components.popup.ProfilePopupData
import com.ninezero.halo.core.ui.components.popup.ProfilePopupHost
import com.ninezero.halo.core.ui.components.popup.rememberProfilePopupHostState
import com.ninezero.halo.core.ui.components.post.LocalReportRequester
import com.ninezero.halo.core.ui.components.post.PostCardSkeleton
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.presentation.components.ProfileHeaderSkeleton
import com.ninezero.halo.feature.profile.presentation.components.ProfileScreenShell
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab
import com.ninezero.halo.feature.profile.presentation.my_profile.components.CollapsibleHeaderLayout
import com.ninezero.halo.feature.profile.presentation.my_profile.components.rememberCollapsibleHeaderState
import com.ninezero.halo.feature.share.presentation.ShareIntent
import com.ninezero.halo.feature.share.presentation.ShareSheetHost
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MyProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCommentDetail: (Int) -> Unit,
    onNavigateToPostDetail: (Int) -> Unit = {},
    onNavigateToUserProfile: (Int) -> Unit = {},
    onNavigateToEditPost: (Int) -> Unit = {},
    onNavigateToProductDetail: (Int) -> Unit = {},
    onNavigateToSectionDetail: (SectionType, Int, Int?, String) -> Unit = { _, _, _, _ -> },
    onNavigateToCreateCommunityPost: (Int) -> Unit = {},
    onNavigateToFollowList: (userId: Int, displayName: String, initialTab: Int, followerCount: Int, followingCount: Int, subscriberCount: Int, showSubscribers: Boolean) -> Unit = { _, _, _, _, _, _, _ -> },
    onNavigateToMembershipManage: () -> Unit = {},
    onNavigateToMySubscriptions: () -> Unit = {},
    onNavigateToPoint: () -> Unit = {},
    onNavigateToCoupon: () -> Unit = {},
    onNavigateToCouponManage: () -> Unit = {},
    onNavigateToMyReviews: () -> Unit = {},
    onNavigateToAddress: () -> Unit = {},
    onNavigateToWishlist: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToMyOrders: () -> Unit = {},
    onNavigateToMyProducts: () -> Unit = {},
    onNavigateToSalesManage: () -> Unit = {},
    onNavigateToChatRoom: (Int) -> Unit = {},
    tabReselectFlow: Flow<Unit>? = null,
    onShowProfileImageViewer: ((String?) -> Unit)? = null,
    viewModel: MyProfileViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MyProfileSideEffect.NavigateToEditProfile -> onNavigateToEditProfile()
            is MyProfileSideEffect.NavigateToSettings -> onNavigateToSettings()
            is MyProfileSideEffect.NavigateToMembershipManage -> onNavigateToMembershipManage()
            is MyProfileSideEffect.NavigateToMySubscriptions -> onNavigateToMySubscriptions()
            is MyProfileSideEffect.NavigateToPoint -> onNavigateToPoint()
            is MyProfileSideEffect.NavigateToCoupon -> onNavigateToCoupon()
            is MyProfileSideEffect.NavigateToCouponManage -> onNavigateToCouponManage()
            is MyProfileSideEffect.NavigateToMyReviews -> onNavigateToMyReviews()
            is MyProfileSideEffect.NavigateToAddress -> onNavigateToAddress()
            is MyProfileSideEffect.NavigateToWishlist -> onNavigateToWishlist()
            is MyProfileSideEffect.NavigateToCart -> onNavigateToCart()
            is MyProfileSideEffect.NavigateToMyOrders -> onNavigateToMyOrders()
            is MyProfileSideEffect.NavigateToMyProducts -> onNavigateToMyProducts()
            is MyProfileSideEffect.NavigateToSalesManage -> onNavigateToSalesManage()
            is MyProfileSideEffect.NavigateToPostDetail -> onNavigateToPostDetail(sideEffect.postId)
            is MyProfileSideEffect.NavigateToCommentDetail -> onNavigateToCommentDetail(sideEffect.commentId)
            is MyProfileSideEffect.NavigateToUserProfile -> onNavigateToUserProfile(sideEffect.userId)
            is MyProfileSideEffect.NavigateToEditPost -> onNavigateToEditPost(sideEffect.postId)
            is MyProfileSideEffect.NavigateToProductDetail -> onNavigateToProductDetail(sideEffect.productId)
            is MyProfileSideEffect.NavigateToCreateCommunityPost -> onNavigateToCreateCommunityPost(sideEffect.creatorId)
            is MyProfileSideEffect.NavigateToChatRoom -> onNavigateToChatRoom(sideEffect.roomId)
            is MyProfileSideEffect.NavigateToSectionDetail -> onNavigateToSectionDetail(
                sideEffect.sectionType,
                sideEffect.creatorId,
                sideEffect.tagId,
                sideEffect.title
            )
        }
    }

    ProfileScreenShell(
        onResume = { viewModel.onIntent(MyProfileIntent.RefreshOnResume) },
        externalShowProfileImageViewer = onShowProfileImageViewer
    ) { callbacks ->
        MyProfileScreenContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onShowMediaViewer = callbacks.onShowMediaViewer,
            onShowProfileImageViewer = callbacks.onShowProfileImageViewer,
            tabReselectFlow = tabReselectFlow,
            onFollowerClick = {
                state.profile?.let { profile ->
                    onNavigateToFollowList(
                        profile.userId,
                        profile.displayName,
                        0,  // FOLLOWERS
                        profile.followerCount,
                        profile.followingCount,
                        profile.subscriberCount ?: 0,
                        profile.isCreator
                    )
                }
            },
            onFollowingClick = {
                state.profile?.let { profile ->
                    onNavigateToFollowList(
                        profile.userId,
                        profile.displayName,
                        1,  // FOLLOWING
                        profile.followerCount,
                        profile.followingCount,
                        profile.subscriberCount ?: 0,
                        profile.isCreator
                    )
                }
            },
            onSubscriberClick = {
                state.profile?.let { profile ->
                    onNavigateToFollowList(
                        profile.userId,
                        profile.displayName,
                        2,  // SUBSCRIBERS
                        profile.followerCount,
                        profile.followingCount,
                        profile.subscriberCount ?: 0,
                        profile.isCreator
                    )
                }
            },
            onIntent = viewModel::onIntent
        )
    }

    ShareSheetHost(
        visible = state.showShareSheet && state.sharePostId != null,
        shareAction = { vm -> vm.onIntent(ShareIntent.SharePost(state.sharePostId!!)) },
        onDismiss = { viewModel.onIntent(MyProfileIntent.DismissShareSheet) },
        onShareSuccess = { roomIds, count -> viewModel.onIntent(MyProfileIntent.ShareCompleted(roomIds, count)) },
        externalLink = state.sharePostId?.let(DeepLinks::postLink)
    )

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onAction = {
            if (state.snackbarState?.actionLabel == "채팅 열기") {
                state.navigateRoomId?.let { roomId ->
                    viewModel.onIntent(MyProfileIntent.NavigateToChatRoom(roomId))
                }
            }
        },
        onDismiss = { viewModel.onIntent(MyProfileIntent.SnackbarDismissed) }
    )
}

@Composable
internal fun MyProfileScreenContent(
    state: MyProfileState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onShowMediaViewer: (List<ViewerMediaItem>, Int, String?) -> Unit = { _, _, _ -> },
    onShowProfileImageViewer: (String?) -> Unit = {},
    tabReselectFlow: Flow<Unit>? = null,
    onFollowerClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    onSubscriberClick: () -> Unit = {},
    onIntent: (MyProfileIntent) -> Unit
) {
    val hazeState = remember { HazeState() }
    val topBarBlurColor = MaterialTheme.colorScheme.background

    val isCreator = state.profile?.isCreator == true
    val tabs = if (isCreator) {
        listOf(ProfileTab.HOME, ProfileTab.POSTS, ProfileTab.REPLIES, ProfileTab.COMMUNITY, ProfileTab.STORE)
    } else {
        listOf(ProfileTab.POSTS, ProfileTab.REPLIES)
    }

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val homeListState = rememberLazyListState()
    val postsListState = rememberLazyListState()
    val repliesListState = rememberLazyListState()
    val communityListState = rememberLazyListState()
    val storeListState = rememberLazyListState()
    val headerState = rememberCollapsibleHeaderState()

    // 같은 탭 재선택은 header 펼치기 + list top
    // 완전 top·펼침에서 한 번 더면 PullToRefresh
    LaunchedEffect(tabReselectFlow, state.selectedTab) {
        tabReselectFlow?.collect {
            val activeListState = when (state.selectedTab) {
                ProfileTab.HOME -> homeListState
                ProfileTab.POSTS -> postsListState
                ProfileTab.REPLIES -> repliesListState
                ProfileTab.COMMUNITY -> communityListState
                ProfileTab.STORE -> storeListState
            }
            val listAtTop = activeListState.firstVisibleItemIndex == 0 &&
                    activeListState.firstVisibleItemScrollOffset == 0
            val headerExpanded = headerState.isFullyExpanded

            if (listAtTop && headerExpanded) {
                onIntent(MyProfileIntent.RefreshProfile)
            } else {
                // list scroll과 header expand 병행
                launch { activeListState.animateScrollToItem(0) }
                launch { headerState.expand() }
            }
        }
    }

    val profilePopupState = rememberProfilePopupHostState()

    // scrollToPage로 중간 탭 컴포지션 방지
    LaunchedEffect(state.selectedTab, tabs) {
        val targetPage = tabs.indexOf(state.selectedTab).takeIf { it >= 0 } ?: 0
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    // 페이저 스와이프 시 탭 동기화
    LaunchedEffect(pagerState.currentPage, tabs) {
        val newTab = tabs.getOrNull(pagerState.currentPage) ?: tabs.first()
        if (state.selectedTab != newTab) {
            onIntent(MyProfileIntent.SelectTab(newTab))
        }
    }

    // 포스트 무한 스크롤
    InfiniteScrollTrigger(
        listState = postsListState,
        isLoading = state.isPostsLoading,
        hasMore = state.postsHasMore && state.selectedTab == ProfileTab.POSTS
    ) {
        onIntent(MyProfileIntent.LoadMorePosts)
    }

    // 답글 무한 스크롤
    InfiniteScrollTrigger(
        listState = repliesListState,
        isLoading = state.isRepliesLoading,
        hasMore = state.repliesHasMore && state.selectedTab == ProfileTab.REPLIES
    ) {
        onIntent(MyProfileIntent.LoadMoreCommentThreads)
    }

    Scaffold(
        snackbarHost = {
            // PillFootprint만 더해 이중 계산 방지
            DefaultSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = BottomBarDefaults.PillFootprint)
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    val username = state.profile?.username
                    Text(
                        text = if (!username.isNullOrBlank()) username else "프로필",
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
                            ) { onIntent(MyProfileIntent.NavigateToSettings) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = "설정"
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
                    .haloBlur(state = hazeState) {
                        // 헤더 접힐수록 블러 강화
                        val h = headerState.headerHeight
                        val collapse = if (h > 0f) {
                            (-headerState.scrollOffset / h).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                        style = HaloBlurStyle(
                            backgroundColor = topBarBlurColor,
                            tint = HaloBlurTint(topBarBlurColor),
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
            onRefresh = { onIntent(MyProfileIntent.RefreshProfile) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading && state.profile == null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = LocalBottomBarPadding.current)
                    ) {
                        item(key = "profile_header_skeleton") {
                            ProfileHeaderSkeleton()
                        }
                        items(5) {
                            PostCardSkeleton()
                        }
                    }
                }

                state.error != null && state.profile == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                                onClick = { onIntent(MyProfileIntent.LoadProfile) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }

                state.profile != null -> {
                    CollapsibleHeaderLayout(
                        state = state,
                        pagerState = pagerState,
                        homeListState = homeListState,
                        postsListState = postsListState,
                        repliesListState = repliesListState,
                        communityListState = communityListState,
                        storeListState = storeListState,
                        tabs = tabs,
                        hazeState = hazeState,
                        onShowMediaViewer = onShowMediaViewer,
                        onShowProfileImageViewer = onShowProfileImageViewer,
                        onProfileClick = { authorId, offset ->
                            if (state.profile.userId != authorId) {
                                profilePopupState.show(authorId, offset)
                            }
                        },
                        onFollowerClick = onFollowerClick,
                        onFollowingClick = onFollowingClick,
                        onSubscriberClick = onSubscriberClick,
                        onMembershipClick = { onIntent(MyProfileIntent.NavigateToMySubscriptions) },
                        onMembershipManageClick = { onIntent(MyProfileIntent.NavigateToMembershipManage) },
                        onReviewClick = { onIntent(MyProfileIntent.NavigateToMyReviews) },
                        onPointClick = { onIntent(MyProfileIntent.NavigateToPoint) },
                        onCouponClick = { onIntent(MyProfileIntent.NavigateToCoupon) },
                        onCouponManageClick = { onIntent(MyProfileIntent.NavigateToCouponManage) },
                        onAddressClick = { onIntent(MyProfileIntent.NavigateToAddress) },
                        onStoreClick = { onIntent(MyProfileIntent.NavigateToMyProducts) },
                        onSalesClick = { onIntent(MyProfileIntent.NavigateToSalesManage) },
                        onOrderClick = { onIntent(MyProfileIntent.NavigateToMyOrders) },
                        onCartClick = { onIntent(MyProfileIntent.NavigateToCart) },
                        onWishlistClick = { onIntent(MyProfileIntent.NavigateToWishlist) },
                        headerState = headerState,
                        onIntent = onIntent
                    )
                }
            }

                // 커뮤니티 글 작성 FAB
                val canWriteCommunityPost = state.selectedTab == ProfileTab.COMMUNITY && state.profile?.isCreator == true
                AnimatedScrollAwareFAB(
                    visible = canWriteCommunityPost,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .padding(bottom = LocalBottomBarPadding.current),
                    contentDescription = "커뮤니티 글 작성",
                    onClick = { onIntent(MyProfileIntent.CreateCommunityPost) }
                )
            }
        }
    }

    if (state.showPostOptionsSheet && state.selectedPost != null) {
        val selectedPost = state.selectedPost
        // 커뮤니티 글은 contextType으로 분기
        val isCommunityPost = selectedPost.contextType == PostContextType.COMMUNITY
        val isOwnPost = selectedPost.author.id == state.profile?.userId
        // 포스트 탭 커뮤니티 글은 남의 커뮤니티일 수 있음
        val fromCommunityTab = state.selectedTab == ProfileTab.COMMUNITY
        val reportRequester = LocalReportRequester.current
        PostOptionBottomSheet(
            isOwnPost = isOwnPost,
            isBookmarked = selectedPost.isBookmarked,
            copyLink = selectedPost.takeUnless { it.isSecret }?.let { DeepLinks.postLink(it.id) },
            onDismiss = { onIntent(MyProfileIntent.DismissPostOptions) },
            onEdit = { onIntent(if (isCommunityPost) MyProfileIntent.EditCommunityPost else MyProfileIntent.EditPost) },
            onDelete = { onIntent(if (isCommunityPost) MyProfileIntent.DeleteCommunityPost else MyProfileIntent.DeletePost) },
            onBookmark = { onIntent(MyProfileIntent.BookmarkPost) },
            onNotInterested = null,
            onBlock = { onIntent(MyProfileIntent.BlockPostAuthor) },
            onReport = { reportRequester(ReportTargetType.POST, selectedPost.id) },
            // 고정은 내 커뮤니티·크리에이터 피드 글, 비밀글은 해제만
            canPin = state.profile?.isCreator == true && when {
                fromCommunityTab -> !selectedPost.isSecret || selectedPost.isPinned
                else -> selectedPost.contextType == PostContextType.CREATOR_FEED
            },
            isPinned = selectedPost.isPinned,
            canCreatorDelete = fromCommunityTab && !isOwnPost,
            onPin = { onIntent(if (fromCommunityTab) MyProfileIntent.PinCommunityPost else MyProfileIntent.PinPost) }
        )
    }

    if (state.showCommentOptionsSheet && state.selectedComment != null) {
        val reportRequester = LocalReportRequester.current
        CommentOptionBottomSheet(
            isOwnComment = state.selectedComment.isProfileUser,
            isDeleted = state.selectedComment.isDeleted,
            isBookmarked = state.selectedComment.isBookmarked,
            onDismiss = { onIntent(MyProfileIntent.DismissCommentOptions) },
            onDelete = { onIntent(MyProfileIntent.DeleteComment) },
            onBookmark = { onIntent(MyProfileIntent.BookmarkComment) },
            onBlock = { onIntent(MyProfileIntent.BlockCommentAuthor) },
            onReport = { reportRequester(ReportTargetType.COMMENT, state.selectedComment.id) }
        )
    }

    if (state.showThreadPostOptionsSheet && state.selectedThreadPost != null) {
        val reportRequester = LocalReportRequester.current
        PostOptionBottomSheet(
            isOwnPost = false,  // replies tab의 포스트는 항상 다른 사람의 포스트
            isBookmarked = state.selectedThreadPost.isBookmarked,
            copyLink = state.selectedThreadPost.takeUnless { it.isSecret }?.let { DeepLinks.postLink(it.id) },
            onDismiss = { onIntent(MyProfileIntent.DismissThreadPostOptions) },
            onEdit = {},
            onDelete = {},
            onBookmark = { onIntent(MyProfileIntent.BookmarkThreadPost) },
            onNotInterested = null,
            onBlock = { onIntent(MyProfileIntent.BlockThreadPostAuthor) },
            onReport = { reportRequester(ReportTargetType.POST, state.selectedThreadPost.id) }
        )
    }

    ProfilePopupHost(
        state = profilePopupState,
        resolveAuthor = { authorId ->
            state.myPosts.firstOrNull { it.author.id == authorId }?.author?.let { ProfilePopupData(it.id, it.isFollowing) }
                ?: (state.communityPosts + state.communityPinnedPosts).firstOrNull { it.author.id == authorId }?.author?.let { ProfilePopupData(it.id, it.isFollowing) }
                ?: state.myCommentThreads.firstNotNullOfOrNull { thread ->
                    (thread.post.author.takeIf { it.id == authorId }
                        ?: thread.comments.firstOrNull { it.author.id == authorId }?.author)
                        ?.let { ProfilePopupData(it.id, it.isFollowing) }
                }
        },
        onNavigateToProfile = { onIntent(MyProfileIntent.NavigateToUserProfile(it)) },
        onFollow = { onIntent(MyProfileIntent.FollowUser(it)) },
        onUnfollow = { onIntent(MyProfileIntent.UnfollowUser(it)) }
    )
}
