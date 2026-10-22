package com.ninezero.halo.feature.feed.presentation.liked_posts

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.deeplink.DeepLinks
import com.ninezero.halo.core.model.ReportTargetType
import com.ninezero.halo.core.ui.components.bottomsheet.CommentOptionBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.PostOptionBottomSheet
import com.ninezero.halo.core.ui.components.chip.HaloFilterChipsRow
import com.ninezero.halo.core.ui.components.comment.CommentItem
import com.ninezero.halo.core.ui.components.layout.PagedListScreenShell
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.media.PrefetchUpcomingPostImages
import com.ninezero.halo.core.ui.components.media.rememberSettledVideoPostId
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.modifiers.LocalActiveViewerMediaKey
import com.ninezero.halo.core.ui.components.modifiers.LocalMediaSharedTransitionScope
import com.ninezero.halo.core.ui.components.popup.ProfilePopupData
import com.ninezero.halo.core.ui.components.popup.ProfilePopupHost
import com.ninezero.halo.core.ui.components.popup.rememberProfilePopupHostState
import com.ninezero.halo.core.ui.components.post.LocalReportRequester
import com.ninezero.halo.core.ui.components.post.PostCard
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.video_player.VideoPreloader
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.core.ui.components.viewer.ViewerOverlayHost
import com.ninezero.halo.core.ui.components.viewer.rememberViewerOverlayHostState
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.LikeTargetType
import com.ninezero.halo.feature.feed.domain.model.LikedItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.feed.domain.model.nextVideoUrlsAfter
import com.ninezero.halo.feature.share.presentation.ShareIntent
import com.ninezero.halo.feature.share.presentation.ShareSheetHost
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun LikedPostsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPostDetail: (Int) -> Unit = {},
    onNavigateToCommentDetail: (Int) -> Unit = {},
    onNavigateToUserProfile: (Int) -> Unit = {},
    onNavigateToEditPost: ((Int) -> Unit)? = null,
    onNavigateToChatRoom: ((Int) -> Unit)? = null,
    viewModel: LikedPostsViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val viewerHostState = rememberViewerOverlayHostState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is LikedPostsSideEffect.NavigateToPostDetail -> onNavigateToPostDetail(sideEffect.postId)
            is LikedPostsSideEffect.NavigateToCommentDetail -> onNavigateToCommentDetail(sideEffect.commentId)
            is LikedPostsSideEffect.NavigateToUserProfile -> onNavigateToUserProfile(sideEffect.userId)
            is LikedPostsSideEffect.NavigateToEditPost -> onNavigateToEditPost?.invoke(sideEffect.postId)
        }
    }

    ShareSheetHost(
        visible = state.showShareSheet && state.sharePostId != null,
        shareAction = { vm -> vm.onIntent(ShareIntent.SharePost(state.sharePostId!!)) },
        onDismiss = { viewModel.onIntent(LikedPostsIntent.DismissShareSheet) },
        onShareSuccess = { roomIds, count ->
            viewModel.onIntent(
                LikedPostsIntent.ShareCompleted(
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
            state.navigateRoomId?.let { roomId ->
                viewModel.onIntent(LikedPostsIntent.SnackbarDismissed)
                onNavigateToChatRoom?.invoke(roomId)
            }
        },
        onDismiss = { viewModel.onIntent(LikedPostsIntent.SnackbarDismissed) }
    )

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalViewerOverlayHostState provides viewerHostState,
            LocalMediaSharedTransitionScope provides this@SharedTransitionLayout,
            LocalActiveViewerMediaKey provides viewerHostState.activeMediaKey
        ) {
            LikedPostsScreenContent(
                state = state,
                snackbarHostState = snackbarHostState,
                onNavigateBack = onNavigateBack,
                onIntent = viewModel::onIntent,
                onShowMediaViewer = { items, index ->
                    viewerHostState.showMediaViewer(items, index)
                }
            )

            ViewerOverlayHost(
                state = viewerHostState,
                sharedTransitionScope = this@SharedTransitionLayout
            )
        }
    }
}

@Composable
internal fun LikedPostsScreenContent(
    state: LikedPostsState,
    onNavigateBack: () -> Unit,
    onIntent: (LikedPostsIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit = { _, _ -> }
) {
    val profilePopupState = rememberProfilePopupHostState()

    PagedListScreenShell(
        title = "좋아요",
        isRefreshing = state.isRefreshing,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        isEmpty = state.items.isEmpty(),
        error = state.error,
        emptyTitle = "좋아요한 항목이 없어요",
        emptyDescription = "포스트나 댓글에 좋아요하면 여기에 표시돼요",
        onNavigateBack = onNavigateBack,
        onRefresh = { onIntent(LikedPostsIntent.RefreshItems) },
        onRetry = { onIntent(LikedPostsIntent.LoadItems) },
        onLoadMore = { onIntent(LikedPostsIntent.LoadMoreItems) },
        snackbarHostState = snackbarHostState,
        filterContent = { chipHazeState ->
            HaloFilterChipsRow(
                labels = listOf("전체", "포스트", "댓글"),
                selectedIndex = state.selectedTab,
                onSelect = { onIntent(LikedPostsIntent.SelectTab(it)) },
                hazeState = chipHazeState
            )
        }
    ) { listState, headerHeight ->
        // 자동재생 후보는 영상 포스트만
        val videoPostIds = remember(state.items) {
            state.items.mapNotNull { it.post }
                .filter { post -> post.mediaAttachments.any { it.type == MediaType.VIDEO } }
                .mapTo(HashSet()) { it.id }
        }
        // 중앙 최근접 영상 1개만, 스크롤 정착 후 확정
        val visiblePostId by rememberSettledVideoPostId(listState, videoPostIds) {
            (it as? String)?.removePrefix("POST_")?.toIntOrNull()
        }
        // 다음 영상 포스트 앞부분 미리 캐시
        val context = LocalContext.current
        LaunchedEffect(visiblePostId, state.items) {
            val posts = state.items.mapNotNull { it.post }
            VideoPreloader.prefetch(context, posts.nextVideoUrlsAfter(visiblePostId, count = 2))
        }
        val postList = remember(state.items) { state.items.mapNotNull { it.post } }
        PrefetchUpcomingPostImages(listState = listState, posts = postList) {
            (it as? String)?.removePrefix("POST_")?.toIntOrNull()
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = headerHeight, bottom = 16.dp)
        ) {
            itemsIndexed(
                items = state.items,
                key = { _, item -> "${item.targetType}_${item.targetId}" }
            ) { index, item ->
                Column(modifier = Modifier.animateItem()) {
                    if (index == 0) {
                        HorizontalDivider(
                            thickness = 0.25.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    when (item.targetType) {
                        LikeTargetType.POST -> {
                            item.post?.let { post ->
                                PostCard(
                                    post = post,
                                    isVideoPlaying = visiblePostId == post.id,
                                    onPostClick = {
                                        onIntent(
                                            LikedPostsIntent.NavigateToPostDetail(
                                                post.id
                                            )
                                        )
                                    },
                                    onProfileClick = { offset ->
                                        if (state.currentUserId == post.author.id) {
                                            onIntent(LikedPostsIntent.NavigateToUserProfile(post.author.id))
                                        } else {
                                            profilePopupState.show(post.author.id, offset)
                                        }
                                    },
                                    onLikeClick = {
                                        if (post.isLiked) {
                                            onIntent(LikedPostsIntent.UnlikePost(post.id))
                                        } else {
                                            onIntent(LikedPostsIntent.LikePost(post.id))
                                        }
                                    },
                                    onCommentClick = {
                                        onIntent(
                                            LikedPostsIntent.NavigateToPostDetail(
                                                post.id
                                            )
                                        )
                                    },
                                    onShareClick = { onIntent(LikedPostsIntent.SharePost(post.id)) },
                                    onMoreClick = { onIntent(LikedPostsIntent.ShowPostOptions(post)) },
                                    onMediaClick = { index ->
                                        onShowMediaViewer(
                                            post.mediaAttachments.toViewerItems(),
                                            index
                                        )
                                    }
                                )
                            }
                        }

                        LikeTargetType.COMMENT -> {
                            item.comment?.let { comment ->
                                CommentItem(
                                    authorName = comment.author.displayName
                                        ?: comment.author.username,
                                    authorRole = comment.author.role,
                                    authorAvatarUrl = comment.author.avatarThumbUrl
                                        ?: comment.author.avatarUrl,
                                    content = comment.content,
                                    createdAt = comment.createdAt,
                                    likeCount = comment.likeCount,
                                    isLiked = comment.isLiked,
                                    isBlocked = false,
                                    showLine = false,
                                    mediaAttachments = comment.mediaAttachments,
                                    onLikeClick = {
                                        if (comment.isLiked) {
                                            onIntent(LikedPostsIntent.UnlikeComment(comment.id))
                                        } else {
                                            onIntent(LikedPostsIntent.LikeComment(comment.id))
                                        }
                                    },
                                    onReplyClick = {
                                        onIntent(
                                            LikedPostsIntent.NavigateToCommentDetail(
                                                comment.id
                                            )
                                        )
                                    },
                                    onMoreClick = {
                                        onIntent(
                                            LikedPostsIntent.ShowCommentOptions(
                                                comment
                                            )
                                        )
                                    },
                                    onProfileClick = { offset ->
                                        when {
                                            comment.author.isDeleted -> Unit  // 탈퇴자는 프로필로 이동 안 함
                                            state.currentUserId == comment.author.id ->
                                                onIntent(LikedPostsIntent.NavigateToUserProfile(comment.author.id))
                                            else -> profilePopupState.show(comment.author.id, offset)
                                        }
                                    },
                                    onCommentClick = {
                                        onIntent(
                                            LikedPostsIntent.NavigateToCommentDetail(
                                                comment.id
                                            )
                                        )
                                    },
                                    showReplyButton = false,
                                    onMediaClick = { index ->
                                        onShowMediaViewer(
                                            comment.mediaAttachments.toViewerItems(),
                                            index
                                        )
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            if (state.isLoading && state.items.isNotEmpty()) {
                item(key = "loading_more") {
                    ListLoadMoreIndicator()
                }
            }
        }
    }

    ProfilePopupHost(
        state = profilePopupState,
        resolveAuthor = { authorId ->
            state.items.findAuthor(authorId)?.let { ProfilePopupData(it.id, it.isFollowing) }
        },
        onNavigateToProfile = { onIntent(LikedPostsIntent.NavigateToUserProfile(it)) },
        onFollow = { onIntent(LikedPostsIntent.FollowUser(it)) },
        onUnfollow = { onIntent(LikedPostsIntent.UnfollowUser(it)) }
    )

    if (state.showPostOptionsSheet && state.selectedPost != null) {
        val isOwnPost = state.currentUserId == state.selectedPost.author.id
        val reportRequester = LocalReportRequester.current
        PostOptionBottomSheet(
            isOwnPost = isOwnPost,
            isBookmarked = state.selectedPost.isBookmarked,
            copyLink = state.selectedPost.takeUnless { it.isSecret }
                ?.let { DeepLinks.postLink(it.id) },
            onDismiss = { onIntent(LikedPostsIntent.DismissPostOptions) },
            onEdit = { onIntent(LikedPostsIntent.EditPost) },
            onDelete = { onIntent(LikedPostsIntent.DeletePost) },
            onBookmark = { onIntent(LikedPostsIntent.BookmarkPost(state.selectedPost.id)) },
            onNotInterested = null,
            onBlock = { onIntent(LikedPostsIntent.BlockUser(state.selectedPost.author.id)) },
            onReport = { reportRequester(ReportTargetType.POST, state.selectedPost.id) },
            // 피드 고정은 크리에이터 피드의 내 글만
            canPin = isOwnPost && state.isCurrentUserCreator &&
                state.selectedPost.contextType == PostContextType.CREATOR_FEED,
            isPinned = state.selectedPost.isPinned,
            onPin = { onIntent(LikedPostsIntent.PinPost) }
        )
    }

    if (state.showCommentOptionsSheet && state.selectedComment != null) {
        val isOwnComment = state.currentUserId == state.selectedComment.author.id
        val reportRequester = LocalReportRequester.current
        CommentOptionBottomSheet(
            isOwnComment = isOwnComment,
            isBookmarked = state.selectedComment.isBookmarked,
            onDismiss = { onIntent(LikedPostsIntent.DismissCommentOptions) },
            onDelete = { onIntent(LikedPostsIntent.DeleteComment) },
            onBookmark = { onIntent(LikedPostsIntent.BookmarkComment) },
            onBlock = { onIntent(LikedPostsIntent.BlockCommentAuthor(state.selectedComment.author.id)) },
            onReport = { reportRequester(ReportTargetType.COMMENT, state.selectedComment.id) }
        )
    }
}

private fun List<LikedItem>.findAuthor(authorId: Int): Author? {
    return firstNotNullOfOrNull { item ->
        when (item.targetType) {
            LikeTargetType.POST -> item.post?.author?.takeIf { it.id == authorId }
            LikeTargetType.COMMENT -> item.comment?.author?.takeIf { it.id == authorId }
        }
    }
}
