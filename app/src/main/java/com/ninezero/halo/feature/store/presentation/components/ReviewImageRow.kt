package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun ReviewImageRow(
    images: List<String>,
    startPadding: Dp,
    endPadding: Dp,
    onImageClick: (images: List<String>, index: Int) -> Unit
) {
    if (images.isEmpty()) return

    val isSingleMedia = images.size == 1
    val baseMediaSize = if (isSingleMedia) 300.dp else 200.dp
    val imagePadding = 4.dp
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant

    if (isSingleMedia) {
        AsyncImage(
            model = images.first(),
            contentDescription = "리뷰 이미지",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = startPadding, end = endPadding)
                .padding(vertical = imagePadding)
                .height(baseMediaSize)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onImageClick(images, 0) },
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(placeholderColor),
            error = ColorPainter(placeholderColor)
        )
    } else {
        LazyRow(
            modifier = Modifier.padding(vertical = imagePadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = startPadding, end = endPadding)
        ) {
            items(images.size) { index ->
                AsyncImage(
                    model = images[index],
                    contentDescription = "리뷰 이미지",
                    modifier = Modifier
                        .size(baseMediaSize)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onImageClick(images, index) },
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(placeholderColor),
                    error = ColorPainter(placeholderColor)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@HaloLightDarkPreview
@Composable
private fun ReviewImageRowPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                ReviewImageRow(
                    images = List(5) { "https://picsum.photos/200/200?random=$it" },
                    startPadding = 16.dp,
                    endPadding = 16.dp,
                    onImageClick = { _, _ -> }
                )
            }
        }
    }
}
