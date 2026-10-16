package com.ninezero.halo.feature.feed.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.feed.domain.model.PostDraft
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun PostDraftBottomSheet(
    drafts: List<PostDraft>,
    isLoading: Boolean,
    showDeletedSnackbar: Boolean,
    sheetState: SheetState,
    onDraftClick: (PostDraft) -> Unit,
    onDeleteDraft: (PostDraft) -> Unit,
    onDismiss: () -> Unit,
    onSnackbarDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val hazeState = remember { HazeState() }
    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(0.dp) }

    // X 버튼 외 모든 dismiss 경로 차단
    val sheetProperties = remember {
        ModalBottomSheetProperties(
            shouldDismissOnBackPress = false,
            shouldDismissOnClickOutside = false
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        dragHandle = null,
        containerColor = surfaceColor,
        contentWindowInsets = { WindowInsets(0) },
        properties = sheetProperties,
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
        ) {
            // 콘텐츠
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            strokeWidth = 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                drafts.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = headerHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "임시 저장된 글이 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState)
                            .blockSheetFling(),
                        contentPadding = PaddingValues(top = headerHeight),
                        overscrollEffect = null
                    ) {
                        item {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                        items(
                            items = drafts,
                            key = { it.id }
                        ) { draft ->
                            PostDraftItem(
                                draft = draft,
                                onClick = { onDraftClick(draft) },
                                onDelete = { onDeleteDraft(draft) }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // 헤더
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .haloBlur(state = hazeState) {
                        style = HaloBlurStyle(
                            backgroundColor = surfaceColor,
                            tint = HaloBlurTint(surfaceColor),
                            noiseFactor = 0f
                        )
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0.5f,
                            endIntensity = 0f
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
                    .onGloballyPositioned { coordinates ->
                        headerHeight = with(density) { coordinates.size.height.toDp() }
                    }
            ) {
                BottomSheetDragHandle()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    GlassIconButton(
                        onClick = onDismiss,
                        icon = painterResource(R.drawable.ic_close),
                        contentDescription = "닫기",
                        hazeState = hazeState,
                        backgroundColor = surfaceColor,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    Text(
                        text = "임시 저장",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            DefaultSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    SnackbarHandler(
        message = "삭제됨".takeIf { showDeletedSnackbar },
        hostState = snackbarHostState,
        onDismiss = onSnackbarDismiss
    )
}

@Composable
private fun PostDraftItem(
    draft: PostDraft,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    val imageSize = 100.dp
    val horizontalPadding = 16.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 12.dp)
    ) {
        // 상단: displayName + 시간 + 더보기 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = draft.authorDisplayName ?: draft.authorUsername,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alignByBaseline()
            )

            Text(
                text = " · ${draft.updatedAt.toRelativeTime()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alignByBaseline()
            )

            Spacer(modifier = Modifier.weight(1f))

            Box {
                Icon(
                    painter = painterResource(R.drawable.ic_ellipsis_horizontal),
                    contentDescription = "더보기",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showMenu = true },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (showMenu) {
                    Popup(
                        alignment = Alignment.TopEnd,
                        onDismissRequest = { showMenu = false },
                        offset = IntOffset(24, with(LocalDensity.current) { 24.dp.roundToPx() }),
                        properties = PopupProperties(focusable = true)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        showMenu = false
                                        onDelete()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "임시 저장 삭제",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(
                                        painter = painterResource(R.drawable.ic_delete),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 본문
        if (draft.content.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = draft.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // 첨부 미디어
        if (draft.mediaUris.isNotEmpty()) {
            val context = LocalContext.current
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                overscrollEffect = null
            ) {
                items(draft.mediaUris) { uri ->
                    val isVideo = remember(uri) {
                        MediaUtils.resolveMimeType(context, uri).startsWith("video/")
                    }
                    PostDraftMediaThumbnail(
                        model = uri,
                        isVideo = isVideo,
                        size = imageSize,
                        placeholderColor = placeholderColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PostDraftMediaThumbnail(
    model: Any?,
    isVideo: Boolean,
    size: Dp,
    placeholderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = model,
            contentDescription = if (isVideo) "첨부 비디오" else "첨부 이미지",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(placeholderColor),
            error = ColorPainter(placeholderColor)
        )
        if (isVideo) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play_circle),
                    contentDescription = "재생",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDraftBottomSheetPreview() {
    HaloTheme {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        PostDraftBottomSheet(
            drafts = listOf(
                PostDraft(
                    id = 1,
                    content = "오늘 새로운 프로젝트를 시작했습니다! 정말 기대되네요.",
                    mediaUris = listOf(
                        "https://picsum.photos/200/200".toUri(),
                        "https://picsum.photos/201/200".toUri()
                    ),
                    authorUsername = "developer",
                    authorDisplayName = "개발자",
                    updatedAt = System.currentTimeMillis() - 3600000
                ),
                PostDraft(
                    id = 2,
                    content = "두 번째 임시 저장 글입니다.",
                    mediaUris = emptyList(),
                    authorUsername = "developer",
                    authorDisplayName = "개발자",
                    updatedAt = System.currentTimeMillis() - 7200000
                )
            ),
            isLoading = false,
            showDeletedSnackbar = false,
            sheetState = sheetState,
            onDraftClick = {},
            onDeleteDraft = {},
            onDismiss = {},
            onSnackbarDismiss = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDraftBottomSheetLoadingPreview() {
    HaloTheme {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        PostDraftBottomSheet(
            drafts = emptyList(),
            isLoading = true,
            showDeletedSnackbar = false,
            sheetState = sheetState,
            onDraftClick = {},
            onDeleteDraft = {},
            onDismiss = {},
            onSnackbarDismiss = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDraftBottomSheetEmptyPreview() {
    HaloTheme {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        PostDraftBottomSheet(
            drafts = emptyList(),
            isLoading = false,
            showDeletedSnackbar = false,
            sheetState = sheetState,
            onDraftClick = {},
            onDeleteDraft = {},
            onDismiss = {},
            onSnackbarDismiss = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDraftItemPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PostDraftItem(
                draft = PostDraft(
                    id = 1,
                    content = "오늘 새로운 프로젝트를 시작했습니다! 정말 기대되네요. 많은 분들이 관심 가져주시면 좋겠습니다.",
                    mediaUris = listOf(
                        "https://picsum.photos/200/200".toUri(),
                        "https://picsum.photos/201/200".toUri()
                    ),
                    authorUsername = "developer",
                    authorDisplayName = "개발자",
                    updatedAt = System.currentTimeMillis() - 3600000
                ),
                onClick = {},
                onDelete = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDraftMediaThumbnailImagePreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                PostDraftMediaThumbnail(
                    model = "https://picsum.photos/200/200",
                    isVideo = false,
                    size = 100.dp,
                    placeholderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDraftMediaThumbnailVideoPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                PostDraftMediaThumbnail(
                    model = "https://picsum.photos/200/200",
                    isVideo = true,
                    size = 100.dp,
                    placeholderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
