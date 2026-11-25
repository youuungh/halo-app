package com.ninezero.halo.feature.feed.presentation.post_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.SecretPostBadge
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.button.FollowButton
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.post.BlindedPostNotice
import com.ninezero.halo.core.ui.components.post.PostMediaAttachments
import com.ninezero.halo.core.ui.components.text.AnimatedCounter
import com.ninezero.halo.core.ui.components.text.buildHashtagAnnotatedString
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.Like
import com.ninezero.halo.core.ui.theme.OutlineVariant
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.Post

@Composable
fun PostContentSection(
    post: Post,
    isFollowing: Boolean,
    showFollowingButton: Boolean,
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
                    color = MaterialTheme.colorScheme.onSurface
                )

                VerifiedBadge(role = post.author.role, size = 14.dp)

                Text(
                    text = " · ${post.createdAt.toRelativeTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OutlineVariant
                )

                if (post.isSecret) SecretPostBadge()
            }

            // 팔로우 버튼, 이 화면에서 누른 경우만 팔로잉으로 남김
            if (!isOwnPost && (!isFollowing || showFollowingButton)) {
                FollowButton(isFollowing = isFollowing, onClick = onFollowClick)
            }
        }

        // 본문, 미디어만 있는 글은 본문 줄 없음
        if (post.content.isNotBlank() || post.mediaAttachments.isEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

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
        }

        // 미디어 첨부
        if (post.mediaAttachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            // 영상 뷰어가 열리면 인라인 재생 정지
            val overlayOpen = LocalViewerOverlayHostState.current?.pauseInlineVideo ?: false
            PostMediaAttachments(
                attachments = post.mediaAttachments,
                startPadding = horizontalPadding,
                endPadding = horizontalPadding,
                isVideoPlaying = isVideoPlaying && !overlayOpen,
                onMediaClick = onMediaClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 액션 버튼
        val actionsEnabled = !post.isBlinded
        val actionColor = if (actionsEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        val likeColor = if (post.isLiked && actionsEnabled) Like else actionColor

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

            // 공유, 비밀글은 숨김
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
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
                showFollowingButton = false,
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
                showFollowingButton = true,
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
