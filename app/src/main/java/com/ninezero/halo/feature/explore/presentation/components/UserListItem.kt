package com.ninezero.halo.feature.explore.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.util.toFormattedFollower
import com.ninezero.halo.ui.theme.HaloTheme

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
        AsyncImage(
            model = avatarUrl,
            contentDescription = "$displayName 프로필",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_profile_placeholder),
            error = painterResource(R.drawable.ic_profile_placeholder),
            fallback = painterResource(R.drawable.ic_profile_placeholder)
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

        if (isFollowing) {
            OutlinedButton(
                onClick = onFollowClick,
                modifier = Modifier
                    .height(32.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = "팔로잉",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Button(
                onClick = onFollowClick,
                modifier = Modifier
                    .height(32.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "팔로우",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
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
