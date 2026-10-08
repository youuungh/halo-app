@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.ninezero.halo.feature.feed.presentation.create_post

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassActionButton
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.media.MediaDisplayPolicy
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.core.util.createCameraImageUri
import com.ninezero.halo.core.util.showToast
import com.ninezero.halo.feature.feed.presentation.components.CreatePostBottomBar
import com.ninezero.halo.feature.feed.presentation.components.PostDraftBottomSheet
import com.ninezero.halo.feature.feed.presentation.components.PostSettingsBottomSheet
import com.ninezero.halo.feature.feed.presentation.components.hasSettingsOptions
import com.ninezero.halo.feature.feed.presentation.create_post.components.SelectedMediaPreviewItem
import com.ninezero.halo.feature.tag.presentation.TagBottomSheet
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isCameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> isCameraPermissionGranted = granted }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CreatePostSideEffect.NavigateBack -> {
                keyboardController?.hide()
                onNavigateBack()
            }
            is CreatePostSideEffect.PostSubmitted -> {
                keyboardController?.hide()
                onNavigateBack()
            }
            is CreatePostSideEffect.PostUpdated -> {
                keyboardController?.hide()
                onNavigateBack()
            }
            is CreatePostSideEffect.ShowToast -> {
                context.showToast(sideEffect.message)
            }
        }
    }

    CreatePostScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        isCameraPermissionGranted = isCameraPermissionGranted,
        onRequestCameraPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
    )
}

@Composable
internal fun CreatePostScreenContent(
    state: CreatePostState,
    onIntent: (CreatePostIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    isCameraPermissionGranted: Boolean = true,
    onRequestCameraPermission: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val hazeState = remember { HazeState() }
    val backgroundColor = MaterialTheme.colorScheme.background
    val density = LocalDensity.current
    var headerHeight by rememberSaveableDp()
    var showClearDialog by remember { mutableStateOf(false) }
    var showDraftSaveDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    val hasContent = state.content.isNotBlank()
    val hasAnyContent = state.content.isNotBlank() || state.selectedMediaUris.isNotEmpty()
    // 수정 모드는 바뀐 게 있을 때만 확인
    val hasEditChanges = state.content != state.originalContent ||
        state.selectedMediaUris.map { it.toString() } != state.originalMediaItems.map { it.url } ||
        state.tags.isNotEmpty()
    val confirmExit = if (state.isEditMode) hasEditChanges else hasAnyContent
    val draftSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tagSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val contentFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // 신규 작성만 본문 자동 focus + 키보드
    LaunchedEffect(Unit) {
        if (!state.isEditMode) {
            contentFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // 시스템 back도 X 버튼과 같은 확인
    BackHandler {
        if (confirmExit) {
            showExitConfirmDialog = true
        } else {
            onIntent(CreatePostIntent.NavigateBack)
        }
    }

    val horizontalPadding = 8.dp
    val avatarSize = 40.dp
    val avatarSpacing = 12.dp
    val contentStartPadding = horizontalPadding + avatarSize + avatarSpacing  // 8 + 40 + 12 = 60dp
    val bottomBarHeight = 56.dp  // CreatePostBottomBar 콘텐츠 높이
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant

    // 카메라 촬영용 임시 URI
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onIntent(CreatePostIntent.AddMedia(uris))
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onIntent(CreatePostIntent.AddMedia(listOf(it))) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            onIntent(CreatePostIntent.AddMedia(listOf(cameraImageUri!!)))
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { onIntent(CreatePostIntent.SnackbarDismissed) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 스크롤 컨텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .imePadding()
                .verticalScroll(scrollState)
        ) {
            // 헤더 높이만큼 spacer
            Spacer(modifier = Modifier.height(headerHeight + 16.dp))

            // 프로필 + 텍스트 입력 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(top = 12.dp)
            ) {
                // 내 프로필 사진
                AsyncImage(
                    model = state.myProfile?.avatarThumbUrl ?: state.myProfile?.avatarUrl,
                    contentDescription = "내 프로필",
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_profile_placeholder),
                    error = painterResource(R.drawable.ic_profile_placeholder),
                    fallback = painterResource(R.drawable.ic_profile_placeholder)
                )

                Spacer(modifier = Modifier.width(avatarSpacing))

                // 입력 영역
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.myProfile?.displayName?.takeIf { it.isNotBlank() } ?: state.myProfile?.username ?: "",
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
                                    ) { showClearDialog = true },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // hint/text 수직 정렬 일치
                    val contentTextStyle = MaterialTheme.typography.bodyMedium.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.None
                        )
                    )

                    var contentFieldValue by remember { mutableStateOf(TextFieldValue(state.content)) }
                    if (contentFieldValue.text != state.content) {
                        contentFieldValue = TextFieldValue(
                            text = state.content,
                            selection = TextRange(state.content.length)
                        )
                    }

                    // 커서 따라 스크롤, 하단 바 높이만큼 여유
                    val bringIntoViewRequester = remember { BringIntoViewRequester() }
                    var contentLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                    var isContentFocused by remember { mutableStateOf(false) }
                    val imeBottom = WindowInsets.ime.getBottom(density)
                    LaunchedEffect(isContentFocused, contentFieldValue.text, contentFieldValue.selection.start, imeBottom) {
                        if (!isContentFocused || imeBottom <= 0) return@LaunchedEffect
                        // 텍스트 변경 직후엔 한 프레임 뒤 커서 사용
                        withFrameNanos {}
                        val cursorRect = contentLayoutResult?.let { layout ->
                            val offset = contentFieldValue.selection.start.coerceIn(0, layout.layoutInput.text.length)
                            runCatching { layout.getCursorRect(offset) }.getOrNull()
                        } ?: return@LaunchedEffect
                        // 예약 공간
                        val hasContentBelowField = state.tags.isNotEmpty() ||
                            state.selectedSectionTagId != null ||
                            state.selectedMediaUris.isNotEmpty()
                        val attachRowReserve = if (hasContentBelowField) 0.dp else 48.dp
                        val reservePx = with(density) { (attachRowReserve + bottomBarHeight + 8.dp).toPx() }
                        bringIntoViewRequester.bringIntoView(
                            Rect(cursorRect.left, cursorRect.top, cursorRect.right, cursorRect.bottom + reservePx)
                        )
                    }

                    BasicTextField(
                        value = contentFieldValue,
                        onValueChange = {
                            contentFieldValue = it
                            onIntent(CreatePostIntent.UpdateContent(it.text))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .focusRequester(contentFocusRequester)
                            .onFocusChanged { isContentFocused = it.isFocused },
                        onTextLayout = { contentLayoutResult = it },
                        textStyle = contentTextStyle.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box {
                                if (state.content.isEmpty()) {
                                    Text(
                                        text = "새로운 소식이 있나요?",
                                        style = contentTextStyle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            // 선택된 태그
            if (state.tags.isNotEmpty() || state.selectedSectionTagId != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.padding(horizontal = contentStartPadding),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.sectionTags.find { it.id == state.selectedSectionTagId }?.let { sectionTag ->
                        Text(
                            text = "#${sectionTag.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 일반 태그
                    if (state.tags.isNotEmpty()) {
                        Text(
                            text = state.tags.joinToString(" ") { "#$it" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 선택된 미디어 미리보기
            if (state.selectedMediaUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val isSingleSelection = state.selectedMediaUris.size == 1
                val multiSelectionHeight = MediaDisplayPolicy.FEED_MULTI_ITEM_HEIGHT
                val cornerShape = RoundedCornerShape(12.dp)

                if (isSingleSelection) {
                    val singleUri = state.selectedMediaUris.first()
                    val isVideo = remember(singleUri) {
                        MediaUtils.resolveMimeType(context, singleUri).startsWith("video/")
                    }

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = contentStartPadding, end = horizontalPadding)
                    ) {
                        // 동영상은 실제 비율, 로드 전 16:9
                        val videoRatio by produceState(16f / 9f, singleUri, isVideo) {
                            if (isVideo) {
                                val (w, h) = MediaUtils.getVideoDimensions(context, singleUri)
                                if (w != null && h != null && w > 0 && h > 0) value = w.toFloat() / h
                            }
                        }
                        val preferredRatio = if (isVideo) videoRatio else 1f
                        val clampedRatio = preferredRatio.coerceIn(
                            MediaDisplayPolicy.FEED_SINGLE_MIN_RATIO,
                            MediaDisplayPolicy.FEED_SINGLE_MAX_RATIO
                        )
                        val mediaHeight = maxWidth / clampedRatio

                        SelectedMediaPreviewItem(
                            uri = singleUri,
                            isVideo = isVideo,
                            placeholderColor = placeholderColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(mediaHeight)
                                .clip(cornerShape),
                            onRemove = { onIntent(CreatePostIntent.RemoveMedia(singleUri)) }
                        )
                    }
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val maxItemWidth = (maxWidth - contentStartPadding - horizontalPadding).coerceAtLeast(1.dp)
                        val maxVisibleRatio = (maxItemWidth / multiSelectionHeight).coerceIn(
                            MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
                            MediaDisplayPolicy.FEED_MULTI_MAX_RATIO
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(start = contentStartPadding, end = horizontalPadding),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = state.selectedMediaUris,
                                key = { it.toString() }
                            ) { uri ->
                                val isVideo = remember(uri) {
                                    MediaUtils.resolveMimeType(context, uri).startsWith("video/")
                                }
                                val videoRatio by produceState(16f / 9f, uri, isVideo) {
                                    if (isVideo) {
                                        val (w, h) = MediaUtils.getVideoDimensions(context, uri)
                                        if (w != null && h != null && w > 0 && h > 0) value = w.toFloat() / h
                                    }
                                }
                                val preferredRatio = if (isVideo) videoRatio else 1f
                                val itemRatio = preferredRatio.coerceIn(
                                    MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
                                    maxVisibleRatio
                                )

                                SelectedMediaPreviewItem(
                                    uri = uri,
                                    isVideo = isVideo,
                                    placeholderColor = placeholderColor,
                                    modifier = Modifier
                                        .width(multiSelectionHeight * itemRatio)
                                        .height(multiSelectionHeight)
                                        .clip(cornerShape),
                                    onRemove = { onIntent(CreatePostIntent.RemoveMedia(uri)) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 이미지/동영상/카메라 첨부 버튼
            Row(
                modifier = Modifier.padding(start = contentStartPadding, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_image),
                    contentDescription = "이미지 첨부",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            // 커뮤니티는 이미지만 허용 → 피커도 이미지 전용
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(
                                    if (state.isCommunityMode) {
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    } else {
                                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                    }
                                )
                            )
                        },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 커뮤니티는 비디오 업로드 불가 → 동영상 첨부 버튼 숨김
                if (!state.isCommunityMode) {
                    Icon(
                        painter = painterResource(R.drawable.ic_video),
                        contentDescription = "동영상 첨부",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = "카메라 촬영",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (isCameraPermissionGranted) {
                                val uri = context.createCameraImageUri()
                                cameraImageUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                onRequestCameraPermission()
                            }
                        },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 하단 바 높이만큼 여백
            Spacer(modifier = Modifier.height(bottomBarHeight + 8.dp))
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .hazeEffect(state = hazeState) {
                    style = HazeStyle(
                        backgroundColor = backgroundColor,
                        tint = HazeTint(backgroundColor),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // X 버튼
                GlassIconButton(
                    onClick = {
                        if (confirmExit) {
                            showExitConfirmDialog = true
                        } else {
                            onIntent(CreatePostIntent.NavigateBack)
                        }
                    },
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 타이틀
                Text(
                    text = if (state.isEditMode) "수정" else "새로운 포스트",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 임시 저장 버튼, 수정 모드 숨김
                if (!state.isEditMode) {
                    GlassIconButton(
                        onClick = {
                            focusManager.clearFocus(force = true)
                            if (hasAnyContent) {
                                showDraftSaveDialog = true
                            } else {
                                onIntent(CreatePostIntent.OpenDraftBottomSheet)
                            }
                        },
                        icon = painterResource(R.drawable.ic_save_draft),
                        contentDescription = "임시 저장",
                        hazeState = hazeState,
                        backgroundColor = backgroundColor
                    )

                    Spacer(modifier = Modifier.width(12.dp))
                }

                // 게시 버튼
                val isEnabled = state.content.isNotBlank() && !state.isSubmitting
                GlassActionButton(
                    onClick = { onIntent(CreatePostIntent.SubmitPost) },
                    hazeState = hazeState,
                    enabled = isEnabled
                ) {
                    if (state.isEditMode && state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = LocalContentColor.current,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "게시",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 하단 바
        CreatePostBottomBar(
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            // 수정 API가 티어·섹션을 안 받아 숨김
            showSettingsButton = !state.isEditMode && hasSettingsOptions(
                isCreator = state.myProfile?.isCreator == true,
                isCommunityMode = state.isCommunityMode
            ),
            onSettingsClick = {
                focusManager.clearFocus(force = true)
                onIntent(CreatePostIntent.OpenSettingsBottomSheet)
            },
            onTagClick = {
                focusManager.clearFocus(force = true)
                onIntent(CreatePostIntent.OpenTagBottomSheet)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .padding(bottom = bottomBarHeight)
        )

        if (showClearDialog) {
            ConfirmDialog(
                title = "포스트를 삭제하시겠어요?",
                confirmText = "삭제",
                dismissText = "취소",
                confirmColor = MaterialTheme.colorScheme.error,
                onConfirm = {
                    onIntent(CreatePostIntent.UpdateContent(""))
                    onIntent(CreatePostIntent.ClearAllMedia)
                    showClearDialog = false
                },
                onDismiss = { showClearDialog = false }
            )
        }

        if (showDraftSaveDialog) {
            ConfirmDialog(
                title = "임시 저장할까요?",
                confirmText = "저장",
                dismissText = "저장 안 함",
                neutralText = "계속 수정",
                dismissColor = MaterialTheme.colorScheme.error,
                onConfirm = {
                    showDraftSaveDialog = false
                    onIntent(CreatePostIntent.SaveDraft(openDraftsAfterSave = true))
                },
                onDismiss = {
                    showDraftSaveDialog = false
                    onIntent(CreatePostIntent.OpenDraftBottomSheet)
                },
                onNeutral = {
                    showDraftSaveDialog = false
                }
            )
        }

        if (showExitConfirmDialog && state.isEditMode) {
            // 수정은 임시저장 대상 아님
            ConfirmDialog(
                title = "수정을 취소할까요?",
                message = "변경한 내용은 저장되지 않습니다",
                confirmText = "나가기",
                dismissText = "계속 수정",
                confirmColor = MaterialTheme.colorScheme.error,
                onConfirm = {
                    focusManager.clearFocus(force = true)
                    showExitConfirmDialog = false
                    onIntent(CreatePostIntent.NavigateBack)
                },
                onDismiss = {
                    showExitConfirmDialog = false
                }
            )
        } else if (showExitConfirmDialog) {
            ConfirmDialog(
                title = "임시 저장할까요?",
                confirmText = "저장",
                dismissText = "저장 안 함",
                neutralText = "계속 수정",
                dismissColor = MaterialTheme.colorScheme.error,
                onConfirm = {
                    focusManager.clearFocus(force = true)
                    showExitConfirmDialog = false
                    // 저장이 끝난 뒤 나감, 먼저 나가면 글 유실
                    onIntent(CreatePostIntent.SaveDraft(exitAfterSave = true))
                },
                onDismiss = {
                    focusManager.clearFocus(force = true)
                    showExitConfirmDialog = false
                    onIntent(CreatePostIntent.NavigateBack)
                },
                onNeutral = {
                    showExitConfirmDialog = false
                }
            )
        }
    }

    if (state.showDraftBottomSheet) {
        PostDraftBottomSheet(
            drafts = state.drafts,
            isLoading = state.isDraftLoading,
            showDeletedToast = state.showDraftDeletedToast,
            sheetState = draftSheetState,
            onDraftClick = { draft -> onIntent(CreatePostIntent.LoadDraft(draft)) },
            onDeleteDraft = { draft -> onIntent(CreatePostIntent.DeleteDraft(draft)) },
            onDismiss = { onIntent(CreatePostIntent.CloseDraftBottomSheet) },
            onToastDismiss = { onIntent(CreatePostIntent.DismissDraftDeletedToast) }
        )
    }

    if (state.showTagBottomSheet) {
        TagBottomSheet(
            sheetState = tagSheetState,
            isCreator = state.myProfile?.isCreator == true && !state.isEditMode,
            tags = state.pendingTags,
            sectionTags = state.sectionTags,
            selectedSectionTagId = state.tempSectionTagId,
            isLoadingSectionTags = state.isLoadingSectionTags,
            showToast = state.showTagToast,
            toastMessage = state.tagToastMessage,
            onAddTag = { tag -> onIntent(CreatePostIntent.AddTag(tag)) },
            onRemoveTag = { tag -> onIntent(CreatePostIntent.RemoveTag(tag)) },
            onSelectSectionTag = { tagId -> onIntent(CreatePostIntent.SelectSectionTag(tagId)) },
            onCreateSectionTag = { name -> onIntent(CreatePostIntent.CreateSectionTag(name)) },
            onUpdateSectionTag = { tagId, name -> onIntent(CreatePostIntent.UpdateSectionTag(tagId, name)) },
            onDeleteSectionTag = { tagId -> onIntent(CreatePostIntent.DeleteSectionTag(tagId)) },
            onConfirm = { onIntent(CreatePostIntent.ConfirmTagChanges) },
            onDismiss = { onIntent(CreatePostIntent.CloseTagBottomSheet) },
            onToastDismiss = { onIntent(CreatePostIntent.DismissTagToast) }
        )
    }

    if (state.showSettingsBottomSheet) {
        PostSettingsBottomSheet(
            sheetState = settingsSheetState,
            isCreator = state.myProfile?.isCreator == true,
            isCommunityMode = state.isCommunityMode,
            isSecret = state.isSecret,
            requiredTier = state.requiredTier,
            planTiers = state.planTiers,
            onSecretChange = { isSecret -> onIntent(CreatePostIntent.SetIsSecret(isSecret)) },
            onTierChange = { tier -> onIntent(CreatePostIntent.SetRequiredTier(tier)) },
            onDismiss = { onIntent(CreatePostIntent.CloseSettingsBottomSheet) }
        )
    }
}

