package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R

/** 상품 사진, 멤버십 전용은 흐림 + 자물쇠 */
@Composable
fun ProductStatusImage(
    imageUrl: String?,
    contentDescription: String?,
    isMembersOnly: Boolean,
    isUnavailable: Boolean,
    size: Dp,
    lockIconSize: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(MaterialTheme.shapes.medium)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier
                .matchParentSize()
                .then(if (isMembersOnly) Modifier.blur(8.dp) else Modifier)
                .alpha(if (isUnavailable && !isMembersOnly) 0.45f else 1f),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_image_placeholder),
            error = painterResource(R.drawable.ic_image_placeholder),
            fallback = painterResource(R.drawable.ic_image_placeholder)
        )
        if (isMembersOnly) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.28f))
            )
            Icon(
                painter = painterResource(R.drawable.ic_lock),
                contentDescription = null,
                modifier = Modifier.size(lockIconSize),
                tint = Color.White
            )
        }
    }
}
