package com.ninezero.halo.core.ui.components.post

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.ninezero.halo.core.ui.components.modifiers.ImageThumbnailInfoStore
import com.ninezero.halo.core.ui.components.modifiers.LocalActiveViewerMediaKey
import com.ninezero.halo.core.ui.components.modifiers.LocalMediaSharedTransitionScope
import com.ninezero.halo.core.ui.components.modifiers.blockHorizontalScrollLeak
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.mediaSharedElement
import com.ninezero.halo.core.ui.components.text.buildHashtagAnnotatedString
import com.ninezero.halo.core.ui.components.video_player.VideoPlayer
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.showToast
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.Like
import com.ninezero.halo.ui.theme.OutlineVariant

/** 포스트 카드 */
@Composable
fun PostCard(
    post: Post,
    modifier: Modifier = Modifier,
    isUploading: Boolean = post.isUploading,
    isVideoPlaying: Boolean = false,
    onPostClick: () -> Unit = {},
    onProfileClick: (IntOffset) -> Unit = {},
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onMediaClick: (index: Int) -> Unit = {},
    onSecretPostClick: (() -> Unit)? = null
) {
    val horizontalPadding = 8.dp
    val profileSize = 40.dp
    val profileSpacing = 12.dp
    val contentStartPadding = horizontalPadding + profileSize + profileSpacing  // 8 + 40 + 12 = 60dp

    // 위치는 클릭 때만 필요해 일반 홀더에 보관
    val profilePosition = remember { object { var value = IntOffset.Zero } }

    // 영상 뷰어가 열려 있으면 인라인 영상 정지
    val overlayOpen = LocalViewerOverlayHostState.current?.suppressInlineVideo ?: false
    val effectiveVideoPlaying = isVideoPlaying && !overlayOpen

    // 잠금 글은 공용 잠금 카드
    if (post.isRestricted) {
        val context = LocalContext.current
        val subscribeRequester = LocalSubscribeRequester.current
        LockedPostCard(
            post = post,
            onClick = {
                if (post.isSecret) {
                    // 콜백 있으면 위임, 없으면 토스트
                    onSecretPostClick?.invoke() ?: context.showToast("크리에이터만 볼 수 있습니다")
                } else {
                    // 티어 잠금 → 전역 구독 시트 요청
                    subscribeRequester(post.author.id, post.author.displayName ?: post.author.username)
                }
            },
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isUploading) {
                    Modifier  // 업로드 중에는 클릭 불가
                } else {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPostClick
                    )
                }
            )
    ) {
        // 블라인드는 작성자 본인만 도달
        if (post.isBlinded) {
            BlindedPostNotice()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 프로필 이미지
            AsyncImage(
                model = post.author.avatarThumbUrl ?: post.author.avatarUrl,
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(profileSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInWindow()
                        profilePosition.value = IntOffset(
                            position.x.toInt(),
                            position.y.toInt() + coordinates.size.height
                        )
                    }
                    .then(
                        if (isUploading || post.author.isDeleted) Modifier else Modifier.clickable {  // 탈퇴자는 프로필로 이동 안 함
                            onProfileClick(profilePosition.value)
                        }
                    ),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                error = painterResource(R.drawable.ic_profile_placeholder),
                fallback = painterResource(R.drawable.ic_profile_placeholder)
            )

            Spacer(modifier = Modifier.width(profileSpacing))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.author.displayName ?: post.author.username,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    VerifiedBadge(role = post.author.role, size = 14.dp)
                    Text(
                        text = " · ${post.createdAt.toRelativeTime()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OutlineVariant
                    )
                }

                val tagColor = MaterialTheme.colorScheme.onSurfaceVariant
                val annotatedContent = remember(post.content, tagColor) {
                    buildHashtagAnnotatedString(post.content, tagColor)
                }
                Text(
                    text = annotatedContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 더보기 버튼 또는 업로드 중 표시
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
                    tint = OutlineVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onMoreClick
                        )
                )
            }
        }

        // 미디어 첨부가 있으면 표시
        if (post.mediaAttachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            PostMediaAttachments(
                attachments = post.mediaAttachments,
                startPadding = contentStartPadding,
                isUploading = isUploading,
                isVideoPlaying = effectiveVideoPlaying,
                onMediaClick = onMediaClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 액션 버튼, 업로드 중 비활성
        if (!isUploading) {
            Row(
                modifier = Modifier.padding(start = contentStartPadding, end = horizontalPadding),
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

                // 공유
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PostMediaAttachments(
    attachments: List<MediaItem>,
    startPadding: Dp,
    endPadding: Dp = 8.dp,
    isUploading: Boolean = false,
    isVideoPlaying: Boolean = false,
    onMediaClick: (index: Int) -> Unit = {}
) {
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    val multiMediaHeight = MediaDisplayPolicy.FEED_MULTI_ITEM_HEIGHT
    // 썸네일 morph용, scope null이면 일반 표시
    val mediaSharedScope = LocalMediaSharedTransitionScope.current
    val activeViewerKey = LocalActiveViewerMediaKey.current

    if (attachments.size == 1) {
        val media = attachments.first()
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = startPadding, end = endPadding)
        ) {
            val availableWidth = maxWidth
            // 세로 영상은 긴 비율 허용, 이미지 4:5
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
                // 세로 영상 비율은 유지하되 높이만 캡
                val videoHeight = singleMediaHeight.coerceAtMost(
                    availableWidth * MediaDisplayPolicy.FEED_SINGLE_VIDEO_MAX_HEIGHT_RATIO
                )
                VideoPlayer(
                    videoUrl = media.url,
                    thumbnailUrl = media.thumbnailUrl,
                    isPlaying = isVideoPlaying && !isUploading,
                    onClick = { if (!isUploading) onMediaClick(0) },
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
                    // 목록은 썸네일, 키는 원본
                    model = media.thumbnailUrl ?: media.url,
                    contentDescription = "첨부 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(singleMediaHeight)
                        .mediaSharedElement(mediaSharedScope, activeViewerKey, media.url)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isUploading) Modifier else Modifier.clickable { onMediaClick(0) }
                        ),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(placeholderColor),
                    error = ColorPainter(placeholderColor),
                    // 뷰어 morph용 캐시 키/비율 보관
                    onSuccess = ImageThumbnailInfoStore.onSuccessCapture(media.url)
                )
            }
        }
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val maxItemWidth = (maxWidth - startPadding - endPadding).coerceAtLeast(1.dp)
            // peek 여백만큼 줄인 폭으로 비율 캡
            val peekTargetWidth = (maxItemWidth * MediaDisplayPolicy.MULTI_PEEK_FRACTION).coerceAtLeast(1.dp)
            val maxVisibleRatio = (peekTargetWidth / multiMediaHeight).coerceIn(
                MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
                MediaDisplayPolicy.FEED_MULTI_MAX_RATIO
            )
            val rowState = rememberLazyListState()
            // 중앙 미디어 1개만 재생
            val activeMediaId by remember {
                derivedStateOf {
                    val info = rowState.layoutInfo
                    val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
                    info.visibleItemsInfo
                        .minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }
                        ?.key as? Int
                }
            }
            // 영상 1개면 중앙 매칭 생략, 여럿이면 중앙만 재생
            val videoCount = remember(attachments) { attachments.count { it.type == MediaType.VIDEO } }

            LazyRow(
                state = rowState,
                modifier = Modifier
                    .fillMaxWidth()
                    .blockHorizontalScrollLeak(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = startPadding, end = endPadding)
            ) {
                itemsIndexed(
                    items = attachments,
                    key = { _, media -> media.id }
                ) { index, media ->
                    if (media.type == MediaType.VIDEO) {
                        VideoPlayer(
                            videoUrl = media.url,
                            thumbnailUrl = media.thumbnailUrl,
                            isPlaying = isVideoPlaying && !isUploading && (videoCount <= 1 || activeMediaId == media.id),
                            onClick = { if (!isUploading) onMediaClick(index) },
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
                            // 목록은 썸네일, 키는 원본
                            model = media.thumbnailUrl ?: media.url,
                            contentDescription = "첨부 이미지",
                            modifier = Modifier
                                .width(multiMediaHeight * itemRatio)
                                .height(multiMediaHeight)
                                .mediaSharedElement(mediaSharedScope, activeViewerKey, media.url)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isUploading) Modifier else Modifier.clickable { onMediaClick(index) }
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
private fun PostCardPreview() {
    HaloTheme {
        PostCard(
            post = Post(
                id = 1,
                content = "내용내용내용내용내용내용내용",
                author = Author(
                    id = 1,
                    username = "username",
                    displayName = "displayName",
                    avatarUrl = "avatarUrl"
                ),
                likeCount = 12,
                commentCount = 3,
                isLiked = true,
                isBookmarked = false,
                createdAt = System.currentTimeMillis() - 3600000,
                mediaAttachments = emptyList()
            ),
            onPostClick = {},
            onProfileClick = {},
            onLikeClick = {},
            onCommentClick = {},
            onShareClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostCardWithImagePreview() {
    HaloTheme {
        PostCard(
            post = Post(
                id = 2,
                content = "내용내용내용내용내용내용내용내용내용내용내용",
                author = Author(
                    id = 2,
                    username = "username",
                    displayName = "displayName",
                    avatarUrl = "avatarUrl"
                ),
                likeCount = 1234,
                commentCount = 56,
                isLiked = false,
                isBookmarked = true,
                createdAt = System.currentTimeMillis() - 86400000,
                mediaAttachments = listOf(
                    MediaItem(
                        id = 1,
                        type = MediaType.IMAGE,
                        url = "url",
                        thumbnailUrl = null
                    )
                )
            ),
            onPostClick = {},
            onProfileClick = {},
            onLikeClick = {},
            onCommentClick = {},
            onShareClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostCardWithMultipleImagesPreview() {
    HaloTheme {
        PostCard(
            post = Post(
                id = 3,
                content = "내용내용내용내용내용내용내용내용내용내용내용내용내용",
                author = Author(
                    id = 3,
                    username = "photographer",
                    displayName = "displayName",
                    avatarUrl = "avatarUrl"
                ),
                likeCount = 123,
                commentCount = 45,
                isLiked = true,
                isBookmarked = false,
                createdAt = System.currentTimeMillis() - 5400000,
                mediaAttachments = listOf(
                    MediaItem(
                        id = 1,
                        type = MediaType.IMAGE,
                        url = "url",
                        thumbnailUrl = null
                    ),
                    MediaItem(
                        id = 2,
                        type = MediaType.IMAGE,
                        url = "url",
                        thumbnailUrl = null
                    ),
                    MediaItem(
                        id = 3,
                        type = MediaType.IMAGE,
                        url = "url",
                        thumbnailUrl = null
                    )
                )
            ),
            onPostClick = {},
            onProfileClick = {},
            onLikeClick = {},
            onCommentClick = {},
            onShareClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostCardWithVideoPreview() {
    HaloTheme {
        PostCard(
            post = Post(
                id = 4,
                content = "동영상이 포함된 포스트입니다",
                author = Author(
                    id = 4,
                    username = "videocreator",
                    displayName = "비디오 크리에이터",
                    avatarUrl = null
                ),
                likeCount = 256,
                commentCount = 32,
                isLiked = false,
                isBookmarked = false,
                createdAt = System.currentTimeMillis() - 7200000,
                mediaAttachments = listOf(
                    MediaItem(
                        id = 1,
                        type = MediaType.VIDEO,
                        url = "https://example.com/video.mp4",
                        thumbnailUrl = "https://picsum.photos/400/400"
                    )
                )
            ),
            isVideoPlaying = false,
            onPostClick = {},
            onProfileClick = {},
            onLikeClick = {},
            onCommentClick = {},
            onShareClick = {},
            onMediaClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostCardWithVideoAndImagesPreview() {
    HaloTheme {
        PostCard(
            post = Post(
                id = 5,
                content = "동영상과 이미지가 함께 포함된 포스트입니다",
                author = Author(
                    id = 5,
                    username = "contentcreator",
                    displayName = "콘텐츠 크리에이터",
                    avatarUrl = null
                ),
                likeCount = 512,
                commentCount = 64,
                isLiked = true,
                isBookmarked = true,
                createdAt = System.currentTimeMillis() - 10800000,
                mediaAttachments = listOf(
                    MediaItem(
                        id = 1,
                        type = MediaType.VIDEO,
                        url = "https://example.com/video.mp4",
                        thumbnailUrl = "https://picsum.photos/400/400"
                    ),
                    MediaItem(
                        id = 2,
                        type = MediaType.IMAGE,
                        url = "https://picsum.photos/401/400",
                        thumbnailUrl = null
                    ),
                    MediaItem(
                        id = 3,
                        type = MediaType.IMAGE,
                        url = "https://picsum.photos/402/400",
                        thumbnailUrl = null
                    )
                )
            ),
            isVideoPlaying = false,
            onPostClick = {},
            onProfileClick = {},
            onLikeClick = {},
            onCommentClick = {},
            onShareClick = {},
            onMediaClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostCardWithVerticalVideoAndImagesPreview() {
    HaloTheme {
        PostCard(
            post = Post(
                id = 6,
                content = "세로형 비디오가 포함된 다중 미디어 포스트입니다",
                author = Author(
                    id = 6,
                    username = "shortscreator",
                    displayName = "숏폼 크리에이터",
                    avatarUrl = null
                ),
                likeCount = 301,
                commentCount = 27,
                isLiked = false,
                isBookmarked = false,
                createdAt = System.currentTimeMillis() - 3600000,
                mediaAttachments = listOf(
                    MediaItem(
                        id = 1,
                        type = MediaType.VIDEO,
                        url = "https://example.com/vertical-video.mp4",
                        thumbnailUrl = "https://picsum.photos/360/640",
                        width = 1080,
                        height = 1920
                    ),
                    MediaItem(
                        id = 2,
                        type = MediaType.IMAGE,
                        url = "https://picsum.photos/640/360",
                        thumbnailUrl = null
                    ),
                    MediaItem(
                        id = 3,
                        type = MediaType.IMAGE,
                        url = "https://picsum.photos/500/500",
                        thumbnailUrl = null
                    )
                )
            ),
            isVideoPlaying = false,
            onPostClick = {},
            onProfileClick = {},
            onLikeClick = {},
            onCommentClick = {},
            onShareClick = {},
            onMediaClick = {}
        )
    }
}
