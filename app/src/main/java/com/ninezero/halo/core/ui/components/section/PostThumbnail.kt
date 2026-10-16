package com.ninezero.halo.core.ui.components.section

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.post.LocalSubscribeRequester
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.showToast
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.core.util.toFormattedViewCount
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlin.random.Random

private val PostCardHeight = 140.dp

/** 포스트 썸네일 */
@Composable
fun PostThumbnail(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 270.dp
) {
    // 잠금 글은 공용 잠금 썸네일
    if (post.isRestricted) {
        LockedPostThumbnail(post = post, modifier = modifier, width = width)
        return
    }
    PostTextCard(post = post, onClick = onClick, modifier = modifier.width(width))
}

/** 목록용 포스트 썸네일 */
@Composable
fun PostThumbnailListItem(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (post.isRestricted) {
        LockedPostThumbnail(post = post, modifier = modifier, width = null)
        return
    }
    PostTextCard(post = post, onClick = onClick, modifier = modifier.fillMaxWidth())
}

@Composable
private fun PostTextCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 첫 이미지, 없으면 첫 비디오 포스터
    val thumbnailUrl = post.mediaAttachments.firstOrNull { it.type == MediaType.IMAGE }?.let { it.thumbnailUrl ?: it.url }
        ?: post.mediaAttachments.firstOrNull { it.type == MediaType.VIDEO }?.thumbnailUrl
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val hazeState = remember { HazeState() }

    Box(
        modifier = modifier
            .height(PostCardHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceVariant)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) { onClick() }
    ) {
        if (thumbnailUrl != null) {
            // 배경 이미지
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            ) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to surfaceVariant
                            )
                        )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haloBlur(state = hazeState) {
                        style = HaloBlurStyle(
                            backgroundColor = surfaceVariant,
                            tint = HaloBlurTint(surfaceVariant.copy(alpha = 0.35f)),
                            blurRadius = 16.dp,
                            noiseFactor = 0f
                        )
                    }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            PostMetaRow(post = post)
        }
    }
}

@Composable
private fun PostMetaRow(post: Post, modifier: Modifier = Modifier) {
    val numberFormat = rememberKoreanNumberFormat()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetaCount(iconRes = R.drawable.ic_heart, text = numberFormat.format(post.likeCount))
        MetaCount(iconRes = R.drawable.ic_comment, text = numberFormat.format(post.commentCount))
        MetaCount(iconRes = R.drawable.ic_eye, text = post.viewCount.toFormattedViewCount())
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = post.createdAt.toFormattedDate("MM.dd"),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetaCount(iconRes: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LockedPostThumbnail(
    post: Post,
    modifier: Modifier = Modifier,
    width: Dp? = null
) {
    val context = LocalContext.current
    val subscribeRequester = LocalSubscribeRequester.current
    val previewUrl = post.mediaAttachments.firstOrNull()?.url
    val surfaceColor = MaterialTheme.colorScheme.surface
    val hazeState = remember { HazeState() }

    Box(
        modifier = modifier
            .then(if (width == null) Modifier.fillMaxWidth() else Modifier.width(width))
            .height(PostCardHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) {
                if (post.isSecret) {
                    context.showToast("크리에이터만 볼 수 있습니다")
                } else {
                    subscribeRequester(post.author.id, post.author.displayName ?: post.author.username)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 텍스트 카드와 같은 프레임
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
        ) {
            if (previewUrl != null) {
                AsyncImage(
                    model = previewUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 실제 카드의 실루엣
                val lineWidths = remember(post.id) {
                    val random = Random(post.id)
                    List(4) { index ->
                        if (index == 3) 0.3f + random.nextFloat() * 0.25f
                        else 0.72f + random.nextFloat() * 0.24f
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    lineWidths.forEach { fraction ->
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .fillMaxWidth(fraction)
                                .height(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    // 메타줄 실루엣
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))
                        )
                    }
                }
            }
        }
        // 스크림 + 라벨
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = surfaceColor,
                        tint = HaloBlurTint(Color.Black.copy(alpha = 0.5f)),
                        blurRadius = 14.dp,
                        noiseFactor = 0f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (post.isSecret) "비밀글" else "${post.requiredTier} 이상 멤버십 전용",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

/** 포스트 썸네일 스켈레톤 */
@Composable
fun PostThumbnailSkeleton(
    modifier: Modifier = Modifier,
    width: Dp = 270.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .height(PostCardHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

private val previewAuthor = Author(
    id = 1,
    username = "creator",
    displayName = "크리에이터",
    avatarUrl = null
)

private val previewPost = Post(
    id = 1,
    content = "오늘의 일상을 공유합니다! 정말 좋은 하루였어요",
    author = previewAuthor,
    mediaAttachments = emptyList(),
    likeCount = 1234,
    commentCount = 56,
    viewCount = 9876,
    createdAt = System.currentTimeMillis(),
)

private val previewPostNoImage = Post(
    id = 2,
    content = "이미지가 없는 텍스트 포스트입니다.",
    author = previewAuthor,
    mediaAttachments = emptyList(),
    likeCount = 42,
    commentCount = 3,
    viewCount = 150,
    createdAt = System.currentTimeMillis() - 86400000 * 7,
)

@HaloLightDarkPreview
@Composable
private fun PostThumbnailWithImagePreview() {
    HaloTheme {
        Surface {
            PostThumbnail(
                post = previewPost,
                onClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostThumbnailPlaceholderPreview() {
    HaloTheme {
        Surface {
            PostThumbnail(
                post = previewPostNoImage,
                onClick = {}
            )
        }
    }
}
