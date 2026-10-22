package com.ninezero.halo.core.ui.components.comment

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
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
import com.ninezero.halo.core.ui.components.counter.AnimatedCounter
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.overlay.ObscuredOverlay
import com.ninezero.halo.core.ui.components.overlay.ObscuredOverlayShape
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.ui.theme.Like
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/** 댓글 아이템 */
@Composable
fun CommentItem(
    authorName: String,
    authorAvatarUrl: String?,
    content: String,
    createdAt: Long,
    likeCount: Int,
    isLiked: Boolean,
    isBlocked: Boolean,
    showLine: Boolean,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onMoreClick: () -> Unit,
    onProfileClick: (IntOffset) -> Unit,
    modifier: Modifier = Modifier,
    authorRole: String? = null,
    onCommentClick: (() -> Unit)? = null,
    mediaAttachments: List<MediaItem> = emptyList(),
    replyCount: Int? = null,
    replyToText: String? = null,
    isPostAuthorReply: Boolean = false,
    isDeleted: Boolean = false,
    isBlinded: Boolean = false,
    isUploading: Boolean = false,
    showReplyButton: Boolean = true,
    authorFontWeight: FontWeight = FontWeight.SemiBold,
    horizontalPadding: Dp = 8.dp,
    avatarSize: Dp = 32.dp,
    avatarSpacing: Dp = 12.dp,
    indentStart: Dp = 0.dp,  // 들여쓰기는 안에서 처리, 미디어는 풀폭 유지
    lineWidth: Dp = 1.5.dp,
    lineColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    lineBottomSpacing: Dp = 12.dp,
    onMediaClick: (index: Int) -> Unit = {},
    isVideoPlaying: Boolean = false,
    mediaOwnerKey: String? = null  // shared element owner 키, null이면 URL만
) {
    val hazeState = remember { HazeState() }
    var isBlindRevealed by rememberSaveable { mutableStateOf(false) }
    val obscuredByPolicy = isBlocked || isDeleted
    val isObscured = obscuredByPolicy || (isBlinded && !isBlindRevealed)
    val hasMedia = mediaAttachments.isNotEmpty() && !isObscured
    val contentStartPadding = horizontalPadding + indentStart + avatarSize + avatarSpacing

    val likeAction = if (isDeleted) ({}) else onLikeClick
    val replyAction = if (isDeleted) ({}) else onReplyClick

    var profilePosition by remember { mutableStateOf(IntOffset.Zero) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onCommentClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCommentClick
                    )
                } else Modifier
            )
    ) {
        // 텍스트 컨텐츠 섹션
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = horizontalPadding + indentStart, end = horizontalPadding)
                .height(IntrinsicSize.Min)
        ) {
            // 왼쪽: 프로필 + 연결선
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
                        model = authorAvatarUrl,
                        contentDescription = "댓글 작성자 프로필",
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
                                onClick = { onProfileClick(profilePosition) }
                            ),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_profile_placeholder),
                        error = painterResource(R.drawable.ic_profile_placeholder),
                        fallback = painterResource(R.drawable.ic_profile_placeholder)
                    )
                }

                // 연결선은 실제 스레드일 때만
                if (showLine) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(lineWidth)
                            .weight(1f)
                            .background(lineColor)
                    )
                }
            }

            Spacer(modifier = Modifier.width(avatarSpacing))

            // 오른쪽: 댓글 내용
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
                            if (isObscured || isBlinded) Modifier.hazeSource(state = hazeState) else Modifier
                        )
                ) {
                    // 작성자 정보 + 시간 + 더보기
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = authorName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = authorFontWeight,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        VerifiedBadge(role = authorRole, size = 14.dp)
                        Text(
                            text = " · ${createdAt.toRelativeTime()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isPostAuthorReply && !isBlocked && !isDeleted) PostAuthorBadge()
                        Spacer(modifier = Modifier.weight(1f))
                        // 펼친 블라인드 본문 다시 가리기
                        if (isBlinded && !isDeleted && isBlindRevealed) {
                            BlindHideButton(onClick = { isBlindRevealed = false })
                        }
                        if (!isBlocked && !isDeleted) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
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
                    }

                    // 답글 대상 표시
                    if (replyToText != null) {
                        Text(
                            text = replyToText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 이미지·버튼 없을 때만 버튼, 업로드 중 숨김
                    if (!hasMedia && !isUploading) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CommentActionButtons(
                            likeCount = likeCount,
                            isLiked = isLiked,
                            replyCount = replyCount,
                            showReplyButton = showReplyButton,
                            onLikeClick = likeAction,
                            onReplyClick = replyAction,
                            enabled = !isBlinded
                        )
                    }
                }

                // 차단/삭제/블라인드 댓글 오버레이
                if (isObscured) {
                    ObscuredOverlay(
                        hazeState = hazeState,
                        // 블라인드가 차단·삭제보다 우선, 본문 없는 블라인드 자리는 남의 댓글
                        icon = when {
                            isBlinded -> R.drawable.ic_warning
                            isBlocked -> R.drawable.ic_block
                            else -> R.drawable.ic_delete
                        },
                        label = when {
                            isBlinded && isDeleted -> "신고 누적으로 가려진 댓글입니다"
                            isBlinded -> "신고 누적으로 블라인드 처리되었습니다"
                            isBlocked -> "차단된 사용자입니다"
                            else -> "삭제된 댓글입니다"
                        },
                        color = if (isBlinded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        // 펼칠 본문 있는 작성자만 탭해서 펼침
                        onClick = if (isBlinded && !isDeleted) ({ isBlindRevealed = true }) else null
                    )
                }
            }
        }

        // 선 아래 여백
        if (!hasMedia && showLine && lineBottomSpacing > 0.dp) {
            Spacer(modifier = Modifier.height(lineBottomSpacing))
        }

        // 미디어 섹션
        if (hasMedia) {
            CommentMedia(
                mediaAttachments = mediaAttachments,
                startPadding = contentStartPadding,
                endPadding = horizontalPadding,
                showLine = showLine,
                linePadStart = horizontalPadding + indentStart + (avatarSize - lineWidth) / 2,
                lineWidth = lineWidth,
                lineColor = lineColor,
                onMediaClick = onMediaClick,
                isVideoPlaying = isVideoPlaying,
                isUploading = isUploading,
                mediaOwnerKey = mediaOwnerKey
            )

            // 버튼 섹션, 업로드 중 숨김
            if (!isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // 선
                    if (showLine) {
                        Box(
                            modifier = Modifier
                                .padding(start = horizontalPadding + indentStart + (avatarSize - lineWidth) / 2)
                                .width(lineWidth)
                                .fillMaxHeight()
                                .background(lineColor)
                        )
                    }

                    // 버튼
                    Row(
                        modifier = Modifier
                            .padding(start = contentStartPadding, end = horizontalPadding)
                            .padding(top = 8.dp, bottom = 8.dp)
                    ) {
                        CommentActionButtons(
                            likeCount = likeCount,
                            isLiked = isLiked,
                            replyCount = replyCount,
                            showReplyButton = showReplyButton,
                            onLikeClick = likeAction,
                            onReplyClick = replyAction,
                            enabled = !isBlinded
                        )
                    }
                }
            }

            if (showLine) {
                Spacer(modifier = Modifier.height(lineBottomSpacing))
            } else if (isUploading) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CommentActionButtons(
    likeCount: Int,
    isLiked: Boolean,
    replyCount: Int?,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    showReplyButton: Boolean = true,
    enabled: Boolean = true
) {
    val baseColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val likeColor = if (isLiked && enabled) Like else baseColor

    Row(
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
                    if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                ),
                contentDescription = "좋아요",
                modifier = Modifier
                    .size(20.dp)
                    .bounceClick(enabled = enabled),
                tint = likeColor
            )
            if (likeCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                AnimatedCounter(
                    count = likeCount,
                    style = MaterialTheme.typography.bodySmall,
                    color = likeColor
                )
            }
        }

        // 답글
        if (showReplyButton) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = enabled,
                    onClick = onReplyClick
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
                if (replyCount != null && replyCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    AnimatedCounter(
                        count = replyCount,
                        style = MaterialTheme.typography.bodySmall,
                        color = baseColor
                    )
                }
            }
        }
    }
}

@Composable
fun BlindHideButton(
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_eye_off),
            contentDescription = "다시 가리기",
            modifier = Modifier
                .size(18.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (showDivider) {
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .width(0.5.dp)
                    .height(14.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

/** Comment 모델 편의 오버로드 */
@Composable
fun CommentItem(
    comment: Comment,
    onProfileClick: (Int, IntOffset) -> Unit,
    onLikeClick: (Int) -> Unit,
    onReplyClick: (Int) -> Unit,
    onMoreClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onCommentClick: ((Int) -> Unit)? = null,
    showLine: Boolean = false,
    showReplyButton: Boolean = true,
    indentStart: Dp = 0.dp,
    avatarSize: Dp = 32.dp,
    lineBottomSpacing: Dp = 12.dp,
    isUploading: Boolean = false,
    onMediaClick: (List<ViewerMediaItem>, Int) -> Unit = { _, _ -> },
    isVideoPlaying: Boolean = false,
    postAuthorId: Int? = null
) {
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
        showLine = showLine,
        onLikeClick = { onLikeClick(comment.id) },
        onReplyClick = { onReplyClick(comment.id) },
        onMoreClick = { onMoreClick(comment.id) },
        onProfileClick = { offset -> if (!comment.author.isDeleted) onProfileClick(comment.author.id, offset) },  // 탈퇴자는 프로필로 이동 안 함
        modifier = modifier,
        onCommentClick = onCommentClick?.let { { it(comment.id) } },
        mediaAttachments = comment.mediaAttachments,
        replyCount = comment.replyCount,
        isPostAuthorReply = comment.parentCommentId != null && comment.author.id == postAuthorId,
        showReplyButton = showReplyButton,
        indentStart = indentStart,
        avatarSize = avatarSize,
        lineBottomSpacing = lineBottomSpacing,
        isUploading = isUploading,
        onMediaClick = { index -> onMediaClick(comment.mediaAttachments.toViewerItems(), index) },
        isVideoPlaying = isVideoPlaying
    )
}

@HaloLightDarkPreview
@Composable
private fun CommentItemPreview() {
    CommentItemPreviewContainer {
            CommentItem(
                authorName = "사용자1",
                authorAvatarUrl = null,
                content = "좋은 글이네요! 많이 배워갑니다.",
                createdAt = System.currentTimeMillis() - 3600000,
                likeCount = 5,
                isLiked = false,
                isBlocked = false,
                showLine = true,
                replyCount = 3,
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = {}
            )
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentItemWithReplyToPreview() {
    CommentItemPreviewContainer {
            CommentItem(
                authorName = "나",
                authorAvatarUrl = null,
                content = "감사합니다!",
                createdAt = System.currentTimeMillis() - 1800000,
                likeCount = 2,
                isLiked = true,
                isBlocked = false,
                showLine = false,
                replyCount = 0,
                replyToText = "사용자1님에게 남긴 답글",
                authorFontWeight = FontWeight.Bold,
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = {}
            )
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentItemBlockedPreview() {
    CommentItemPreviewContainer {
            CommentItem(
                authorName = "차단된 사용자",
                authorAvatarUrl = null,
                content = "차단된 내용입니다.",
                createdAt = System.currentTimeMillis() - 7200000,
                likeCount = 0,
                isLiked = false,
                isBlocked = true,
                showLine = true,
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = {}
            )
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentItemDeletedPreview() {
    CommentItemPreviewContainer {
            CommentItem(
                authorName = "삭제된 사용자",
                authorAvatarUrl = null,
                content = "삭제된 내용입니다.",
                createdAt = System.currentTimeMillis() - 7200000,
                likeCount = 0,
                isLiked = false,
                isBlocked = false,
                isDeleted = true,
                showLine = false,
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = {}
            )
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentItemNoLinePreview() {
    CommentItemPreviewContainer {
            CommentItem(
                authorName = "사용자1",
                authorAvatarUrl = null,
                content = "마지막 댓글입니다.",
                createdAt = System.currentTimeMillis() - 600000,
                likeCount = 1,
                isLiked = true,
                isBlocked = false,
                showLine = false,
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = {}
            )
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentItemWithImagesPreview() {
    CommentItemPreviewContainer {
            CommentItem(
                authorName = "사용자1",
                authorAvatarUrl = null,
                content = "이미지가 포함된 댓글입니다.",
                createdAt = System.currentTimeMillis() - 3600000,
                likeCount = 10,
                isLiked = true,
                isBlocked = false,
                showLine = true,
                replyCount = 2,
                mediaAttachments = listOf(
                    MediaItem(id = 1, type = MediaType.IMAGE, url = "https://picsum.photos/400/300", thumbnailUrl = null),
                    MediaItem(id = 2, type = MediaType.IMAGE, url = "https://picsum.photos/400/301", thumbnailUrl = null),
                    MediaItem(id = 3, type = MediaType.IMAGE, url = "https://picsum.photos/400/302", thumbnailUrl = null)
                ),
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = {}
            )
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentItemWithVideoPreview() {
    CommentItemPreviewContainer {
            CommentItem(
                authorName = "사용자1",
                authorAvatarUrl = null,
                content = "비디오가 포함된 댓글입니다.",
                createdAt = System.currentTimeMillis() - 3600000,
                likeCount = 5,
                isLiked = false,
                isBlocked = false,
                showLine = true,
                replyCount = 1,
                mediaAttachments = listOf(
                    MediaItem(id = 1, type = MediaType.VIDEO, url = "https://example.com/video.mp4", thumbnailUrl = null)
                ),
                onLikeClick = {},
                onReplyClick = {},
                onMoreClick = {},
                onProfileClick = {}
            )
    }
}

@Composable
private fun CommentItemPreviewContainer(
    content: @Composable () -> Unit
) {
    HaloSurfacePreview {
        content()
    }
}
