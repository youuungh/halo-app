package com.ninezero.halo.feature.chat.presentation.chat_room.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.modifiers.bounceClick

@Composable
fun ChatRoomProfileHeader(
    otherUser: UserSummary?,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = otherUser?.avatarThumbUrl ?: otherUser?.avatarUrl,
            contentDescription = "${otherUser?.displayName} 프로필",
            modifier = Modifier
                .size(88.dp)
                .bounceClick()  // 뒤 회색 원이 테두리로 비치지 않게 먼저 적용
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_profile_placeholder),
            error = painterResource(R.drawable.ic_profile_placeholder),
            fallback = painterResource(R.drawable.ic_profile_placeholder)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = otherUser?.displayName ?: otherUser?.username ?: "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            VerifiedBadge(role = otherUser?.role, size = 20.dp)
        }

        otherUser?.username?.let { username ->
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = username,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 0.5 테두리 작은 알약
        Box(
            modifier = Modifier
                .bounceClick()
                .height(28.dp)
                .clip(CircleShape)
                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .clickable(onClick = onProfileClick)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "프로필 보기",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
