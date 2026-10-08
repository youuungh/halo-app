package com.ninezero.halo.feature.profile.presentation.my_profile.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.profile_image_viewer.LocalProfileImageOverlayVisible
import com.ninezero.halo.core.ui.components.profile_image_viewer.LocalProfileImageTransitionScope
import com.ninezero.halo.core.ui.components.profile_image_viewer.PROFILE_IMAGE_SHARED_KEY
import com.ninezero.halo.core.util.launchCustomTab
import com.ninezero.halo.feature.profile.domain.model.UserProfile
import com.ninezero.halo.feature.profile.presentation.components.ProfileMenuGrid
import com.ninezero.halo.feature.profile.presentation.components.StatItem

@Composable
fun MyProfileHeaderContent(
    profile: UserProfile,
    onEditClick: () -> Unit,
    onProfileImageClick: () -> Unit = {},
    onOrderClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onWishlistClick: () -> Unit = {},
    onReviewClick: () -> Unit = {},
    onPointClick: () -> Unit = {},
    onCouponClick: () -> Unit = {},
    onMembershipClick: () -> Unit = {},
    onAddressClick: () -> Unit = {},
    onMembershipManageClick: () -> Unit = {},
    onStoreClick: () -> Unit = {},
    onSalesClick: () -> Unit = {},
    onCouponManageClick: () -> Unit = {},
    onFollowerClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    onSubscriberClick: () -> Unit = {}
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
            // 프로필 이미지
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

            // 오버레이와 같은 캐시 키
            val context = LocalContext.current
            val avatarData = profile.avatarThumbUrl ?: profile.avatarUrl
            val avatarRequest = remember(avatarData, context) {
                avatarData?.let {
                    ImageRequest.Builder(context)
                        .data(it)
                        .memoryCacheKey(it)
                        .placeholderMemoryCacheKey(it)
                        .size(Size.ORIGINAL)
                        // 투명 PNG 아바타는 crossfade를 끔
                        .crossfade(false)
                        .build()
                }
            }

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
                            )
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    VerifiedBadge(role = profile.role, size = 18.dp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                val showSubscriberCount = profile.isCreator && profile.subscriberCount != null
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatItem(
                        count = profile.postCount,
                        label = "포스트",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        count = profile.followerCount,
                        label = "팔로워",
                        onClick = onFollowerClick,
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        count = profile.followingCount,
                        label = "팔로잉",
                        onClick = onFollowingClick,
                        modifier = Modifier.weight(1f)
                    )
                    if (showSubscriberCount) {
                        StatItem(
                            count = profile.subscriberCount,
                            label = "구독자",
                            onClick = onSubscriberClick,
                            modifier = Modifier.weight(1f)
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

        // 위치
        if (!profile.location.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
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
            val context = LocalContext.current
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { context.launchCustomTab(profile.website) }
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

        // 프로필 편집 버튼
        OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("프로필 편집")
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfileMenuGrid(
            isCreator = profile.isCreator,
            onOrderClick = onOrderClick,
            onCartClick = onCartClick,
            onWishlistClick = onWishlistClick,
            onReviewClick = onReviewClick,
            onPointClick = onPointClick,
            onCouponClick = onCouponClick,
            onMembershipClick = onMembershipClick,
            onAddressClick = onAddressClick,
            onMembershipManageClick = onMembershipManageClick,
            onStoreClick = onStoreClick,
            onSalesClick = onSalesClick,
            onCouponManageClick = onCouponManageClick
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
