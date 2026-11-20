package com.ninezero.halo.feature.chat.presentation.chat_room.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.overlay.lockedOverlayStyle
import com.ninezero.halo.core.ui.components.post.LocalSubscribeRequester
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.feature.chat.domain.model.PostMessageInfo
import com.ninezero.halo.feature.chat.domain.model.ProductMessageInfo
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
internal fun PostMessageContent(
    postInfo: PostMessageInfo?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLockedInfo: (String) -> Unit = {}
) {
    if (postInfo == null) {
        DeletedShareCard(text = "삭제된 포스트입니다", width = 220.dp, modifier = modifier)
        return
    }

    val subscribeRequester = LocalSubscribeRequester.current
    // 크리에이터 해제로 숨겨진 글, 잠금과 구분
    val isHidden = postInfo.isHidden
    val isRestricted = !postInfo.canAccess && !isHidden
    val isOverlayVisible = isHidden || isRestricted
    val isClickable = postInfo.canAccess
    val hazeState = remember { HazeState() }
    val mediaAttachments = postInfo.mediaAttachments
    val hasMedia = mediaAttachments.isNotEmpty()
    val isVideo = postInfo.mediaType == "VIDEO"

    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = modifier
            .widthIn(max = 220.dp)
            .then(
                if (isOverlayVisible) Modifier
                    .widthIn(min = 200.dp)
                    .heightIn(min = 120.dp) else Modifier
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .cardBorder(shape)
            .then(
                when {
                    isClickable -> Modifier.clickable { onClick() }
                    // TIER 잠금만 구독 시트, FREE·null 잠금은 안내만
                    isRestricted -> Modifier.clickable {
                        if (postInfo.requiredTier != SubscriptionPlanTier.FREE && postInfo.creatorId != null) {
                            subscribeRequester(postInfo.creatorId, postInfo.authorName)
                        } else {
                            onLockedInfo(
                                if (postInfo.creatorId != null) "${postInfo.authorName} 팔로워 전용 포스트예요"
                                else "차단한 유저의 포스트예요"
                            )
                        }
                    }

                    else -> Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier
                .then(if (isOverlayVisible) Modifier.hazeSource(state = hazeState) else Modifier)
        ) {
            Row(
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = postInfo.authorAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_profile_placeholder),
                        error = painterResource(R.drawable.ic_profile_placeholder),
                        fallback = painterResource(R.drawable.ic_profile_placeholder)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = postInfo.authorName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (postInfo.content.isNotBlank()) {
                Text(
                    text = postInfo.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (hasMedia) {
                if (mediaAttachments.size == 1) {
                    // 공유 카드의 미디어는 프리뷰
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 14.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = if (isVideo) postInfo.thumbnailUrl
                            else postInfo.mediaThumbnails.firstOrNull()
                                ?: mediaAttachments.first(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (isVideo) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_play),
                                    contentDescription = "재생",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // 여러 미디어는 가로로, 4개부터 +N
                    val displayCount = minOf(mediaAttachments.size, 3)
                    val remainingCount = mediaAttachments.size - 3

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0 until displayCount) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = postInfo.mediaThumbnails.getOrNull(i)
                                        ?: mediaAttachments[i],
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // 동영상 타일 재생 배지
                                if (postInfo.mediaTypes.getOrNull(i) == "VIDEO" && !(i == 2 && remainingCount > 0)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_play),
                                            contentDescription = "재생",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                                // 3번째 이미지에 +N 오버레이
                                if (i == 2 && remainingCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+$remainingCount",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 좋아요 · 댓글 · 공유 수
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_post_heart),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${postInfo.likeCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_post_comment),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${postInfo.commentCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_post_send),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${postInfo.shareCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "포스트 보기",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (isOverlayVisible) {
            val tier = postInfo.requiredTier
            LinkStatusOverlay(
                // TIER1·2는 멤버십 잠금, FREE·null은 차단 안내
                message = when {
                    isHidden -> "유효하지 않은 포스트"
                    tier != SubscriptionPlanTier.FREE -> "${tier.label} 이상 멤버십 전용"
                    postInfo.creatorId != null -> "팔로워 전용 포스트"
                    else -> "차단한 유저의 포스트"
                },
                creatorName = if (!isHidden && (tier != SubscriptionPlanTier.FREE || postInfo.creatorId != null)) postInfo.authorName else null,
                iconRes = if (isHidden) R.drawable.ic_eye_off else R.drawable.ic_lock,
                hazeState = hazeState,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
internal fun ProductMessageContent(
    productInfo: ProductMessageInfo?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLockedInfo: (String) -> Unit = {}
) {
    if (productInfo == null) {
        DeletedShareCard(text = "삭제된 상품입니다", width = 248.dp, modifier = modifier)
        return
    }

    val subscribeRequester = LocalSubscribeRequester.current
    val isRestricted = !productInfo.canAccess
    val isClickable = productInfo.canAccess
    val hazeState = remember { HazeState() }
    val hasImage = !productInfo.imageUrl.isNullOrBlank()

    val numberFormat = rememberKoreanNumberFormat()
    val hasDiscount = productInfo.originalPrice != null
            && productInfo.discountRate != null
            && productInfo.discountRate > 0
    val formattedPrice = numberFormat.format(productInfo.price)

    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = modifier
            .width(248.dp)
            .height(128.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .cardBorder(shape)
            .then(
                when {
                    isClickable -> Modifier.clickable { onClick() }
                    isRestricted -> Modifier.clickable {
                        if (productInfo.requiredTier != SubscriptionPlanTier.FREE && productInfo.creatorId != null) {
                            subscribeRequester(productInfo.creatorId, productInfo.creatorName ?: "")
                        } else {
                            onLockedInfo("${productInfo.creatorName ?: "크리에이터"} 팔로워 전용 상품이에요")
                        }
                    }

                    else -> Modifier
                }
            )
    ) {
        // 상품 이미지
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .hazeSource(state = hazeState),
            contentAlignment = Alignment.Center
        ) {
            // 플레이스홀더
            Icon(
                painter = painterResource(R.drawable.ic_image),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            if (hasImage) {
                AsyncImage(
                    model = productInfo.imageUrl,
                    contentDescription = productInfo.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (isRestricted) {
            // 잠금
            val tier = productInfo.requiredTier
            LinkStatusOverlay(
                // FREE·null 잠금은 팔로워 전용 안내
                message = if (tier != SubscriptionPlanTier.FREE) "${tier.label} 이상 멤버십 전용" else "팔로워 전용 상품",
                creatorName = productInfo.creatorName ?: "크리에이터",
                iconRes = R.drawable.ic_lock,
                hazeState = hazeState,
                modifier = Modifier.matchParentSize()
            )
        } else {
            val isPreview = LocalInspectionMode.current
            val variantColor = MaterialTheme.colorScheme.surfaceVariant
            val barGlass = if (isPreview) {
                Modifier.background(Color.Black.copy(alpha = 0.45f))
            } else {
                Modifier.haloBlur(
                    hazeState,
                    HaloBlurStyle(
                        backgroundColor = variantColor,
                        tint = Color.Black.copy(alpha = 0.35f),
                        blurRadius = 20.dp,
                        noiseFactor = 0f
                    )
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(46.dp)
                    .then(barGlass)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 상품명
                Text(
                    text = productInfo.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // 가격
                if (hasDiscount) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "${productInfo.discountRate}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alignByBaseline()
                        )
                        Text(
                            text = "${formattedPrice}원",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                } else {
                    Text(
                        text = "${formattedPrice}원",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/** 삭제된 공유, 카드 폭의 얇은 회색 줄 */
@Composable
private fun DeletedShareCard(
    text: String,
    width: Dp,
    modifier: Modifier = Modifier
) {
    // 공유 카드와 같은 테두리 한 줄, '볼 수 없음' 아이콘
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .width(width)
            .height(48.dp)
            .cardBorder(shape),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_block),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LinkStatusOverlay(
    message: String,
    iconRes: Int,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    creatorName: String? = null
) {
    val isPreview = LocalInspectionMode.current
    val overlayModifier = if (isPreview) {
        Modifier.background(Color.Black.copy(alpha = 0.55f))
    } else {
        Modifier.haloBlur(hazeState, lockedOverlayStyle())
    }

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .then(overlayModifier),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (creatorName != null) {
                Text(
                    text = creatorName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
