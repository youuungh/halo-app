package com.ninezero.halo.feature.profile.presentation.edit_profile.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.media.Avatar

@Composable
internal fun AvatarSection(
    avatarUrl: String?,
    selectedAvatarUri: Uri?,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onAvatarClick() },
        contentAlignment = Alignment.Center
    ) {
        Avatar(
            model = selectedAvatarUri ?: avatarUrl,
            size = 120.dp,
            contentDescription = "프로필 이미지"
        )

        // 카메라 아이콘 오버레이
        Box(
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-2).dp, y = (-2).dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shape = CircleShape
                )
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = "사진 변경",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
