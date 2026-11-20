package com.ninezero.halo.feature.profile.presentation.follow_list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.button.FollowButton
import com.ninezero.halo.core.ui.components.button.OutlinedPillButton
import com.ninezero.halo.core.ui.components.media.Avatar
import com.ninezero.halo.core.ui.components.popup.HaloPopupMenu
import dev.chrisbanes.haze.HazeState

@Composable
fun FollowListItem(
    hazeState: HazeState?,
    user: UserSummary,
    isMyProfile: Boolean,
    isFollowingTab: Boolean,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit,
    onUnfollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPopupMenu by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지
        Avatar(
            model = user.avatarThumbUrl ?: user.avatarUrl,
            size = 40.dp,
            contentDescription = "${user.displayName} 프로필"
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 이름 + 유저네임
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.displayName ?: user.username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                VerifiedBadge(role = user.role, size = 14.dp)
            }
            Text(
                text = user.username,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 내 팔로잉 탭은 메시지와 더보기, 나머지는 팔로우 버튼
        if (isMyProfile && isFollowingTab && user.isFollowing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedPillButton(text = "메시지 보내기", onClick = onMessageClick)

                Spacer(modifier = Modifier.width(8.dp))

                // 더보기 버튼
                Box {
                    Icon(
                        painter = painterResource(R.drawable.ic_ellipsis_horizontal),
                        contentDescription = "더보기",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showPopupMenu = true },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (showPopupMenu) {
                        HaloPopupMenu(
                            hazeState = hazeState,
                            alignment = Alignment.TopEnd,
                            onDismiss = { showPopupMenu = false },
                            offset = IntOffset(24, with(density) { 24.dp.roundToPx() })
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        showPopupMenu = false
                                        onUnfollowClick()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_circle_minus),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "팔로우 취소",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // 내 팔로워 탭에서 팔로우 안 한 사람은 맞팔로우
            FollowButton(
                isFollowing = user.isFollowing,
                onClick = onFollowClick,
                followText = if (isMyProfile && !isFollowingTab) "맞팔로우" else "팔로우"
            )
        }
    }
}
