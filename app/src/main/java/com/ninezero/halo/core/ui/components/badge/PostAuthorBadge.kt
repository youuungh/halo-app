package com.ninezero.halo.core.ui.components.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

/** 포스트 작성자가 댓글에 단 답글 표시 */
@Composable
fun PostAuthorBadge(modifier: Modifier = Modifier) {
    Text(
        text = "작성자",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        modifier = modifier
            .padding(start = 6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    )
}

@HaloLightDarkPreview
@Composable
private fun PostAuthorBadgePreview() {
    HaloSurfacePreview {
        PostAuthorBadge(modifier = Modifier.padding(16.dp))
    }
}
