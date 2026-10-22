package com.ninezero.halo.feature.profile.presentation.user_profile

import android.widget.Toast
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.DeepLinks
import com.ninezero.halo.core.model.ReportTargetType
import com.ninezero.halo.core.ui.components.bottomsheet.CommentOptionBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.PostOptionBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.ProfileOptionBottomSheet
import com.ninezero.halo.core.ui.components.dialog.AlertDialog
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.popup.ProfilePopupData
import com.ninezero.halo.core.ui.components.popup.ProfilePopupHost
import com.ninezero.halo.core.ui.components.popup.rememberProfilePopupHostState
import com.ninezero.halo.core.ui.components.post.LocalReportRequester
import com.ninezero.halo.core.ui.components.post.PostCardSkeleton
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.util.showToast
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.presentation.components.ProfileScreenShell
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab
import com.ninezero.halo.feature.profile.presentation.components.UserProfileHeaderSkeleton
import com.ninezero.halo.feature.profile.presentation.user_profile.components.CollapsingToolbarLayout
import com.ninezero.halo.feature.share.presentation.ShareIntent
import com.ninezero.halo.feature.share.presentation.ShareSheetHost
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditPost: (Int) -> Unit,
    onNavigateToCommentDetail: (Int) -> Unit,
    onNavigateToPostDetail: (Int) -> Unit = {},
    onNavigateToUserProfile: (Int) -> Unit = {},
    onNavigateToMessage: (Int) -> Unit = {},
    onNavigateToNewChat: (targetUserId: Int, username: String, displayName: String, avatarUrl: String, role: String, isBlockedByMe: Boolean, isBlockedByOther: Boolean) -> Unit = { _, _, _, _, _, _, _ -> },
    onNavigateToProductDetail: (Int) -> Unit = {},
    onNavigateToSectionDetail: (SectionType, Int, Int?, String) -> Unit = { _, _, _, _ -> },
    onNavigateToCreateCommunityPost: (Int) -> Unit = {},
    onNavigateToFollowList: (userId: Int, displayName: String, initialTab: Int, followerCount: Int, followingCount: Int, subscriberCount: Int, showSubscribers: Boolean) -> Unit = { _, _, _, _, _, _, _ -> },
    onNavigateToEditProfile: () -> Unit = {},
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // 아바타 원본 preload
    LaunchedEffect(state.profile?.avatarUrl) {
        val url = state.profile?.avatarUrl
        if (!url.isNullOrBlank()) {
            SingletonImageLoader.get(context).execute(
                ImageRequest.Builder(context)
                    .data(url)
                    .memoryCacheKey(url)
                    .size(Size.ORIGINAL)
                    .build()
            )
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is UserProfileSideEffect.NavigateBack -> onNavigateBack()
            is UserProfileSideEffect.NavigateToPostDetail -> onNavigateToPostDetail(sideEffect.postId)
            is UserProfileSideEffect.NavigateToCommentDetail -> onNavigateToCommentDetail(sideEffect.commentId)
            is UserProfileSideEffect.NavigateToEditPost -> onNavigateToEditPost(sideEffect.postId)
            is UserProfileSideEffect.NavigateToUserProfile -> onNavigateToUserProfile(sideEffect.userId)
            is UserProfileSideEffect.NavigateToMessage -> onNavigateToMessage(sideEffect.roomId)
            is UserProfileSideEffect.NavigateToNewChat -> onNavigateToNewChat(
                sideEffect.targetUserId,
                sideEffect.username,
                sideEffect.displayName,
                sideEffect.avatarUrl,
                sideEffect.role,
                sideEffect.isBlockedByMe,
                sideEffect.isBlockedByOther
            )

            is UserProfileSideEffect.NavigateToProductDetail -> onNavigateToProductDetail(sideEffect.productId)
            is UserProfileSideEffect.NavigateToSectionDetail -> onNavigateToSectionDetail(
                sideEffect.sectionType,
                sideEffect.creatorId,
                sideEffect.tagId,
                sideEffect.title
            )

            is UserProfileSideEffect.ShowToast -> {
                context.showToast(sideEffect.message, Toast.LENGTH_SHORT)
            }

            is UserProfileSideEffect.NavigateToCreateCommunityPost -> onNavigateToCreateCommunityPost(
                sideEffect.creatorId
            )
        }
    }

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        ProfileScreenShell(
            onResume = { viewModel.onIntent(UserProfileIntent.RefreshOnResume) },
            sharedTransitionScope = this@SharedTransitionLayout
        ) { callbacks ->
            UserProfileScreenContent(
                state = state,
                snackbarHostState = snackbarHostState,
                onShowMediaViewer = callbacks.onShowMediaViewer,
                onShowProfileImageViewer = callbacks.onShowProfileImageViewer,
                onCreateCommunityPost = { viewModel.onIntent(UserProfileIntent.CreateCommunityPost) },
                onFollowerClick = {
                    state.profile?.let { profile ->
                        onNavigateToFollowList(
                            profile.userId,
                            profile.displayName,
                            0,  // FOLLOWERS
                            profile.followerCount,
                            profile.followingCount,
                            profile.subscriberCount ?: 0,
                            false
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
                            false
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
                            true
                        )
                    }
                },
                onEditProfileClick = onNavigateToEditProfile,
                onIntent = viewModel::onIntent
            )
        }
    }

    ShareSheetHost(
        visible = state.showShareSheet && state.sharePostId != null,
        shareAction = { vm -> vm.onIntent(ShareIntent.SharePost(state.sharePostId!!)) },
        onDismiss = { viewModel.onIntent(UserProfileIntent.DismissShareSheet) },
        onShareSuccess = { roomIds, count ->
            viewModel.onIntent(
                UserProfileIntent.ShareCompleted(
                    roomIds,
                    count
                )
            )
        },
        externalLink = state.sharePostId?.let(DeepLinks::postLink)
    )

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onAction = {
            when (state.snackbarState?.actionLabel) {
                "채팅 열기" -> {
                    state.navigateRoomId?.let { roomId ->
                        viewModel.onIntent(UserProfileIntent.NavigateToChatRoom(roomId))
                    }
                }

                "재시도" -> viewModel.onIntent(UserProfileIntent.LoadProfile)
            }
        },
        onDismiss = { viewModel.onIntent(UserProfileIntent.SnackbarDismissed) }
    )
}

@Composable
internal fun UserProfileScreenContent(
    state: UserProfileState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onShowMediaViewer: (List<ViewerMediaItem>, Int, String?) -> Unit = { _, _, _ -> },
    onShowProfileImageViewer: (String?) -> Unit = {},
    onCreateCommunityPost: () -> Unit = {},
    onFollowerClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    onSubscriberClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onIntent: (UserProfileIntent) -> Unit
) {
    val collapsingState = rememberCollapsingToolbarScaffoldState()

    val profilePopupState = rememberProfilePopupHostState()

    val isCreator = state.profile?.isCreator == true
    val tabs = if (isCreator) {
        listOf(
            ProfileTab.HOME,
            ProfileTab.POSTS,
            ProfileTab.REPLIES,
            ProfileTab.COMMUNITY,
            ProfileTab.STORE
        )
    } else {
        listOf(ProfileTab.POSTS, ProfileTab.REPLIES)
    }

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val homeListState = rememberLazyListState()
    val postsListState = rememberLazyListState()
    val repliesListState = rememberLazyListState()
    val communityListState = rememberLazyListState()
    val storeListState = rememberLazyListState()

    // 탭 변경 시 페이저 동기화
    LaunchedEffect(state.selectedTab, tabs) {
        val targetPage = tabs.indexOf(state.selectedTab).coerceAtLeast(0)
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.currentPage, tabs) {
        val newTab = tabs.getOrElse(pagerState.currentPage) { tabs.first() }
        if (state.selectedTab != newTab) {
            onIntent(UserProfileIntent.SelectTab(newTab))
        }
    }

    // 포스트 무한 스크롤
    InfiniteScrollTrigger(
        listState = postsListState,
        isLoading = state.isPostsLoading,
        hasMore = state.postsHasMore && state.selectedTab == ProfileTab.POSTS
    ) {
        onIntent(UserProfileIntent.LoadMorePosts)
    }

    // 답글 무한 스크롤
    InfiniteScrollTrigger(
        listState = repliesListState,
        isLoading = state.isRepliesLoading,
        hasMore = state.repliesHasMore && state.selectedTab == ProfileTab.REPLIES
    ) {
        onIntent(UserProfileIntent.LoadMoreCommentThreads)
    }


    Scaffold(
        snackbarHost = { DefaultSnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        when {
            state.profile == null -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item(key = "profile_header_skeleton") {
                            UserProfileHeaderSkeleton()
                        }
                        items(5) {
                            PostCardSkeleton()
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .size(40.dp)
                            .bounceClick()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true)
                            ) { onIntent(UserProfileIntent.NavigateBack) },
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_left),
                                contentDescription = "뒤로가기",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            else -> {
                CollapsingToolbarLayout(
                    state = state,
                    collapsingState = collapsingState,
                    pagerState = pagerState,
                    homeListState = homeListState,
                    postsListState = postsListState,
                    repliesListState = repliesListState,
                    communityListState = communityListState,
                    storeListState = storeListState,
                    tabs = tabs,
                    profilePopupState = profilePopupState,
                    onShowMediaViewer = onShowMediaViewer,
                    onShowProfileImageViewer = onShowProfileImageViewer,
                    onCreateCommunityPost = onCreateCommunityPost,
                    onFollowerClick = onFollowerClick,
                    onFollowingClick = onFollowingClick,
                    onSubscriberClick = onSubscriberClick,
                    onEditProfileClick = onEditProfileClick,
                    onIntent = onIntent
                )
            }
        }
    }

    if (state.showPostOptionsSheet && state.selectedPost != null) {
        val selectedPost = state.selectedPost
        val isOwnPost = state.myUserId == selectedPost.author.id

        val reportRequester = LocalReportRequester.current
        PostOptionBottomSheet(
            isOwnPost = isOwnPost,
            isBookmarked = selectedPost.isBookmarked,
            isPinned = false,
            canCreatorDelete = false,
            copyLink = selectedPost.takeUnless { it.isSecret }?.let { DeepLinks.postLink(it.id) },
            onDismiss = { onIntent(UserProfileIntent.DismissPostOptions) },
            onEdit = { onIntent(UserProfileIntent.EditPost) },
            onDelete = { onIntent(UserProfileIntent.DeletePost) },
            onPin = {},
            onBookmark = { onIntent(UserProfileIntent.BookmarkPost) },
            onNotInterested = null,
            onBlock = { onIntent(UserProfileIntent.BlockPostAuthor) },
            onReport = { reportRequester(ReportTargetType.POST, selectedPost.id) }
        )
    }

    if (state.showCommentOptionsSheet && state.selectedComment != null) {
        val reportRequester = LocalReportRequester.current
        CommentOptionBottomSheet(
            isOwnComment = false,
            isDeleted = state.selectedComment.isDeleted,
            isBookmarked = state.selectedComment.isBookmarked,
            onDismiss = { onIntent(UserProfileIntent.DismissCommentOptions) },
            onDelete = {},
            onBookmark = { onIntent(UserProfileIntent.BookmarkComment) },
            onBlock = { onIntent(UserProfileIntent.BlockCommentAuthor) },
            onReport = { reportRequester(ReportTargetType.COMMENT, state.selectedComment.id) }
        )
    }

    if (state.showThreadPostOptionsSheet && state.selectedThreadPost != null) {
        val reportRequester = LocalReportRequester.current
        PostOptionBottomSheet(
            isOwnPost = false,
            isBookmarked = state.selectedThreadPost.isBookmarked,
            copyLink = state.selectedThreadPost.takeUnless { it.isSecret }
                ?.let { DeepLinks.postLink(it.id) },
            onDismiss = { onIntent(UserProfileIntent.DismissThreadPostOptions) },
            onEdit = {},
            onDelete = {},
            onBookmark = { onIntent(UserProfileIntent.BookmarkThreadPost) },
            onNotInterested = null,
            onBlock = { onIntent(UserProfileIntent.BlockThreadPostAuthor) },
            onReport = { reportRequester(ReportTargetType.POST, state.selectedThreadPost.id) }
        )
    }

    // 알림 벨
    if (state.showNotifyFollowDialog && state.profile != null) {
        ConfirmDialog(
            title = "알림을 받아보려면\n${state.profile.displayName}님을\n팔로우하세요",
            confirmText = "팔로우",
            dismissText = "취소",
            onConfirm = { onIntent(UserProfileIntent.ConfirmNotifyFollow) },
            onDismiss = { onIntent(UserProfileIntent.DismissNotifyFollowDialog) }
        )
    }

    if (state.showProfileOptionsSheet && state.profile?.isOwnProfile == false) {
        val reportRequester = LocalReportRequester.current
        ProfileOptionBottomSheet(
            isBlocked = state.profile.isBlockedByMe,
            profileLink = DeepLinks.profileLink(state.profile.userId),
            onDismiss = { onIntent(UserProfileIntent.DismissProfileOptions) },
            onReport = { reportRequester(ReportTargetType.USER, state.profile.userId) },
            onBlock = {
                if (state.profile.isBlockedByMe) {
                    onIntent(UserProfileIntent.ShowUnblockConfirmDialog)
                } else {
                    onIntent(UserProfileIntent.ShowBlockConfirmDialog)
                }
            }
        )
    }

    if (state.showBlockConfirmDialog && state.profile != null) {
        ConfirmDialog(
            profileImageUrl = state.profile.avatarUrl,
            title = "${state.profile.displayName}님을 차단하시겠어요?",
            message = "차단하면 서로의 포스트와 프로필을 볼 수 없습니다.",
            confirmText = "차단",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(UserProfileIntent.ConfirmBlockUser) },
            onDismiss = { onIntent(UserProfileIntent.DismissBlockConfirmDialog) }
        )
    }

    if (state.showBlockCompletedDialog && state.profile != null) {
        AlertDialog(
            title = "${state.profile.displayName}님이 차단되었습니다",
            onDismiss = { onIntent(UserProfileIntent.DismissBlockCompletedDialog) }
        )
    }

    if (state.showUnblockConfirmDialog && state.profile != null) {
        ConfirmDialog(
            profileImageUrl = state.profile.avatarUrl,
            title = "${state.profile.displayName}님을 차단 해제하시겠어요?",
            confirmText = "차단 해제",
            dismissText = "취소",
            onConfirm = { onIntent(UserProfileIntent.ConfirmUnblockUser) },
            onDismiss = { onIntent(UserProfileIntent.DismissUnblockConfirmDialog) }
        )
    }

    if (state.showUnblockCompletedDialog && state.profile != null) {
        AlertDialog(
            title = "${state.profile.displayName}님이 차단 해제되었습니다",
            onDismiss = { onIntent(UserProfileIntent.DismissUnblockCompletedDialog) }
        )
    }

    ProfilePopupHost(
        state = profilePopupState,
        resolveAuthor = { authorId ->
            state.userPosts.firstOrNull { it.author.id == authorId }?.author?.let {
                ProfilePopupData(
                    it.id,
                    it.isFollowing
                )
            }
                ?: (state.communityPosts + state.communityPinnedPosts).firstOrNull { it.author.id == authorId }?.author?.let {
                    ProfilePopupData(
                        it.id,
                        it.isFollowing
                    )
                }
                ?: state.userCommentThreads.firstNotNullOfOrNull { thread ->
                    (thread.post.author.takeIf { it.id == authorId }
                        ?: thread.comments.firstOrNull { it.author.id == authorId }?.author)
                        ?.let { ProfilePopupData(it.id, it.isFollowing) }
                }
        },
        onNavigateToProfile = { onIntent(UserProfileIntent.NavigateToUserProfile(it)) },
        onFollow = { onIntent(UserProfileIntent.FollowOtherUser(it)) },
        onUnfollow = { onIntent(UserProfileIntent.UnfollowOtherUser(it)) }
    )
}
