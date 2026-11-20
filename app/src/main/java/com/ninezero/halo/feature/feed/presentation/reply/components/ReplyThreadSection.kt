package com.ninezero.halo.feature.feed.presentation.reply.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.media.Avatar
import com.ninezero.halo.core.ui.components.media.MediaDisplayPolicy
import com.ninezero.halo.core.ui.components.media.MediaPreviewSizes
import com.ninezero.halo.core.ui.components.media.SelectedMediaRow
import com.ninezero.halo.core.ui.components.media.VideoPlayer
import com.ninezero.halo.core.ui.components.media.clampedAspectRatio
import com.ninezero.halo.core.ui.components.media.preferredAspectRatio
import com.ninezero.halo.core.ui.components.media.rememberAnimatedImage
import com.ninezero.halo.core.ui.components.media.singleMediaHeightForWidth
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewMediaItem
import com.ninezero.halo.core.ui.preview.previewUserProfile
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.presentation.components.AttachButtonRow
import com.ninezero.halo.feature.profile.domain.model.UserProfile

@Composable
internal fun ReplyThreadSection(
    authorName: String,
    authorAvatarUrl: String?,
    content: String,
    mediaAttachments: List<MediaItem>,
    replyToName: String,
    myProfile: UserProfile?,
    replyContent: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    authorRole: String? = null,
    selectedMediaUris: List<Uri> = emptyList(),
    onImageClick: () -> Unit = {},
    onVideoClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onRemoveMedia: (Uri) -> Unit = {},
    hasContent: Boolean = false,
    onClearClick: () -> Unit = {},
    onProfileClick: (IntOffset) -> Unit = {},
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val horizontalPadding = 8.dp
    val avatarSize = 40.dp
    val avatarSpacing = 12.dp
    val contentStartPadding = horizontalPadding + avatarSize + avatarSpacing  // 8 + 40 + 12 = 60dp
    val lineWidth = 1.5.dp
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    var profilePosition by remember { mutableStateOf(IntOffset.Zero) }

    Column(modifier = modifier.fillMaxWidth()) {
        // 원본 포스트 섹션
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(top = 12.dp)
                .height(IntrinsicSize.Min)
        ) {
            // 왼쪽: 프로필 + 연결선
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(avatarSize)
            ) {
                AsyncImage(
                    model = authorAvatarUrl,
                    contentDescription = "프로필",
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInWindow()
                            profilePosition = IntOffset(
                                position.x.toInt(),
                                position.y.toInt() + coordinates.size.height
                            )
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onProfileClick(profilePosition) },
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_profile_placeholder),
                    error = painterResource(R.drawable.ic_profile_placeholder),
                    fallback = painterResource(R.drawable.ic_profile_placeholder)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .width(lineWidth)
                        .weight(1f)
                        .background(lineColor)
                )
            }

            Spacer(modifier = Modifier.width(avatarSpacing))

            // 오른쪽: 텍스트 컨텐츠
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = authorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    VerifiedBadge(role = authorRole, size = 14.dp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 미디어 섹션
        OriginalPostMedia(
            mediaAttachments = mediaAttachments,
            horizontalPadding = horizontalPadding,
            avatarSize = avatarSize,
            contentStartPadding = contentStartPadding,
            lineWidth = lineWidth,
            lineColor = lineColor,
            placeholderColor = placeholderColor
        )

        // 원본 포스트와 답글 입력 섹션 사이 여백
        Spacer(modifier = Modifier.height(12.dp))

        // 답글 입력 섹션
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
        ) {
            // 내 프로필
            Avatar(
                model = myProfile?.avatarThumbUrl ?: myProfile?.avatarUrl,
                size = avatarSize,
                contentDescription = "내 프로필"
            )

            Spacer(modifier = Modifier.width(avatarSpacing))

            // 입력 영역
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = myProfile?.displayName?.takeIf { it.isNotBlank() } ?: myProfile?.username ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (hasContent) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "내용 삭제",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(18.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onClearClick() },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // hint·text 수직 정렬 일치
                val replyTextStyle = MaterialTheme.typography.bodyMedium.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.None
                    )
                )
                BasicTextField(
                    value = replyContent,
                    onValueChange = onContentChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = replyTextStyle.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (replyContent.isEmpty()) {
                                Text(
                                    text = "${replyToName}님에게 답글 남기기",
                                    style = replyTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        // 선택된 미디어 미리보기
        if (selectedMediaUris.isNotEmpty()) {
            SelectedMediaRow(
                uris = selectedMediaUris,
                startPadding = contentStartPadding,
                endPadding = horizontalPadding,
                placeholderColor = placeholderColor,
                sizes = MediaPreviewSizes.Comment,
                onRemove = onRemoveMedia
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 이미지·비디오·카메라 첨부 버튼
        AttachButtonRow(
            startPadding = contentStartPadding,
            showVideoButton = true,
            onImageClick = onImageClick,
            onVideoClick = onVideoClick,
            onCameraClick = onCameraClick
        )
    }
}

/** 원본 글 미디어, 없으면 연결선만 이어 줌 */
@Composable
private fun OriginalPostMedia(
    mediaAttachments: List<MediaItem>,
    horizontalPadding: Dp,
    avatarSize: Dp,
    contentStartPadding: Dp,
    lineWidth: Dp,
    lineColor: Color,
    placeholderColor: Color
) {
    if (mediaAttachments.isNotEmpty()) {
        val isSingleMedia = mediaAttachments.size == 1
        val mediaPadding = 4.dp

        if (isSingleMedia) {
            val media = mediaAttachments.first()
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val availableWidth = maxWidth - contentStartPadding - horizontalPadding
                // 세로 영상 9:16 허용, 이미지 4:5
                val singleMinRatio = if (media.type == MediaType.VIDEO) {
                    MediaDisplayPolicy.COMMENT_SINGLE_VIDEO_MIN_RATIO
                } else {
                    MediaDisplayPolicy.COMMENT_SINGLE_MIN_RATIO
                }
                val singleMediaHeight = singleMediaHeightForWidth(
                    availableWidth = availableWidth,
                    media = media,
                    minRatio = singleMinRatio,
                    maxRatio = MediaDisplayPolicy.COMMENT_SINGLE_MAX_RATIO
                )
                val clampedRatio = clampedAspectRatio(
                    aspectRatio = media.preferredAspectRatio(),
                    minRatio = singleMinRatio,
                    maxRatio = MediaDisplayPolicy.COMMENT_SINGLE_MAX_RATIO
                )
                // 세로 영상은 높이만 비율 유지
                val displayHeight = if (media.type == MediaType.VIDEO) {
                    singleMediaHeight.coerceAtMost(availableWidth * MediaDisplayPolicy.COMMENT_SINGLE_VIDEO_MAX_HEIGHT_RATIO)
                } else {
                    singleMediaHeight
                }

                // 선은 여백까지 채워 끊김 없이
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(displayHeight + mediaPadding * 2)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = horizontalPadding + (avatarSize - lineWidth) / 2)
                            .width(lineWidth)
                            .fillMaxHeight()
                            .background(lineColor)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = mediaPadding)
                            .padding(start = contentStartPadding, end = horizontalPadding)
                    ) {
                        if (media.type == MediaType.VIDEO) {
                            VideoPlayer(
                                videoUrl = media.url,
                                thumbnailUrl = media.thumbnailUrl,
                                isPlaying = false,
                                onClick = {},
                                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                                showMuteButton = false,
                                fixedHeight = displayHeight,
                                maxWidth = availableWidth,
                                preferredAspectRatio = clampedRatio,
                                minAspectRatio = singleMinRatio,
                                maxAspectRatio = MediaDisplayPolicy.COMMENT_SINGLE_MAX_RATIO,
                                durationMs = media.durationMs
                            )
                        } else {
                            AsyncImage(
                                model = rememberAnimatedImage(media.thumbnailUrl ?: media.url),
                                contentDescription = "첨부 이미지",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(singleMediaHeight)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop,
                                placeholder = ColorPainter(placeholderColor),
                                error = ColorPainter(placeholderColor)
                            )
                        }
                    }
                }
            }
        } else {
            val multiMediaHeight = MediaDisplayPolicy.COMMENT_MULTI_ITEM_HEIGHT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(multiMediaHeight + mediaPadding * 2)
            ) {
                // 선
                Box(
                    modifier = Modifier
                        .padding(start = horizontalPadding + (avatarSize - lineWidth) / 2)
                        .width(lineWidth)
                        .fillMaxHeight()
                        .background(lineColor)
                )

                // 미디어
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = mediaPadding)
                ) {
                    val maxItemWidth = (maxWidth - contentStartPadding - horizontalPadding).coerceAtLeast(1.dp)
                    val maxVisibleRatio = (maxItemWidth / multiMediaHeight).coerceIn(
                        MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
                        MediaDisplayPolicy.COMMENT_MULTI_MAX_RATIO
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(start = contentStartPadding, end = horizontalPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(mediaAttachments) { _, media ->
                            if (media.type == MediaType.VIDEO) {
                                VideoPlayer(
                                    videoUrl = media.url,
                                    thumbnailUrl = media.thumbnailUrl,
                                    isPlaying = false,
                                    onClick = {},
                                    modifier = Modifier.clip(MaterialTheme.shapes.medium),
                                    showMuteButton = false,
                                    fixedHeight = multiMediaHeight,
                                    maxWidth = maxItemWidth,
                                    preferredAspectRatio = clampedAspectRatio(
                                        aspectRatio = media.preferredAspectRatio(),
                                        minRatio = MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
                                        maxRatio = maxVisibleRatio
                                    ),
                                    minAspectRatio = MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
                                    maxAspectRatio = maxVisibleRatio,
                                    durationMs = media.durationMs
                                )
                            } else {
                                val itemRatio = clampedAspectRatio(
                                    aspectRatio = media.preferredAspectRatio(),
                                    minRatio = MediaDisplayPolicy.COMMENT_MULTI_MIN_RATIO,
                                    maxRatio = maxVisibleRatio
                                )
                                AsyncImage(
                                    model = rememberAnimatedImage(media.thumbnailUrl ?: media.url),
                                    contentDescription = "첨부 이미지",
                                    modifier = Modifier
                                        .width(multiMediaHeight * itemRatio)
                                        .height(multiMediaHeight)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop,
                                    placeholder = ColorPainter(placeholderColor),
                                    error = ColorPainter(placeholderColor)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 미디어 아래 여백
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(start = horizontalPadding + (avatarSize - lineWidth) / 2)
                    .width(lineWidth)
                    .fillMaxHeight()
                    .background(lineColor)
            )
        }
    } else {
        // 이미지 없으면 텍스트 아래 여백
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(start = horizontalPadding + (avatarSize - lineWidth) / 2)
                    .width(lineWidth)
                    .fillMaxHeight()
                    .background(lineColor)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ReplyThreadSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ReplyThreadSection(
                authorName = "개발자",
                authorAvatarUrl = null,
                content = "testtesttesttesttesttesttesttesttesttesttesttesttesttesttest",
                mediaAttachments = emptyList(),
                replyToName = "개발자",
                myProfile = previewUserProfile(),
                replyContent = "",
                onContentChange = {}
            )
        }
    }
}

/** 미디어 포함 원본의 답글 입력 섹션 */
@HaloLightDarkPreview
@Composable
private fun ReplyThreadSectionWithMediaPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ReplyThreadSection(
                authorName = "디자인러버",
                authorRole = "CREATOR",
                authorAvatarUrl = null,
                content = "작업물 공유합니다.",
                mediaAttachments = listOf(
                    previewMediaItem(1, url = "https://picsum.photos/400/300"),
                    previewMediaItem(2, url = "https://picsum.photos/400/301")
                ),
                replyToName = "디자인러버",
                myProfile = previewUserProfile(),
                replyContent = "멋진 작업이네요!",
                onContentChange = {},
                hasContent = true
            )
        }
    }
}

/** 첨부 미디어 미리보기 행 */
@HaloLightDarkPreview
@Composable
private fun ReplyThreadSectionSelectedMediaPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ReplyThreadSection(
                authorName = "개발자",
                authorAvatarUrl = null,
                content = "댓글에 사진 붙여보세요.",
                mediaAttachments = emptyList(),
                replyToName = "개발자",
                myProfile = previewUserProfile(),
                replyContent = "이렇게요!",
                onContentChange = {},
                selectedMediaUris = listOf(
                    "https://picsum.photos/200/200".toUri(),
                    "https://picsum.photos/201/200".toUri()
                ),
                hasContent = true
            )
        }
    }
}
