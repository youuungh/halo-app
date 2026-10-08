package com.ninezero.halo.feature.feed.presentation.post_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.counter.AnimatedCounter
import com.ninezero.halo.core.ui.components.media.MediaDisplayPolicy
import com.ninezero.halo.core.ui.components.media.clampedAspectRatio
import com.ninezero.halo.core.ui.components.media.preferredAspectRatio
import com.ninezero.halo.core.ui.components.media.singleMediaHeightForWidth
import com.ninezero.halo.core.ui.components.modifiers.ImageThumbnailInfoStore
import com.ninezero.halo.core.ui.components.modifiers.LocalActiveViewerMediaKey
import com.ninezero.halo.core.ui.components.modifiers.LocalMediaSharedTransitionScope
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.mediaSharedElement
import com.ninezero.halo.core.ui.components.post.BlindedPostNotice
import com.ninezero.halo.core.ui.components.text.buildHashtagAnnotatedString
import com.ninezero.halo.core.ui.components.video_player.VideoPlayer
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.Like
import com.ninezero.halo.ui.theme.OutlineVariant

@Composable
fun PostContentSection(
    post: Post,
    isFollowing: Boolean,
    isOwnPost: Boolean,
    onAuthorSectionPositioned: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onFollowClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier,
    onShareClick: () -> Unit = {},
    onMediaClick: (index: Int) -> Unit = {},
    isVideoPlaying: Boolean = false
) {
    val horizontalPadding = 8.dp
    val profileSize = 40.dp
    val profileSpacing = 12.dp

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 블라인드는 작성자 본인만 도달
        if (post.isBlinded) {
            BlindedPostNotice()
        }
        // 프로필 섹션
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .heightIn(min = 48.dp)  // 팔로우 버튼이 사라져도 높이 유지
                .onGloballyPositioned { coordinates ->
                    val bottom = coordinates.size.height
                    onAuthorSectionPositioned(bottom)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 이미지
            AsyncImage(
                model = post.author.avatarThumbUrl ?: post.author.avatarUrl,
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(profileSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !post.author.isDeleted,  // 탈퇴자는 프로필로 이동 안 함
                        onClick = onProfileClick
                    ),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                error = painterResource(R.drawable.ic_profile_placeholder),
                fallback = painterResource(R.drawable.ic_profile_placeholder)
            )

            Spacer(modifier = Modifier.width(profileSpacing))

            // 이름 + 시간
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.author.displayName ?: post.author.username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alignByBaseline()
                )

                VerifiedBadge(role = post.author.role, size = 14.dp)

                Text(
                    text = " · ${post.createdAt.toRelativeTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OutlineVariant,
                    modifier = Modifier.alignByBaseline()
                )
            }

            // 팔로우 버튼
            if (!isOwnPost && !isFollowing) {
                Surface(
                    onClick = onFollowClick,
                    modifier = Modifier.bounceClick(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                ) {
                    Text(
                        text = "팔로우",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 본문
        val tagColor = MaterialTheme.colorScheme.onSurfaceVariant
        val annotatedContent = remember(post.content, tagColor) {
            buildHashtagAnnotatedString(post.content, tagColor)
        }
        Text(
            text = annotatedContent,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = horizontalPadding)
        )

        // 미디어 첨부
        if (post.mediaAttachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            PostMediaSection(
                attachments = post.mediaAttachments,
                startPadding = horizontalPadding,
                isVideoPlaying = isVideoPlaying,
                onMediaClick = onMediaClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 액션 버튼
        Row(
            modifier = Modifier.padding(horizontal = horizontalPadding),
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
                        if (post.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                    ),
                    contentDescription = "좋아요",
                    tint = if (post.isLiked) Like else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(20.dp)
                        .bounceClick()
                )
                if (post.likeCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    AnimatedCounter(
                        count = post.likeCount,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (post.isLiked) Like else MaterialTheme.colorScheme.onSurface
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
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(20.dp)
                        .bounceClick()
                )
                if (post.commentCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    AnimatedCounter(
                        count = post.commentCount,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 공유, 비밀글은 숨김
            if (!post.isSecret) Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onShareClick
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_send),
                    contentDescription = "공유",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(20.dp)
                        .bounceClick()
                )
                if (post.shareCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    AnimatedCounter(
                        count = post.shareCount,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PostMediaSection(
    attachments: List<MediaItem>,
    startPadding: Dp,
    onMediaClick: (index: Int) -> Unit = {},
    isVideoPlaying: Boolean = false
) {
    if (attachments.isEmpty()) return

    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    val multiMediaHeight = MediaDisplayPolicy.FEED_MULTI_ITEM_HEIGHT
    // 본문 이미지 ↔ 풀스크린 뷰어 morph
    val mediaSharedScope = LocalMediaSharedTransitionScope.current
    val activeViewerKey = LocalActiveViewerMediaKey.current
    // 영상 뷰어 열리면 인라인 재생 정지
    val overlayOpen = LocalViewerOverlayHostState.current?.suppressInlineVideo ?: false
    val effectiveVideoPlaying = isVideoPlaying && !overlayOpen

    if (attachments.size == 1) {
        val media = attachments.first()
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = startPadding)
        ) {
            val availableWidth = maxWidth
            // 세로 영상은 긴 비율 허용, 이미지는 4:5
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

            if (media.type == MediaType.VIDEO) {
                // 세로 영상 비율은 유지하되 높이만 유지
                val videoHeight = singleMediaHeight.coerceAtMost(
                    availableWidth * MediaDisplayPolicy.FEED_SINGLE_VIDEO_MAX_HEIGHT_RATIO
                )
                VideoPlayer(
                    videoUrl = media.url,
                    thumbnailUrl = media.thumbnailUrl,
                    isPlaying = effectiveVideoPlaying,
                    onClick = { onMediaClick(0) },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    fixedHeight = videoHeight,
                    maxWidth = availableWidth,
                    preferredAspectRatio = clampedRatio,
                    minAspectRatio = singleMinRatio,
                    maxAspectRatio = MediaDisplayPolicy.FEED_SINGLE_MAX_RATIO,
                    durationMs = media.durationMs
                )
            } else {
                AsyncImage(
                    model = media.thumbnailUrl ?: media.url,
                    contentDescription = "첨부 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(singleMediaHeight)
                        .mediaSharedElement(mediaSharedScope, activeViewerKey, media.url)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onMediaClick(0) }
                        ),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(placeholderColor),
                    error = ColorPainter(placeholderColor),
                    onSuccess = ImageThumbnailInfoStore.onSuccessCapture(media.url)
                )
            }
        }
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val maxItemWidth = (maxWidth - startPadding * 2).coerceAtLeast(1.dp)
            // peek 여백만큼 줄인 폭 기준으로 비율 유지
            val peekTargetWidth = (maxItemWidth * MediaDisplayPolicy.MULTI_PEEK_FRACTION).coerceAtLeast(1.dp)
            val maxVisibleRatio = (peekTargetWidth / multiMediaHeight).coerceIn(
                MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
                MediaDisplayPolicy.FEED_MULTI_MAX_RATIO
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
            val videoCount = remember(attachments) { attachments.count { it.type == MediaType.VIDEO } }

            LazyRow(
                state = rowState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = startPadding, end = startPadding)
            ) {
                itemsIndexed(
                    items = attachments,
                    key = { _, media -> media.id }
                ) { index, media ->
                    if (media.type == MediaType.VIDEO) {
                        VideoPlayer(
                            videoUrl = media.url,
                            thumbnailUrl = media.thumbnailUrl,
                            isPlaying = effectiveVideoPlaying && (videoCount <= 1 || activeMediaId == media.id),
                            onClick = { onMediaClick(index) },
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                            fixedHeight = multiMediaHeight,
                            maxWidth = maxItemWidth,
                            preferredAspectRatio = clampedAspectRatio(
                                aspectRatio = media.preferredAspectRatio(),
                                minRatio = MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
                                maxRatio = maxVisibleRatio
                            ),
                            minAspectRatio = MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
                            maxAspectRatio = maxVisibleRatio,
                            durationMs = media.durationMs
                        )
                    } else {
                        val itemRatio = clampedAspectRatio(
                            aspectRatio = media.preferredAspectRatio(),
                            minRatio = MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
                            maxRatio = maxVisibleRatio
                        )
                        AsyncImage(
                            model = media.thumbnailUrl ?: media.url,
                            contentDescription = "첨부 이미지",
                            modifier = Modifier
                                .width(multiMediaHeight * itemRatio)
                                .height(multiMediaHeight)
                                .mediaSharedElement(mediaSharedScope, activeViewerKey, media.url)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onMediaClick(index) }
                                ),
                            contentScale = ContentScale.Crop,
                            placeholder = ColorPainter(placeholderColor),
                            error = ColorPainter(placeholderColor),
                            onSuccess = ImageThumbnailInfoStore.onSuccessCapture(media.url)
                        )
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostContentSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostContentSection(
                post = Post(
                    id = 1,
                    content = "testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest",
                    author = Author(
                        id = 1,
                        username = "developer",
                        displayName = "개발자",
                        avatarUrl = null
                    ),
                    mediaAttachments = emptyList(),
                    likeCount = 42,
                    commentCount = 15,
                    viewCount = 1240,
                    isLiked = true,
                    isBookmarked = false,
                    createdAt = System.currentTimeMillis() - 3600000,
                ),
                isFollowing = false,
                isOwnPost = false,
                onAuthorSectionPositioned = {},
                onProfileClick = {},
                onFollowClick = {},
                onLikeClick = {},
                onCommentClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostContentSectionFollowingPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostContentSection(
                post = Post(
                    id = 1,
                    content = "팔로우 중인 유저의 포스트입니다.",
                    author = Author(
                        id = 1,
                        username = "friend",
                        displayName = "친구",
                        avatarUrl = null
                    ),
                    mediaAttachments = emptyList(),
                    likeCount = 10,
                    commentCount = 5,
                    viewCount = 500,
                    isLiked = false,
                    isBookmarked = false,
                    createdAt = System.currentTimeMillis() - 7200000,
                ),
                isFollowing = true,
                isOwnPost = false,
                onAuthorSectionPositioned = {},
                onProfileClick = {},
                onFollowClick = {},
                onLikeClick = {},
                onCommentClick = {}
            )
        }
    }
}
