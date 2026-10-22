package com.ninezero.halo.feature.feed.presentation.saved_posts

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
import com.ninezero.halo.feature.feed.domain.model.BookmarkItem
import com.ninezero.halo.feature.feed.domain.model.BookmarkTargetType
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.feed.domain.model.nextVideoUrlsAfter
import com.ninezero.halo.feature.share.presentation.ShareIntent
import com.ninezero.halo.feature.share.presentation.ShareSheetHost
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SavedPostsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPostDetail: (Int) -> Unit = {},
    onNavigateToCommentDetail: (Int) -> Unit = {},
    onNavigateToUserProfile: (Int) -> Unit = {},
    onNavigateToEditPost: ((Int) -> Unit)? = null,
    onNavigateToChatRoom: ((Int) -> Unit)? = null,
    viewModel: SavedPostsViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val viewerHostState = rememberViewerOverlayHostState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SavedPostsSideEffect.NavigateToPostDetail -> onNavigateToPostDetail(sideEffect.postId)
            is SavedPostsSideEffect.NavigateToCommentDetail -> onNavigateToCommentDetail(sideEffect.commentId)
            is SavedPostsSideEffect.NavigateToUserProfile -> onNavigateToUserProfile(sideEffect.userId)
            is SavedPostsSideEffect.NavigateToEditPost -> onNavigateToEditPost?.invoke(sideEffect.postId)
        }
    }

    ShareSheetHost(
        visible = state.showShareSheet && state.sharePostId != null,
        shareAction = { vm -> vm.onIntent(ShareIntent.SharePost(state.sharePostId!!)) },
        onDismiss = { viewModel.onIntent(SavedPostsIntent.DismissShareSheet) },
        onShareSuccess = { roomIds, count ->
            viewModel.onIntent(
                SavedPostsIntent.ShareCompleted(
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
                viewModel.onIntent(SavedPostsIntent.SnackbarDismissed)
                onNavigateToChatRoom?.invoke(roomId)
            }
        },
        onDismiss = { viewModel.onIntent(SavedPostsIntent.SnackbarDismissed) }
    )

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalViewerOverlayHostState provides viewerHostState,
            LocalMediaSharedTransitionScope provides this@SharedTransitionLayout,
            LocalActiveViewerMediaKey provides viewerHostState.activeMediaKey
        ) {
            SavedPostsScreenContent(
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
internal fun SavedPostsScreenContent(
    state: SavedPostsState,
    onNavigateBack: () -> Unit,
    onIntent: (SavedPostsIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit = { _, _ -> }
) {
    val profilePopupState = rememberProfilePopupHostState()

    PagedListScreenShell(
        title = "저장됨",
        isRefreshing = state.isRefreshing,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        isEmpty = state.items.isEmpty(),
        error = state.error,
        emptyTitle = "저장된 항목이 없어요",
        emptyDescription = "포스트나 댓글을 저장하면 여기에 표시돼요",
        onNavigateBack = onNavigateBack,
        onRefresh = { onIntent(SavedPostsIntent.RefreshItems) },
        onRetry = { onIntent(SavedPostsIntent.LoadItems) },
        onLoadMore = { onIntent(SavedPostsIntent.LoadMoreItems) },
        snackbarHostState = snackbarHostState,
        filterContent = { chipHazeState ->
            HaloFilterChipsRow(
                labels = listOf("전체", "포스트", "댓글"),
                selectedIndex = state.selectedTab,
                onSelect = { onIntent(SavedPostsIntent.SelectTab(it)) },
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
        // 다음 영상 포스트 앞부분 미리 캐시 → 전환 지연 감소
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
                        BookmarkTargetType.POST -> {
                            item.post?.let { post ->
                                PostCard(
                                    post = post,
                                    isVideoPlaying = visiblePostId == post.id,
                                    onPostClick = {
                                        onIntent(
                                            SavedPostsIntent.NavigateToPostDetail(
                                                post.id
                                            )
                                        )
                                    },
                                    onProfileClick = { offset ->
                                        if (state.currentUserId == post.author.id) {
                                            onIntent(SavedPostsIntent.NavigateToUserProfile(post.author.id))
                                        } else {
                                            profilePopupState.show(post.author.id, offset)
                                        }
                                    },
                                    onLikeClick = {
                                        if (post.isLiked) {
                                            onIntent(SavedPostsIntent.UnlikePost(post.id))
                                        } else {
                                            onIntent(SavedPostsIntent.LikePost(post.id))
                                        }
                                    },
                                    onCommentClick = {
                                        onIntent(
                                            SavedPostsIntent.NavigateToPostDetail(
                                                post.id
                                            )
                                        )
                                    },
                                    onShareClick = { onIntent(SavedPostsIntent.SharePost(post.id)) },
                                    onMoreClick = { onIntent(SavedPostsIntent.ShowPostOptions(post)) },
                                    onMediaClick = { index ->
                                        onShowMediaViewer(post.mediaAttachments.toViewerItems(), index)
                                    }
                                )
                            }
                        }

                        BookmarkTargetType.COMMENT -> {
                            item.comment?.let { comment ->
                                CommentItem(
                                    authorName = comment.author.displayName
                                        ?: comment.author.username,
                                    authorRole = comment.author.role,
                                    authorAvatarUrl = comment.author.avatarThumbUrl ?: comment.author.avatarUrl,
                                    content = comment.content,
                                    createdAt = comment.createdAt,
                                    likeCount = comment.likeCount,
                                    isLiked = comment.isLiked,
                                    isBlocked = false,
                                    showLine = false,
                                    mediaAttachments = comment.mediaAttachments,
                                    onLikeClick = {
                                        if (comment.isLiked) {
                                            onIntent(SavedPostsIntent.UnlikeComment(comment.id))
                                        } else {
                                            onIntent(SavedPostsIntent.LikeComment(comment.id))
                                        }
                                    },
                                    onReplyClick = {
                                        onIntent(
                                            SavedPostsIntent.NavigateToCommentDetail(
                                                comment.id
                                            )
                                        )
                                    },
                                    onMoreClick = {
                                        onIntent(
                                            SavedPostsIntent.ShowCommentOptions(
                                                comment
                                            )
                                        )
                                    },
                                    onProfileClick = { offset ->
                                        when {
                                            comment.author.isDeleted -> Unit  // 탈퇴자는 프로필로 이동 안 함
                                            state.currentUserId == comment.author.id ->
                                                onIntent(SavedPostsIntent.NavigateToUserProfile(comment.author.id))
                                            else -> profilePopupState.show(comment.author.id, offset)
                                        }
                                    },
                                    onCommentClick = { onIntent(SavedPostsIntent.NavigateToCommentDetail(comment.id)) },
                                    showReplyButton = false,
                                    onMediaClick = { index ->
                                        onShowMediaViewer(comment.mediaAttachments.toViewerItems(), index)
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
        onNavigateToProfile = { onIntent(SavedPostsIntent.NavigateToUserProfile(it)) },
        onFollow = { onIntent(SavedPostsIntent.FollowUser(it)) },
        onUnfollow = { onIntent(SavedPostsIntent.UnfollowUser(it)) }
    )

    if (state.showPostOptionsSheet && state.selectedPost != null) {
        val isOwnPost = state.currentUserId == state.selectedPost.author.id
        val reportRequester = LocalReportRequester.current
        PostOptionBottomSheet(
            isOwnPost = isOwnPost,
            isBookmarked = state.selectedPost.isBookmarked,
            copyLink = state.selectedPost.takeUnless { it.isSecret }?.let { DeepLinks.postLink(it.id) },
            onDismiss = { onIntent(SavedPostsIntent.DismissPostOptions) },
            onEdit = { onIntent(SavedPostsIntent.EditPost) },
            onDelete = { onIntent(SavedPostsIntent.DeletePost) },
            onBookmark = { onIntent(SavedPostsIntent.BookmarkPost(state.selectedPost.id)) },
            onNotInterested = null,
            onBlock = { onIntent(SavedPostsIntent.BlockUser(state.selectedPost.author.id)) },
            onReport = { reportRequester(ReportTargetType.POST, state.selectedPost.id) },
            // 피드 고정은 크리에이터 피드의 내 글만
            canPin = isOwnPost && state.isCurrentUserCreator &&
                state.selectedPost.contextType == PostContextType.CREATOR_FEED,
            isPinned = state.selectedPost.isPinned,
            onPin = { onIntent(SavedPostsIntent.PinPost) }
        )
    }

    if (state.showCommentOptionsSheet && state.selectedComment != null) {
        val isOwnComment = state.currentUserId == state.selectedComment.author.id
        val reportRequester = LocalReportRequester.current
        CommentOptionBottomSheet(
            isOwnComment = isOwnComment,
            isBookmarked = state.selectedComment.isBookmarked,
            onDismiss = { onIntent(SavedPostsIntent.DismissCommentOptions) },
            onDelete = { onIntent(SavedPostsIntent.DeleteComment) },
            onBookmark = { onIntent(SavedPostsIntent.BookmarkComment) },
            onBlock = { onIntent(SavedPostsIntent.BlockCommentAuthor(state.selectedComment.author.id)) },
            onReport = { reportRequester(ReportTargetType.COMMENT, state.selectedComment.id) }
        )
    }
}

private fun List<BookmarkItem>.findAuthor(authorId: Int): Author? {
    return firstNotNullOfOrNull { item ->
        when (item.targetType) {
            BookmarkTargetType.POST -> item.post?.author?.takeIf { it.id == authorId }
            BookmarkTargetType.COMMENT -> item.comment?.author?.takeIf { it.id == authorId }
        }
    }
}
