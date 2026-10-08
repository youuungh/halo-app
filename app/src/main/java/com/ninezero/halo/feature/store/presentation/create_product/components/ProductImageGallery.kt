package com.ninezero.halo.feature.store.presentation.create_product.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun ProductImageGallery(
    existingImageUrls: List<String>,
    selectedImageUris: List<Uri>,
    onAddImagesClick: () -> Unit,
    onRemoveExistingImage: (String) -> Unit,
    onRemoveNewImage: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    val imageCornerShape = RoundedCornerShape(12.dp)
    val hasImages = existingImageUrls.isNotEmpty() || selectedImageUris.isNotEmpty()
    val totalImageCount = existingImageUrls.size + selectedImageUris.size

    Column(
        modifier = modifier.padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(onClick = onAddImagesClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "이미지 추가",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$totalImageCount / 10",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }

        if (hasImages) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(existingImageUrls, key = { it }) { url ->
                    ImageThumbnail(
                        model = url,
                        contentDescription = "상품 이미지",
                        cornerShape = imageCornerShape,
                        placeholderColor = placeholderColor,
                        onRemove = { onRemoveExistingImage(url) }
                    )
                }
                items(selectedImageUris, key = { it.toString() }) { uri ->
                    ImageThumbnail(
                        model = uri,
                        contentDescription = "선택된 이미지",
                        cornerShape = imageCornerShape,
                        placeholderColor = placeholderColor,
                        onRemove = { onRemoveNewImage(uri) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageThumbnail(
    model: Any?,
    contentDescription: String,
    cornerShape: RoundedCornerShape,
    placeholderColor: Color,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .aspectRatio(1f)
            .clip(cornerShape)
    ) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(placeholderColor),
            error = ColorPainter(placeholderColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "이미지 삭제",
                modifier = Modifier.size(14.dp),
                tint = Color.White
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductImageGalleryPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                ProductImageGallery(
                    existingImageUrls = List(2) { "https://picsum.photos/300/300?random=$it" },
                    selectedImageUris = emptyList(),
                    onAddImagesClick = {},
                    onRemoveExistingImage = {},
                    onRemoveNewImage = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductImageGalleryEmptyPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                ProductImageGallery(
                    existingImageUrls = emptyList(),
                    selectedImageUris = emptyList(),
                    onAddImagesClick = {},
                    onRemoveExistingImage = {},
                    onRemoveNewImage = {}
                )
            }
        }
    }
}
