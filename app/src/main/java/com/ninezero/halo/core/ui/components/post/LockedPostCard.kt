package com.ninezero.halo.core.ui.components.post

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.overlay.LockedOverlayLabelColor
import com.ninezero.halo.core.ui.components.overlay.LockedOverlaySubLabelColor
import com.ninezero.halo.core.ui.components.overlay.lockedOverlayStyle
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/** 잠금 포스트 카드 */
@Composable
fun LockedPostCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = remember { HazeState() }
    val previewUrl = post.mediaAttachments.firstOrNull()?.url

    val labelColor = LockedOverlayLabelColor
    val subLabelColor = LockedOverlaySubLabelColor

    val title = if (post.isSecret) "비밀글" else "${post.requiredTier} 이상 멤버십 전용"
    val subtitle = if (post.isSecret) "크리에이터만 볼 수 있습니다" else "멤버십 가입하고 보기"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = post.author.avatarThumbUrl ?: post.author.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                error = painterResource(R.drawable.ic_profile_placeholder)
            )
            Spacer(modifier = Modifier.width(12.dp))
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
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                // 콘텐츠 영역
                val lockShape = RoundedCornerShape(8.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(lockShape)
                            .hazeSource(state = hazeState)
                    ) {
                        // 사진 있으면 흐린 사진, 없으면 빈 칸
                        if (previewUrl != null) {
                            AsyncImage(
                                model = previewUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(lockShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            )
                        }
                    }

                    // 블러 오버레이 + 자물쇠 라벨
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(lockShape)
                            .haloBlur(state = hazeState, style = lockedOverlayStyle()),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.ic_lock),
                                contentDescription = null,
                                tint = labelColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = labelColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = subLabelColor
                            )
                        }
                    }
                }
            }
        }

        // 푸터, 카운트만
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 60.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(if (post.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.likeCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_comment),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.commentCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun LockedPostCardPreview() {
    val author = Author(id = 1, username = "creator1", displayName = "크리에이터", avatarUrl = null, role = "CREATOR")
    val createdAt = System.currentTimeMillis() - 3 * 60 * 60 * 1000L
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                LockedPostCard(
                    post = Post(id = 1, content = "", author = author, requiredTier = "TIER1", canAccess = false, createdAt = createdAt),
                    onClick = {}
                )
                LockedPostCard(
                    post = Post(id = 2, content = "", author = author, isSecret = true, canAccess = false, createdAt = createdAt),
                    onClick = {}
                )
            }
        }
    }
}
