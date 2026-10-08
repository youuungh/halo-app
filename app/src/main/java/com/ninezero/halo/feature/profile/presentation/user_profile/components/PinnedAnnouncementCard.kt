package com.ninezero.halo.feature.profile.presentation.user_profile.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun PinnedAnnouncementCard(
    posts: List<Post>,
    onPostClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (posts.isEmpty()) return

    // 공지가 2개 이상이면 무한 루프
    val isInfinite = posts.size > 1
    val pagerState = rememberPagerState(
        initialPage = if (isInfinite) posts.size * 1000 else 0,
        pageCount = { if (isInfinite) Int.MAX_VALUE else posts.size }
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val realPage = if (isInfinite) page % posts.size else page
                val post = posts[realPage]
                AnnouncementContent(
                    post = post,
                    onClick = { onPostClick(post.id) }
                )
            }

            // 인디케이터, 2개 이상만
            if (posts.size > 1) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(posts.size) { index ->
                        val dotColor by animateColorAsState(
                            targetValue = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (pagerState.currentPage % posts.size == index) 0.6f else 0.15f
                            ),
                            label = "dotColor"
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnouncementContent(
    post: Post,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = post.author.avatarThumbUrl ?: post.author.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                error = painterResource(R.drawable.ic_profile_placeholder)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.author.displayName ?: post.author.username,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                VerifiedBadge(role = post.author.role, size = 12.dp)
            }
            Text(
                text = post.createdAt.toRelativeTime(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = post.content,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(
                        if (post.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (post.isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.likeCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_comment),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.commentCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val previewAuthor = Author(
    id = 1,
    username = "creator",
    displayName = "크리에이터",
    avatarUrl = "https://picsum.photos/200"
)

private val previewPosts = listOf(
    Post(
        id = 1,
        content = "[공지] 이번 주 라이브 방송 일정 안내드립니다!\n\n금요일 저녁 8시에 팬미팅 라이브가 있을 예정이에요. 많은 참여 부탁드려요~",
        author = previewAuthor,
        likeCount = 234,
        commentCount = 56,
        isPinned = true,
        isLiked = true,
        createdAt = System.currentTimeMillis() - 3600000,
    ),
    Post(
        id = 2,
        content = "[공지] 새 앨범 발매 기념 이벤트!\n\n멤버십 회원분들께 특별 포토카드를 증정합니다. 자세한 내용은 링크를 확인해주세요.",
        author = previewAuthor,
        likeCount = 189,
        commentCount = 42,
        isPinned = true,
        createdAt = System.currentTimeMillis() - 86400000,
    ),
    Post(
        id = 3,
        content = "[공지] 커뮤니티 이용 규칙을 꼭 확인해주세요. 서로 존중하는 문화를 만들어가요!",
        author = previewAuthor,
        likeCount = 128,
        commentCount = 23,
        isPinned = true,
        createdAt = System.currentTimeMillis() - 172800000,
    )
)

@HaloLightDarkPreview
@Composable
private fun PinnedAnnouncementCardPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinnedAnnouncementCard(
                posts = previewPosts,
                onPostClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PinnedAnnouncementCardSinglePreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinnedAnnouncementCard(
                posts = listOf(previewPosts.first()),
                onPostClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PinnedAnnouncementCardTextOnlyPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinnedAnnouncementCard(
                posts = previewPosts.drop(1),
                onPostClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
