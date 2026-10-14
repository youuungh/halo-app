package com.ninezero.halo.feature.feed.presentation.comment_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
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
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.counter.AnimatedCounter
import com.ninezero.halo.core.ui.components.media.MediaDisplayPolicy
import com.ninezero.halo.core.ui.components.media.clampedAspectRatio
import com.ninezero.halo.core.ui.components.media.preferredAspectRatio
import com.ninezero.halo.core.ui.components.media.singleMediaHeightForWidth
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.overlay.ObscuredOverlay
import com.ninezero.halo.core.ui.components.text.buildHashtagAnnotatedString
import com.ninezero.halo.core.ui.components.video_player.VideoPlayer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.presentation.UnavailablePostReason
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.Like
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun PostPreviewSection(
    post: Post?,
    unavailableReason: UnavailablePostReason?,
    onPostClick: () -> Unit,
    onProfileClick: (IntOffset) -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val horizontalPadding = 8.dp
    val avatarSize = 40.dp
    val avatarSpacing = 12.dp
    val contentStartPadding = horizontalPadding + avatarSize + avatarSpacing  // 8 + 40 + 12 = 60dp
    val lineWidth = 1.5.dp
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val hasPostImages = post?.mediaAttachments?.isNotEmpty() == true

    val hazeState = remember { HazeState() }
    val isObscured = post?.isBlocked == true
    val isRestricted = !isObscured && post?.isRestricted == true
    val isMasked = isObscured || isRestricted

    var profilePosition by remember { mutableStateOf(IntOffset.Zero) }

    // 볼 수 없어 못 받은 원글 자리
    if (post == null && unavailableReason != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .height(IntrinsicSize.Min)
            ) {
                AvatarWithLine(avatarSize, lineWidth, lineColor) {
                    AvatarPlaceholder(avatarSize)
                }

                Spacer(modifier = Modifier.width(avatarSpacing))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp)
                            .hazeSource(state = hazeState)
                    )
                    ObscuredOverlay(
                        hazeState = hazeState,
                        icon = when (unavailableReason) {
                            UnavailablePostReason.NOT_FOUND -> R.drawable.ic_eye_off
                            UnavailablePostReason.BLOCKED -> R.drawable.ic_block
                        },
                        label = when (unavailableReason) {
                            UnavailablePostReason.NOT_FOUND -> "볼 수 없는 포스트입니다"
                            UnavailablePostReason.BLOCKED -> "차단된 사용자입니다"
                        }
                    )
                }
            }

            // 댓글 아바타 위 여백
            Spacer(modifier = Modifier.height(12.dp))
        }
        return
    }

    AnimatedVisibility(
        visible = post != null,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        val currentPost = post ?: return@AnimatedVisibility

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !isObscured,
                    onClick = onPostClick
                )
                .padding(top = 12.dp)
        ) {
            // 포스트 작성자 + 내용
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .height(IntrinsicSize.Min)
            ) {
                // 포스트 작성자 프로필 + 연결선 시작
                AvatarWithLine(avatarSize, lineWidth, lineColor) {
                    if (isObscured) {
                        // 차단/관심없음 시 아바타 자리표시
                        AvatarPlaceholder(avatarSize)
                    } else {
                        AsyncImage(
                            model = currentPost.author.avatarThumbUrl ?: currentPost.author.avatarUrl,
                            contentDescription = "포스트 작성자 프로필",
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
                                    enabled = !currentPost.author.isDeleted,  // 탈퇴자는 프로필로 이동 안 함
                                    onClick = { onProfileClick(profilePosition) }
                                ),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.ic_profile_placeholder),
                            error = painterResource(R.drawable.ic_profile_placeholder),
                            fallback = painterResource(R.drawable.ic_profile_placeholder)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(avatarSpacing))

                // 포스트 정보
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp)
                ) {
                    // 콘텐츠
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isRestricted) Modifier.heightIn(min = 80.dp) else Modifier)
                            .then(if (isMasked) Modifier.hazeSource(state = hazeState) else Modifier)
                    ) {
                        // 포스트 작성자 이름 + 시간 + 더보기
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentPost.author.displayName ?: currentPost.author.username,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            VerifiedBadge(role = currentPost.author.role, size = 14.dp)
                            Text(
                                text = " · ${currentPost.createdAt.toRelativeTime()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            if (!isRestricted) {
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

                        // 본문
                        if (currentPost.content.isNotBlank()) {
                            val tagColor = MaterialTheme.colorScheme.onSurfaceVariant
                            val annotatedContent = remember(currentPost.content, tagColor) {
                                buildHashtagAnnotatedString(currentPost.content, tagColor)
                            }
                            Text(
                                text = annotatedContent,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // 좋아요·댓글 버튼, 이미지 있으면 숨김
                        if (!(hasPostImages && !isObscured) && !isRestricted) Row(
                            modifier = Modifier.padding(top = 12.dp),
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
                                        if (currentPost.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                                    ),
                                    contentDescription = "좋아요",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .bounceClick(),
                                    tint = if (currentPost.isLiked) {
                                        Like
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (currentPost.likeCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    AnimatedCounter(
                                        count = currentPost.likeCount,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (currentPost.isLiked) Like else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // 댓글
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onCommentClick
                                )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_comment),
                                    contentDescription = "댓글",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .bounceClick(),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                if (currentPost.commentCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    AnimatedCounter(
                                        count = currentPost.commentCount,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // 차단/잠금 오버레이
                    if (isMasked) {
                        val overlayIcon = when {
                            currentPost.isBlocked -> R.drawable.ic_block
                            else -> R.drawable.ic_lock
                        }
                        val overlayText = when {
                            currentPost.isBlocked -> "차단된 사용자입니다"
                            currentPost.isSecret -> "비밀글"
                            else -> "${currentPost.requiredTier} 이상 멤버십 전용"
                        }
                        ObscuredOverlay(
                            hazeState = hazeState,
                            icon = overlayIcon,
                            label = overlayText
                        )
                    }
                }
            }

            // 이미지 영역, 잠금 글은 숨김
            if (hasPostImages && !isMasked) {
                val mediaItems = currentPost.mediaAttachments
                val isSingleMedia = mediaItems.size == 1
                val imagePadding = 4.dp
                val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
                if (isSingleMedia) {
                    val media = mediaItems.first()
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        val availableWidth = maxWidth - contentStartPadding - horizontalPadding
                        val singleMinRatio = if (media.type == MediaType.VIDEO) {
                            MediaDisplayPolicy.FEED_SINGLE_VIDEO_MIN_RATIO
                        } else {
                            MediaDisplayPolicy.FEED_SINGLE_MIN_RATIO
                        }
                        val singleMediaHeight = singleMediaHeightForWidth(
                            availableWidth = availableWidth,
                            media = media,
                            minRatio = singleMinRatio,
                            maxRatio = MediaDisplayPolicy.FEED_SINGLE_MAX_RATIO
                        )
                        val clampedRatio = clampedAspectRatio(
                            aspectRatio = media.preferredAspectRatio(),
                            minRatio = singleMinRatio,
                            maxRatio = MediaDisplayPolicy.FEED_SINGLE_MAX_RATIO
                        )
                        // 세로 영상은 높이만 비율 유지
                        val displayHeight = if (media.type == MediaType.VIDEO) {
                            singleMediaHeight.coerceAtMost(availableWidth * MediaDisplayPolicy.FEED_SINGLE_VIDEO_MAX_HEIGHT_RATIO)
                        } else {
                            singleMediaHeight
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(displayHeight + imagePadding * 2)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(start = horizontalPadding + (avatarSize - lineWidth) / 2)
                                    .width(lineWidth)
                                    .fillMaxHeight()
                                    .background(lineColor)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = imagePadding)
                                    .padding(start = contentStartPadding, end = horizontalPadding)
                            ) {
                                if (media.type == MediaType.VIDEO) {
                                    VideoPlayer(
                                        videoUrl = media.url,
                                        thumbnailUrl = media.thumbnailUrl,
                                        // 원글 미리보기는 자동재생 안 함
                                        isPlaying = false,
                                        onClick = onPostClick,
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                                        fixedHeight = displayHeight,
                                        maxWidth = availableWidth,
                                        preferredAspectRatio = clampedRatio,
                                        minAspectRatio = singleMinRatio,
                                        maxAspectRatio = MediaDisplayPolicy.FEED_SINGLE_MAX_RATIO,
                                        durationMs = media.durationMs
                                    )
                                } else {
                                    AsyncImage(
                                        model = media.thumbnailUrl ?: media.url,
                                        contentDescription = "포스트 이미지",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(singleMediaHeight)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop,
                                        placeholder = ColorPainter(placeholderColor),
                                        error = ColorPainter(placeholderColor)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val multiMediaHeight = MediaDisplayPolicy.COMMENT_MULTI_ITEM_HEIGHT
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(multiMediaHeight + imagePadding * 2)
                    ) {
                        // 선
                        Box(
                            modifier = Modifier
                                .padding(start = horizontalPadding + (avatarSize - lineWidth) / 2)
                                .width(lineWidth)
                                .fillMaxHeight()
                                .background(lineColor)
                        )

                        // 미디어
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = imagePadding)
                        ) {
                            val maxItemWidth = (maxWidth - contentStartPadding - horizontalPadding).coerceAtLeast(1.dp)
                            val maxVisibleRatio = (maxItemWidth / multiMediaHeight).coerceIn(
                                MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
                                MediaDisplayPolicy.COMMENT_MULTI_MAX_RATIO
                            )

                            // 가로줄에서 중앙에 가장 가까운 영상 1개만 재생
                            val rowState = rememberLazyListState()
                            val activeMediaId by remember {
                                derivedStateOf {
                                    val info = rowState.layoutInfo
                                    val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
                                    info.visibleItemsInfo
                                        .minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }
                                        ?.key as? Int
                                }
                            }
                            // 영상 1개면 중앙 매칭 생략
                            val videoCount = remember(mediaItems) { mediaItems.count { it.type == MediaType.VIDEO } }

                            LazyRow(
                                state = rowState,
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(start = contentStartPadding, end = horizontalPadding),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(mediaItems, key = { it.id }) { media ->
                                    if (media.type == MediaType.VIDEO) {
                                        VideoPlayer(
                                            videoUrl = media.url,
                                            thumbnailUrl = media.thumbnailUrl,
                                            isPlaying = videoCount <= 1 || activeMediaId == media.id,
                                            onClick = onPostClick,
                                            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                                            fixedHeight = multiMediaHeight,
                                            maxWidth = maxItemWidth,
                                            preferredAspectRatio = clampedAspectRatio(
                                                aspectRatio = media.preferredAspectRatio(),
                                                minRatio = MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
                                                maxRatio = maxVisibleRatio
                                            ),
                                            minAspectRatio = MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
                                            maxAspectRatio = maxVisibleRatio,
                                            durationMs = media.durationMs
                                        )
                                    } else {
                                        val itemRatio = clampedAspectRatio(
                                            aspectRatio = media.preferredAspectRatio(),
                                            minRatio = MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
                                            maxRatio = maxVisibleRatio
                                        )
                                        AsyncImage(
                                            model = media.thumbnailUrl ?: media.url,
                                            contentDescription = "포스트 이미지",
                                            modifier = Modifier
                                                .width(multiMediaHeight * itemRatio)
                                                .height(multiMediaHeight)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop,
                                            placeholder = ColorPainter(placeholderColor),
                                            error = ColorPainter(placeholderColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 이미지 아래 좋아요/댓글 버튼 + 선
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

                    // 좋아요·댓글 버튼
                    Row(
                        modifier = Modifier
                            .padding(start = contentStartPadding, end = horizontalPadding)
                            .padding(top = 8.dp, bottom = 8.dp),
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
                                    if (currentPost.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                                ),
                                contentDescription = "좋아요",
                                modifier = Modifier
                                    .size(20.dp)
                                    .bounceClick(),
                                tint = if (currentPost.isLiked) {
                                    Like
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            if (currentPost.likeCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                AnimatedCounter(
                                    count = currentPost.likeCount,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (currentPost.isLiked) Like else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // 댓글
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onCommentClick
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_comment),
                                contentDescription = "댓글",
                                modifier = Modifier
                                    .size(20.dp)
                                    .bounceClick(),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (currentPost.commentCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                AnimatedCounter(
                                    count = currentPost.commentCount,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 댓글 아바타 위 여백
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/** 아바타와 아래 댓글로 이어지는 연결선 */
@Composable
private fun AvatarWithLine(
    avatarSize: Dp,
    lineWidth: Dp,
    lineColor: Color,
    avatar: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(avatarSize)
    ) {
        avatar()

        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .width(lineWidth)
                .weight(1f)
                .background(lineColor)
        )
    }
}

@Composable
private fun AvatarPlaceholder(size: Dp) {
    Image(
        painter = painterResource(R.drawable.ic_profile_placeholder),
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@HaloLightDarkPreview
@Composable
private fun PostPreviewSectionPreview() {
    val samplePost = Post(
        id = 1,
        content = "오늘 새로운 프로젝트 시작했어요! 정말 설레네요",
        author = Author(
            id = 1,
            username = "ninezero",
            displayName = "나인제로",
            avatarUrl = null
        ),
        likeCount = 42,
        commentCount = 7,
        isLiked = true,
        createdAt = System.currentTimeMillis(),
    )

    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostPreviewSection(
                post = samplePost,
                unavailableReason = null,
                onPostClick = {},
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
private fun PostPreviewSectionBlockedPreview() {
    val samplePost = Post(
        id = 1,
        content = "이 포스트는 차단된 사용자의 포스트입니다.",
        author = Author(
            id = 1,
            username = "blocked_user",
            displayName = "차단된 사용자",
            avatarUrl = null
        ),
        likeCount = 5,
        commentCount = 1,
        isBlocked = true,
        createdAt = System.currentTimeMillis(),
    )

    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostPreviewSection(
                post = samplePost,
                unavailableReason = null,
                onPostClick = {},
                onProfileClick = {},
                onLikeClick = {},
                onCommentClick = {},
                onMoreClick = {}
            )
        }
    }
}
