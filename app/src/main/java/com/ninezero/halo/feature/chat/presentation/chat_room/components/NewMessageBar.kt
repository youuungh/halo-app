package com.ninezero.halo.feature.chat.presentation.chat_room.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import dev.chrisbanes.haze.HazeState

@Composable
fun NewMessageBar(
    visible: Boolean,
    messageType: ChatMessageType,
    messageContent: String?,
    onClick: () -> Unit,
    hazeState: HazeState,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.medium

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        // 입력창 위 12, 맨 아래로 버튼과 같은 높이
        Row(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
                .fillMaxWidth()
                .height(40.dp)  // 입력창과 같은 높이
                // 숫자 알약과 같은 블러, 경계는 테두리로
                .clip(shape)
                .haloBlur(hazeState, HaloBlurs.glassField(backgroundColor))
                .cardBorder(shape)
                .clickable { onClick() }
                .padding(start = 14.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isMedia = messageType != ChatMessageType.TEXT

            Text(
                text = when (messageType) {
                    ChatMessageType.IMAGE -> "사진을 보냈습니다"
                    ChatMessageType.VIDEO -> "동영상을 보냈습니다"
                    ChatMessageType.FILE -> "파일을 보냈습니다"
                    ChatMessageType.POST_LINK -> "포스트를 공유했습니다"
                    ChatMessageType.PRODUCT_LINK -> "상품을 공유했습니다"
                    else -> messageContent ?: ""
                },
                style = MaterialTheme.typography.bodyMedium,  // 말풍선과 같은 14
                color = if (isMedia) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                painter = painterResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
