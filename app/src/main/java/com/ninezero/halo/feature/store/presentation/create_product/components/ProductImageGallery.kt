package com.ninezero.halo.feature.store.presentation.create_product.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.input.inputBorder
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme

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
    val imageCornerShape = MaterialTheme.shapes.medium
    val totalImageCount = existingImageUrls.size + selectedImageUris.size

    // 사진 줄 맨 앞이 추가 칸, 첫 사진은 대표
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 20.dp)
    ) {
        item(key = "add") {
            AddImageTile(count = totalImageCount, onClick = onAddImagesClick)
        }
        items(existingImageUrls, key = { it }) { url ->
            ImageThumbnail(
                model = url,
                contentDescription = "상품 이미지",
                cornerShape = imageCornerShape,
                placeholderColor = placeholderColor,
                isMainImage = url == existingImageUrls.firstOrNull(),
                onRemove = { onRemoveExistingImage(url) }
            )
        }
        items(selectedImageUris, key = { it.toString() }) { uri ->
            ImageThumbnail(
                model = uri,
                contentDescription = "선택된 이미지",
                cornerShape = imageCornerShape,
                placeholderColor = placeholderColor,
                isMainImage = existingImageUrls.isEmpty() && uri == selectedImageUris.firstOrNull(),
                onRemove = { onRemoveNewImage(uri) }
            )
        }
    }
}

/** 카메라 아이콘 + 'N / 10' 추가 칸 */
@Composable
private fun AddImageTile(count: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .size(ImageTileSize)
            .clip(shape)
            .inputBorder(shape)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_camera),
            contentDescription = "이미지 추가",
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$count / 10",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ImageThumbnail(
    model: Any?,
    contentDescription: String,
    cornerShape: Shape,
    placeholderColor: Color,
    isMainImage: Boolean,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(ImageTileSize)
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
        if (isMainImage) {
            Text(
                text = "대표",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.55f), MaterialTheme.shapes.extraSmall)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            )
        }
    }
}

private val ImageTileSize = 88.dp

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
