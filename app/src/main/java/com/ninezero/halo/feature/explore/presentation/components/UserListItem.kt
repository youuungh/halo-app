package com.ninezero.halo.feature.explore.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.button.FollowButton
import com.ninezero.halo.core.ui.components.media.Avatar
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.util.toFormattedFollower

@Composable
fun UserListItem(
    avatarUrl: String?,
    displayName: String,
    username: String,
    followerCount: Int,
    role: String?,
    isFollowing: Boolean,
    onClick: () -> Unit,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Avatar(
            model = avatarUrl,
            size = 36.dp,
            contentDescription = "$displayName 프로필"
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                VerifiedBadge(role = role, size = 16.dp)
            }
            Text(
                text = username,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "팔로워 ${followerCount.toFormattedFollower()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        FollowButton(isFollowing = isFollowing, onClick = onFollowClick)
    }
}

@Preview(name = "Not Following")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Not Following Dark")
@Composable
private fun UserListItemPreview() {
    HaloTheme {
        Surface {
            UserListItem(
                avatarUrl = null,
                displayName = "크리에이터",
                username = "creator",
                followerCount = 1299,
                role = "CREATOR",
                isFollowing = false,
                onClick = {},
                onFollowClick = {}
            )
        }
    }
}

@Preview(name = "Following")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Following Dark")
@Composable
private fun UserListItemFollowingPreview() {
    HaloTheme {
        Surface {
            UserListItem(
                avatarUrl = null,
                displayName = "사용자",
                username = "user123",
                followerCount = 18500,
                role = "USER",
                isFollowing = true,
                onClick = {},
                onFollowClick = {}
            )
        }
    }
}
