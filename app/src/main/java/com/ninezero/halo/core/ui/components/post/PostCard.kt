package com.ninezero.halo.core.ui.components.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.SecretPostBadge
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.snackbar.LocalSnackbarRelay
import com.ninezero.halo.core.ui.components.snackbar.showOrToast
import com.ninezero.halo.core.ui.components.text.AnimatedCounter
import com.ninezero.halo.core.ui.components.text.buildHashtagAnnotatedString
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.Like
import com.ninezero.halo.core.ui.theme.OutlineVariant
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.Post

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
    onMediaClick: (index: Int) -> Unit = {}
) {
    val horizontalPadding = 8.dp
    val profileSize = 40.dp
    val profileSpacing = 12.dp
    val contentStartPadding = horizontalPadding + profileSize + profileSpacing  // 8 + 40 + 12 = 60dp

    // 위치는 클릭 때만 필요해 일반 홀더에 보관
    val profilePosition = remember { object { var value = IntOffset.Zero } }

    // 영상 뷰어가 열려 있으면 인라인 영상 정지
    val overlayOpen = LocalViewerOverlayHostState.current?.pauseInlineVideo ?: false
    val shouldPlayVideo = isVideoPlaying && !overlayOpen

    // 잠금 글은 공용 잠금 카드
    if (post.isRestricted) {
        val context = LocalContext.current
        val snackbarRelay = LocalSnackbarRelay.current
        val subscribeRequester = LocalSubscribeRequester.current
        LockedPostCard(
            post = post,
            onClick = {
                if (post.isSecret) {
                    snackbarRelay.showOrToast(context, "크리에이터만 볼 수 있습니다")
                } else {
                    // 티어 잠금 - 전역 구독 시트 요청
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
                    if (post.isSecret) SecretPostBadge()
                }

                // 미디어만 있는 글은 본문 줄 없음
                if (post.content.isNotBlank() || post.mediaAttachments.isEmpty()) {
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
                isVideoPlaying = shouldPlayVideo,
                onMediaClick = onMediaClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 액션 버튼, 업로드 중 비활성
        if (!isUploading) {
            val actionsEnabled = !post.isBlinded
            val actionColor = if (actionsEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            val likeColor = if (post.isLiked && actionsEnabled) Like else actionColor

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
                        enabled = actionsEnabled,
                        onClick = onLikeClick
                    )
                ) {
                    Icon(
                        painter = painterResource(
                            if (post.isLiked) R.drawable.ic_post_heart_filled else R.drawable.ic_post_heart
                        ),
                        contentDescription = "좋아요",
                        tint = likeColor,
                        modifier = Modifier
                            .size(20.dp)
                            .bounceClick(enabled = actionsEnabled)
                    )
                    if (post.likeCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        AnimatedCounter(
                            count = post.likeCount,
                            style = MaterialTheme.typography.bodySmall,
                            color = likeColor
                        )
                    }
                }

                // 댓글
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = actionsEnabled,
                        onClick = onCommentClick
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_post_comment),
                        contentDescription = "댓글",
                        tint = actionColor,
                        modifier = Modifier
                            .size(20.dp)
                            .bounceClick(enabled = actionsEnabled)
                    )
                    if (post.commentCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        AnimatedCounter(
                            count = post.commentCount,
                            style = MaterialTheme.typography.bodySmall,
                            color = actionColor
                        )
                    }
                }

                // 공유
                if (!post.isSecret) Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = actionsEnabled,
                        onClick = onShareClick
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_post_send),
                        contentDescription = "공유",
                        tint = actionColor,
                        modifier = Modifier
                            .size(20.dp)
                            .bounceClick(enabled = actionsEnabled)
                    )
                    if (post.shareCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        AnimatedCounter(
                            count = post.shareCount,
                            style = MaterialTheme.typography.bodySmall,
                            color = actionColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
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
