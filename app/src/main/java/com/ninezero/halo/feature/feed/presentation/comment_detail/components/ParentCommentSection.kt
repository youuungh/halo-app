package com.ninezero.halo.feature.feed.presentation.comment_detail.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.PostAuthorBadge
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.comment.BlindHideButton
import com.ninezero.halo.core.ui.components.comment.CommentMedia
import com.ninezero.halo.core.ui.components.counter.AnimatedCounter
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.overlay.ObscuredOverlay
import com.ninezero.halo.core.ui.components.overlay.ObscuredOverlayShape
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewComment
import com.ninezero.halo.core.ui.preview.previewMediaItem
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.Like
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun ParentCommentSection(
    comment: Comment,
    onProfileClick: (IntOffset) -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onMoreClick: () -> Unit,
    onMediaClick: (List<ViewerMediaItem>, Int) -> Unit = { _, _ -> },
    isVideoPlaying: Boolean = false,
    connectTop: Boolean = false,
    isPostAuthorReply: Boolean = false
) {
    val horizontalPadding = 8.dp
    val avatarSize = 40.dp
    val avatarSpacing = 12.dp
    val lineWidth = 1.5.dp
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val isBlocked = comment.isBlocked

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            // 연결선
            .then(
                if (connectTop) Modifier.drawBehind {
                    val x = (avatarSize / 2).toPx()
                    drawLine(
                        color = lineColor,
                        start = Offset(x, 0f),
                        end = Offset(x, (avatarSize / 2).toPx()),
                        strokeWidth = lineWidth.toPx()
                    )
                } else Modifier
            )
    ) {
        if (isBlocked || comment.isDeleted) {
            // 삭제 자리는 좋아요·답글 불가
            ObscuredParentContent(
                comment = comment,
                avatarSize = avatarSize,
                avatarSpacing = avatarSpacing,
                onLikeClick = if (comment.isDeleted) ({}) else onLikeClick,
                onCommentClick = if (comment.isDeleted) ({}) else onCommentClick
            )
        } else {
            // 상단: 프로필 + 이름 + 시간 + 더보기
            var profilePosition by remember { mutableStateOf(IntOffset.Zero) }
            var isBlindRevealed by rememberSaveable { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Spacer(modifier = Modifier.width(avatarSpacing))

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
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
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    if (isPostAuthorReply) PostAuthorBadge()
                }

                // 펼친 블라인드 본문 다시 가리기
                if (comment.isBlinded && isBlindRevealed) {
                    BlindHideButton(onClick = { isBlindRevealed = false })
                }

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

            Spacer(modifier = Modifier.height(12.dp))

            // 본문·미디어·버튼, 본인 블라인드 댓글은 가림
            val hazeState = remember { HazeState() }
            val isBlindObscured = comment.isBlinded && !isBlindRevealed
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isBlindObscured) Modifier.clip(ObscuredOverlayShape) else Modifier)
                        // 오버레이 꺼져도 블러 소스 유지
                        .then(if (comment.isBlinded) Modifier.hazeSource(state = hazeState) else Modifier)
                ) {
                    // 본문
                    Text(
                        text = comment.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 미디어
                    if (comment.mediaAttachments.isNotEmpty() && !isBlindObscured) {
                        CommentMedia(
                            mediaAttachments = comment.mediaAttachments,
                            startPadding = 0.dp,
                            endPadding = 0.dp,
                            onMediaClick = { index -> onMediaClick(comment.mediaAttachments.toViewerItems(), index) },
                            isVideoPlaying = isVideoPlaying
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 액션 버튼
                    ParentSectionActionButtons(
                        comment = comment,
                        onLikeClick = onLikeClick,
                        onCommentClick = onCommentClick
                    )
                }

                if (isBlindObscured) {
                    ObscuredOverlay(
                        hazeState = hazeState,
                        icon = R.drawable.ic_warning,
                        label = "신고 누적으로 블라인드 처리되었습니다",
                        contentDescription = "가려짐",
                        color = MaterialTheme.colorScheme.error,
                        onClick = { isBlindRevealed = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ObscuredParentContent(
    comment: Comment,
    avatarSize: Dp,
    avatarSpacing: Dp,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    val hazeState = remember { HazeState() }

    Row(modifier = Modifier.fillMaxWidth()) {
        // 아바타 자리표시
        Image(
            painter = painterResource(R.drawable.ic_profile_placeholder),
            contentDescription = null,
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.width(avatarSpacing))

        // 우측: 본문 영역만 블러 + 오버레이
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ObscuredOverlayShape)
                    .hazeSource(state = hazeState)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                ParentSectionActionButtons(
                    comment = comment,
                    onLikeClick = onLikeClick,
                    onCommentClick = onCommentClick
                )
            }

            // 차단·삭제·블라인드 자리 오버레이
            val isBlindedPlaceholder = comment.isBlinded && comment.isDeleted
            ObscuredOverlay(
                hazeState = hazeState,
                icon = when {
                    isBlindedPlaceholder -> R.drawable.ic_warning
                    comment.isDeleted -> R.drawable.ic_delete
                    else -> R.drawable.ic_block
                },
                label = when {
                    isBlindedPlaceholder -> "신고 누적으로 가려진 댓글입니다"
                    comment.isDeleted -> "삭제된 댓글입니다"
                    else -> "차단된 사용자입니다"
                },
                contentDescription = when {
                    isBlindedPlaceholder -> "가려짐"
                    comment.isDeleted -> "삭제됨"
                    else -> "차단됨"
                },
                color = if (isBlindedPlaceholder) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ParentSectionActionButtons(
    comment: Comment,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    val enabled = !comment.isBlinded
    val baseColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val likeColor = if (comment.isLiked && enabled) Like else baseColor

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 좋아요 버튼
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
                AnimatedCounter(
                    count = comment.likeCount,
                    style = MaterialTheme.typography.bodySmall,
                    color = likeColor
                )
            }
        }

        // 댓글 버튼
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
                AnimatedCounter(
                    count = comment.replyCount,
                    style = MaterialTheme.typography.bodySmall,
                    color = baseColor
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ParentCommentSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ParentCommentSection(
                comment = previewComment(
                    content = "부모 댓글입니다. 이 아래로 답글 스레드가 이어집니다.",
                    replyCount = 5,
                    likeCount = 12,
                    isLiked = true,
                    createdAtOffset = 3_600_000
                ),
                onProfileClick = {},
                onLikeClick = {},
                onCommentClick = {},
                onMoreClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ParentCommentSectionConnectedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ParentCommentSection(
                comment = previewComment(
                    content = "조상 댓글이 있는 경우입니다. 위쪽으로 연결선이 그려집니다.",
                    parentCommentId = 1,
                    replyCount = 2,
                    likeCount = 3,
                    createdAtOffset = 1_800_000
                ),
                onProfileClick = {},
                onLikeClick = {},
                onCommentClick = {},
                onMoreClick = {},
                connectTop = true
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ParentCommentSectionWithMediaPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ParentCommentSection(
                comment = previewComment(
                    content = "이미지가 첨부된 부모 댓글입니다.",
                    mediaAttachments = listOf(
                        previewMediaItem(1, url = "https://picsum.photos/400/300"),
                        previewMediaItem(2, url = "https://picsum.photos/400/301")
                    ),
                    replyCount = 1,
                    likeCount = 8,
                    createdAtOffset = 7_200_000
                ),
                onProfileClick = {},
                onLikeClick = {},
                onCommentClick = {},
                onMoreClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ParentCommentSectionBlockedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ParentCommentSection(
                comment = previewComment(
                    content = "차단된 사용자의 댓글 내용입니다.",
                    isBlocked = true,
                    replyCount = 2,
                    likeCount = 5,
                    createdAtOffset = 3_600_000
                ),
                onProfileClick = {},
                onLikeClick = {},
                onCommentClick = {},
                onMoreClick = {}
            )
        }
    }
}
