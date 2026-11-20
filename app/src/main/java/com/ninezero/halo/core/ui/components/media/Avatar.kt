package com.ninezero.halo.core.ui.components.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

/** 프로필 사진, 원형 · 회색 바탕 · 사진이 없으면 기본 그림 */
@Composable
fun Avatar(
    model: Any?,
    size: Dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.ic_profile_placeholder),
        error = painterResource(R.drawable.ic_profile_placeholder),
        fallback = painterResource(R.drawable.ic_profile_placeholder)
    )
}

@HaloLightDarkPreview
@Composable
private fun AvatarPreview() {
    HaloSurfacePreview {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Avatar(model = null, size = 32.dp)
            Avatar(model = null, size = 48.dp)
        }
    }
}
