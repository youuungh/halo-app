package com.ninezero.halo.feature.profile.presentation.my_profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.DeepLinks
import com.ninezero.halo.core.model.ReportTargetType
import com.ninezero.halo.core.ui.components.bottomsheet.CommentOptionBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.PostOptionBottomSheet
import com.ninezero.halo.core.ui.components.layout.collapsingGlassTopBarHeight
import com.ninezero.halo.core.ui.components.layout.jumpOrAnimateToTop
import com.ninezero.halo.core.ui.components.list.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.list.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.list.ListErrorState
import com.ninezero.halo.core.ui.components.navigation.BottomBarDefaults
import com.ninezero.halo.core.ui.components.navigation.BottomBarSideButton
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarSideButton
import com.ninezero.halo.core.ui.components.popup.ProfilePopupData
import com.ninezero.halo.core.ui.components.popup.ProfilePopupHost
import com.ninezero.halo.core.ui.components.popup.rememberProfilePopupHostState
import com.ninezero.halo.core.ui.components.post.LocalReportRequester
import com.ninezero.halo.core.ui.components.post.PostCardSkeleton
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.viewer.ViewerMediaItem
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.presentation.components.ProfileHeaderSkeleton
import com.ninezero.halo.feature.profile.presentation.components.ProfileScreenShell
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab
import com.ninezero.halo.feature.profile.presentation.components.rememberCollapsibleHeaderState
import com.ninezero.halo.feature.profile.presentation.my_profile.components.CollapsibleHeaderLayout
import com.ninezero.halo.feature.profile.presentation.my_profile.components.MyProfileTopBar
import com.ninezero.halo.feature.share.presentation.ShareIntent
import com.ninezero.halo.feature.share.presentation.ShareSheetHost
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
        shareAction = { shareViewModel -> state.sharePostId?.let { shareViewModel.onIntent(ShareIntent.SharePost(it)) } },
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

    // 커뮤니티 글 작성 버튼은 하단 바 옆에 붙음
    val canWriteCommunityPost = state.selectedTab == ProfileTab.COMMUNITY && state.profile?.isCreator == true
    val bottomBarSideButton = LocalBottomBarSideButton.current
    val latestOnIntent by rememberUpdatedState(onIntent)
    DisposableEffect(bottomBarSideButton, canWriteCommunityPost) {
        if (canWriteCommunityPost) {
            bottomBarSideButton?.show(
                BottomBarSideButton(R.drawable.ic_chat_bubbles, "커뮤니티 글 작성") {
                    latestOnIntent(MyProfileIntent.CreateCommunityPost)
                }
            )
        }
        onDispose { bottomBarSideButton?.hide() }
    }

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

    // 같은 탭 재선택은 header 펼치기 + list top, 완전 top·펼침에서 한 번 더면 PullToRefresh
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
                launch {
                    activeListState.jumpOrAnimateToTop()
                    headerState.expand()
                }
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
        hasMore = state.hasMorePosts && state.selectedTab == ProfileTab.POSTS
    ) {
        onIntent(MyProfileIntent.LoadMorePosts)
    }

    // 답글 무한 스크롤
    InfiniteScrollTrigger(
        listState = repliesListState,
        isLoading = state.isRepliesLoading,
        hasMore = state.hasMoreReplies && state.selectedTab == ProfileTab.REPLIES
    ) {
        onIntent(MyProfileIntent.LoadMoreCommentThreads)
    }

    val topBarHeight = collapsingGlassTopBarHeight()
    val topBar: @Composable (collapseReveal: () -> Float, blurAlpha: () -> Float) -> Unit = { collapseReveal, blurAlpha ->
        MyProfileTopBar(
            title = state.profile?.username?.takeIf { it.isNotBlank() } ?: "프로필",
            hazeState = hazeState,
            backgroundColor = topBarBlurColor,
            collapseReveal = collapseReveal,
            blurAlpha = blurAlpha,
            onSettingsClick = { onIntent(MyProfileIntent.NavigateToSettings) }
        )
    }

    Scaffold(
        snackbarHost = {
            // PillSpace만 더해 이중 계산 방지
            DefaultSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = BottomBarDefaults.PillSpace)
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(MyProfileIntent.RefreshProfile) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            indicatorPadding = PaddingValues(top = topBarHeight)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading && state.profile == null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = topBarHeight, bottom = LocalBottomBarPadding.current)
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
                        ListErrorState(
                            title = "프로필을 불러오지 못했습니다",
                            reason = state.error,
                            onRetry = { onIntent(MyProfileIntent.LoadProfile) }
                        )
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
                        topBarHeight = topBarHeight,
                        topBar = topBar,
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

            if (state.profile == null) topBar({ 0f }, { 1f })
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
        hazeState = hazeState,
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
