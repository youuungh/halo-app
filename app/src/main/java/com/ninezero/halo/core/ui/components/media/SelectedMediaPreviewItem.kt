package com.ninezero.halo.core.ui.components.media

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

/** 첨부 전에 고른 사진 · 영상 한 칸, 삭제 버튼 */
@Composable
internal fun SelectedMediaPreviewItem(
    uri: Uri,
    isVideo: Boolean,
    placeholderColor: Color,
    modifier: Modifier = Modifier,
    onRemove: () -> Unit
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = uri,
            contentDescription = if (isVideo) "선택된 동영상" else "선택된 이미지",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(placeholderColor),
            error = ColorPainter(placeholderColor)
        )

        if (isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = "동영상",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = if (isVideo) "동영상 삭제" else "이미지 삭제",
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SelectedMediaPreviewItemPreview() {
    HaloSurfacePreview {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectedMediaPreviewItem(
                uri = "content://preview/image".toUri(),
                isVideo = false,
                placeholderColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(width = 135.dp, height = 180.dp),
                onRemove = {}
            )
            SelectedMediaPreviewItem(
                uri = "content://preview/video".toUri(),
                isVideo = true,
                placeholderColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(width = 135.dp, height = 180.dp),
                onRemove = {}
            )
        }
    }
}
