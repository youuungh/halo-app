package com.ninezero.halo.feature.profile.presentation.user_profile.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.text.AnimatedCounter
import com.ninezero.halo.core.ui.components.viewer.LocalProfileImageOverlayVisible
import com.ninezero.halo.core.ui.components.viewer.LocalProfileImageTransitionScope
import com.ninezero.halo.core.ui.components.viewer.PROFILE_IMAGE_SHARED_KEY
import com.ninezero.halo.core.ui.theme.HaloGradient
import com.ninezero.halo.core.ui.theme.MembershipSparkle
import com.ninezero.halo.core.ui.theme.OnSurface
import com.ninezero.halo.core.ui.theme.Pretendard
import com.ninezero.halo.feature.profile.domain.model.UserProfile

private val HeaderTopGap = 4.dp
private val HeaderAvatarSize = 104.dp
private val AvatarTitleGap = 12.dp
private val NameLineHeight = 32.dp
private val UsernameLineHeight = 20.dp
private val CollapsedNameLineHeight = 23.dp

private val HeaderCardShape = RoundedCornerShape(22.dp)
private val CardTint = Color.Black.copy(alpha = 0.2f)
private val CardRim = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.1f)))
private val CardSecondaryText = Color.White.copy(alpha = 0.75f)

// 양옆 버튼을 피하는 톱바 안 이름 폭에 맞춘 값
private val TitleMaxWidth = 220.dp
private const val NAME_COLLAPSED_SCALE = 0.75f
private const val USERNAME_COLLAPSED_SCALE = 0.93f
private const val AVATAR_COLLAPSED_SCALE = 0.7f
private const val BIO_MAX_LINES = 3
private const val STAT_PRESSED_ALPHA = 0.16f
private val StatPressedRadius = 18.dp

private const val MEMBERSHIP_GRADIENT_ALPHA = 0.62f
private val CheckScaleSpec = spring<Float>(dampingRatio = 0.5f, stiffness = 500f)
private val CheckSizeSpec = spring<IntSize>(dampingRatio = 0.8f, stiffness = 500f)

/** 헤더 맨 위에서 이름까지의 거리 */
internal val ProfileHeaderTitleOffset = HeaderTopGap + HeaderAvatarSize + AvatarTitleGap

/** 마지막 카드 아래에서 커버가 끝나는 구간 */
internal val ProfileHeaderBottomGap = 24.dp

@Composable
fun UserProfileHeaderContent(
    profile: UserProfile,
    topPadding: Dp,
    avatarCollapse: () -> Float,
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
            .padding(start = 16.dp, top = topPadding + HeaderTopGap, end = 16.dp)
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
                .align(Alignment.CenterHorizontally)
                .size(HeaderAvatarSize)
                .graphicsLayer {
                    val collapse = avatarCollapse()
                    // 오프스크린이면 그림자가 사진 크기로 잘림
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                    alpha = 1f - collapse
                    scaleX = lerp(1f, AVATAR_COLLAPSED_SCALE, collapse)
                    scaleY = scaleX
                }
                .dropShadow(
                    CircleShape,
                    Shadow(radius = 28.dp, color = Color.Black.copy(alpha = 0.28f), offset = DpOffset(0.dp, 10.dp))
                )
                .then(avatarSharedModifier)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onProfileImageClick() },
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_profile_placeholder),
            error = painterResource(R.drawable.ic_profile_placeholder),
            fallback = painterResource(R.drawable.ic_profile_placeholder)
        )

        // 이름은 톱바로 옮겨 가야 해서 헤더 위 별도 층에 그림
        Spacer(modifier = Modifier.height(AvatarTitleGap + NameLineHeight + UsernameLineHeight + 14.dp))

        ProfileActionRow(
            profile = profile,
            onFollowClick = onFollowClick,
            onSubscribeClick = onSubscribeClick,
            onMessageClick = onMessageClick,
            onUnblockClick = onUnblockClick,
            onEditProfileClick = onEditProfileClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardTint, HeaderCardShape)
                .border(0.5.dp, CardRim, HeaderCardShape)
                .padding(4.dp)
        ) {
            HeaderStatItem(
                count = profile.postCount,
                label = "포스트"
            )
            HeaderStatItem(
                count = profile.followerCount,
                label = "팔로워",
                onClick = if (profile.isBlocked) null else onFollowerClick
            )
            HeaderStatItem(
                count = profile.followingCount,
                label = "팔로잉",
                onClick = if (profile.isBlocked) null else onFollowingClick
            )
            if (profile.isCreator && profile.subscriberCount != null) {
                HeaderStatItem(
                    count = profile.subscriberCount,
                    label = "구독자",
                    // 내 프로필만 구독자 목록 접근
                    onClick = if (profile.isOwnProfile) onSubscriberClick else null
                )
            }
        }

        val hasBio = !profile.bio.isNullOrBlank()
        val hasLocation = !profile.location.isNullOrBlank()
        val hasWebsite = !profile.website.isNullOrBlank()
        if (hasBio || hasLocation || hasWebsite) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardTint, HeaderCardShape)
                    .border(0.5.dp, CardRim, HeaderCardShape)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasBio) {
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = BIO_MAX_LINES,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (hasLocation) {
                    ProfileInfoRow(iconRes = R.drawable.ic_location, text = profile.location)
                }
                if (hasWebsite) {
                    ProfileInfoRow(
                        iconRes = R.drawable.ic_link,
                        text = profile.website,
                        onClick = onWebsiteClick
                    )
                }
            }
        }

        // 커버가 탭 위에서 끝나는 구간
        Spacer(modifier = Modifier.height(ProfileHeaderBottomGap))
    }
}

/** 팔로우, 구독, 메시지 버튼 */
@Composable
private fun ProfileActionRow(
    profile: UserProfile,
    onFollowClick: () -> Unit,
    onSubscribeClick: () -> Unit,
    onMessageClick: () -> Unit,
    onUnblockClick: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (profile.isOwnProfile) {
            CardActionButton("프로필 편집", onEditProfileClick)
        } else {
            // 차단 중이면 차단 해제, 아니면 팔로우
            if (profile.isBlockedByMe) {
                CardActionButton("차단 해제", onUnblockClick)
            } else {
                FollowActionButton(profile.isFollowing, onFollowClick)
            }

            // 구독 버튼, 플랜 있는 크리에이터만
            if (profile.isCreator && profile.hasSubscriptionPlans) {
                MembershipActionButton(profile.isSubscribed, onSubscribeClick)
            }

            CardActionButton("메시지", onMessageClick)
        }
    }
}

@Composable
private fun RowScope.ActionButtonFrame(
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .bounceClick()
            .then(modifier)
            .clip(CircleShape)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun ActionLabel(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}

@Composable
private fun RowScope.CardActionButton(text: String, onClick: () -> Unit) {
    ActionButtonFrame(
        modifier = Modifier
            .background(CardTint, CircleShape)
            .border(0.5.dp, CardRim, CircleShape),
        onClick = onClick
    ) {
        ActionLabel(text, Color.White)
    }
}

/** 팔로우 버튼 */
@Composable
private fun RowScope.FollowActionButton(isFollowing: Boolean, onClick: () -> Unit) {
    val colorSpec = tween<Color>(durationMillis = if (isFollowing) 220 else 120)
    val container by animateColorAsState(if (isFollowing) CardTint else Color.White, colorSpec, label = "followContainer")
    val content by animateColorAsState(if (isFollowing) Color.White else OnSurface, colorSpec, label = "followContent")

    ActionButtonFrame(
        modifier = Modifier
            .background(container, CircleShape)
            .then(if (isFollowing) Modifier.border(0.5.dp, CardRim, CircleShape) else Modifier),
        onClick = onClick
    ) {
        AnimatedVisibility(
            visible = isFollowing,
            enter = expandHorizontally(CheckSizeSpec) + scaleIn(CheckScaleSpec, initialScale = 0.4f) + fadeIn(),
            exit = shrinkHorizontally(tween(120)) + fadeOut(tween(100))
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(15.dp),
                tint = content
            )
        }
        ActionLabel(if (isFollowing) "팔로잉" else "팔로우", content)
    }
}

/** 멤버십 버튼 */
@Composable
private fun RowScope.MembershipActionButton(isSubscribed: Boolean, onClick: () -> Unit) {
    val gradientAlpha by animateFloatAsState(
        targetValue = if (isSubscribed) MEMBERSHIP_GRADIENT_ALPHA else 0f,
        animationSpec = tween(durationMillis = if (isSubscribed) 260 else 120),
        label = "membershipGradient"
    )

    ActionButtonFrame(
        modifier = Modifier
            .background(CardTint, CircleShape)
            .background(HaloGradient, CircleShape, alpha = gradientAlpha)
            .border(1.dp, HaloGradient, CircleShape),
        onClick = onClick
    ) {
        AnimatedContent(
            targetState = isSubscribed,
            transitionSpec = {
                val enter = if (targetState) scaleIn(CheckScaleSpec, initialScale = 0.4f) + fadeIn() else fadeIn(tween(120))
                enter togetherWith fadeOut(tween(80))
            },
            label = "membershipIcon"
        ) { subscribed ->
            Icon(
                painter = painterResource(if (subscribed) R.drawable.ic_check else R.drawable.ic_sparkle),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(15.dp),
                tint = if (subscribed) Color.White else MembershipSparkle
            )
        }
        ActionLabel(if (isSubscribed) "구독중" else "멤버십", Color.White)
    }
}

/** 헤더를 따라 올라오다 톱바에 멈추며 줄어드는 이름 */
@Composable
internal fun CollapsingProfileTitle(
    profile: UserProfile,
    expandedTop: Dp,
    collapsedTop: Dp,
    scrollOffset: () -> Float,
    topBarReveal: () -> Float
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val collapsedUsernameColor = onSurface.copy(alpha = 0.75f)

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = TitleMaxWidth)
                .height(NameLineHeight)
                .graphicsLayer {
                    val progress = titleProgress(expandedTop.toPx(), collapsedTop.toPx(), scrollOffset())
                    applyTitleMotion(lerp(expandedTop.toPx(), collapsedTop.toPx(), progress), progress, NAME_COLLAPSED_SCALE)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicText(
                text = profile.displayName,
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both
                    )
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = { lerp(Color.White, onSurface, topBarReveal()) }
            )
            VerifiedBadge(role = profile.role, size = 20.dp)
        }

        BasicText(
            text = profile.username,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = TitleMaxWidth)
                .graphicsLayer {
                    // 이름과 같은 진행도로 움직여 한 덩어리로 보임
                    val progress = titleProgress(expandedTop.toPx(), collapsedTop.toPx(), scrollOffset())
                    val nameTop = lerp(expandedTop.toPx(), collapsedTop.toPx(), progress)
                    val nameHeight = lerp(NameLineHeight.toPx(), CollapsedNameLineHeight.toPx(), progress)
                    applyTitleMotion(nameTop + nameHeight, progress, USERNAME_COLLAPSED_SCALE)
                },
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = { lerp(Color.White.copy(alpha = 0.8f), collapsedUsernameColor, topBarReveal()) }
        )
    }
}

private fun titleProgress(expandedTop: Float, collapsedTop: Float, scrollOffset: Float): Float =
    (-scrollOffset / (expandedTop - collapsedTop)).coerceIn(0f, 1f)

private fun GraphicsLayerScope.applyTitleMotion(top: Float, progress: Float, collapsedScale: Float) {
    translationY = top
    scaleX = lerp(1f, collapsedScale, progress)
    scaleY = scaleX
    transformOrigin = TransformOrigin(0.5f, 0f)
}

@Composable
private fun ProfileInfoRow(
    iconRes: Int,
    text: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = if (onClick != null) {
            Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
        } else {
            Modifier
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = CardSecondaryText
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = CardSecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RowScope.HeaderStatItem(
    count: Int,
    label: String,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressAlpha by animateFloatAsState(
        targetValue = if (isPressed) STAT_PRESSED_ALPHA else 0f,
        animationSpec = tween(durationMillis = if (isPressed) 100 else 240),
        label = "statPress"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .then(
                if (onClick != null) {
                    Modifier
                        .bounceClick()
                        .drawBehind {
                            drawRoundRect(
                                color = Color.White.copy(alpha = pressAlpha),
                                cornerRadius = CornerRadius(StatPressedRadius.toPx())
                            )
                        }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                } else {
                    Modifier
                }
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
            color = Color.White.copy(alpha = 0.78f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
