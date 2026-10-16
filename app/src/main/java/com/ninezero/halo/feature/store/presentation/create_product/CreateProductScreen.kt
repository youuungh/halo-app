@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.ninezero.halo.feature.store.presentation.create_product

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassActionButton
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.util.CommaVisualTransformation
import com.ninezero.halo.feature.store.domain.model.RichTextBlockStyle
import com.ninezero.halo.feature.store.presentation.create_product.components.CollapsibleSectionHeader
import com.ninezero.halo.feature.store.presentation.create_product.components.CreateProductBottomBar
import com.ninezero.halo.feature.store.presentation.create_product.components.EditorDetailImageBlock
import com.ninezero.halo.feature.store.presentation.create_product.components.FieldDivider
import com.ninezero.halo.feature.store.presentation.create_product.components.ProductImageGallery
import com.ninezero.halo.feature.store.presentation.create_product.components.ProductTextField
import com.ninezero.halo.feature.store.presentation.create_product.components.RichEditorTextField
import com.ninezero.halo.feature.store.presentation.create_product.components.RichTextActionBar
import com.ninezero.halo.feature.store.presentation.create_product.components.SectionDivider
import com.ninezero.halo.feature.store.presentation.create_product.components.TierSelector
import com.ninezero.halo.feature.tag.presentation.TagBottomSheet
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import sh.calvin.reorderable.ReorderableColumn

@Composable
fun CreateProductScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: CreateProductViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler {
        viewModel.onIntent(CreateProductIntent.NavigateBack)
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CreateProductSideEffect.NavigateBack -> {
                keyboardController?.hide()
                onNavigateBack()
            }
            is CreateProductSideEffect.SaveSuccess -> {
                keyboardController?.hide()
                onSaveSuccess()
            }
            is CreateProductSideEffect.ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(CreateProductIntent.SnackbarDismissed) }
    )

    // 로딩은 Content 안에서 본문만 덮음
    CreateProductScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )

    if (state.isSaving) {
        LoadingDialog()
    }
}

@Composable
internal fun CreateProductScreenContent(
    state: CreateProductState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    previewFocusedSelection: Pair<String, TextRange>? = null,
    onIntent: (CreateProductIntent) -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    var headerHeight by rememberSaveableDp()
    var bottomContentHeight by remember { mutableStateOf(0.dp) }
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val tagSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val blockSelections = remember { mutableStateMapOf<String, TextRange>() }
    val detailImageAspectCache = remember { mutableStateMapOf<String, Float>() }
    // NUMBERED 연속 번호
    val numberedIndices = remember(state.detailBlocks) {
        buildMap {
            var n = 0
            state.detailBlocks.forEach { b ->
                if (b.type == EditorBlockType.TEXT && b.textStyle == RichTextBlockStyle.NUMBERED) {
                    n += 1
                    put(b.id, n)
                } else {
                    n = 0
                }
            }
        }
    }
    val focusedTextBlock = state.detailBlocks.firstOrNull {
        it.id == state.focusedBlockId && it.type == EditorBlockType.TEXT
    }
    val focusedSelection = previewFocusedSelection
        ?.takeIf { it.first == focusedTextBlock?.id }
        ?.second
        ?: focusedTextBlock?.id?.let { blockSelections[it] }
        ?: focusedTextBlock?.text?.length?.let(::TextRange)
        ?: TextRange(0)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onIntent(CreateProductIntent.AddImages(uris))
        }
    }

    val detailImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            // 커서 위치 전달, 그 지점에 이미지 삽입
            val cursorOffset = state.focusedBlockId?.let { blockSelections[it]?.start }
            onIntent(CreateProductIntent.InsertDetailImages(uris, cursorOffset))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures { focusManager.clearFocus() }
                }
        ) {
            Spacer(modifier = Modifier.height(headerHeight + 8.dp))

            // 상품 이미지 섹션
            CollapsibleSectionHeader(
                title = "상품 대표 이미지",
                isCollapsed = "images" in state.collapsedSections,
                onToggle = { onIntent(CreateProductIntent.ToggleSectionCollapse("images")) }
            )

            AnimatedVisibility(
                visible = "images" !in state.collapsedSections,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                val allImages = state.existingImageUrls.filter { it !in state.removedImageUrls }

                ProductImageGallery(
                    existingImageUrls = allImages,
                    selectedImageUris = state.selectedImageUris,
                    onAddImagesClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemoveExistingImage = { onIntent(CreateProductIntent.RemoveExistingImage(it)) },
                    onRemoveNewImage = { onIntent(CreateProductIntent.RemoveImage(it)) }
                )
            }

            SectionDivider()

            // 판매 정보 섹션
            CollapsibleSectionHeader(
                title = "판매 정보",
                isCollapsed = "sales" in state.collapsedSections,
                onToggle = { onIntent(CreateProductIntent.ToggleSectionCollapse("sales")) }
            )

            AnimatedVisibility(
                visible = "sales" !in state.collapsedSections,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductTextField(
                        value = state.originalPrice,
                        onValueChange = { onIntent(CreateProductIntent.UpdateOriginalPrice(it.filter { c -> c.isDigit() })) },
                        label = "원가 (선택)",
                        outlined = true,
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                        visualTransformation = CommaVisualTransformation
                    )

                    ProductTextField(
                        value = state.price,
                        onValueChange = { onIntent(CreateProductIntent.UpdatePrice(it.filter { c -> c.isDigit() })) },
                        label = "판매가",
                        outlined = true,
                        isError = state.priceError != null,
                        errorMessage = state.priceError,
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                        visualTransformation = CommaVisualTransformation
                    )

                    ProductTextField(
                        value = state.stock,
                        onValueChange = { onIntent(CreateProductIntent.UpdateStock(it.filter { c -> c.isDigit() })) },
                        label = "재고 수량",
                        outlined = true,
                        isError = state.stockError != null,
                        errorMessage = state.stockError,
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                        visualTransformation = CommaVisualTransformation
                    )

                    // 구매 가능 등급
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(14.dp)
                    ) {
                        TierSelector(
                            selectedTier = state.requiredTier,
                            planTiers = state.planTiers,
                            onTierSelected = { onIntent(CreateProductIntent.UpdateRequiredTier(it)) }
                        )
                    }

                    // 태그
                    if (state.tags.isNotEmpty() || state.selectedSectionTagId != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "태그",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    state.sectionTags.find { it.id == state.selectedSectionTagId }?.let { sectionTag ->
                                        Text(
                                            text = "#${sectionTag.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(50)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                    state.tags.forEach { tag ->
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = RoundedCornerShape(50)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SectionDivider()

            // 상품 정보 섹션
            CollapsibleSectionHeader(
                title = "상품 정보",
                isCollapsed = "info" in state.collapsedSections,
                onToggle = { onIntent(CreateProductIntent.ToggleSectionCollapse("info")) }
            )

            AnimatedVisibility(
                visible = "info" !in state.collapsedSections,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    ProductTextField(
                        value = state.name,
                        onValueChange = { onIntent(CreateProductIntent.UpdateName(it)) },
                        label = "상품명을 입력하세요",
                        isError = state.nameError != null,
                        errorMessage = state.nameError,
                        errorAbove = true,
                        errorAboveTopPadding = 0.dp,
                        errorAboveBottomPadding = 2.dp,
                        singleLine = true,
                        hintCenterVertically = true,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )

                    FieldDivider()

                    ProductTextField(
                        value = state.description,
                        onValueChange = { onIntent(CreateProductIntent.UpdateDescription(it)) },
                        label = "상품 설명을 입력하세요",
                        isError = state.descriptionError != null,
                        errorMessage = state.descriptionError,
                        errorAbove = true,
                        singleLine = false,
                        maxLines = Int.MAX_VALUE,
                        minHeight = 48.dp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )

                    FieldDivider()

                    // 상세 블록 리스트, 영역 단위 재정렬
                    // 드래그·변위·드롭은 ReorderableColumn에 위임
                    var regions by remember(state.detailBlocks) {
                        mutableStateOf(state.detailBlocks.toDetailRegions())
                    }
                    ReorderableColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 500.dp)
                            .padding(top = 10.dp, bottom = 24.dp),
                        list = regions,
                        onSettle = { fromIndex, toIndex ->
                            val key = regions.getOrNull(fromIndex)?.firstOrNull()?.id
                            regions = regions.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
                            if (key != null) onIntent(CreateProductIntent.MoveRegion(key, toIndex))
                        }
                    ) { _, regionBlocks, isDragging ->
                        val firstBlock = regionBlocks.first()
                        val isImageRegion = regionBlocks.size == 1 && firstBlock.type == EditorBlockType.IMAGE
                        // 영역별 stable key로 커서 튐 방지
                        key(firstBlock.id) {
                            ReorderableItem {
                                // 잡았을 때 반투명 + 그림자
                                val liftShape = RoundedCornerShape(10.dp)
                                // 텍스트 영역은 드래그 시 surface로 채움
                                val textLift = if (isDragging) {
                                    Modifier
                                        .graphicsLayer {
                                            alpha = 0.8f
                                            shadowElevation = 6.dp.toPx()
                                            shape = liftShape
                                            clip = true
                                        }
                                        .background(MaterialTheme.colorScheme.surface)
                                } else Modifier
                                // 이미지는 배경 없이 반투명 + 그림자만
                                val imageLift = if (isDragging) {
                                    Modifier.graphicsLayer {
                                        alpha = 0.8f
                                        shadowElevation = 6.dp.toPx()
                                        shape = liftShape
                                    }
                                } else Modifier
                                if (isImageRegion) {
                                    val imageModel = firstBlock.imageUri ?: firstBlock.imageUrl
                                    val aspectKey = imageModel?.toString() ?: firstBlock.id
                                    EditorDetailImageBlock(
                                        model = imageModel,
                                        onRemove = { onIntent(CreateProductIntent.RemoveBlock(firstBlock.id)) },
                                        onTap = { onIntent(CreateProductIntent.ToggleTextAfterImage(firstBlock.id)) },
                                        modifier = imageLift.longPressDraggableHandle(
                                            onDragStarted = {
                                                focusManager.clearFocus()
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        ),
                                        initialAspectRatio = detailImageAspectCache[aspectKey],
                                        onAspectRatioResolved = { detailImageAspectCache[aspectKey] = it }
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(textLift),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            regionBlocks.forEach { block ->
                                                key(block.id) {
                                                    RichEditorTextField(
                                                        block = block,
                                                        isFocused = block.id == state.focusedBlockId,
                                                        // 빈 텍스트 블록이면 hint 표시
                                                        showPlaceholder = true,
                                                        pendingFocus = block.id == state.pendingFocusBlockId,
                                                        onRequestMergeBackward = {
                                                            onIntent(CreateProductIntent.MergeBlockBackward(block.id))
                                                        },
                                                        onSplitListItem = { offset ->
                                                            onIntent(CreateProductIntent.SplitListItem(block.id, offset))
                                                        },
                                                        onExitQuote = { offset ->
                                                            onIntent(CreateProductIntent.ExitQuote(block.id, offset))
                                                        },
                                                        listNumber = numberedIndices[block.id],
                                                        onTextChange = { text, cursor ->
                                                            onIntent(CreateProductIntent.UpdateBlockText(block.id, text, cursor))
                                                        },
                                                        onSelectionChange = { selection ->
                                                            blockSelections[block.id] = selection
                                                        },
                                                        onFocus = { onIntent(CreateProductIntent.FocusBlock(block.id)) },
                                                        modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                                                        bottomReserve = bottomContentHeight,
                                                        selectionOverride = previewFocusedSelection
                                                            ?.takeIf { it.first == block.id }
                                                            ?.second
                                                    )
                                                }
                                            }
                                        }
                                        // 텍스트 영역 핸들
                                        Icon(
                                            painter = painterResource(R.drawable.ic_chart_no_axes_gantt),
                                            contentDescription = "영역 순서 이동",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .padding(end = 10.dp)
                                                .size(24.dp)
                                                .draggableHandle(
                                                    onDragStarted = {
                                                        focusManager.clearFocus()
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    }
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(bottomContentHeight + 10.dp))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // 로딩·에러 오버레이, 헤더는 위에
        if (state.isLoading || state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(top = headerHeight),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CenteredLoadingIndicator(size = LoadingIndicatorSize.Large)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { onIntent(CreateProductIntent.Retry) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.bounceClick()
                        ) {
                            Text("재시도")
                        }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                GlassIconButton(
                    onClick = { onIntent(CreateProductIntent.NavigateBack) },
                    icon = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = if (state.isEditMode) "상품 수정" else "상품 등록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )

                GlassActionButton(
                    onClick = { onIntent(CreateProductIntent.Save) },
                    modifier = Modifier.align(Alignment.CenterEnd),
                    hazeState = hazeState,
                    enabled = !state.isSaving
                ) {
                    Text(
                        text = "저장",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onGloballyPositioned { coordinates ->
                    bottomContentHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            // 툴바는 즉시 표시·숨김
            if (state.showTextFormatBar && focusedTextBlock != null) {
                focusedTextBlock.let { block ->
                    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                        RichTextActionBar(
                            block = block,
                            selection = focusedSelection,
                            typingFormat = state.typingFormats[block.id],
                            hazeState = hazeState,
                            backgroundColor = backgroundColor,
                            onToggleBold = { onIntent(CreateProductIntent.ToggleBold(block.id, it.start, it.end)) },
                            onToggleItalic = { onIntent(CreateProductIntent.ToggleItalic(block.id, it.start, it.end)) },
                            onToggleUnderline = { onIntent(CreateProductIntent.ToggleUnderline(block.id, it.start, it.end)) },
                            onToggleStrikethrough = { onIntent(CreateProductIntent.ToggleStrikethrough(block.id, it.start, it.end)) },
                            onSetTextColor = { colorHex ->
                                onIntent(CreateProductIntent.SetTextColor(block.id, focusedSelection.start, focusedSelection.end, colorHex))
                            },
                            onUpdateTextStyle = { onIntent(CreateProductIntent.UpdateBlockTextStyle(block.id, it, focusedSelection.start)) },
                            onUpdateTextAlignment = { onIntent(CreateProductIntent.UpdateBlockTextAlignment(block.id, it)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            CreateProductBottomBar(
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                onImageClick = {
                    focusManager.clearFocus()
                    detailImagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onTextFormatClick = { onIntent(CreateProductIntent.ToggleTextFormatBar) },
                onTagClick = {
                    focusManager.clearFocus()
                    onIntent(CreateProductIntent.OpenTagBottomSheet)
                }
            )
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomContentHeight)
        )
    }

    if (state.showTagBottomSheet) {
        TagBottomSheet(
            sheetState = tagSheetState,
            isCreator = true,
            tags = state.pendingTags,
            sectionTags = state.sectionTags,
            selectedSectionTagId = state.pendingSectionTagId,
            isLoadingSectionTags = state.isLoadingSectionTags,
            onAddTag = { tag -> onIntent(CreateProductIntent.AddTag(tag)) },
            onRemoveTag = { tag -> onIntent(CreateProductIntent.RemoveTag(tag)) },
            onSelectSectionTag = { tagId -> onIntent(CreateProductIntent.SelectSectionTag(tagId)) },
            onCreateSectionTag = { name -> onIntent(CreateProductIntent.CreateSectionTag(name)) },
            onUpdateSectionTag = { tagId, name -> onIntent(CreateProductIntent.UpdateSectionTag(tagId, name)) },
            onDeleteSectionTag = { tagId -> onIntent(CreateProductIntent.DeleteSectionTag(tagId)) },
            onConfirm = { onIntent(CreateProductIntent.ConfirmTagChanges) },
            onDismiss = { onIntent(CreateProductIntent.CloseTagBottomSheet) },
            snackbarMessage = state.tagSnackbarMessage,
            onSnackbarDismiss = { onIntent(CreateProductIntent.DismissTagSnackbar) }
        )
    }
}
