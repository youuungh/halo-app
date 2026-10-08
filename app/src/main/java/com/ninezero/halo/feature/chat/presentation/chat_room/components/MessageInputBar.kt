package com.ninezero.halo.feature.chat.presentation.chat_room.components

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.chat.presentation.chat_room.AttachmentState
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSending: Boolean = false,
    hazeState: HazeState? = null,
    backgroundColor: Color = Color.Transparent,
    isSearchMode: Boolean = false,
    searchResultIndex: Int = 0,
    searchResultCount: Int = 0,
    onPreviousResult: () -> Unit = {},
    onNextResult: () -> Unit = {},
    isAttachmentExpanded: Boolean = false,
    onAttachmentToggle: () -> Unit = {},
    onImageClick: () -> Unit = {},
    onVideoClick: () -> Unit = {},
    onFileClick: () -> Unit = {},
    selectedImages: List<AttachmentState> = emptyList(),
    selectedVideo: AttachmentState? = null,
    selectedFile: AttachmentState? = null,
    onRemoveImage: (Int) -> Unit = {},
    onRemoveVideo: () -> Unit = {},
    onRemoveFile: () -> Unit = {},
    canSend: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (hazeState != null) {
                    Modifier.hazeEffect(state = hazeState) {
                        style = HazeStyle(
                            backgroundColor = backgroundColor,
                            tint = HazeTint(backgroundColor),
                            noiseFactor = 0f
                        )
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0f,
                            endIntensity = 0.5f
                        )
                    }
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surface)
                }
            )
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            AnimatedContent(
                targetState = isSearchMode,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "InputBarContent"
            ) { searchMode ->
                if (searchMode) {
                    SearchNavigationContent(
                        currentIndex = searchResultIndex,
                        totalCount = searchResultCount,
                        hazeState = hazeState,
                        backgroundColor = backgroundColor,
                        onPreviousClick = onPreviousResult,
                        onNextClick = onNextResult
                    )
                } else {
                    MessageInputContent(
                        value = value,
                        onValueChange = onValueChange,
                        onSendClick = onSendClick,
                        isAttachmentExpanded = isAttachmentExpanded,
                        onAttachmentToggle = onAttachmentToggle,
                        isSending = isSending,
                        canSend = canSend,
                        hazeState = hazeState,
                        backgroundColor = backgroundColor,
                        selectedImages = selectedImages,
                        selectedVideo = selectedVideo,
                        selectedFile = selectedFile,
                        onRemoveImage = onRemoveImage,
                        onRemoveVideo = onRemoveVideo,
                        onRemoveFile = onRemoveFile
                    )
                }
            }
        }

        AttachmentExpandedPanel(
            isExpanded = isAttachmentExpanded && !isSearchMode,
            onImageClick = onImageClick,
            onVideoClick = onVideoClick,
            onFileClick = onFileClick,
            hazeState = hazeState,
            backgroundColor = backgroundColor
        )
    }
}

@Composable
private fun MessageInputContent(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isAttachmentExpanded: Boolean,
    onAttachmentToggle: () -> Unit,
    isSending: Boolean,
    canSend: Boolean,
    hazeState: HazeState?,
    backgroundColor: Color,
    selectedImages: List<AttachmentState> = emptyList(),
    selectedVideo: AttachmentState? = null,
    selectedFile: AttachmentState? = null,
    onRemoveImage: (Int) -> Unit = {},
    onRemoveVideo: () -> Unit = {},
    onRemoveFile: () -> Unit = {}
) {
    val attachmentIconRotation by animateFloatAsState(
        targetValue = if (isAttachmentExpanded) 45f else 0f,
        label = "attachmentRotation"
    )

    val hasAttachment = selectedImages.isNotEmpty() || selectedVideo != null || selectedFile != null

    val fieldGlassTint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        GlassIconButton(
            onClick = onAttachmentToggle,
            icon = painterResource(R.drawable.ic_plus),
            contentDescription = if (isAttachmentExpanded) "닫기" else "첨부",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            iconSize = 22.dp,
            modifier = Modifier
                .rotate(attachmentIconRotation)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .then(
                    if (hazeState != null) {
                        Modifier.hazeEffect(state = hazeState) {
                            style = HazeStyle(
                                backgroundColor = backgroundColor,
                                tint = HazeTint(fieldGlassTint),
                                blurRadius = 20.dp,
                                noiseFactor = 0f
                            )
                        }
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    }
                )
        ) {
            if (hasAttachment) {
                AttachmentPreviewArea(
                    images = selectedImages,
                    video = selectedVideo,
                    file = selectedFile,
                    onRemoveImage = onRemoveImage,
                    onRemoveVideo = onRemoveVideo,
                    onRemoveFile = onRemoveFile,
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalPadding = 14.dp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp)
                    .padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val inputTextStyle = MaterialTheme.typography.bodyMedium.copy(
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    ),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 6.dp),
                    textStyle = inputTextStyle.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (canSend) onSendClick()
                        }
                    ),
                    singleLine = false,
                    maxLines = 5,
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    text = "메시지 입력",
                                    style = inputTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .bounceClick(enabled = canSend)
                        .clip(CircleShape)
                        .clickable(
                            enabled = canSend,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onSendClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_up_circle),
                            contentDescription = "전송",
                            tint = if (canSend) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            },
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentExpandedPanel(
    isExpanded: Boolean,
    onImageClick: () -> Unit,
    onVideoClick: () -> Unit,
    onFileClick: () -> Unit,
    hazeState: HazeState?,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AttachmentTypeButton(
                icon = R.drawable.ic_image,
                label = "이미지",
                onClick = onImageClick,
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                modifier = Modifier.weight(1f)
            )
            AttachmentTypeButton(
                icon = R.drawable.ic_video,
                label = "비디오",
                onClick = onVideoClick,
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                modifier = Modifier.weight(1f)
            )
            AttachmentTypeButton(
                icon = R.drawable.ic_file,
                label = "파일",
                onClick = onFileClick,
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AttachmentTypeButton(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    hazeState: HazeState?,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val glassTint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    Column(
        modifier = modifier
            .bounceClick()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (hazeState != null) {
                    Modifier.hazeEffect(state = hazeState) {
                        style = HazeStyle(
                            backgroundColor = backgroundColor,
                            tint = HazeTint(glassTint),
                            blurRadius = 20.dp,
                            noiseFactor = 0f
                        )
                    }
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                }
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchNavigationContent(
    currentIndex: Int,
    totalCount: Int,
    hazeState: HazeState?,
    backgroundColor: Color,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val hasResults = totalCount > 0
    val canGoPrevious = hasResults && currentIndex > 0
    val canGoNext = hasResults && currentIndex < totalCount - 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(
            onClick = onPreviousClick,
            icon = painterResource(R.drawable.ic_chevron_up),
            contentDescription = "이전 결과",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            enabled = canGoPrevious
        )

        Text(
            text = if (hasResults) {
                "${currentIndex + 1}/$totalCount"
            } else {
                "검색결과 없음"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (hasResults) FontWeight.Medium else FontWeight.Normal,
            color = if (hasResults) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        GlassIconButton(
            onClick = onNextClick,
            icon = painterResource(R.drawable.ic_chevron_down),
            contentDescription = "다음 결과",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            enabled = canGoNext
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageInputBarPreview() {
    HaloTheme {
        MessageInputBar(
            value = "",
            onValueChange = {},
            onSendClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageInputBarWithTextPreview() {
    HaloTheme {
        MessageInputBar(
            value = "안녕하세요!",
            onValueChange = {},
            onSendClick = {},
            canSend = true
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageInputBarSendingPreview() {
    HaloTheme {
        MessageInputBar(
            value = "전송 중...",
            onValueChange = {},
            onSendClick = {},
            isSending = true
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageInputBarAttachmentExpandedPreview() {
    HaloTheme {
        MessageInputBar(
            value = "",
            onValueChange = {},
            onSendClick = {},
            isAttachmentExpanded = true,
            onAttachmentToggle = {},
            onImageClick = {},
            onVideoClick = {},
            onFileClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageInputBarWithImagesPreview() {
    HaloTheme {
        MessageInputBar(
            value = "이미지 첨부 테스트",
            onValueChange = {},
            onSendClick = {},
            canSend = true,
            selectedImages = listOf(
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
                ),
                AttachmentState(
                    uri = Uri.EMPTY,
                    fileName = "image4.jpg",
                    fileSize = 1024 * 650
                ),
                AttachmentState(
                    uri = Uri.EMPTY,
                    fileName = "image5.jpg",
                    fileSize = 1024 * 420
                ),
                AttachmentState(
                    uri = Uri.EMPTY,
                    fileName = "image6.jpg",
                    fileSize = 1024 * 780
                ),
                AttachmentState(
                    uri = Uri.EMPTY,
                    fileName = "image7.jpg",
                    fileSize = 1024 * 550
                )
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageInputBarWithVideoPreview() {
    HaloTheme {
        MessageInputBar(
            value = "",
            onValueChange = {},
            onSendClick = {},
            canSend = true,
            selectedVideo = AttachmentState(
                uri = Uri.EMPTY,
                fileName = "video.mp4",
                fileSize = 1024 * 1024 * 15,
                duration = 40_000
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageInputBarWithFilePreview() {
    HaloTheme {
        MessageInputBar(
            value = "파일 첨부합니다",
            onValueChange = {},
            onSendClick = {},
            canSend = true,
            selectedFile = AttachmentState(
                uri = Uri.EMPTY,
                fileName = "document.pdf",
                fileSize = 1024 * 1024 * 2
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageInputBarSearchModePreview() {
    HaloTheme {
        MessageInputBar(
            value = "",
            onValueChange = {},
            onSendClick = {},
            isSearchMode = true,
            searchResultIndex = 2,
            searchResultCount = 10,
            onPreviousResult = {},
            onNextResult = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MessageInputBarSearchModeNoResultsPreview() {
    HaloTheme {
        MessageInputBar(
            value = "",
            onValueChange = {},
            onSendClick = {},
            isSearchMode = true,
            searchResultIndex = 0,
            searchResultCount = 0,
            onPreviousResult = {},
            onNextResult = {}
        )
    }
}
