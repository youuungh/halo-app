package com.ninezero.halo.feature.feed.presentation.comment_detail

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.deeplink.DeepLinks
import com.ninezero.halo.core.model.ReportTargetType
import com.ninezero.halo.core.ui.components.bottomsheet.CommentOptionBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.PostOptionBottomSheet
import com.ninezero.halo.core.ui.components.comment.CommentItem
import com.ninezero.halo.core.ui.components.comment.CommentItemSkeleton
import com.ninezero.halo.core.ui.components.dialog.AlertDialog
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.media.rememberAutoplayTarget
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.modifiers.LocalActiveViewerMediaKey
import com.ninezero.halo.core.ui.components.modifiers.LocalMediaSharedTransitionScope
import com.ninezero.halo.core.ui.components.popup.ProfilePopupData
import com.ninezero.halo.core.ui.components.popup.ProfilePopupHost
import com.ninezero.halo.core.ui.components.popup.rememberProfilePopupHostState
import com.ninezero.halo.core.ui.components.post.LocalReportRequester
import com.ninezero.halo.core.ui.components.post.LocalSubscribeRequester
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.video_player.VideoPreloader
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.core.ui.components.viewer.ViewerOverlayHost
import com.ninezero.halo.core.ui.components.viewer.rememberViewerOverlayHostState
import com.ninezero.halo.core.util.showToast
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.feed.presentation.comment_detail.components.ParentCommentItem
import com.ninezero.halo.feature.feed.presentation.comment_detail.components.ParentCommentSection
import com.ninezero.halo.feature.feed.presentation.comment_detail.components.PostPreviewSection
import com.ninezero.halo.feature.feed.presentation.components.CommentInputBar
import com.ninezero.halo.feature.feed.presentation.components.ReplyCurveContainer
import com.ninezero.halo.feature.feed.presentation.components.SortPopupMenu
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CommentDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (Int) -> Unit,
    onNavigateToCommentDetail: (Int) -> Unit,
    onNavigateToPost: (Int) -> Unit,
    onNavigateToEditPost: (Int) -> Unit,
    onNavigateToReply: (Int, Int) -> Unit,
    viewModel: CommentDetailViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val viewerHostState = rememberViewerOverlayHostState()
    val keyboardController = LocalSoftwareKeyboardController.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CommentDetailSideEffect.NavigateBack -> {
                keyboardController?.hide()
                onNavigateBack()
            }

            is CommentDetailSideEffect.NavigateToUserProfile -> onNavigateToUserProfile(sideEffect.userId)
            is CommentDetailSideEffect.NavigateToCommentDetail -> onNavigateToCommentDetail(
                sideEffect.commentId
            )

            is CommentDetailSideEffect.NavigateToPost -> onNavigateToPost(sideEffect.postId)
            is CommentDetailSideEffect.NavigateToEditPost -> onNavigateToEditPost(sideEffect.postId)

            is CommentDetailSideEffect.NavigateToReply -> onNavigateToReply(
                sideEffect.postId,
                sideEffect.parentCommentId
            )
        }
    }

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalViewerOverlayHostState provides viewerHostState,
            LocalMediaSharedTransitionScope provides this@SharedTransitionLayout,
            LocalActiveViewerMediaKey provides viewerHostState.activeMediaKey
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CommentDetailScreenContent(
                    state = state,
                    snackbarHostState = snackbarHostState,
                    onNavigateBack = { viewModel.onIntent(CommentDetailIntent.NavigateBack) },
                    onNavigateToReply = { viewModel.onIntent(CommentDetailIntent.NavigateToReply) },
                    onShowMediaViewer = { items, index ->
                        viewerHostState.showMediaViewer(items, index)
                    },
                    onIntent = viewModel::onIntent
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
internal fun CommentDetailScreenContent(
    state: CommentDetailState,
    onNavigateBack: () -> Unit,
    onIntent: (CommentDetailIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateToReply: () -> Unit = {},
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit = { _, _ -> }
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    var headerHeight by rememberSaveableDp()
    // 입력바 실측 높이, 스낵바 앵커용
    var inputBarHeight by remember { mutableStateOf(0.dp) }

    // 자동재생은 중앙 영상 1개
    val videoItemKeys =
        remember(state.parentChain, state.parentComment, state.pendingReplies, state.replies) {
            buildSet<Any> {
                state.parentChain.forEach { p ->
                    if (p.mediaAttachments.any { it.type == MediaType.VIDEO }) add(
                        "parent_${p.id}"
                    )
                }
                if (state.parentComment?.mediaAttachments?.any { it.type == MediaType.VIDEO } == true) add(
                    "tapped_parent"
                )
                state.pendingReplies.forEach { r ->
                    if (r.mediaAttachments.any { it.type == MediaType.VIDEO }) add(
                        r.pendingId ?: r.id.toLong()
                    )
                }
                state.replies.forEach { r ->
                    val hasVideo = r.mediaAttachments.any { it.type == MediaType.VIDEO } ||
                            r.firstReply?.mediaAttachments?.any { it.type == MediaType.VIDEO } == true
                    if (hasVideo) add("reply_${r.id}")
                }
            }
        }
    val activeVideoItemKey by rememberAutoplayTarget(listState) { item -> item.key.takeIf { it in videoItemKeys } }

    // 다음 영상 2개를 미리 캐시
    val context = LocalContext.current
    val orderedVideoUrls =
        remember(state.parentChain, state.parentComment, state.pendingReplies, state.replies) {
            buildList<Pair<Any, String>> {
                state.parentChain.forEach { p ->
                    p.mediaAttachments.firstOrNull { it.type == MediaType.VIDEO }
                        ?.let { add("parent_${p.id}" to it.url) }
                }
                state.parentComment?.mediaAttachments?.firstOrNull { it.type == MediaType.VIDEO }
                    ?.let { add("tapped_parent" to it.url) }
                state.pendingReplies.forEach { r ->
                    r.mediaAttachments.firstOrNull { it.type == MediaType.VIDEO }
                        ?.let { add((r.pendingId ?: r.id.toLong()) to it.url) }
                }
                state.replies.forEach { r ->
                    val url = r.mediaAttachments.firstOrNull { it.type == MediaType.VIDEO }?.url
                        ?: r.firstReply?.mediaAttachments?.firstOrNull { it.type == MediaType.VIDEO }?.url
                    url?.let { add("reply_${r.id}" to it) }
                }
            }
        }
    LaunchedEffect(activeVideoItemKey, orderedVideoUrls) {
        val idx = orderedVideoUrls.indexOfFirst { it.first == activeVideoItemKey }
        val start = if (idx >= 0) idx + 1 else 0
        VideoPreloader.prefetch(context, orderedVideoUrls.drop(start).take(2).map { it.second })
    }

    // 잠금 글 답글: 비밀글 토스트, 티어 구독 시트
    val subscribeRequester = LocalSubscribeRequester.current
    val onReplyComposeClick: () -> Unit = {
        val restrictedPost = state.post?.takeIf { it.isRestricted }
        when {
            restrictedPost == null -> onNavigateToReply()
            restrictedPost.isSecret -> context.showToast("크리에이터만 댓글을 남길 수 있습니다")
            else -> subscribeRequester(
                restrictedPost.author.id,
                restrictedPost.author.displayName ?: restrictedPost.author.username
            )
        }
    }

    var hasAnchoredToParent by rememberSaveable { mutableStateOf(false) }
    var anchorBottomFiller by rememberSaveable { mutableFloatStateOf(0f) }
    LaunchedEffect(
        state.isParentLoading,
        state.parentComment,
        state.isPostLoading,
        state.isRepliesLoading,
        state.replies.size,
        state.parentChain.size,
        headerHeight
    ) {
        if (
            !hasAnchoredToParent &&
            !state.isParentLoading &&
            state.parentComment != null &&
            !state.isPostLoading &&
            !state.isRepliesLoading &&
            headerHeight > 0.dp
        ) {
            // 헤더·원글·조상 체인·탭한 댓글 순서
            val parentIndex = 2 + state.parentChain.size
            val anchorOffsetPx = with(density) { (headerHeight + 16.dp).roundToPx() }

            // 최신 콘텐츠가 레이아웃에 반영된 뒤 측정하도록 한 프레임 대기
            withFrameNanos { }
            val viewportPx =
                listState.layoutInfo.let { it.viewportEndOffset - it.viewportStartOffset }

            // 1) 하단 여백 먼저 확보
            anchorBottomFiller = with(density) { viewportPx.toDp().value }
            withTimeoutOrNull(1000) {
                snapshotFlow { listState.canScrollForward }.first { it }
            }

            // 2) 탭한 댓글을 헤더 아래로 앵커
            // offset이 2프레임 유지될 때까지 재보정
            var stableFrames = 0
            var attempts = 0
            while (attempts < 30 && stableFrames < 2) {
                listState.scrollToItem(parentIndex, -anchorOffsetPx)
                withFrameNanos { }
                val parentItem = listState.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.index == parentIndex }
                stableFrames = if (
                    parentItem != null &&
                    kotlin.math.abs(parentItem.offset - anchorOffsetPx) <= 2
                ) {
                    stableFrames + 1
                } else {
                    0
                }
                attempts++
            }

            // 3) 넘치는 여백 축소
            // 안 보이면 = 답글이 충분하니 0으로 되돌림
            val info = listState.layoutInfo
            val fillerItem =
                info.visibleItemsInfo.firstOrNull { it.index == info.totalItemsCount - 1 }
            val requiredFillerPx = if (fillerItem != null) {
                (info.viewportEndOffset - fillerItem.offset).coerceAtLeast(0)
            } else {
                0
            }
            anchorBottomFiller = with(density) { requiredFillerPx.toDp().value }

            hasAnchoredToParent = true
        }
    }

    // 알림 진입 하이라이트
    var anchorHighlightPlayed by rememberSaveable { mutableStateOf(false) }
    val anchorHighlight = remember { Animatable(0f) }
    LaunchedEffect(hasAnchoredToParent) {
        if (state.highlightAnchor && hasAnchoredToParent && !anchorHighlightPlayed) {
            anchorHighlightPlayed = true
            anchorHighlight.snapTo(1f)
            delay(400)
            anchorHighlight.animateTo(0f, animationSpec = tween(durationMillis = 1200))
        }
    }

    // REPLY 알림 진입
    var replyHighlightPlayed by rememberSaveable { mutableStateOf(false) }
    val replyHighlight = remember { Animatable(0f) }
    var replyHighlightItemIndex by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(hasAnchoredToParent, state.replies, state.pendingReplies.size) {
        val targetId = state.highlightReplyId ?: return@LaunchedEffect
        if (replyHighlightPlayed || !hasAnchoredToParent) return@LaunchedEffect
        val replyIndex = state.replies.indexOfFirst { it.id == targetId }
        if (replyIndex < 0) return@LaunchedEffect
        replyHighlightPlayed = true

        // 헤더·원글·조상·탭한 댓글·구분선 3·대기 답글 순서
        replyHighlightItemIndex =
            6 + state.parentChain.size + state.pendingReplies.size + replyIndex
    }
    LaunchedEffect(replyHighlightItemIndex) {
        val itemIndex = replyHighlightItemIndex ?: return@LaunchedEffect
        val anchorOffsetPx = with(density) { (headerHeight + 16.dp).roundToPx() }
        listState.animateScrollToItem(itemIndex, -anchorOffsetPx)
        // 위 아이템 늦은 실측 대비 안착까지 재보정
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
        replyHighlight.snapTo(1f)
        delay(400)
        replyHighlight.animateTo(0f, animationSpec = tween(durationMillis = 1200))
    }

    val profilePopupState = rememberProfilePopupHostState()

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { onIntent(CommentDetailIntent.SnackbarDismissed) }
    )

    HaloPullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onIntent(CommentDetailIntent.RefreshReplies) },
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

            // 원본 포스트 미리보기
            item {
                PostPreviewSection(
                    post = state.post,
                    unavailableReason = state.unavailablePost,
                    onPostClick = { onIntent(CommentDetailIntent.NavigateToPost) },
                    onProfileClick = { offset ->
                        state.post?.author?.let { author ->
                            if (state.myUserId == author.id) {
                                onIntent(CommentDetailIntent.NavigateToUserProfile(author.id))
                            } else {
                                profilePopupState.show(author.id, offset)
                            }
                        }
                    },
                    onLikeClick = {
                        state.post?.let { post ->
                            if (post.isLiked) {
                                onIntent(CommentDetailIntent.UnlikePost)
                            } else {
                                onIntent(CommentDetailIntent.LikePost)
                            }
                        }
                    },
                    onCommentClick = { onIntent(CommentDetailIntent.NavigateToPost) },
                    onMoreClick = {
                        onIntent(CommentDetailIntent.ShowPostOptions)
                    }
                )
            }

            // 조상 체인
            items(
                items = state.parentChain,
                key = { "parent_${it.id}" }
            ) { parent ->
                ParentCommentItem(
                    isPostAuthorReply = parent.parentCommentId != null && parent.author.id == state.post?.author?.id,
                    comment = parent,
                    onProfileClick = { offset ->
                        if (state.myUserId == parent.author.id) {
                            onIntent(CommentDetailIntent.NavigateToUserProfile(parent.author.id))
                        } else {
                            profilePopupState.show(parent.author.id, offset)
                        }
                    },
                    onCommentClick = {
                        onIntent(CommentDetailIntent.NavigateToCommentDetail(parent.id))
                    },
                    onMoreClick = {
                        onIntent(CommentDetailIntent.ShowCommentOptions(parent))
                    },
                    onLikeClick = {
                        if (parent.isLiked) onIntent(CommentDetailIntent.UnlikeReply(parent.id))
                        else onIntent(CommentDetailIntent.LikeReply(parent.id))
                    },
                    onMediaClick = onShowMediaViewer,
                    isVideoPlaying = activeVideoItemKey == "parent_${parent.id}"
                )
            }

            // 부모 댓글
            item(key = "tapped_parent") {
                if (state.isParentLoading) {
                    CommentItemSkeleton(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        showLine = false
                    )
                } else {
                    state.parentComment?.let { comment ->
                        // 알림 진입 하이라이트 배경
                        val highlightColor = MaterialTheme.colorScheme.primary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    val alpha = anchorHighlight.value * 0.04f
                                    if (alpha > 0f) drawRect(highlightColor.copy(alpha = alpha))
                                }
                        ) {
                            ParentCommentSection(
                                isPostAuthorReply = comment.parentCommentId != null && comment.author.id == state.post?.author?.id,
                                comment = comment,
                                onProfileClick = { offset ->
                                    if (state.myUserId == comment.author.id) {
                                        onIntent(CommentDetailIntent.NavigateToUserProfile(comment.author.id))
                                    } else {
                                        profilePopupState.show(comment.author.id, offset)
                                    }
                                },
                                onLikeClick = {
                                    if (comment.isLiked) {
                                        onIntent(CommentDetailIntent.UnlikeParentComment)
                                    } else {
                                        onIntent(CommentDetailIntent.LikeParentComment)
                                    }
                                },
                                onCommentClick = onReplyComposeClick,
                                onMoreClick = {
                                    onIntent(
                                        CommentDetailIntent.ShowCommentOptions(
                                            comment
                                        )
                                    )
                                },
                                onMediaClick = onShowMediaViewer,
                                isVideoPlaying = activeVideoItemKey == "tapped_parent",
                                connectTop = state.parentChain.isNotEmpty()
                            )
                        }
                    }
                }
            }

            // 답글
            val hasReplies = state.pendingReplies.isNotEmpty() || state.replies.isNotEmpty()

            // 답글이 있거나 로딩 중이면 구분선 + 정렬 표시
            if (hasReplies || state.isRepliesLoading) {
                // 구분선
                item {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                // 정렬 드롭다운
                item {
                    SortPopupMenu(
                        currentSort = state.repliesSortBy,
                        onSortChange = { onIntent(CommentDetailIntent.ChangeSortType(it)) }
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

            // 답글 로딩
            if (state.isRepliesLoading && !hasReplies) {
                items(3) {
                    CommentItemSkeleton(
                        modifier = Modifier.padding(top = 12.dp),
                        showLine = false
                    )
                }
            }

            // 업로드 중인 답글
            itemsIndexed(
                items = state.pendingReplies,
                key = { _, reply -> reply.pendingId ?: reply.id.toLong() }
            ) { index, reply ->
                Column {
                    if (index > 0 || state.replies.isNotEmpty()) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }

                    CommentItem(
                        postAuthorId = state.post?.author?.id,
                        comment = reply,
                        onProfileClick = { _, _ -> },
                        onLikeClick = {},
                        onReplyClick = {},
                        onMoreClick = {},
                        modifier = Modifier.padding(top = 12.dp),
                        isUploading = true,
                        onMediaClick = onShowMediaViewer,
                        isVideoPlaying = activeVideoItemKey == (reply.pendingId
                            ?: reply.id.toLong())
                    )
                }
            }

            // 답글 목록
            itemsIndexed(
                items = state.replies,
                key = { _, reply -> "reply_${reply.id}" }
            ) { replyIndex, reply ->
                // REPLY 알림 진입 하이라이트 배경
                val isHighlightTarget = reply.id == state.highlightReplyId
                val replyHighlightColor = MaterialTheme.colorScheme.primary
                Column(
                    modifier = if (isHighlightTarget) {
                        Modifier.drawBehind {
                            val alpha = replyHighlight.value * 0.04f
                            if (alpha > 0f) drawRect(replyHighlightColor.copy(alpha = alpha))
                        }
                    } else {
                        Modifier
                    }
                ) {
                    // 답글 사이 구분선
                    if (replyIndex > 0 || state.pendingReplies.isNotEmpty()) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }

                    // firstReply가 있으면 스레드로 연결
                    if (reply.firstReply != null) {
                        val threadLineColor =
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        val threadLineWidth = 1.5.dp
                        val threadAvatarSize = 32.dp
                        val threadReplyAvatarSize = 24.dp
                        val threadParentLineX = 8.dp + threadAvatarSize / 2
                        val threadCurveTopLead = 12.dp

                        // 부모 답글
                        CommentItem(
                            postAuthorId = state.post?.author?.id,
                            comment = reply,
                            onProfileClick = { userId, _ ->
                                onIntent(CommentDetailIntent.NavigateToUserProfile(userId))
                            },
                            onLikeClick = {
                                if (reply.isLiked) onIntent(CommentDetailIntent.UnlikeReply(it))
                                else onIntent(CommentDetailIntent.LikeReply(it))
                            },
                            onReplyClick = {
                                onIntent(CommentDetailIntent.NavigateToCommentDetail(it))
                            },
                            onCommentClick = {
                                onIntent(CommentDetailIntent.NavigateToCommentDetail(it))
                            },
                            onMoreClick = {
                                onIntent(CommentDetailIntent.ShowCommentOptions(reply))
                            },
                            modifier = Modifier.padding(top = 12.dp),
                            showLine = true,
                            lineBottomSpacing = 0.dp,
                            onMediaClick = onShowMediaViewer,
                            isVideoPlaying = activeVideoItemKey == "reply_${reply.id}"
                        )

                        // firstReply
                        ReplyCurveContainer(
                            parentLineX = threadParentLineX,
                            curveEndX = threadParentLineX + 18.dp,
                            avatarCenterY = threadCurveTopLead + threadReplyAvatarSize / 2,
                            topLead = threadCurveTopLead,
                            lineColor = threadLineColor,
                            lineWidth = threadLineWidth
                        ) {
                            CommentItem(
                                postAuthorId = state.post?.author?.id,
                                comment = reply.firstReply,
                                onProfileClick = { userId, _ ->
                                    onIntent(CommentDetailIntent.NavigateToUserProfile(userId))
                                },
                                onLikeClick = {
                                    if (reply.firstReply.isLiked) {
                                        onIntent(CommentDetailIntent.UnlikeReply(it))
                                    } else {
                                        onIntent(CommentDetailIntent.LikeReply(it))
                                    }
                                },
                                onReplyClick = {
                                    onIntent(CommentDetailIntent.NavigateToCommentDetail(it))
                                },
                                onCommentClick = {
                                    onIntent(CommentDetailIntent.NavigateToCommentDetail(it))
                                },
                                onMoreClick = {
                                    onIntent(CommentDetailIntent.ShowCommentOptions(reply.firstReply))
                                },
                                indentStart = threadAvatarSize + 12.dp,
                                avatarSize = threadReplyAvatarSize,
                                onMediaClick = onShowMediaViewer,
                                isVideoPlaying = activeVideoItemKey == "reply_${reply.id}"
                            )
                        }
                    } else {
                        // firstReply 없으면 일반 CommentItem
                        CommentItem(
                            postAuthorId = state.post?.author?.id,
                            comment = reply,
                            onProfileClick = { userId, _ ->
                                onIntent(CommentDetailIntent.NavigateToUserProfile(userId))
                            },
                            onLikeClick = {
                                if (reply.isLiked) {
                                    onIntent(CommentDetailIntent.UnlikeReply(it))
                                } else {
                                    onIntent(CommentDetailIntent.LikeReply(it))
                                }
                            },
                            onReplyClick = {
                                onIntent(CommentDetailIntent.NavigateToCommentDetail(it))
                            },
                            onCommentClick = {
                                onIntent(CommentDetailIntent.NavigateToCommentDetail(it))
                            },
                            onMoreClick = {
                                onIntent(CommentDetailIntent.ShowCommentOptions(reply))
                            },
                            modifier = Modifier.padding(top = 12.dp),
                            onMediaClick = onShowMediaViewer,
                            isVideoPlaying = activeVideoItemKey == "reply_${reply.id}"
                        )
                    }
                }
            }

            // 마지막 답글 뒤 구분선
            if (hasReplies) {
                item {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // 추가 로딩
            if (state.isRepliesLoading && hasReplies) {
                items(2) {
                    CommentItemSkeleton(
                        modifier = Modifier.padding(top = 12.dp),
                        showLine = false
                    )
                }
            }

            // 답글이 없는 경우
            if (!state.isRepliesLoading && !hasReplies && state.parentComment != null) {
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
                            text = "첫 번째 답글을 남겨보세요",
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

            // 앵커링용 보충 여백
            item {
                Spacer(modifier = Modifier.height(anchorBottomFiller.dp))
            }
        }

        InfiniteScrollTrigger(
            listState = listState,
            isLoading = state.isRepliesLoading,
            hasMore = state.repliesHasMore,
            onLoadMore = { onIntent(CommentDetailIntent.LoadMoreReplies) }
        )

        HazeScreenHeader(
            title = "답글 달기",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = onNavigateBack,
            onHeightMeasured = { headerHeight = it }
        )

        // 답글 입력창, 로드 성공 시에만
        val isPostObscured = state.post?.isBlocked == true || state.unavailablePost != null  // 못 보는 원글엔 답글 불가
        val isParentCommentObscured = state.parentComment?.let { it.isBlocked || it.isDeleted } == true  // 삭제 자리엔 답글 불가
        if (!state.isRepliesLoading && state.repliesError == null && state.parentComment != null && !isPostObscured && !isParentCommentObscured) {
            CommentInputBar(
                targetAuthorName = state.parentComment.author.displayName
                    ?: state.parentComment.author.username,
                replyTo = null,
                hazeState = hazeState,
                onInputClick = onReplyComposeClick,
                onCancelReply = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onGloballyPositioned { coordinates ->
                        inputBarHeight = with(density) { coordinates.size.height.toDp() }
                    },
                actionText = "답글"
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

    if (state.showCommentOptionsSheet && state.selectedComment != null) {
        val reportRequester = LocalReportRequester.current
        CommentOptionBottomSheet(
            isOwnComment = state.selectedComment.author.id == state.myUserId,
            isBookmarked = state.selectedComment.isBookmarked,
            onDismiss = { onIntent(CommentDetailIntent.DismissCommentOptions) },
            onDelete = { onIntent(CommentDetailIntent.DeleteComment) },
            onBookmark = { onIntent(CommentDetailIntent.BookmarkComment) },
            onBlock = { onIntent(CommentDetailIntent.BlockCommentAuthor) },
            onReport = { reportRequester(ReportTargetType.COMMENT, state.selectedComment.id) }
        )
    }

    if (state.showPostOptionsSheet && state.post != null) {
        val reportRequester = LocalReportRequester.current
        PostOptionBottomSheet(
            isOwnPost = state.post.author.id == state.myUserId,
            isBookmarked = state.post.isBookmarked,
            copyLink = state.post.takeUnless { it.isSecret }?.let { DeepLinks.postLink(it.id) },
            onDismiss = { onIntent(CommentDetailIntent.DismissPostOptions) },
            onEdit = { onIntent(CommentDetailIntent.EditPost) },
            onDelete = { /* CommentDetail에서는 삭제 불가 */ },
            onBookmark = { onIntent(CommentDetailIntent.BookmarkPost) },
            onNotInterested = { onIntent(CommentDetailIntent.HidePost) },
            onBlock = { onIntent(CommentDetailIntent.BlockPostAuthor) },
            onReport = { reportRequester(ReportTargetType.POST, state.post.id) },
            // 피드 고정은 크리에이터 피드의 내 글만
            canPin = state.post.author.id == state.myUserId && state.isCurrentUserCreator &&
                state.post.contextType == PostContextType.CREATOR_FEED,
            isPinned = state.post.isPinned,
            onPin = { onIntent(CommentDetailIntent.PinPost) }
        )
    }

    if (state.showBlockConfirmDialog && state.blockTargetUser != null) {
        ConfirmDialog(
            profileImageUrl = state.blockTargetUser.avatarUrl,
            title = "${state.blockTargetUser.displayName}님을 차단하시겠어요?",
            confirmText = "차단",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(CommentDetailIntent.ConfirmBlockUser) },
            onDismiss = { onIntent(CommentDetailIntent.DismissBlockConfirmDialog) }
        )
    }

    if (state.showBlockCompletedDialog && state.blockTargetUser != null) {
        AlertDialog(
            title = "${state.blockTargetUser.displayName}님이 차단되었습니다",
            onDismiss = { onIntent(CommentDetailIntent.DismissBlockCompletedDialog) }
        )
    }

    ProfilePopupHost(
        state = profilePopupState,
        resolveAuthor = { authorId ->
            val author = state.post?.author?.takeIf { it.id == authorId }
                ?: state.parentChain.find { it.author.id == authorId }?.author
                ?: state.parentComment?.author?.takeIf { it.id == authorId }
                ?: state.replies.find { it.author.id == authorId }?.author
                ?: state.replies.mapNotNull { it.firstReply }
                    .find { it.author.id == authorId }?.author
            author?.let { ProfilePopupData(it.id, it.isFollowing) }
        },
        onNavigateToProfile = { onIntent(CommentDetailIntent.NavigateToUserProfile(it)) },
        onFollow = { onIntent(CommentDetailIntent.FollowAuthor(it)) },
        onUnfollow = { onIntent(CommentDetailIntent.UnfollowAuthor(it)) }
    )
}
