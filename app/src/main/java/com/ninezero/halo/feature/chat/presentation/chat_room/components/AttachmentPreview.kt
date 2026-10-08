package com.ninezero.halo.feature.chat.presentation.chat_room.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.core.util.toReadableFileSize
import com.ninezero.halo.core.util.toVideoDuration
import com.ninezero.halo.feature.chat.presentation.chat_room.AttachmentState
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun AttachmentPreviewArea(
    images: List<AttachmentState>,
    video: AttachmentState?,
    file: AttachmentState?,
    onRemoveImage: (Int) -> Unit,
    onRemoveVideo: () -> Unit,
    onRemoveFile: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 14.dp
) {
    val hasAttachment = images.isNotEmpty() || video != null || file != null

    AnimatedVisibility(
        visible = hasAttachment,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (images.isNotEmpty()) {
                ImageAttachmentPreview(
                    images = images,
                    onRemove = onRemoveImage,
                    horizontalPadding = horizontalPadding
                )
            }

            video?.let {
                VideoAttachmentPreview(
                    video = it,
                    onRemove = onRemoveVideo,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }

            file?.let {
                FileAttachmentPreview(
                    file = it,
                    onRemove = onRemoveFile,
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
            }
        }
    }
}

@Composable
private fun ImageAttachmentPreview(
    images: List<AttachmentState>,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 14.dp
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = horizontalPadding)
    ) {
        itemsIndexed(images) { index, image ->
            ImageThumbnailItem(
                image = image,
                onRemove = { onRemove(index) }
            )
        }
    }
}

@Composable
private fun ImageThumbnailItem(
    image: AttachmentState,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        when {
            image.thumbnail != null -> {
                Image(
                    bitmap = image.thumbnail.asImageBitmap(),
                    contentDescription = "이미지 미리보기",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            image.uri != Uri.EMPTY -> {
                AsyncImage(
                    model = image.uri,
                    contentDescription = "이미지 미리보기",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_image),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        RemoveButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
        )
    }
}

@Composable
private fun VideoAttachmentPreview(
    video: AttachmentState,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 100.dp, height = 75.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        if (video.thumbnail != null) {
            Image(
                bitmap = video.thumbnail.asImageBitmap(),
                contentDescription = "비디오 미리보기",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_video),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (video.duration > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.duration.toVideoDuration(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontSize = 9.sp
                )
            }
        }

        RemoveButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
        )
    }
}

@Composable
private fun FileAttachmentPreview(
    file: AttachmentState,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extension = MediaUtils.fileExtension(file.fileName)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_file),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = file.fileName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$extension · ${file.fileSize.toReadableFileSize()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = "삭제",
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .clickable { onRemove() },
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RemoveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = "삭제",
            modifier = Modifier.size(12.dp),
            tint = Color.White
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ImageAttachmentPreviewPreview() {
    HaloTheme {
        AttachmentPreviewArea(
            images = listOf(
                AttachmentState(
                    uri = Uri.EMPTY,
                    fileName = "image1.jpg",
                    fileSize = 1024 * 500
                ),
                AttachmentState(
                    uri = Uri.EMPTY,
                    fileName = "image2.jpg",
                    fileSize = 1024 * 800
                ),
                AttachmentState(
                    uri = Uri.EMPTY,
                    fileName = "image3.jpg",
                    fileSize = 1024 * 300
                )
            ),
            video = null,
            file = null,
            onRemoveImage = {},
            onRemoveVideo = {},
            onRemoveFile = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun VideoAttachmentPreviewPreview() {
    HaloTheme {
        AttachmentPreviewArea(
            images = emptyList(),
            video = AttachmentState(
                uri = Uri.EMPTY,
                fileName = "video.mp4",
                fileSize = 1024 * 1024 * 15,
                duration = 40_000,  // 40초
            ),
            file = null,
            onRemoveImage = {},
            onRemoveVideo = {},
            onRemoveFile = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun FileAttachmentPreviewPreview() {
    HaloTheme {
        AttachmentPreviewArea(
            images = emptyList(),
            video = null,
            file = AttachmentState(
                uri = Uri.EMPTY,
                fileName = "document.pdf",
                fileSize = 1024 * 1024 * 2 + 500 * 1024,  // 2.5MB
            ),
            onRemoveImage = {},
            onRemoveVideo = {},
            onRemoveFile = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MultipleAttachmentsPreview() {
    HaloTheme {
        AttachmentPreviewArea(
            images = listOf(
                AttachmentState(
                    uri = Uri.EMPTY,
                    fileName = "photo1.jpg",
                    fileSize = 1024 * 600
                ),
                AttachmentState(
                    uri = Uri.EMPTY,
                    fileName = "photo2.jpg",
                    fileSize = 1024 * 450
                )
            ),
            video = null,
            file = AttachmentState(
                uri = Uri.EMPTY,
                fileName = "report.xlsx",
                fileSize = 1024 * 350
            ),
            onRemoveImage = {},
            onRemoveVideo = {},
            onRemoveFile = {}
        )
    }
}
