package com.ninezero.halo.feature.feed.presentation.post_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.comment.CommentItem
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.UserPreview
import com.ninezero.halo.feature.feed.presentation.components.ReplyCurveContainer
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun PostCommentThreadItem(
    comment: Comment,
    expandedReplies: List<Comment>?,
    isLoadingReplies: Boolean,
    hasMoreReplies: Boolean,
    onExpandReplies: () -> Unit,
    onLoadMoreReplies: () -> Unit,
    onLikeClick: (Int) -> Unit,
    onReplyClick: (Int) -> Unit,
    onMoreClick: (Int) -> Unit,
    onProfileClick: (Int, IntOffset) -> Unit,
    onCommentClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isUploading: Boolean = false,
    postAuthorId: Int? = null,
    pendingReplies: List<Comment> = emptyList(),
    onMediaClick: (List<ViewerMediaItem>, Int) -> Unit = { _, _ -> },
    isVideoPlaying: Boolean = false
) {
    val horizontalPadding = 8.dp
    val avatarSize = 32.dp
    val replyAvatarSize = 24.dp
    val avatarSpacing = 12.dp
    val lineWidth = 1.5.dp
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    // 부모 댓글 세로선 x
    val parentLineX = horizontalPadding + avatarSize / 2
    // 대댓글 프사 좌측 = 부모 내용 시작점
    val replyStartX = horizontalPadding + avatarSize + avatarSpacing
    // 곡선 도착 x
    val replyCurveEndX = parentLineX + 18.dp
    // 대댓글 CommentItem 들여쓰기
    val replyIndent = replyStartX - horizontalPadding
    // 곡선 상단 여백
    val curveTopLead = 12.dp

    val hasReplies = comment.replyCount > 0
    val isExpanded = expandedReplies != null

    // 첫 답글이 작성자면 인라인 표시
    val firstReply = comment.firstReply
    val showAuthorReplyInline = !isExpanded && !comment.isBlocked &&
        firstReply != null && postAuthorId != null && firstReply.author.id == postAuthorId
    // 접혀 있고 남은 답글이 있으면 답글 보기
    val showViewReplies = !isExpanded && !comment.isBlocked && hasReplies &&
        !(showAuthorReplyInline && comment.replyCount <= 1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        // 메인 댓글
        CommentItem(
            authorName = comment.author.displayName ?: comment.author.username,
            authorRole = comment.author.role,
            authorAvatarUrl = comment.author.avatarThumbUrl ?: comment.author.avatarUrl,
            content = comment.content,
            createdAt = comment.createdAt,
            likeCount = comment.likeCount,
            isLiked = comment.isLiked,
            isBlocked = comment.isBlocked,
            isBlinded = comment.isBlinded,
            isDeleted = comment.isDeleted,
            showLine = hasReplies && !comment.isBlocked,
            mediaAttachments = comment.mediaAttachments,
            replyCount = comment.replyCount,
            horizontalPadding = horizontalPadding,
            avatarSize = avatarSize,
            avatarSpacing = avatarSpacing,
            lineWidth = lineWidth,
            lineColor = lineColor,
            // 곡선 커넥터가 바로 이어지도록 부모 선 하단 여백 제거
            lineBottomSpacing = 0.dp,
            isUploading = isUploading,
            onLikeClick = { onLikeClick(comment.id) },
            onReplyClick = { onReplyClick(comment.id) },
            onMoreClick = { onMoreClick(comment.id) },
            onProfileClick = { offset -> if (!comment.author.isDeleted) onProfileClick(comment.author.id, offset) },  // 탈퇴자는 프로필로 이동 안 함
            onCommentClick = if (isUploading) null else {
                { onCommentClick(comment.id) }
            },
            onMediaClick = { index ->
                onMediaClick(comment.mediaAttachments.toViewerItems(), index)
            },
            isVideoPlaying = isVideoPlaying
        )

        // 작성자 답글은 인라인 표시
        if (showAuthorReplyInline) {
            ReplyCurveContainer(
                parentLineX = parentLineX,
                curveEndX = replyCurveEndX,
                avatarCenterY = curveTopLead + replyAvatarSize / 2,
                topLead = curveTopLead,
                lineColor = lineColor,
                lineWidth = lineWidth
            ) {
                CommentItem(
                    postAuthorId = postAuthorId,
                    comment = firstReply,
                    onProfileClick = onProfileClick,
                    onLikeClick = onLikeClick,
                    onReplyClick = onReplyClick,
                    onMoreClick = onMoreClick,
                    onCommentClick = onCommentClick,
                    showLine = comment.replyCount > 1,
                    indentStart = replyIndent,
                    avatarSize = replyAvatarSize,
                    onMediaClick = onMediaClick,
                    isVideoPlaying = isVideoPlaying
                )
            }
        }

        // 답글 보기 버튼
        if (showViewReplies) {
            val previewAvatarSize = 20.dp
            val borderWidth = 2.dp
            val overlapOffset = 10.dp
            val backgroundColor = MaterialTheme.colorScheme.background
            // replyPreviewUsers는 첫 답글 제외
            // 첫 답글 미표시면 작성자를 앞에 끼움
            val previewUsers = if (showAuthorReplyInline) {
                comment.replyPreviewUsers.take(2)
            } else {
                val firstAuthorPreview = firstReply?.author?.let {
                    UserPreview(id = it.id, avatarUrl = it.avatarUrl, avatarThumbUrl = it.avatarThumbUrl)
                }
                (listOfNotNull(firstAuthorPreview) + comment.replyPreviewUsers).take(2)
            }
            // 겹친 프사 + 아이콘 너비
            val clusterItemCount = previewUsers.size + 1
            val clusterWidth = ((clusterItemCount - 1) * overlapOffset.value + previewAvatarSize.value).dp

            // 답글 보기 행
            val viewRepliesContent: @Composable () -> Unit = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onExpandReplies
                        )
                        .padding(start = replyStartX, end = horizontalPadding)
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 왼쪽: 스택형 프로필
                    Box(
                        modifier = Modifier
                            .width(clusterWidth)
                            .height(previewAvatarSize)
                    ) {
                        // 프로필 사진들 + 아이콘
                        previewUsers.forEachIndexed { index, user ->
                            AsyncImage(
                                model = user.avatarThumbUrl ?: user.avatarUrl,
                                contentDescription = "답글 작성자",
                                modifier = Modifier
                                    .offset(x = (index * overlapOffset.value).dp)
                                    .size(previewAvatarSize)
                                    .border(borderWidth, backgroundColor, CircleShape)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                                error = painterResource(R.drawable.ic_profile_placeholder),
                                fallback = painterResource(R.drawable.ic_profile_placeholder)
                            )
                        }

                        val iconOffsetX = (previewUsers.size * overlapOffset.value).dp
                        if (isLoadingReplies) {
                            Box(
                                modifier = Modifier
                                    .offset(x = iconOffsetX)
                                    .size(previewAvatarSize)
                                    .border(borderWidth, backgroundColor, CircleShape)
                                    .background(backgroundColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_down_circle),
                                contentDescription = "답글 보기",
                                modifier = Modifier
                                    .offset(x = iconOffsetX)
                                    .size(previewAvatarSize)
                                    .border(borderWidth, backgroundColor, CircleShape)
                                    .clip(CircleShape),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 답글 보기
                    Text(
                        text = "답글 보기",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 인라인 답글이 곡선을 쓰면 답글 보기는 정렬만
            if (showAuthorReplyInline) {
                viewRepliesContent()
            } else {
                ReplyCurveContainer(
                    parentLineX = parentLineX,
                    curveEndX = replyCurveEndX,
                    avatarCenterY = curveTopLead + previewAvatarSize / 2,
                    topLead = curveTopLead,
                    lineColor = lineColor,
                    lineWidth = lineWidth,
                    content = viewRepliesContent
                )
            }
        }

        // 확장된 답글 + 펜딩 답글
        if ((isExpanded && expandedReplies.isNotEmpty()) || pendingReplies.isNotEmpty()) {
            // 위에 답글이 하나라도 있으면 곡선은 이미 씀
            val firstReplyConsumed = !expandedReplies.isNullOrEmpty() ||
                showAuthorReplyInline || showViewReplies

            Column {
                expandedReplies?.forEachIndexed { index, reply ->
                    val isLast = index == (expandedReplies.size - 1) &&
                        !hasMoreReplies && pendingReplies.isEmpty()
                    val replyContent: @Composable () -> Unit = {
                        CommentItem(
                            isPostAuthorReply = reply.parentCommentId != null && reply.author.id == postAuthorId,
                            authorName = reply.author.displayName ?: reply.author.username,
                            authorRole = reply.author.role,
                            authorAvatarUrl = reply.author.avatarThumbUrl ?: reply.author.avatarUrl,
                            content = reply.content,
                            createdAt = reply.createdAt,
                            likeCount = reply.likeCount,
                            isLiked = reply.isLiked,
                            isBlocked = reply.isBlocked,
                            isBlinded = reply.isBlinded,
                            isDeleted = reply.isDeleted,
                            showLine = !isLast,
                            replyCount = reply.replyCount,
                            mediaAttachments = reply.mediaAttachments,
                            horizontalPadding = horizontalPadding,
                            indentStart = replyIndent,
                            avatarSize = replyAvatarSize,
                            avatarSpacing = avatarSpacing,
                            lineWidth = lineWidth,
                            lineColor = lineColor,
                            onLikeClick = { onLikeClick(reply.id) },
                            onReplyClick = { onReplyClick(reply.id) },
                            onMoreClick = { onMoreClick(reply.id) },
                            onProfileClick = { offset -> if (!reply.author.isDeleted) onProfileClick(reply.author.id, offset) },
                            onCommentClick = { onCommentClick(reply.id) },
                            onMediaClick = { idx ->
                                onMediaClick(reply.mediaAttachments.toViewerItems(), idx)
                            },
                            isVideoPlaying = isVideoPlaying
                        )
                    }
                    // 첫 대댓글만 곡선
                    if (index == 0) {
                        ReplyCurveContainer(
                            parentLineX = parentLineX,
                            curveEndX = replyCurveEndX,
                            avatarCenterY = curveTopLead + replyAvatarSize / 2,
                            topLead = curveTopLead,
                            lineColor = lineColor,
                            lineWidth = lineWidth,
                            content = replyContent
                        )
                    } else {
                        replyContent()
                    }
                }

                // 더 보기 버튼
                if (isExpanded && hasMoreReplies) {
                    val previewAvatarSize = 20.dp
                    val borderWidth = 2.dp
                    val backgroundColor = MaterialTheme.colorScheme.background

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onLoadMoreReplies
                            )
                            .padding(start = replyStartX, end = horizontalPadding)
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 왼쪽: 아이콘
                        Box(
                            modifier = Modifier
                                .width(replyAvatarSize)
                                .height(previewAvatarSize),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingReplies) {
                                Box(
                                    modifier = Modifier
                                        .size(previewAvatarSize)
                                        .border(borderWidth, backgroundColor, CircleShape)
                                        .background(backgroundColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_down_circle),
                                    contentDescription = "더 보기",
                                    modifier = Modifier
                                        .size(previewAvatarSize)
                                        .border(borderWidth, backgroundColor, CircleShape)
                                        .clip(CircleShape),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(avatarSpacing))

                        Text(
                            text = "답글 더 보기",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 업로드 중 펜딩 답글
                pendingReplies.forEachIndexed { index, reply ->
                    val pendingContent: @Composable () -> Unit = {
                        CommentItem(
                            postAuthorId = postAuthorId,
                            comment = reply,
                            onProfileClick = { _, _ -> },
                            onLikeClick = {},
                            onReplyClick = {},
                            onMoreClick = {},
                            showLine = index < pendingReplies.size - 1,
                            indentStart = replyIndent,
                            avatarSize = replyAvatarSize,
                            isUploading = true,
                            onMediaClick = onMediaClick,
                            isVideoPlaying = isVideoPlaying
                        )
                    }
                    // 위에 답글이 없는 첫 대기 답글만 곡선
                    if (index == 0 && !firstReplyConsumed) {
                        ReplyCurveContainer(
                            parentLineX = parentLineX,
                            curveEndX = replyCurveEndX,
                            avatarCenterY = curveTopLead + replyAvatarSize / 2,
                            topLead = curveTopLead,
                            lineColor = lineColor,
                            lineWidth = lineWidth,
                            content = pendingContent
                        )
                    } else {
                        pendingContent()
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostCommentThreadItemPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostCommentThreadItem(
                comment = Comment(
                    id = 1,
                    postId = 1,
                    author = Author(
                        id = 1,
                        username = "user1",
                        displayName = "사용자1",
                        avatarUrl = null
                    ),
                    content = "좋은 글이네요! 많이 배워갑니다.",
                    parentCommentId = null,
                    replyCount = 3,
                    likeCount = 5,
                    isLiked = false,
                    isBlocked = false,
                    firstReply = null,
                    replyPreviewUsers = listOf(
                        UserPreview(id = 2, avatarUrl = null),
                        UserPreview(id = 3, avatarUrl = null)
                    ),
                    createdAt = System.currentTimeMillis(),
                ),
                expandedReplies = null,
                isLoadingReplies = false,
                hasMoreReplies = false,
                onExpandReplies = {},
                onLoadMoreReplies = {},
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = { _, _ -> },
                onCommentClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostCommentThreadItemExpandedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostCommentThreadItem(
                comment = Comment(
                    id = 1,
                    postId = 1,
                    author = Author(
                        id = 1,
                        username = "user1",
                        displayName = "사용자1",
                        avatarUrl = null
                    ),
                    content = "좋은 글이네요! 많이 배워갑니다.",
                    parentCommentId = null,
                    replyCount = 2,
                    likeCount = 5,
                    isLiked = true,
                    isBlocked = false,
                    createdAt = System.currentTimeMillis(),
                ),
                expandedReplies = listOf(
                    Comment(
                        id = 2,
                        postId = 1,
                        author = Author(
                            id = 2,
                            username = "user2",
                            displayName = "사용자2",
                            avatarUrl = null
                        ),
                        content = "감사합니다! 더 좋은 글로 보답하겠습니다.",
                        parentCommentId = 1,
                        replyCount = 0,
                        likeCount = 2,
                        isLiked = false,
                        isBlocked = false,
                        createdAt = System.currentTimeMillis(),
                    ),
                    Comment(
                        id = 3,
                        postId = 1,
                        author = Author(
                            id = 3,
                            username = "user3",
                            displayName = "사용자3",
                            avatarUrl = null
                        ),
                        content = "저도 많이 배워가요!",
                        parentCommentId = 1,
                        replyCount = 0,
                        likeCount = 1,
                        isLiked = true,
                        isBlocked = false,
                        createdAt = System.currentTimeMillis(),
                    )
                ),
                isLoadingReplies = false,
                hasMoreReplies = true,
                onExpandReplies = {},
                onLoadMoreReplies = {},
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = { _, _ -> },
                onCommentClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostCommentThreadItemLoadingPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostCommentThreadItem(
                comment = Comment(
                    id = 1,
                    postId = 1,
                    author = Author(
                        id = 1,
                        username = "user1",
                        displayName = "사용자1",
                        avatarUrl = null
                    ),
                    content = "좋은 글이네요!",
                    parentCommentId = null,
                    replyCount = 3,
                    likeCount = 5,
                    isLiked = false,
                    isBlocked = false,
                    replyPreviewUsers = listOf(
                        UserPreview(id = 2, avatarUrl = null)
                    ),
                    createdAt = System.currentTimeMillis(),
                ),
                expandedReplies = null,
                isLoadingReplies = true,
                hasMoreReplies = false,
                onExpandReplies = {},
                onLoadMoreReplies = {},
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = { _, _ -> },
                onCommentClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostCommentThreadItemWithImagesPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostCommentThreadItem(
                comment = Comment(
                    id = 1,
                    postId = 1,
                    author = Author(
                        id = 1,
                        username = "user1",
                        displayName = "사용자1",
                        avatarUrl = null
                    ),
                    content = "이미지가 포함된 댓글입니다.",
                    mediaAttachments = listOf(
                        MediaItem(id = 1, type = MediaType.IMAGE, url = "https://picsum.photos/400/300", thumbnailUrl = null),
                        MediaItem(id = 2, type = MediaType.IMAGE, url = "https://picsum.photos/400/301", thumbnailUrl = null),
                        MediaItem(id = 3, type = MediaType.IMAGE, url = "https://picsum.photos/400/302", thumbnailUrl = null)
                    ),
                    parentCommentId = null,
                    replyCount = 2,
                    likeCount = 10,
                    isLiked = true,
                    isBlocked = false,
                    replyPreviewUsers = listOf(
                        UserPreview(id = 2, avatarUrl = null),
                        UserPreview(id = 3, avatarUrl = null)
                    ),
                    createdAt = System.currentTimeMillis(),
                ),
                expandedReplies = null,
                isLoadingReplies = false,
                hasMoreReplies = false,
                onExpandReplies = {},
                onLoadMoreReplies = {},
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = { _, _ -> },
                onCommentClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostCommentThreadItemWithVideoPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostCommentThreadItem(
                comment = Comment(
                    id = 1,
                    postId = 1,
                    author = Author(
                        id = 1,
                        username = "user1",
                        displayName = "사용자1",
                        avatarUrl = null
                    ),
                    content = "비디오가 포함된 댓글입니다.",
                    mediaAttachments = listOf(
                        MediaItem(id = 1, type = MediaType.VIDEO, url = "https://example.com/video.mp4", thumbnailUrl = null)
                    ),
                    parentCommentId = null,
                    replyCount = 1,
                    likeCount = 5,
                    isLiked = false,
                    isBlocked = false,
                    createdAt = System.currentTimeMillis(),
                ),
                expandedReplies = null,
                isLoadingReplies = false,
                hasMoreReplies = false,
                onExpandReplies = {},
                onLoadMoreReplies = {},
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = { _, _ -> },
                onCommentClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostCommentThreadItemBlockedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostCommentThreadItem(
                comment = Comment(
                    id = 1,
                    postId = 1,
                    author = Author(
                        id = 1,
                        username = "blocked_user",
                        displayName = "차단된 사용자",
                        avatarUrl = null
                    ),
                    content = "이 댓글은 차단된 사용자의 댓글입니다.",
                    parentCommentId = null,
                    replyCount = 0,
                    likeCount = 0,
                    isLiked = false,
                    isBlocked = true,
                    createdAt = System.currentTimeMillis(),
                ),
                expandedReplies = null,
                isLoadingReplies = false,
                hasMoreReplies = false,
                onExpandReplies = {},
                onLoadMoreReplies = {},
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = { _, _ -> },
                onCommentClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostCommentThreadItemNoRepliesPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostCommentThreadItem(
                comment = Comment(
                    id = 1,
                    postId = 1,
                    author = Author(
                        id = 1,
                        username = "user1",
                        displayName = "사용자1",
                        avatarUrl = null
                    ),
                    content = "아직 답글이 없는 댓글입니다.",
                    parentCommentId = null,
                    replyCount = 0,
                    likeCount = 2,
                    isLiked = true,
                    isBlocked = false,
                    createdAt = System.currentTimeMillis(),
                ),
                expandedReplies = null,
                isLoadingReplies = false,
                hasMoreReplies = false,
                onExpandReplies = {},
                onLoadMoreReplies = {},
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = { _, _ -> },
                onCommentClick = {}
            )
        }
    }
}
