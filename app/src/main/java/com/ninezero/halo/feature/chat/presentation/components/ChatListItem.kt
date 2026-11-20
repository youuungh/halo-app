package com.ninezero.halo.feature.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.media.Avatar
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.chat.domain.model.Chat
import com.ninezero.halo.feature.chat.domain.model.LastMessageInfo

@Composable
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .bounceClick()
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            model = chat.otherUser.avatarThumbUrl ?: chat.otherUser.avatarUrl,
            size = 56.dp,
            contentDescription = "${chat.otherUser.displayName} 프로필"
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = chat.otherUser.displayName ?: chat.otherUser.username,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    VerifiedBadge(role = chat.otherUser.role, size = 14.dp)
                }

                chat.lastMessage?.let { lastMessage ->
                    Text(
                        text = lastMessage.timestamp.toRelativeTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val messagePreview = chat.lastMessage?.preview ?: "대화를 시작해보세요"

                Text(
                    text = messagePreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 24.dp)
                )

                if (chat.unreadCount > 0) {
                    UnreadBadge(count = chat.unreadCount)
                }
            }
        }
    }
}

@Composable
private fun UnreadBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    val displayCount = if (count > 99) "99+" else count.toString()

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayCount,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ChatListItemPreview() {
    HaloTheme {
        ChatListItem(
            chat = Chat(
                id = 1,
                otherUser = UserSummary(
                    id = 1,
                    username = "user1",
                    displayName = "김민수",
                    avatarUrl = "avatarUrl",
                    isFollowing = false
                ),
                lastMessage = LastMessageInfo(
                    preview = "안녕하세요! 오늘 시간 되시나요?",
                    timestamp = System.currentTimeMillis() - 3600000
                ),
                unreadCount = 3,
                isArchived = false,
                createdAt = System.currentTimeMillis()
            ),
            onClick = {},
            onLongClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ChatListItemNoUnreadPreview() {
    HaloTheme {
        ChatListItem(
            chat = Chat(
                id = 2,
                otherUser = UserSummary(
                    id = 2,
                    username = "user2",
                    displayName = "이지은",
                    avatarUrl = "avatarUrl",
                    isFollowing = true
                ),
                lastMessage = LastMessageInfo(
                    preview = "네, 감사합니다!",
                    timestamp = System.currentTimeMillis() - 86400000
                ),
                unreadCount = 0,
                isArchived = false,
                createdAt = System.currentTimeMillis()
            ),
            onClick = {},
            onLongClick = {}
        )
    }
}
