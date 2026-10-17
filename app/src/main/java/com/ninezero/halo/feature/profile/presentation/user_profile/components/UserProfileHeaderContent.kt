package com.ninezero.halo.feature.profile.presentation.user_profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.counter.AnimatedCounter
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.profile_image_viewer.LocalProfileImageOverlayVisible
import com.ninezero.halo.core.ui.components.profile_image_viewer.LocalProfileImageTransitionScope
import com.ninezero.halo.core.ui.components.profile_image_viewer.PROFILE_IMAGE_SHARED_KEY
import com.ninezero.halo.feature.profile.domain.model.UserProfile
import com.ninezero.halo.ui.theme.HaloGradient
import com.ninezero.halo.ui.theme.HaloGradientColors
import com.ninezero.halo.ui.theme.Pretendard

@Composable
fun UserProfileHeaderContent(
    profile: UserProfile,
    onFollowClick: () -> Unit = {},
    onSubscribeClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onWebsiteClick: () -> Unit = {},
    onUnblockClick: () -> Unit = {},
    onProfileImageClick: () -> Unit = {},
    onFollowerClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    onSubscriberClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 프로필 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 이미지, 오버레이와 캐시 공유
            val context = LocalContext.current
            val avatarData = profile.avatarThumbUrl ?: profile.avatarUrl
            val avatarRequest = remember(avatarData, context) {
                avatarData?.let {
                    ImageRequest.Builder(context)
                        .data(it)
                        .memoryCacheKey(it)
                        .placeholderMemoryCacheKey(it)
                        .size(Size.ORIGINAL)
                        .crossfade(false)
                        .build()
                }
            }

            // 아바타 morph, scope는 CompositionLocal
            val sharedScope = LocalProfileImageTransitionScope.current
            val overlayVisible = LocalProfileImageOverlayVisible.current
            val avatarSharedModifier = if (sharedScope != null) {
                with(sharedScope) {
                    Modifier.sharedElementWithCallerManagedVisibility(
                        sharedContentState = rememberSharedContentState(key = PROFILE_IMAGE_SHARED_KEY),
                        visible = !overlayVisible
                    )
                }
            } else Modifier

            AsyncImage(
                model = avatarRequest,
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(80.dp)
                    .then(avatarSharedModifier)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onProfileImageClick() },
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                error = painterResource(R.drawable.ic_profile_placeholder),
                fallback = painterResource(R.drawable.ic_profile_placeholder)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            ),
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.4f),
                                offset = Offset.Zero,
                                blurRadius = 8f
                            )
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    VerifiedBadge(role = profile.role, size = 18.dp)
                }

                Text(
                    text = profile.username,
                    style = MaterialTheme.typography.bodySmall.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.4f),
                            offset = Offset.Zero,
                            blurRadius = 8f
                        )
                    ),
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatBoxItem(
                        count = profile.postCount,
                        label = "포스트",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    StatBoxItem(
                        count = profile.followerCount,
                        label = "팔로워",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = onFollowerClick
                    )
                    StatBoxItem(
                        count = profile.followingCount,
                        label = "팔로잉",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = onFollowingClick
                    )
                    if (profile.isCreator && profile.subscriberCount != null) {
                        StatBoxItem(
                            count = profile.subscriberCount,
                            label = "구독자",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            // 내 프로필만 구독자 목록 접근
                            onClick = if (profile.isOwnProfile) onSubscriberClick else null
                        )
                    }
                }
            }
        }

        // 소개
        if (!profile.bio.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = profile.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 위치 & 웹사이트
        val hasLocationOrWebsite = !profile.location.isNullOrBlank() || !profile.website.isNullOrBlank()
        if (hasLocationOrWebsite) {
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 위치
        if (!profile.location.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = profile.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 웹사이트
        if (!profile.website.isNullOrBlank()) {
            if (!profile.location.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onWebsiteClick() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_link),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = profile.website,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 팔로우 / 구독 / 메시지 버튼
        if (!profile.isOwnProfile) {
            val buttonHeight = 40.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 차단 중이면 차단 해제, 아니면 팔로우
                if (profile.isBlockedByMe) {
                    OutlinedButton(
                        onClick = onUnblockClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bounceClick(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("차단 해제")
                    }
                } else if (profile.isFollowing) {
                    OutlinedButton(
                        onClick = onFollowClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bounceClick(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text("팔로잉")
                    }
                } else {
                    Button(
                        onClick = onFollowClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bounceClick(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("팔로우")
                    }
                }

                // 구독 버튼, 플랜 있는 크리에이터만
                if (profile.isCreator && profile.hasSubscriptionPlans) {
                    if (profile.isSubscribed) {
                        // 구독중
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .bounceClick()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    brush = HaloGradient,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSubscribeClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "구독중",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = HaloGradientColors[1]
                            )
                        }
                    } else {
                        // 미구독
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .bounceClick()
                                .background(
                                    brush = HaloGradient,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSubscribeClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "멤버십",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }

                // 메시지 버튼
                OutlinedButton(
                    onClick = onMessageClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .bounceClick(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text("메시지")
                }
            }
        } else {
            // 내 프로필은 편집 버튼
            OutlinedButton(
                onClick = onEditProfileClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("프로필 편집")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatBoxItem(
    count: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val interactiveModifier = if (onClick != null) {
        modifier
            .bounceClick()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    } else {
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.4f))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = interactiveModifier.padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        AnimatedCounter(
            count = count,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = Pretendard
            ),
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
