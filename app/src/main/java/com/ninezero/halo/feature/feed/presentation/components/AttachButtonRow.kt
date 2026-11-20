package com.ninezero.halo.feature.feed.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R

/** 사진 · 영상 · 카메라 첨부 버튼 */
@Composable
internal fun AttachButtonRow(
    startPadding: Dp,
    showVideoButton: Boolean,  // 커뮤니티는 영상 불가
    onImageClick: () -> Unit,
    onVideoClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Row(
        modifier = Modifier.padding(start = startPadding, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_image),
            contentDescription = "이미지 첨부",
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onImageClick() },
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (showVideoButton) {
            Icon(
                painter = painterResource(R.drawable.ic_video),
                contentDescription = "동영상 첨부",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onVideoClick() },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_camera),
            contentDescription = "카메라 촬영",
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onCameraClick() },
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
