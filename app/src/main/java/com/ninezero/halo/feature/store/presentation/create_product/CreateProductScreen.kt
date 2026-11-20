package com.ninezero.halo.feature.store.presentation.create_product

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassActionButton
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.input.FormSection
import com.ninezero.halo.core.ui.components.input.FormTextField
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.list.ListErrorState
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.feature.store.domain.model.RichTextBlockStyle
import com.ninezero.halo.feature.store.presentation.components.discountRatePercent
import com.ninezero.halo.feature.store.presentation.create_product.components.CollapsibleSectionHeader
import com.ninezero.halo.feature.store.presentation.create_product.components.CreateProductBottomBar
import com.ninezero.halo.feature.store.presentation.create_product.components.EditorDetailImageBlock
import com.ninezero.halo.feature.store.presentation.create_product.components.ProductImageGallery
import com.ninezero.halo.feature.store.presentation.create_product.components.RichEditorTextField
import com.ninezero.halo.feature.store.presentation.create_product.components.RichTextActionBar
import com.ninezero.halo.feature.store.presentation.create_product.components.SectionBand
import com.ninezero.halo.feature.store.presentation.create_product.components.TierSelector
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.tag.presentation.TagBottomSheet
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

    // 다른 작성 화면처럼 예측형 미리 보기 없이 아래로 닫힘
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

    if (state.showExitConfirmDialog) {
        ConfirmDialog(
            title = if (state.isEditMode) "수정을 취소할까요?" else "작성을 취소할까요?",
            message = if (state.isEditMode) "변경한 내용은 저장되지 않습니다" else "작성한 내용은 저장되지 않습니다",
            confirmText = "나가기",
            dismissText = if (state.isEditMode) "계속 수정" else "계속 작성",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { viewModel.onIntent(CreateProductIntent.ConfirmExit) },
            onDismiss = { viewModel.onIntent(CreateProductIntent.DismissExitConfirm) }
        )
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
    val numberFormat = rememberKoreanNumberFormat()
    val isImeVisible = WindowInsets.isImeVisible
    // 포커스가 있는 상세 글 칸, 비면 하단 바를 바로 숨김
    val focusedEditorIds = remember { mutableStateMapOf<String, Boolean>() }
    // 병합 · 삭제된 칸은 빼고 지금 있는 칸만 셈
    val isEditorFocused = focusedEditorIds.keys.any { id -> state.detailBlocks.any { it.id == id } }
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

            val visibleImageCount = state.existingImageUrls.count { it !in state.removedImageUrls } + state.selectedImageUris.size

            // 사진
            CollapsibleSectionHeader(
                title = "사진",
                isCollapsed = "images" in state.collapsedSections,
                onToggle = { onIntent(CreateProductIntent.ToggleSectionCollapse("images")) },
                summary = "$visibleImageCount / 10"
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

            SectionBand()

            // 기본 정보
            CollapsibleSectionHeader(
                title = "기본 정보",
                isCollapsed = "basic" in state.collapsedSections,
                onToggle = { onIntent(CreateProductIntent.ToggleSectionCollapse("basic")) },
                summary = state.name
            )

            AnimatedVisibility(
                visible = "basic" !in state.collapsedSections,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                BasicInfoFields(state = state, onIntent = onIntent)
            }

            SectionBand()

            // 판매 정보
            val tierLabel = if (state.requiredTier == SubscriptionPlanTier.FREE) "전체 공개" else state.requiredTier.label
            CollapsibleSectionHeader(
                title = "판매 정보",
                isCollapsed = "sales" in state.collapsedSections,
                onToggle = { onIntent(CreateProductIntent.ToggleSectionCollapse("sales")) },
                summary = listOfNotNull(
                    state.price.toLongOrNull()?.let { "${numberFormat.format(it)}원" },
                    state.stock.toLongOrNull()?.let { "재고 ${numberFormat.format(it)}개" },
                    tierLabel
                ).joinToString(" · ")
            )

            AnimatedVisibility(
                visible = "sales" !in state.collapsedSections,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                SalesInfoFields(state = state, onIntent = onIntent)
            }

            SectionBand()

            // 상세 내용
            CollapsibleSectionHeader(
                title = "상세 내용",
                isCollapsed = "detail" in state.collapsedSections,
                onToggle = { onIntent(CreateProductIntent.ToggleSectionCollapse("detail")) }
            )

            AnimatedVisibility(
                visible = "detail" !in state.collapsedSections,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    // 상세 블록 리스트, 영역 단위 재정렬
                    // 드래그·변위·드롭은 ReorderableColumn에 위임
                    var regions by remember(state.detailBlocks) {
                        mutableStateOf(state.detailBlocks.toDetailRegions())
                    }
                    ReorderableColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
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
                                // 잡으면 작은 조각으로 줄어 따라옴
                                val dragShape = MaterialTheme.shapes.medium
                                val chipLift = Modifier
                                    .padding(horizontal = 16.dp)
                                    .shadow(8.dp, dragShape)
                                    .background(MaterialTheme.colorScheme.surface, dragShape)
                                    .border(1.dp, MaterialTheme.colorScheme.primary, dragShape)
                                if (isImageRegion) {
                                    val imageModel = firstBlock.imageUri ?: firstBlock.imageUrl
                                    val aspectKey = imageModel?.toString() ?: firstBlock.id
                                    // 손잡이를 바깥 상자에 두어 끌기가 이어짐
                                    Box(
                                        modifier = Modifier.longPressDraggableHandle(
                                            onDragStarted = {
                                                focusManager.clearFocus()
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        )
                                    ) {
                                        if (isDragging) {
                                            DragChip(imageModel = imageModel, label = "사진", modifier = chipLift)
                                        } else {
                                            EditorDetailImageBlock(
                                                model = imageModel,
                                                onRemove = { onIntent(CreateProductIntent.RemoveBlock(firstBlock.id)) },
                                                onTap = { onIntent(CreateProductIntent.ToggleTextAfterImage(firstBlock.id)) },
                                                initialAspectRatio = detailImageAspectCache[aspectKey],
                                                onAspectRatioResolved = { detailImageAspectCache[aspectKey] = it }
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(if (isDragging) chipLift else Modifier),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 내용만 조각으로 바꿔 끌기가 이어짐
                                        if (isDragging) {
                                            DragChip(
                                                imageModel = null,
                                                label = regionBlocks.firstOrNull { it.text.isNotBlank() }?.text?.lineSequence()?.first().orEmpty(),
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else Column(modifier = Modifier.weight(1f)) {
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
                                                        onFocusStateChange = { focused ->
                                                            if (focused) focusedEditorIds[block.id] = true else focusedEditorIds.remove(block.id)
                                                        },
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
                                            tint = if (isDragging) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            },
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

                    DetailEndRow(
                        lastImageId = state.detailBlocks.lastOrNull()?.takeIf { it.type == EditorBlockType.IMAGE }?.id,
                        showAttach = !(isEditorFocused && isImeVisible),
                        onAddText = { onIntent(CreateProductIntent.ToggleTextAfterImage(it)) },
                        onAddImage = {
                            focusManager.clearFocus()
                            detailImagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

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
                    ListErrorState(
                        title = "상품 정보를 불러오지 못했습니다",
                        reason = state.error,
                        onRetry = { onIntent(CreateProductIntent.Retry) }
                    )
                }
            }
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .haloBlur(hazeState, HaloBlurs.fadeDown(backgroundColor))
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
            // 하단 바는 상세 글을 쓰는 동안만
            if (focusedTextBlock != null && isEditorFocused && isImeVisible) {
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
                    isFormatOpen = state.showTextFormatBar
                ) {
                    AnimatedVisibility(
                        visible = state.showTextFormatBar,
                        enter = expandHorizontally(expandFrom = Alignment.Start, animationSpec = tween(200)) + fadeIn(tween(200)),
                        exit = shrinkHorizontally(shrinkTowards = Alignment.Start, animationSpec = tween(150)) + fadeOut(tween(150)),
                        modifier = Modifier.weight(1f)
                    ) {
                        RichTextActionBar(
                            block = focusedTextBlock,
                            selection = focusedSelection,
                            typingFormat = state.typingFormats[focusedTextBlock.id],
                            hazeState = hazeState,
                            backgroundColor = backgroundColor,
                            onToggleBold = { onIntent(CreateProductIntent.ToggleBold(focusedTextBlock.id, it.start, it.end)) },
                            onToggleItalic = { onIntent(CreateProductIntent.ToggleItalic(focusedTextBlock.id, it.start, it.end)) },
                            onToggleUnderline = { onIntent(CreateProductIntent.ToggleUnderline(focusedTextBlock.id, it.start, it.end)) },
                            onToggleStrikethrough = { onIntent(CreateProductIntent.ToggleStrikethrough(focusedTextBlock.id, it.start, it.end)) },
                            onSetTextColor = { colorHex ->
                                onIntent(
                                    CreateProductIntent.SetTextColor(focusedTextBlock.id, focusedSelection.start, focusedSelection.end, colorHex)
                                )
                            },
                            onUpdateTextStyle = {
                                onIntent(CreateProductIntent.UpdateBlockTextStyle(focusedTextBlock.id, it, focusedSelection.start))
                            },
                            onUpdateTextAlignment = { onIntent(CreateProductIntent.UpdateBlockTextAlignment(focusedTextBlock.id, it)) }
                        )
                    }
                }
            }
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

/** 기본 정보 구역: 이름 · 설명 */
@Composable
private fun BasicInfoFields(
    state: CreateProductState,
    onIntent: (CreateProductIntent) -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FormSection(title = "상품명") {
            FormTextField(
                value = state.name,
                onValueChange = { onIntent(CreateProductIntent.UpdateName(it)) },
                placeholder = "상품명",
                isError = state.nameError != null,
                errorMessage = state.nameError
            )
        }
        FormSection(title = "상품 설명") {
            FormTextField(
                value = state.description,
                onValueChange = { onIntent(CreateProductIntent.UpdateDescription(it)) },
                placeholder = "상품 설명",
                singleLine = false,
                minLines = 2,
                maxLength = 2000,
                isError = state.descriptionError != null,
                errorMessage = state.descriptionError
            )
        }
    }
}

/** 판매 정보 구역: 정가 · 판매가 · 재고 */
@Composable
private fun SalesInfoFields(
    state: CreateProductState,
    onIntent: (CreateProductIntent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    FormSection(title = "정가") {
                        FormTextField(
                            value = state.originalPrice,
                            onValueChange = { onIntent(CreateProductIntent.UpdateOriginalPrice(it.filter { c -> c.isDigit() })) },
                            placeholder = "할인할 때만",
                            keyboardType = KeyboardType.Number,
                            prefix = "₩",
                            numberFormat = true
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    FormSection(title = "판매가") {
                        FormTextField(
                            value = state.price,
                            onValueChange = { onIntent(CreateProductIntent.UpdatePrice(it.filter { c -> c.isDigit() })) },
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            prefix = "₩",
                            numberFormat = true,
                            isError = state.priceError != null,
                            errorMessage = state.priceError
                        )
                    }
                }
            }
            // 정가가 판매가보다 클 때만, 서버와 같은 반올림
            discountRatePercent(state.originalPrice, state.price)?.let { rate ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    // 이름과 숫자를 붙이고 숫자는 상품 상세 할인율과 같은 빨강
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "할인율",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alignByBaseline()
                    )
                    Text(
                        text = "$rate%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                FormSection(title = "재고") {
                    FormTextField(
                        value = state.stock,
                        onValueChange = { onIntent(CreateProductIntent.UpdateStock(it.filter { c -> c.isDigit() })) },
                        placeholder = "0",
                        keyboardType = KeyboardType.Number,
                        numberFormat = true,
                        isError = state.stockError != null,
                        errorMessage = state.stockError
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        TierSelector(
            selectedTier = state.requiredTier,
            planTiers = state.planTiers,
            onTierSelected = { onIntent(CreateProductIntent.UpdateRequiredTier(it)) }
        )

        ProductTagField(
            sectionTagName = state.sectionTags.find { it.id == state.selectedSectionTagId }?.name,
            tags = state.tags,
            onClick = {
                focusManager.clearFocus()
                onIntent(CreateProductIntent.OpenTagBottomSheet)
            }
        )
    }
}

/** 태그: 알약 줄 + 끝에 점선 추가 */
@Composable
private fun ProductTagField(
    sectionTagName: String?,
    tags: List<String>,
    onClick: () -> Unit
) {
    val isEmpty = sectionTagName == null && tags.isEmpty()
    Column {
        Text(
            text = "태그",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sectionTagName?.let { name ->
                TagPill(text = "#$name", isSection = true)
            }
            tags.forEach { tag -> TagPill(text = "#$tag", isSection = false) }
            AddTagPill(text = if (isEmpty) "태그 추가" else "추가")
        }
    }
}

@Composable
private fun TagPill(text: String, isSection: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (isSection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(
                color = if (isSection) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

/** 회색 점선 알약, 빈 자리라는 느낌 */
@Composable
private fun AddTagPill(text: String) {
    val dashColor = MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .drawBehind {
                drawRoundRect(
                    color = dashColor,
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
                    cornerRadius = CornerRadius(size.height / 2)
                )
            }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 상세 내용 끝: 사진이면 점선 + 힌트 */
@Composable
private fun DetailEndRow(
    lastImageId: String?,
    showAttach: Boolean,
    onAddText: (String) -> Unit,
    onAddImage: () -> Unit
) {
    val dashColor = MaterialTheme.colorScheme.outlineVariant
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (lastImageId != null) {
            Text(
                text = "내용을 입력하세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = dashColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onAddText(lastImageId) }
                    .padding(vertical = 12.dp)
            )
        }
        // 글을 쓰는 중엔 하단 바 사진 버튼과 겹쳐 숨김
        if (showAttach) {
            Icon(
                painter = painterResource(R.drawable.ic_image),
                contentDescription = "사진 넣기",
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false),
                        onClick = onAddImage
                    ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 끄는 동안 보이는 작은 조각 */
@Composable
private fun DragChip(
    imageModel: Any?,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_type),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}
