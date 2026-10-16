package com.ninezero.halo.feature.feed.presentation.post_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.DeepLinks
import com.ninezero.halo.core.model.ReportTargetType
import com.ninezero.halo.core.ui.components.bottomsheet.CommentOptionBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.PostOptionBottomSheet
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.comment.CommentItemSkeleton
import com.ninezero.halo.core.ui.components.dialog.AlertDialog
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.media.rememberAutoplayTarget
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.LocalActiveViewerMediaKey
import com.ninezero.halo.core.ui.components.modifiers.LocalMediaSharedTransitionScope
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.modifiers.statusBarsPaddingIgnoringVisibility
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.post.LocalReportRequester
import com.ninezero.halo.core.ui.components.post.LocalSubscribeRequester
import com.ninezero.halo.core.ui.components.post.LockedPostCard
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.components.video_player.VideoPreloader
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.core.ui.components.viewer.ViewerOverlayHost
import com.ninezero.halo.core.ui.components.viewer.rememberViewerOverlayHostState
import com.ninezero.halo.core.util.showToast
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.feed.presentation.components.CommentInputBar
import com.ninezero.halo.feature.feed.presentation.components.SortPopupMenu
import com.ninezero.halo.feature.feed.presentation.post_detail.components.PostCommentThreadItem
import com.ninezero.halo.feature.feed.presentation.post_detail.components.PostContentSection
import com.ninezero.halo.feature.feed.presentation.post_detail.components.PostContentSkeleton
import com.ninezero.halo.feature.share.presentation.ShareIntent
import com.ninezero.halo.feature.share.presentation.ShareSheetHost
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun PostDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (Int) -> Unit,
    onNavigateToCommentDetail: (Int) -> Unit,
    onNavigateToEditPost: (Int) -> Unit,
    onNavigateToReply: (Int, Int?) -> Unit,
    onNavigateToChatRoom: (Int) -> Unit = {},
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val viewerHostState = rememberViewerOverlayHostState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var centerToastVisible by remember { mutableStateOf(false) }
    var centerToastMessage by remember { mutableStateOf("") }

    val handleBack: () -> Unit = {
        keyboardController?.hide()
        onNavigateBack()
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PostDetailSideEffect.NavigateBack -> handleBack()
            is PostDetailSideEffect.NavigateToUserProfile -> onNavigateToUserProfile(sideEffect.userId)
            is PostDetailSideEffect.NavigateToCommentDetail -> onNavigateToCommentDetail(sideEffect.commentId)
            is PostDetailSideEffect.NavigateToEditPost -> onNavigateToEditPost(sideEffect.postId)

            is PostDetailSideEffect.PostDeleted -> handleBack()
            is PostDetailSideEffect.NavigateToChatRoom -> onNavigateToChatRoom(sideEffect.roomId)
            is PostDetailSideEffect.ShowCenterToast -> {
                centerToastMessage = sideEffect.message
                centerToastVisible = true
            }
        }
    }

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalViewerOverlayHostState provides viewerHostState,
            LocalMediaSharedTransitionScope provides this@SharedTransitionLayout,
            LocalActiveViewerMediaKey provides viewerHostState.activeMediaKey
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                PostDetailScreenContent(
                    state = state,
                    snackbarHostState = snackbarHostState,
                    onNavigateBack = onNavigateBack,
                    onNavigateToReply = onNavigateToReply,
                    onShowMediaViewer = { items, index ->
                        viewerHostState.showMediaViewer(items, index)
                    },
                    onIntent = viewModel::onIntent
                )

                CenterToast(
                    visible = centerToastVisible,
                    message = centerToastMessage,
                    onDismiss = { centerToastVisible = false }
                )

                ViewerOverlayHost(
                    state = viewerHostState,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
        }
    }
}

@Composable
internal fun PostDetailScreenContent(
    state: PostDetailState,
    onNavigateBack: () -> Unit,
    onIntent: (PostDetailIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateToReply: (Int, Int?) -> Unit = { _, _ -> },
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit = { _, _ -> }
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    var headerHeight by rememberSaveableDp()
    // 입력바 실측 높이, 스낵바 앵커용
    var inputBarHeight by remember { mutableStateOf(0.dp) }
    var authorSectionBottom by remember { mutableIntStateOf(0) }

    // 자동재생은 중앙 영상 1개
    val allComments = remember(state.pendingComments, state.comments) {
        state.pendingComments + state.comments
    }
    val videoItemKeys = remember(state.post, allComments) {
        buildSet<Any> {
            if (state.post?.mediaAttachments?.any { it.type == MediaType.VIDEO } == true) add("post_content")
            allComments.forEach { c ->
                if (c.mediaAttachments.any { it.type == MediaType.VIDEO }) add(c.listKey())
            }
        }
    }
    val activeVideoItemKey by rememberAutoplayTarget(listState) { item -> item.key.takeIf { it in videoItemKeys } }

    // 다음 영상 2개를 미리 캐시
    val context = LocalContext.current
    val orderedVideoUrls = remember(state.post, allComments) {
        buildList<Pair<Any, String>> {
            state.post?.mediaAttachments?.firstOrNull { it.type == MediaType.VIDEO }
                ?.let { add("post_content" to it.url) }
            allComments.forEach { c ->
                c.mediaAttachments.firstOrNull { it.type == MediaType.VIDEO }
                    ?.let { add(c.listKey() to it.url) }
            }
        }
    }
    LaunchedEffect(activeVideoItemKey, orderedVideoUrls) {
        val idx = orderedVideoUrls.indexOfFirst { it.first == activeVideoItemKey }
        val start = if (idx >= 0) idx + 1 else 0
        VideoPreloader.prefetch(context, orderedVideoUrls.drop(start).take(2).map { it.second })
    }

    // COMMENT 알림 진입
    var commentHighlightPlayed by rememberSaveable { mutableStateOf(false) }
    val commentHighlight = remember { Animatable(0f) }
    var commentHighlightItemIndex by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(state.isLoading, state.isCommentsLoading, state.comments, headerHeight) {
        val targetId = state.highlightCommentId ?: return@LaunchedEffect
        if (commentHighlightPlayed) return@LaunchedEffect
        if (state.isLoading || state.post == null || state.isCommentsLoading || headerHeight <= 0.dp) return@LaunchedEffect
        val commentIndex = allComments.indexOfFirst { !it.isUploading && it.id == targetId }
        if (commentIndex < 0) return@LaunchedEffect
        commentHighlightPlayed = true

        // 아이템 순서: 헤더 spacer·본문·구분선 3·댓글
        commentHighlightItemIndex = 5 + commentIndex
    }
    LaunchedEffect(commentHighlightItemIndex) {
        val itemIndex = commentHighlightItemIndex ?: return@LaunchedEffect
        val anchorOffsetPx = with(density) { (headerHeight + 16.dp).roundToPx() }
        listState.animateScrollToItem(itemIndex, -anchorOffsetPx)
        // 안착까지 재보정
        var stableFrames = 0
        var attempts = 0
        while (attempts < 10 && stableFrames < 2) {
            val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == itemIndex }
            if (item != null && kotlin.math.abs(item.offset - anchorOffsetPx) <= 2) {
                stableFrames++
            } else {
                listState.scrollToItem(itemIndex, -anchorOffsetPx)
                stableFrames = 0
            }
            withFrameNanos { }
            attempts++
        }
        commentHighlight.snapTo(1f)
        delay(400)
        commentHighlight.animateTo(0f, animationSpec = tween(durationMillis = 1200))
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onAction = {
            val roomId = state.navigateRoomId
            if (state.snackbarState?.actionLabel == "채팅 열기" && roomId != null) {
                onIntent(PostDetailIntent.NavigateToChatRoom(roomId))
            }
        },
        onDismiss = { onIntent(PostDetailIntent.SnackbarDismissed) }
    )

    // 작성자 섹션이 스크롤을 벗어났는지 감지
    val showHeaderProfile by remember {
        derivedStateOf {
            val firstVisibleItemIndex = listState.firstVisibleItemIndex
            val firstVisibleItemOffset = listState.firstVisibleItemScrollOffset

            if (firstVisibleItemIndex > 0) {
                true
            } else {
                firstVisibleItemOffset > authorSectionBottom
            }
        }
    }

    HaloPullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onIntent(PostDetailIntent.RefreshPost) },
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        indicatorPadding = PaddingValues(top = headerHeight)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .imePadding()
        ) {
            // 헤더 spacer
            item {
                Spacer(modifier = Modifier.height(headerHeight + 16.dp))
            }

            // 포스트 본문
            item(key = "post_content") {
                if (state.isLoading) {
                    PostContentSkeleton()
                } else if (state.post == null) {
                    // 로드 실패: 빈화면 대신 에러 메시지 + 재시도
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.error ?: "포스트를 불러올 수 없습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "재시도",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onIntent(PostDetailIntent.RefreshPost) }
                        )
                    }
                } else {
                    state.post.let { post ->
                        if (post.isRestricted) {
                            // 잠금 글은 잠금 카드 + 구독 동선
                            val subscribeRequester = LocalSubscribeRequester.current
                            LockedPostCard(
                                post = post,
                                onClick = {
                                    if (post.isSecret) {
                                        context.showToast("크리에이터만 볼 수 있습니다")
                                    } else {
                                        subscribeRequester(
                                            post.author.id,
                                            post.author.displayName ?: post.author.username
                                        )
                                    }
                                }
                            )
                            return@let
                        }
                        PostContentSection(
                            post = post,
                            isFollowing = state.isFollowing,
                            isOwnPost = post.author.id == state.myUserId,
                            onAuthorSectionPositioned = { bottom ->
                                authorSectionBottom = bottom
                            },
                            onProfileClick = {
                                onIntent(PostDetailIntent.NavigateToUserProfile(post.author.id))
                            },
                            onFollowClick = {
                                if (state.isFollowing) {
                                    onIntent(PostDetailIntent.UnfollowAuthor)
                                } else {
                                    onIntent(PostDetailIntent.FollowAuthor)
                                }
                            },
                            onLikeClick = {
                                if (post.isLiked) {
                                    onIntent(PostDetailIntent.UnlikePost)
                                } else {
                                    onIntent(PostDetailIntent.LikePost)
                                }
                            },
                            onCommentClick = {
                                onNavigateToReply(post.id, null)
                            },
                            onShareClick = {
                                onIntent(PostDetailIntent.SharePost)
                            },
                            onMediaClick = { index ->
                                onShowMediaViewer(post.mediaAttachments.toViewerItems(), index)
                            },
                            isVideoPlaying = activeVideoItemKey == "post_content"
                        )
                    }
                }
            }

            // 댓글이 있거나 로딩 중이면 구분선, 정렬은 댓글이 있거나 정렬을 바꾸는 중일 때만
            if (allComments.isNotEmpty() || state.isCommentsLoading) {
                // 구분선
                item {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                if (allComments.isNotEmpty() || state.isChangingSort) {
                    // 정렬 드롭다운
                    item {
                        SortPopupMenu(
                            currentSort = state.commentSortBy,
                            onSortChange = { onIntent(PostDetailIntent.ChangeSortType(it)) }
                        )
                    }

                    // 정렬 밑 구분선
                    item {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // 댓글 로딩
            if (state.isCommentsLoading && state.comments.isEmpty()) {
                items(3) {
                    CommentItemSkeleton(
                        modifier = Modifier.padding(top = 12.dp),
                        showLine = false
                    )
                }
            }

            // 댓글 목록
            itemsIndexed(
                items = allComments,
                key = { _, comment -> comment.listKey() }
            ) { index, comment ->
                // COMMENT 알림 진입 하이라이트 배경
                val isHighlightTarget =
                    !comment.isUploading && comment.id == state.highlightCommentId
                val highlightColor = MaterialTheme.colorScheme.primary
                Column(
                    modifier = if (isHighlightTarget) {
                        Modifier.drawBehind {
                            val alpha = commentHighlight.value * 0.04f
                            if (alpha > 0f) drawRect(highlightColor.copy(alpha = alpha))
                        }
                    } else {
                        Modifier
                    }
                ) {
                    // 댓글 스레드 사이 구분선
                    if (index > 0) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }

                    PostCommentThreadItem(
                        comment = comment,
                        expandedReplies = state.expandedCommentReplies[comment.id],
                        isLoadingReplies = state.loadingRepliesCommentIds.contains(comment.id),
                        hasMoreReplies = state.repliesHasMore[comment.id] == true,
                        onExpandReplies = { onIntent(PostDetailIntent.ExpandReplies(comment.id)) },
                        onLoadMoreReplies = { onIntent(PostDetailIntent.LoadMoreReplies(comment.id)) },
                        onLikeClick = { commentId ->
                            if (comment.isUploading) return@PostCommentThreadItem
                            onIntent(PostDetailIntent.ToggleCommentLike(commentId))
                        },
                        onReplyClick = { commentId ->
                            if (comment.isUploading) return@PostCommentThreadItem
                            onIntent(PostDetailIntent.NavigateToCommentDetail(commentId))
                        },
                        onMoreClick = { commentId ->
                            if (comment.isUploading) return@PostCommentThreadItem
                            onIntent(PostDetailIntent.ShowCommentOptions(commentId))
                        },
                        onProfileClick = { userId, _ ->
                            if (comment.isUploading) return@PostCommentThreadItem
                            // 댓글/답글 프로필은 팝업 없이 바로 프로필로 이동
                            onIntent(PostDetailIntent.NavigateToUserProfile(userId))
                        },
                        onCommentClick = { commentId ->
                            if (comment.isUploading) return@PostCommentThreadItem
                            onIntent(PostDetailIntent.NavigateToCommentDetail(commentId))
                        },
                        isUploading = comment.isUploading,
                        postAuthorId = state.post?.author?.id,
                        pendingReplies = state.pendingReplies[comment.id] ?: emptyList(),
                        onMediaClick = onShowMediaViewer,
                        isVideoPlaying = activeVideoItemKey == comment.listKey()
                    )
                }
            }

            // 마지막 댓글 뒤 구분선
            if (allComments.isNotEmpty()) {
                item {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // 추가 로딩
            if (state.isCommentsLoading && state.comments.isNotEmpty()) {
                items(2) {
                    CommentItemSkeleton(
                        modifier = Modifier.padding(top = 12.dp),
                        showLine = false
                    )
                }
            }

            // 댓글이 없는 경우
            if (state.post != null && !state.isCommentsLoading && state.comments.isEmpty()) {
                item {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "첫 번째 댓글을 남겨보세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 하단 여백
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        InfiniteScrollTrigger(
            listState = listState,
            isLoading = state.isCommentsLoading,
            hasMore = state.commentsHasMore,
            onLoadMore = { onIntent(PostDetailIntent.LoadMoreComments) }
        )

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = backgroundColor,
                        tint = HaloBlurTint(backgroundColor),
                        noiseFactor = 0f
                    )
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 0.5f,
                        endIntensity = 0f
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .onGloballyPositioned { coordinates ->
                    headerHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPaddingIgnoringVisibility()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 뒤로가기 버튼
                GlassIconButton(
                    onClick = onNavigateBack,
                    icon = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 타이틀 영역
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 프로필 사진, 스크롤 시 표시
                    AnimatedVisibility(
                        visible = showHeaderProfile && state.post != null,
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + expandHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            expandFrom = Alignment.Start,
                            clip = false
                        ),
                        exit = scaleOut(animationSpec = tween(150)) +
                                shrinkHorizontally(
                                    animationSpec = tween(150),
                                    shrinkTowards = Alignment.Start,
                                    clip = false
                                )
                    ) {
                        Row {
                            AsyncImage(
                                model = state.post?.author?.avatarThumbUrl
                                    ?: state.post?.author?.avatarUrl,
                                contentDescription = "프로필",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                                error = painterResource(R.drawable.ic_profile_placeholder),
                                fallback = painterResource(R.drawable.ic_profile_placeholder)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }

                    // 타이틀 + 조회수
                    Column {
                        val numberFormat = rememberKoreanNumberFormat()

                        // 타이틀
                        Text(
                            text = if (showHeaderProfile && state.post != null) {
                                state.post.author.displayName ?: state.post.author.username
                            } else {
                                "포스트"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // 조회수, 로드 후 슬라이드업
                        AnimatedVisibility(
                            visible = state.isViewCountLoaded && state.post != null,
                            enter = expandVertically(
                                animationSpec = tween(300),
                                expandFrom = Alignment.Top
                            ) + slideInVertically(
                                animationSpec = tween(300),
                                initialOffsetY = { it }
                            ) + fadeIn(animationSpec = tween(300)),
                            exit = shrinkVertically(
                                animationSpec = tween(200),
                                shrinkTowards = Alignment.Top
                            ) + slideOutVertically(
                                animationSpec = tween(200),
                                targetOffsetY = { it }
                            ) + fadeOut(animationSpec = tween(200))
                        ) {
                            state.post?.let { post ->
                                Text(
                                    text = "조회 ${numberFormat.format(post.viewCount)}회",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 더보기 버튼
                GlassIconButton(
                    onClick = { onIntent(PostDetailIntent.ShowPostOptions) },
                    icon = painterResource(id = R.drawable.ic_ellipsis_horizontal_circle),
                    contentDescription = "더보기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor
                )
            }
        }

        // 댓글 입력창, 로드 성공 시에만
        if (!(state.isCommentsLoading && state.comments.isEmpty() && !state.isChangingSort) && state.commentsError == null) {
            // 잠금 글: 댓글 열람은 열어두되 작성은 구독자 전용
            val subscribeRequester = LocalSubscribeRequester.current
            CommentInputBar(
                targetAuthorName = state.post?.author?.displayName ?: state.post?.author?.username,
                replyTo = state.replyToComment,
                hazeState = hazeState,
                onInputClick = {
                    state.post?.let { post ->
                        when {
                            post.isRestricted && post.isSecret ->
                                context.showToast("크리에이터만 댓글을 남길 수 있습니다")

                            post.isRestricted ->
                                subscribeRequester(
                                    post.author.id,
                                    post.author.displayName ?: post.author.username
                                )

                            else -> onNavigateToReply(post.id, state.replyToComment?.id)
                        }
                    }
                },
                onCancelReply = { onIntent(PostDetailIntent.SetReplyTo(null)) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onGloballyPositioned { coordinates ->
                        inputBarHeight = with(density) { coordinates.size.height.toDp() }
                    },
                actionText = "댓글"
            )
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = inputBarHeight),
            applyWindowInsets = inputBarHeight <= 0.dp
        )
    }

    if (state.showPostOptionsSheet && state.post != null) {
        val reportRequester = LocalReportRequester.current
        PostOptionBottomSheet(
            isOwnPost = state.post.author.id == state.myUserId,
            isBookmarked = state.post.isBookmarked,
            copyLink = state.post.takeUnless { it.isSecret }?.let { DeepLinks.postLink(it.id) },
            onDismiss = { onIntent(PostDetailIntent.DismissPostOptions) },
            onEdit = { onIntent(PostDetailIntent.EditPost) },
            onDelete = { onIntent(PostDetailIntent.DeletePost) },
            onBookmark = { onIntent(PostDetailIntent.BookmarkPost) },
            onNotInterested = { onIntent(PostDetailIntent.NotInterestedPost) },
            onBlock = { onIntent(PostDetailIntent.BlockAuthor) },
            onReport = { reportRequester(ReportTargetType.POST, state.post.id) },
            // 피드 고정은 크리에이터 피드의 내 글만
            canPin = state.post.author.id == state.myUserId && state.isCurrentUserCreator &&
                state.post.contextType == PostContextType.CREATOR_FEED,
            isPinned = state.post.isPinned,
            onPin = { onIntent(PostDetailIntent.PinPost) }
        )
    }

    if (state.showCommentOptionsSheet && state.selectedComment != null) {
        val reportRequester = LocalReportRequester.current
        CommentOptionBottomSheet(
            isOwnComment = state.selectedComment.author.id == state.myUserId,
            isBookmarked = state.selectedComment.isBookmarked,
            onDismiss = { onIntent(PostDetailIntent.DismissCommentOptions) },
            onDelete = { onIntent(PostDetailIntent.DeleteComment) },
            onBookmark = { onIntent(PostDetailIntent.BookmarkComment) },
            onBlock = { onIntent(PostDetailIntent.BlockCommentAuthor) },
            onReport = { reportRequester(ReportTargetType.COMMENT, state.selectedComment.id) }
        )
    }

    if (state.showBlockConfirmDialog && state.blockTargetUser != null) {
        ConfirmDialog(
            profileImageUrl = state.blockTargetUser.avatarUrl,
            title = "${state.blockTargetUser.displayName}님을 차단하시겠어요?",
            confirmText = "차단",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(PostDetailIntent.ConfirmBlockUser) },
            onDismiss = { onIntent(PostDetailIntent.DismissBlockConfirmDialog) }
        )
    }

    if (state.showBlockCompletedDialog && state.blockTargetUser != null) {
        AlertDialog(
            title = "${state.blockTargetUser.displayName}님이 차단되었습니다",
            onDismiss = { onIntent(PostDetailIntent.DismissBlockCompletedDialog) }
        )
    }

    ShareSheetHost(
        visible = state.showShareSheet && state.post != null,
        shareAction = { vm -> vm.onIntent(ShareIntent.SharePost(state.post!!.id)) },
        onDismiss = { onIntent(PostDetailIntent.DismissShareSheet) },
        onShareSuccess = { roomIds, count ->
            onIntent(
                PostDetailIntent.ShareCompleted(
                    roomIds,
                    count
                )
            )
        },
        externalLink = state.post?.let { DeepLinks.postLink(it.id) }
    )
}

// 대기 행과 서버 id가 안 겹치는 key
private fun Comment.listKey(): String = pendingId?.let { "pending_$it" } ?: "comment_$id"
