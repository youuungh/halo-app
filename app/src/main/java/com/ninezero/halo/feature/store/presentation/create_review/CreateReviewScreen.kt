@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.ninezero.halo.feature.store.presentation.create_review

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassActionButton
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.media.MediaDisplayPolicy
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.store.presentation.create_review.components.RatingSelector
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CreateReviewScreen(
    onNavigateBack: () -> Unit,
    onReviewSaved: () -> Unit = {},
    viewModel: CreateReviewViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val handleBack: () -> Unit = {
        keyboardController?.hide()
        onNavigateBack()
    }

    BackHandler { handleBack() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CreateReviewSideEffect.ReviewSaved -> onReviewSaved()
        }
    }

    CreateReviewScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
internal fun CreateReviewScreenContent(
    state: CreateReviewState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (CreateReviewIntent) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val hazeState = remember { HazeState() }
    val backgroundColor = MaterialTheme.colorScheme.background
    val density = LocalDensity.current
    var headerHeight by rememberSaveableDp()
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    val horizontalPadding = 16.dp
    val avatarSize = 40.dp
    val avatarSpacing = 12.dp
    val contentStartPadding = horizontalPadding + avatarSize + avatarSpacing
    val multiSelectionHeight = MediaDisplayPolicy.FEED_MULTI_ITEM_HEIGHT
    val cornerShape = RoundedCornerShape(12.dp)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onIntent(CreateReviewIntent.AddMedia(uris))
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { onIntent(CreateReviewIntent.SnackbarDismissed) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .imePadding()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(headerHeight + 16.dp))

            // 상품 정보
            if (state.productName.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                ) {
                    val productImageSize = 80.dp
                    AsyncImage(
                        model = state.productImageUrl,
                        contentDescription = state.productName,
                        modifier = Modifier
                            .size(productImageSize)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_img_placeholder),
                        error = painterResource(R.drawable.ic_img_placeholder),
                        fallback = painterResource(R.drawable.ic_img_placeholder)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(productImageSize),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.productName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        RatingSelector(
                            rating = state.rating,
                            onRatingChange = { onIntent(CreateReviewIntent.UpdateRating(it)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            ) {
                AsyncImage(
                    model = state.myAvatarUrl,
                    contentDescription = "내 프로필",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_profile_placeholder),
                    error = painterResource(R.drawable.ic_profile_placeholder),
                    fallback = painterResource(R.drawable.ic_profile_placeholder)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.myDisplayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 커서-힌트 정렬
                    val contentTextStyle = MaterialTheme.typography.bodyMedium.copy(
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.None
                        ),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )

                    var contentFieldValue by remember { mutableStateOf(TextFieldValue(state.content)) }
                    if (contentFieldValue.text != state.content) {
                        contentFieldValue = TextFieldValue(
                            text = state.content,
                            selection = TextRange(state.content.length)
                        )
                    }

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
                        // 미디어 프리뷰가 끼어 있으면 커서만 보장
                        val attachReserve = if (state.selectedMediaUris.isEmpty()) 36.dp else 0.dp
                        val reservePx = with(density) { (attachReserve + 8.dp).toPx() }
                        bringIntoViewRequester.bringIntoView(
                            Rect(cursorRect.left, cursorRect.top, cursorRect.right, cursorRect.bottom + reservePx)
                        )
                    }

                    BasicTextField(
                        value = contentFieldValue,
                        onValueChange = {
                            contentFieldValue = it
                            onIntent(CreateReviewIntent.UpdateContent(it.text))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequester)
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
                                        text = "상품은 어떠셨나요?",
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

            // 수정 모드 기존 사진 받는 중·실패
            if (state.isImagesLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                LoadingIndicator(
                    size = LoadingIndicatorSize.Small,
                    modifier = Modifier.padding(start = contentStartPadding)
                )
            } else if (state.imagesError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.padding(start = contentStartPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.imagesError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "다시 시도",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onIntent(CreateReviewIntent.RetryLoadImages) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // 선택한 미디어 미리보기
            if (state.selectedMediaUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val isSingleSelection = state.selectedMediaUris.size == 1

                if (isSingleSelection) {
                    val singleUri = state.selectedMediaUris.first()
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = contentStartPadding, end = horizontalPadding)
                    ) {
                        val clampedRatio = 1f.coerceIn(
                            MediaDisplayPolicy.FEED_SINGLE_MIN_RATIO,
                            MediaDisplayPolicy.FEED_SINGLE_MAX_RATIO
                        )
                        val mediaHeight = maxWidth / clampedRatio

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(mediaHeight)
                                .clip(cornerShape)
                        ) {
                            AsyncImage(
                                model = singleUri,
                                contentDescription = "선택된 이미지",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = ColorPainter(placeholderColor),
                                error = ColorPainter(placeholderColor)
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable { onIntent(CreateReviewIntent.RemoveMedia(singleUri)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = "이미지 삭제",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        }
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
                                val itemRatio = 1f.coerceIn(
                                    MediaDisplayPolicy.FEED_MULTI_MIN_RATIO,
                                    maxVisibleRatio
                                )

                                Box(
                                    modifier = Modifier
                                        .width(multiSelectionHeight * itemRatio)
                                        .height(multiSelectionHeight)
                                        .clip(cornerShape)
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "선택된 이미지",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        placeholder = ColorPainter(placeholderColor),
                                        error = ColorPainter(placeholderColor)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .clickable { onIntent(CreateReviewIntent.RemoveMedia(uri)) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_close),
                                            contentDescription = "이미지 삭제",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Icon(
                painter = painterResource(R.drawable.ic_image),
                contentDescription = "이미지 첨부",
                modifier = Modifier
                    .padding(start = contentStartPadding)
                    .size(24.dp)
                    .clickable(
                        enabled = state.canEditImages,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                tint = if (state.canEditImages) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = backgroundColor,
                        tint = HaloBlurTint(backgroundColor),
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
                GlassIconButton(
                    onClick = onNavigateBack,
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (state.isEditMode) "리뷰 수정" else "리뷰 작성",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                val isEnabled = state.rating > 0 && state.content.isNotBlank()
                GlassActionButton(
                    onClick = { if (!state.isSubmitting) onIntent(CreateReviewIntent.Submit) },
                    hazeState = hazeState,
                    enabled = isEnabled
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = LocalContentColor.current,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (state.isEditMode) "수정" else "등록",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
        )

        if (state.isSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
            )
        }
    }
}
