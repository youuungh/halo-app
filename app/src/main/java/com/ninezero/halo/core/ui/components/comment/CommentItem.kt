package com.ninezero.halo.core.ui.components.comment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.PostAuthorBadge
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.counter.AnimatedCounter
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.overlay.ObscuredOverlay
import com.ninezero.halo.core.ui.components.overlay.ObscuredOverlayLabelColor
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
    // 블라인드는 아바타 유지, 본문만 토글
    // 차단·삭제는 아바타까지 가림
    var isBlindRevealed by rememberSaveable { mutableStateOf(false) }
    val obscuredByPolicy = isBlocked || isDeleted
    val isObscured = obscuredByPolicy || (isBlinded && !isBlindRevealed)
    val hasMedia = mediaAttachments.isNotEmpty() && !isObscured
    val contentStartPadding = horizontalPadding + indentStart + avatarSize + avatarSpacing
    // 삭제 자리는 좋아요·답글 불가
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
                            onReplyClick = replyAction
                        )
                    }
                }

                // 차단/삭제/블라인드 댓글 오버레이
                if (isObscured) {
                    ObscuredOverlay(
                        hazeState = hazeState,
                        // 블라인드가 차단·삭제보다 우선
                        icon = when {
                            isBlinded -> R.drawable.ic_warning
                            isBlocked -> R.drawable.ic_block
                            else -> R.drawable.ic_delete
                        },
                        label = when {
                            isBlinded -> "신고 누적으로 블라인드 처리되었습니다"
                            isBlocked -> "차단된 사용자입니다"
                            else -> "삭제된 댓글입니다"
                        },
                        // 기본 흰색, 블라인드만 경고색
                        color = if (isBlinded) MaterialTheme.colorScheme.error else ObscuredOverlayLabelColor
                    )
                }

                // 오버레이보다 뒤에 그림
                // 오버레이 밖이라 다시 접기 가능
                if (isBlinded) {
                    BlindRevealToggle(
                        revealed = isBlindRevealed,
                        onToggle = { isBlindRevealed = !isBlindRevealed },
                        hazeState = hazeState,
                        modifier = Modifier.align(Alignment.BottomEnd)
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
                            onReplyClick = replyAction
                        )
                    }
                }
            }

            // 하단 여백, 곡선 커넥터 연결
            Spacer(modifier = Modifier.height(if (showLine) lineBottomSpacing else 12.dp))
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
    showReplyButton: Boolean = true
) {
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
                    .bounceClick(),
                tint = if (isLiked) Like else MaterialTheme.colorScheme.onSurface
            )
            if (likeCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                AnimatedCounter(
                    count = likeCount,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLiked) Like else MaterialTheme.colorScheme.onSurface
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
                    onClick = onReplyClick
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_comment),
                    contentDescription = "답글",
                    modifier = Modifier
                        .size(20.dp)
                        .bounceClick(),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                if (replyCount != null && replyCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    AnimatedCounter(
                        count = replyCount,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/** 블라인드 댓글 본문 펼치기 버튼 */
@Composable
private fun BlindRevealToggle(
    revealed: Boolean,
    onToggle: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface

    Row(
        modifier = modifier
            .padding(end = 6.dp, bottom = 6.dp)
            .clip(CircleShape)
            // 오버레이보다 옅은 틴트
            .haloBlur(state = hazeState) {
                style = HaloBlurStyle(
                    backgroundColor = surfaceColor,
                    tint = HaloBlurTint(Color.Black.copy(alpha = 0.25f)),
                    blurRadius = 14.dp,
                    noiseFactor = 0f
                )
            }
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .bounceClick()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            painter = painterResource(if (revealed) R.drawable.ic_eye_off else R.drawable.ic_eye),
            contentDescription = null,
            modifier = Modifier.size(11.dp),
            tint = ObscuredOverlayLabelColor
        )
        Text(
            text = if (revealed) "숨기기" else "보이기",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Medium,
            color = ObscuredOverlayLabelColor
        )
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
