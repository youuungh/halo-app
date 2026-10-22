package com.ninezero.halo.feature.feed.presentation.comment_detail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.PostAuthorBadge
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.comment.BlindHideButton
import com.ninezero.halo.core.ui.components.comment.CommentMedia
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.overlay.ObscuredOverlay
import com.ninezero.halo.core.ui.components.overlay.ObscuredOverlayShape
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.Like
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun ParentCommentItem(
    comment: Comment,
    onProfileClick: (IntOffset) -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLikeClick: () -> Unit = {},
    onMoreClick: (() -> Unit)? = null,
    onMediaClick: (List<ViewerMediaItem>, Int) -> Unit = { _, _ -> },
    isVideoPlaying: Boolean = false,
    isPostAuthorReply: Boolean = false
) {
    val hazeState = remember { HazeState() }
    var isBlindRevealed by rememberSaveable { mutableStateOf(false) }
    // 차단·삭제 자리는 아바타·본문을 가림
    val obscuredByPolicy = comment.isBlocked || comment.isDeleted
    val isObscured = obscuredByPolicy || (comment.isBlinded && !isBlindRevealed)
    val hasMedia = comment.mediaAttachments.isNotEmpty() && !isObscured
    val likeAction = if (comment.isDeleted) ({}) else onLikeClick

    val horizontalPadding = 8.dp
    val avatarSize = 40.dp
    val avatarSpacing = 12.dp
    val lineWidth = 1.5.dp
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    var profilePosition by remember { mutableStateOf(IntOffset.Zero) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCommentClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .height(IntrinsicSize.Min)
        ) {
            // 프로필 + 연결선
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(avatarSize)
            ) {
                if (obscuredByPolicy) {
                    Image(
                        painter = painterResource(R.drawable.ic_profile_placeholder),
                        contentDescription = null,
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                } else {
                    AsyncImage(
                        model = comment.author.avatarThumbUrl ?: comment.author.avatarUrl,
                        contentDescription = "프로필",
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInWindow()
                                profilePosition = IntOffset(
                                    position.x.toInt(),
                                    position.y.toInt() + coordinates.size.height
                                )
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = !comment.author.isDeleted,  // 탈퇴자는 프로필로 이동 안 함
                                onClick = { onProfileClick(profilePosition) }
                            ),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_profile_placeholder),
                        error = painterResource(R.drawable.ic_profile_placeholder),
                        fallback = painterResource(R.drawable.ic_profile_placeholder)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 연결선
                Box(
                    modifier = Modifier
                        .width(lineWidth)
                        .weight(1f)
                        .background(lineColor)
                )
            }

            Spacer(modifier = Modifier.width(avatarSpacing))

            // 내용
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isObscured) Modifier.clip(ObscuredOverlayShape) else Modifier)
                        // 오버레이 꺼져도 블러 소스 유지
                        .then(
                            if (isObscured || comment.isBlinded) Modifier.hazeSource(state = hazeState) else Modifier
                        )
                ) {
                    // 이름 + 시간 + 더보기
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = comment.author.displayName ?: comment.author.username,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        VerifiedBadge(role = comment.author.role, size = 14.dp)
                        Text(
                            text = " · ${comment.createdAt.toRelativeTime()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isPostAuthorReply && !obscuredByPolicy) PostAuthorBadge()
                        Spacer(modifier = Modifier.weight(1f))
                        // 펼친 블라인드 본문 다시 가리기
                        if (comment.isBlinded && !comment.isDeleted && isBlindRevealed) {
                            BlindHideButton(
                                onClick = { isBlindRevealed = false },
                                showDivider = onMoreClick != null && !obscuredByPolicy
                            )
                        }
                        if (onMoreClick != null && !obscuredByPolicy) {
                            Icon(
                                painter = painterResource(R.drawable.ic_ellipsis_horizontal),
                                contentDescription = "더보기",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onMoreClick
                                    ),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 댓글 내용
                    if (comment.content.isNotBlank()) {
                        Text(
                            text = comment.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // 좋아요·답글 버튼
                    if (!hasMedia) {
                        ParentReplyButtons(
                            comment = comment,
                            onLikeClick = likeAction,
                            onCommentClick = onCommentClick,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }

                // 차단·삭제·블라인드 자리 오버레이
                if (isObscured) {
                    val isBlindedPlaceholder = comment.isBlinded && comment.isDeleted
                    ObscuredOverlay(
                        hazeState = hazeState,
                        icon = when {
                            comment.isBlinded -> R.drawable.ic_warning
                            comment.isDeleted -> R.drawable.ic_delete
                            else -> R.drawable.ic_block
                        },
                        label = when {
                            isBlindedPlaceholder -> "신고 누적으로 가려진 댓글입니다"
                            comment.isBlinded -> "신고 누적으로 블라인드 처리되었습니다"
                            comment.isDeleted -> "삭제된 댓글입니다"
                            else -> "차단된 사용자입니다"
                        },
                        contentDescription = when {
                            comment.isBlinded -> "가려짐"
                            comment.isDeleted -> "삭제됨"
                            else -> "차단됨"
                        },
                        color = if (comment.isBlinded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        // 펼칠 본문 있는 작성자만 탭해서 펼침
                        onClick = if (comment.isBlinded && !comment.isDeleted) ({ isBlindRevealed = true }) else null
                    )
                }
            }
        }

        // 미디어
        if (hasMedia) {
            CommentMedia(
                mediaAttachments = comment.mediaAttachments,
                startPadding = horizontalPadding + avatarSize + avatarSpacing,
                endPadding = horizontalPadding,
                showLine = true,
                linePadStart = horizontalPadding + (avatarSize - lineWidth) / 2,
                lineWidth = lineWidth,
                lineColor = lineColor,
                onMediaClick = { index -> onMediaClick(comment.mediaAttachments.toViewerItems(), index) },
                isVideoPlaying = isVideoPlaying
            )
            // 좋아요·답글 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // 선
                Box(
                    modifier = Modifier
                        .padding(start = horizontalPadding + (avatarSize - lineWidth) / 2)
                        .width(lineWidth)
                        .fillMaxHeight()
                        .background(lineColor)
                )

                // 버튼
                ParentReplyButtons(
                    comment = comment,
                    onLikeClick = onLikeClick,
                    onCommentClick = onCommentClick,
                    modifier = Modifier.padding(
                        start = horizontalPadding + avatarSize + avatarSpacing,
                        top = 8.dp,
                        bottom = 8.dp,
                        end = horizontalPadding
                    )
                )
            }
        }

        // 프로필 아래 여백
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ParentReplyButtons(
    comment: Comment,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = !comment.isBlinded
    val baseColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val likeColor = if (comment.isLiked && enabled) Like else baseColor

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 좋아요
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onLikeClick
            )
        ) {
            Icon(
                painter = painterResource(
                    if (comment.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                ),
                contentDescription = "좋아요",
                modifier = Modifier
                    .size(20.dp)
                    .bounceClick(enabled = enabled),
                tint = likeColor
            )
            if (comment.likeCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = comment.likeCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = likeColor
                )
            }
        }

        // 답글
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onCommentClick
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_comment),
                contentDescription = "답글",
                modifier = Modifier
                    .size(20.dp)
                    .bounceClick(enabled = enabled),
                tint = baseColor
            )
            if (comment.replyCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = comment.replyCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = baseColor
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ParentCommentItemPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ParentCommentItem(
                    comment = Comment(
                        id = 1,
                        postId = 1,
                        author = Author(
                            id = 1,
                            username = "user1",
                            displayName = "사용자1",
                            avatarUrl = null
                        ),
                        content = "첫 번째 댓글입니다. 좋은 글이네요!",
                        parentCommentId = null,
                        replyCount = 3,
                        likeCount = 12,
                        isLiked = true,
                        isBlocked = false,
                        createdAt = System.currentTimeMillis() - 3600000,
                    ),
                    onProfileClick = {},
                    onCommentClick = {}
                )

                ParentCommentItem(
                    comment = Comment(
                        id = 2,
                        postId = 1,
                        author = Author(
                            id = 2,
                            username = "user2",
                            displayName = "사용자2",
                            avatarUrl = null
                        ),
                        content = "답글 달아봅니다!",
                        parentCommentId = 1,
                        replyCount = 1,
                        likeCount = 5,
                        isLiked = false,
                        isBlocked = false,
                        createdAt = System.currentTimeMillis() - 1800000,
                    ),
                    onProfileClick = {},
                    onCommentClick = {},
                    onMoreClick = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ParentCommentItemBlockedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ParentCommentItem(
                    comment = Comment(
                        id = 1,
                        postId = 1,
                        author = Author(
                            id = 1,
                            username = "blocked_user",
                            displayName = "차단된 사용자",
                            avatarUrl = null
                        ),
                        content = "차단된 댓글 내용입니다.",
                        parentCommentId = null,
                        replyCount = 2,
                        likeCount = 5,
                        isLiked = false,
                        isBlocked = true,
                        createdAt = System.currentTimeMillis() - 3600000,
                    ),
                    onProfileClick = {},
                    onCommentClick = {},
                    onMoreClick = {}
                )

                ParentCommentItem(
                    comment = Comment(
                        id = 2,
                        postId = 1,
                        author = Author(
                            id = 2,
                            username = "user2",
                            displayName = "일반 사용자",
                            avatarUrl = null
                        ),
                        content = "일반 댓글입니다.",
                        parentCommentId = 1,
                        replyCount = 0,
                        likeCount = 3,
                        isLiked = true,
                        isBlocked = false,
                        createdAt = System.currentTimeMillis() - 1800000,
                    ),
                    onProfileClick = {},
                    onCommentClick = {},
                    onMoreClick = {}
                )
            }
        }
    }
}
