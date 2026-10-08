package com.ninezero.halo.feature.store.presentation.create_product

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.feed.domain.model.MediaDraftTargetType
import com.ninezero.halo.feature.feed.domain.repository.MediaDraftRepository
import com.ninezero.halo.feature.profile.domain.usecase.GetCachedMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import com.ninezero.halo.feature.store.domain.model.CreateProductRequest
import com.ninezero.halo.feature.store.domain.model.DetailContentBlock
import com.ninezero.halo.feature.store.domain.model.ProductImageRef
import com.ninezero.halo.feature.store.domain.model.RichTextAlignment
import com.ninezero.halo.feature.store.domain.model.RichTextBlockStyle
import com.ninezero.halo.feature.store.domain.model.UpdateProductRequest
import com.ninezero.halo.feature.store.domain.repository.ProductRepository
import com.ninezero.halo.feature.subscription.domain.usecase.GetMyPlanTiersUseCase
import com.ninezero.halo.feature.tag.domain.model.TagTargetType
import com.ninezero.halo.feature.tag.domain.usecase.CreateTagUseCase
import com.ninezero.halo.feature.tag.domain.usecase.DeleteTagUseCase
import com.ninezero.halo.feature.tag.domain.usecase.GetCreatorTagsUseCase
import com.ninezero.halo.feature.tag.domain.usecase.UpdateTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer
import java.util.UUID
import javax.inject.Inject

private const val MAX_TAGS = 5
private const val MAX_SECTION_TAGS = 3
private const val MAX_IMAGES = 10
private const val MAX_DETAIL_CONTENT_IMAGES = 30

@HiltViewModel
class CreateProductViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val mediaDraftRepository: MediaDraftRepository,
    private val getCachedMyProfileUseCase: GetCachedMyProfileUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getCreatorTagsUseCase: GetCreatorTagsUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val getMyPlanTiersUseCase: GetMyPlanTiersUseCase
) : ViewModel(), OrbitContainerHost<CreateProductState, CreateProductState, CreateProductSideEffect> {

    private val productId: Int? = savedStateHandle.get<Int>("productId")?.takeIf { it > 0 }

    override val container: OrbitContainer<CreateProductState, CreateProductState, CreateProductSideEffect> =
        orbitContainer(
            CreateProductState(
                isEditMode = productId != null,
                productId = productId
            )
        )

    init {
        if (productId != null) loadProduct(productId)
        loadSectionTags()
        loadPlanTiers()
    }

    // 플랜 없는 등급은 서버가 등록을 거절
    private fun loadPlanTiers() = intent {
        getMyPlanTiersUseCase().onSuccess { tiers -> reduce { state.copy(planTiers = tiers) } }
    }

    fun onIntent(intent: CreateProductIntent) {
        when (intent) {
            is CreateProductIntent.NavigateBack -> navigateBack()
            is CreateProductIntent.UpdateName -> updateName(intent.name)
            is CreateProductIntent.UpdateDescription -> updateDescription(intent.description)
            is CreateProductIntent.UpdatePrice -> updatePrice(intent.price)
            is CreateProductIntent.UpdateOriginalPrice -> updateOriginalPrice(intent.originalPrice)
            is CreateProductIntent.UpdateStock -> updateStock(intent.stock)
            is CreateProductIntent.UpdateRequiredTier -> updateRequiredTier(intent.tier)
            is CreateProductIntent.AddImages -> addImages(intent.uris)
            is CreateProductIntent.RemoveImage -> removeImage(intent.uri)
            is CreateProductIntent.RemoveExistingImage -> removeExistingImage(intent.url)
            is CreateProductIntent.Save -> save()
            is CreateProductIntent.SnackbarDismissed -> dismissSnackbar()
            // 상세 콘텐츠 블록 에디터
            is CreateProductIntent.UpdateBlockText -> updateBlockText(intent.blockId, intent.text, intent.cursorPosition)
            is CreateProductIntent.RemoveBlock -> removeBlock(intent.blockId)
            is CreateProductIntent.MoveRegion -> moveRegion(intent.fromBlockId, intent.toIndex)
            is CreateProductIntent.ToggleTextAfterImage -> toggleTextAfterImage(intent.imageId)
            is CreateProductIntent.InsertDetailImages -> insertDetailImages(intent.uris, intent.cursorOffset)
            is CreateProductIntent.MergeBlockBackward -> mergeBlockBackward(intent.blockId)
            is CreateProductIntent.SplitListItem -> splitListItem(intent.blockId, intent.splitOffset)
            is CreateProductIntent.ExitQuote -> exitQuote(intent.blockId, intent.splitOffset)
            is CreateProductIntent.FocusBlock -> focusBlock(intent.blockId)
            is CreateProductIntent.ToggleTextFormatBar -> toggleTextFormatBar()
            is CreateProductIntent.ToggleBold -> toggleBold(intent.blockId, intent.start, intent.end)
            is CreateProductIntent.ToggleItalic -> toggleItalic(intent.blockId, intent.start, intent.end)
            is CreateProductIntent.ToggleUnderline -> toggleUnderline(intent.blockId, intent.start, intent.end)
            is CreateProductIntent.ToggleStrikethrough -> toggleStrikethrough(intent.blockId, intent.start, intent.end)
            is CreateProductIntent.SetTextColor -> setTextColor(intent.blockId, intent.start, intent.end, intent.colorHex)
            is CreateProductIntent.UpdateBlockTextStyle -> updateBlockTextStyle(intent.blockId, intent.textStyle, intent.cursorOffset)
            is CreateProductIntent.UpdateBlockTextAlignment -> updateBlockAlign(intent.blockId, intent.alignment)
            // 태그
            is CreateProductIntent.OpenTagBottomSheet -> openTagBottomSheet()
            is CreateProductIntent.CloseTagBottomSheet -> closeTagBottomSheet()
            is CreateProductIntent.ConfirmTagChanges -> confirmTagChanges()
            is CreateProductIntent.AddTag -> addTag(intent.tag)
            is CreateProductIntent.RemoveTag -> removeTag(intent.tag)
            is CreateProductIntent.SelectSectionTag -> selectSectionTag(intent.tagId)
            is CreateProductIntent.CreateSectionTag -> createSectionTag(intent.name)
            is CreateProductIntent.UpdateSectionTag -> updateSectionTag(intent.tagId, intent.name)
            is CreateProductIntent.DeleteSectionTag -> deleteSectionTag(intent.tagId)
            is CreateProductIntent.DismissTagToast -> dismissTagToast()
            is CreateProductIntent.ToggleSectionCollapse -> toggleSectionCollapse(intent.section)
            is CreateProductIntent.Retry -> productId?.let { loadProduct(it) }
        }
    }

    private fun toggleSectionCollapse(section: String) = intent {
        val updated = if (section in state.collapsedSections) {
            state.collapsedSections - section
        } else {
            state.collapsedSections + section
        }
        reduce { state.copy(collapsedSections = updated) }
    }

    private fun loadProduct(productId: Int) = intent {
        reduce { state.copy(isLoading = true, error = null) }

        productRepository.getProduct(productId).onSuccess { product ->
            val blocks = mergeDetailContentToBlocks(product.detailContent).ifEmpty { listOf(EditorBlock()) }

            reduce {
                state.copy(
                    isLoading = false,
                    name = product.name,
                    description = product.description,
                    price = product.basePrice.ifBlank { product.price },  // 딜 활성 중엔 basePrice로 prefill
                    originalPrice = product.baseOriginalPrice ?: "",
                    stock = product.stock.toString(),
                    brandName = product.brandName ?: "",
                    requiredTier = product.requiredTier,
                    loadedStock = product.stock,
                    loadedRequiredTier = product.requiredTier,
                    tags = product.tags,
                    selectedSectionTagId = product.sectionTag?.id,
                    existingImageUrls = product.imageUrls,
                    detailBlocks = blocks
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isLoading = false,
                    error = error.message ?: "상품 정보를 불러올 수 없습니다"
                )
            }
        }
    }

    private fun updateName(name: String) = blockingIntent {
        reduce { state.copy(name = name, nameError = null) }
    }

    private fun updateDescription(description: String) = blockingIntent {
        reduce { state.copy(description = description, descriptionError = null) }
    }

    private fun updatePrice(price: String) = blockingIntent {
        if (price.isEmpty() || price.all { it.isDigit() }) {
            reduce { state.copy(price = price, priceError = null) }
        }
    }

    private fun updateOriginalPrice(originalPrice: String) = blockingIntent {
        if (originalPrice.isEmpty() || originalPrice.all { it.isDigit() }) {
            reduce { state.copy(originalPrice = originalPrice) }
        }
    }

    private fun updateStock(stock: String) = blockingIntent {
        if (stock.isEmpty() || stock.all { it.isDigit() }) {
            reduce { state.copy(stock = stock, stockError = null) }
        }
    }

    private fun updateRequiredTier(tier: String) = intent {
        reduce { state.copy(requiredTier = tier) }
    }

    private fun addImages(uris: List<Uri>) = intent {
        val currentCount = state.selectedImageUris.size + state.existingImageUrls.size - state.removedImageUrls.size
        val available = MAX_IMAGES - currentCount
        if (available <= 0) {
            postSideEffect(CreateProductSideEffect.ShowToast("이미지는 최대 ${MAX_IMAGES}장까지 추가할 수 있습니다"))
            return@intent
        }
        val newUris = uris.take(available)
        reduce { state.copy(selectedImageUris = state.selectedImageUris + newUris) }
    }

    private fun removeImage(uri: Uri) = intent {
        reduce { state.copy(selectedImageUris = state.selectedImageUris - uri) }
    }

    private fun removeExistingImage(url: String) = intent {
        reduce { state.copy(removedImageUrls = state.removedImageUrls + url) }
    }

    private fun save() = intent {
        var hasError = false
        var nameError: String? = null
        var descriptionError: String? = null
        var priceError: String? = null
        var stockError: String? = null

        if (state.name.isBlank()) {
            nameError = "상품명을 입력해주세요"
            hasError = true
        } else if (state.name.length < 2) {
            nameError = "상품명은 2자 이상 입력해주세요"
            hasError = true
        }
        if (state.description.isBlank()) {
            descriptionError = "상품 설명을 입력해주세요"
            hasError = true
        }
        if (state.price.isBlank()) {
            priceError = "가격을 입력해주세요"
            hasError = true
        } else if ((state.price.toLongOrNull() ?: 0) <= 0) {
            priceError = "올바른 가격을 입력해주세요"
            hasError = true
        }
        if (state.stock.isBlank()) {
            stockError = "재고를 입력해주세요"
            hasError = true
        }

        if (hasError) {
            reduce {
                state.copy(
                    nameError = nameError,
                    descriptionError = descriptionError,
                    priceError = priceError,
                    stockError = stockError
                )
            }
            return@intent
        }

        val currentState = state
        reduce { state.copy(isSaving = true) }

        // 이미지 초안 먼저, 상품은 한 요청으로 저장
        val imageDraftIds = mediaDraftRepository.createDrafts(MediaDraftTargetType.PRODUCT, currentState.selectedImageUris.map { it.toString() })
            .getOrElse { return@intent failSave("이미지 업로드에 실패했습니다") }
        val detailImageUris = currentState.detailBlocks
            .filter { it.type == EditorBlockType.IMAGE && it.imageUrl == null }
            .mapNotNull { it.imageUri?.toString() }
        val detailDraftIds = mediaDraftRepository.createDrafts(MediaDraftTargetType.PRODUCT, detailImageUris)
            .getOrElse { return@intent failSave("상세 이미지 업로드에 실패했습니다") }
        if (detailDraftIds.size != detailImageUris.size) {  // 읽지 못한 사진이 있으면 블록 순서가 어긋남
            return@intent failSave("상세 이미지 업로드에 실패했습니다")
        }
        val detailContent = buildDetailContentBlocks(currentState.detailBlocks, ArrayDeque(detailDraftIds))

        val currentProductId = currentState.productId
        val result = if (currentState.isEditMode && currentProductId != null) {
            productRepository.updateProduct(
                productId = currentProductId,
                request = buildUpdateRequest(currentState, imageDraftIds, detailContent)
            )
        } else {
            productRepository.createProduct(
                request = buildCreateRequest(currentState, imageDraftIds, detailContent)
            )
        }

        result.onSuccess {
            reduce { state.copy(isSaving = false) }
            postSideEffect(CreateProductSideEffect.SaveSuccess)
        }.onFailure { error ->
            failSave(error.message ?: "저장에 실패했습니다")
        }
    }

    /** 저장 중단 */
    private suspend fun Syntax<CreateProductState, CreateProductSideEffect>.failSave(message: String) {  // 만든 초안은 서버 만료 정리에 맡김
        reduce { state.copy(isSaving = false, snackbarState = SnackbarState(message = message)) }
    }

    private fun buildCreateRequest(
        currentState: CreateProductState,
        imageDraftIds: List<Int>,
        detailContent: List<DetailContentBlock>
    ): CreateProductRequest {
        val tagIds = currentState.selectedSectionTagId?.let { listOf(it) }
        return CreateProductRequest(
            name = currentState.name.trim(),
            description = currentState.description.trim(),
            price = currentState.price,
            originalPrice = currentState.originalPrice.ifBlank { null },
            stock = currentState.stock.toIntOrNull() ?: 0,
            brandName = currentState.brandName.ifBlank { null },
            requiredTier = currentState.requiredTier,
            imageDraftIds = imageDraftIds,
            detailContent = detailContent.ifEmpty { null },
            tags = currentState.tags.ifEmpty { null },
            tagIds = tagIds,
            sectionTagId = currentState.selectedSectionTagId
        )
    }

    private fun buildUpdateRequest(
        currentState: CreateProductState,
        imageDraftIds: List<Int>,
        detailContent: List<DetailContentBlock>
    ): UpdateProductRequest {
        // 남긴 기존 이미지 뒤에 새 이미지
        val images = currentState.existingImageUrls
            .filterNot { it in currentState.removedImageUrls }
            .map { ProductImageRef.Existing(it) } + imageDraftIds.map { ProductImageRef.Draft(it) }
        return UpdateProductRequest(
            name = currentState.name.trim(),
            description = currentState.description.trim(),
            price = currentState.price,
            originalPrice = currentState.originalPrice.trim(),  // 빈 칸은 ""로 보내 서버에서 정가 지움
            // 재고·티어는 바꾼 경우만, 서버 값 덮지 않게
            stock = (currentState.stock.toIntOrNull() ?: 0).takeIf { it != currentState.loadedStock },
            brandName = currentState.brandName.ifBlank { null },
            requiredTier = currentState.requiredTier.takeIf { it != currentState.loadedRequiredTier },
            images = images,
            // 빈 목록은 지움, null은 유지라 항상 보냄
            detailContent = detailContent,
            tags = currentState.tags,
            tagIds = listOfNotNull(currentState.selectedSectionTagId),
            sectionTagId = currentState.selectedSectionTagId
        )
    }

    private fun navigateBack() = intent {
        postSideEffect(CreateProductSideEffect.NavigateBack)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    // 상세 콘텐츠 블록 에디터
    private fun updateBlockText(blockId: String, newText: String, cursorPosition: Int) = blockingIntent {
        reduce {
            state.copy(
                detailBlocks = state.detailBlocks.map { block ->
                    if (block.id == blockId) {
                        val newFormats = adjustFormats(
                            oldText = block.text,
                            newText = newText,
                            oldFormats = block.formats,
                            cursorPosition = cursorPosition,
                            typingFormat = state.typingFormats[blockId]
                        )
                        block.copy(text = newText, formats = newFormats)
                    } else block
                }
            )
        }
    }

    private fun removeBlock(blockId: String) = intent {
        val blocks = state.detailBlocks.filter { it.id != blockId }
        // 이미지 삭제 후에도 텍스트 병합 안 함
        reduce {
            state.copy(
                detailBlocks = blocks.ifEmpty { listOf(EditorBlock()) }
            )
        }
    }

    /** 영역 단위 드래그 재정렬 */
    private fun moveRegion(fromBlockId: String, toIndex: Int) = intent {  // 텍스트 영역끼리 병합 안 함
        val regions = state.detailBlocks.toDetailRegions()
        val from = regions.indexOfFirst { region -> region.any { it.id == fromBlockId } }
        if (from < 0) return@intent
        val target = toIndex.coerceIn(0, regions.size - 1)
        if (target == from) return@intent
        val moving = regions[from]
        val without = regions.toMutableList().also { it.removeAt(from) }
        without.add(target, moving)
        val result = without.flatten()
        reduce { state.copy(detailBlocks = result.ifEmpty { listOf(EditorBlock()) }) }
    }

    private fun toggleTextAfterImage(imageId: String) = intent {
        val blocks = state.detailBlocks
        val index = blocks.indexOfFirst { it.id == imageId }
        if (index < 0) return@intent
        val next = blocks.getOrNull(index + 1)
        when {
            next != null && next.type == EditorBlockType.TEXT && next.text.isEmpty() -> {
                val updated = blocks.toMutableList().also { it.removeAt(index + 1) }
                reduce {
                    state.copy(
                        detailBlocks = updated.ifEmpty { listOf(EditorBlock()) },
                        focusedBlockId = null
                    )
                }
            }
            next != null && next.type == EditorBlockType.TEXT -> {
                reduce { state.copy(focusedBlockId = next.id, pendingFocusBlockId = next.id) }
            }
            else -> {
                val newBlock = EditorBlock(type = EditorBlockType.TEXT)
                val updated = blocks.toMutableList().also { it.add(index + 1, newBlock) }
                reduce {
                    state.copy(
                        detailBlocks = updated,
                        focusedBlockId = newBlock.id,
                        pendingFocusBlockId = newBlock.id
                    )
                }
            }
        }
    }

    private fun insertDetailImages(uris: List<Uri>, cursorOffset: Int?) = intent {
        val currentImageCount = state.detailBlocks.count { it.type == EditorBlockType.IMAGE }
        val available = MAX_DETAIL_CONTENT_IMAGES - currentImageCount
        if (available <= 0) {
            postSideEffect(CreateProductSideEffect.ShowToast("상세 콘텐츠 이미지는 최대 ${MAX_DETAIL_CONTENT_IMAGES}장까지 추가할 수 있습니다"))
            return@intent
        }

        val newUris = uris.take(available)
        val imageBlocks = newUris.map { EditorBlock(type = EditorBlockType.IMAGE, imageUri = it) }

        val focusedIndex = state.detailBlocks.indexOfFirst { it.id == state.focusedBlockId }
        // 포커스 없으면 마지막 블록 기준
        val anchorIndex = if (focusedIndex >= 0) focusedIndex else state.detailBlocks.lastIndex
        val anchorBlock = state.detailBlocks.getOrNull(anchorIndex)
        val currentBlocks = state.detailBlocks.toMutableList()

        // 삽입 후 포커스 대상 블록
        val continuationBlock: EditorBlock = when {
            anchorBlock == null || anchorBlock.type != EditorBlockType.TEXT -> {
                // 기준이 이미지거나 없으면: 그 아래에 이미지 + 새 빈 텍스트
                val cont = EditorBlock(type = EditorBlockType.TEXT)
                val at = if (anchorIndex >= 0) anchorIndex + 1 else currentBlocks.size
                currentBlocks.addAll(at, imageBlocks + cont)
                cont
            }
            anchorBlock.text.isEmpty() -> {
                // 빈 블록은 앞에 이미지, 블록 재사용
                currentBlocks.addAll(anchorIndex, imageBlocks)
                anchorBlock
            }
            else -> {
                // 내용 있으면 커서에서 쪼개 삽입
                val offset = (cursorOffset ?: anchorBlock.text.length).coerceIn(0, anchorBlock.text.length)
                when (offset) {
                    0 -> {
                        // 맨 앞: 블록 앞에 이미지, 이 블록은 이어쓰기
                        currentBlocks.addAll(anchorIndex, imageBlocks)
                        anchorBlock
                    }
                    anchorBlock.text.length -> {
                        // 맨 뒤: 블록 뒤에 이미지 + 새 빈 텍스트
                        val cont = EditorBlock(
                            type = EditorBlockType.TEXT,
                            textStyle = anchorBlock.textStyle,
                            textAlignment = anchorBlock.textAlignment
                        )
                        currentBlocks.addAll(anchorIndex + 1, imageBlocks + cont)
                        cont
                    }
                    else -> {
                        val fullFormats = formatsOfLength(anchorBlock.text, anchorBlock.formats)
                        val beforeBlock = anchorBlock.copy(
                            text = anchorBlock.text.substring(0, offset),
                            formats = fullFormats.subList(0, offset).toList()
                        )
                        val afterBlock = EditorBlock(
                            type = EditorBlockType.TEXT,
                            text = anchorBlock.text.substring(offset),
                            formats = fullFormats.subList(offset, anchorBlock.text.length).toList(),
                            textStyle = anchorBlock.textStyle,
                            textAlignment = anchorBlock.textAlignment
                        )
                        currentBlocks[anchorIndex] = beforeBlock
                        currentBlocks.addAll(anchorIndex + 1, imageBlocks + afterBlock)
                        afterBlock
                    }
                }
            }
        }

        reduce {
            state.copy(
                detailBlocks = currentBlocks,
                focusedBlockId = continuationBlock.id,
                pendingFocusBlockId = continuationBlock.id
            )
        }
    }

    private fun mergeBlockBackward(blockId: String) = intent {
        val blocks = state.detailBlocks
        val index = blocks.indexOfFirst { it.id == blockId }
        if (index <= 0) return@intent
        val current = blocks[index]
        if (current.type != EditorBlockType.TEXT) return@intent
        val prev = blocks[index - 1]
        val newBlocks = blocks.toMutableList()

        if (prev.type == EditorBlockType.TEXT) {
            // 앞 블록과 병합, 커서는 접합점
            val mergedBlock = prev.copy(
                text = prev.text + current.text,
                formats = formatsOfLength(prev.text, prev.formats) + formatsOfLength(current.text, current.formats)
            )
            newBlocks[index - 1] = mergedBlock
            newBlocks.removeAt(index)
            reduce {
                state.copy(
                    detailBlocks = newBlocks,
                    focusedBlockId = mergedBlock.id,
                    pendingFocusBlockId = mergedBlock.id
                )
            }
        }
    }

    private fun focusBlock(blockId: String?) = intent {
        // 소비된 포커스 요청 해제
        val pending = if (state.pendingFocusBlockId == blockId) null else state.pendingFocusBlockId
        reduce { state.copy(focusedBlockId = blockId, pendingFocusBlockId = pending) }
    }

    /** formats 길이를 text에 맞춤 */
    private fun formatsOfLength(text: String, formats: List<CharFormat>): List<CharFormat> {
        val f = formats.toMutableList()
        while (f.size < text.length) f.add(CharFormat())
        while (f.size > text.length) f.removeAt(f.lastIndex)
        return f
    }

    private fun toggleTextFormatBar() = intent {
        reduce { state.copy(showTextFormatBar = !state.showTextFormatBar) }
    }

    private fun toggleBold(blockId: String, start: Int, end: Int) = intent {
        toggleInlineFormat(blockId, start, end, { it.bold }) { fmt, enabled -> fmt.copy(bold = enabled) }
    }

    private fun toggleItalic(blockId: String, start: Int, end: Int) = intent {
        toggleInlineFormat(blockId, start, end, { it.italic }) { fmt, enabled -> fmt.copy(italic = enabled) }
    }

    private fun toggleUnderline(blockId: String, start: Int, end: Int) = intent {
        toggleInlineFormat(blockId, start, end, { it.underline }) { fmt, enabled -> fmt.copy(underline = enabled) }
    }

    private fun toggleStrikethrough(blockId: String, start: Int, end: Int) = intent {
        toggleInlineFormat(blockId, start, end, { it.strikethrough }) { fmt, enabled ->
            fmt.copy(strikethrough = enabled)
        }
    }

    private fun setTextColor(blockId: String, start: Int, end: Int, colorHex: String?) = intent {
        toggleInlineFormat(blockId, start, end, { it.textColor == colorHex }) { fmt, enabled ->
            fmt.copy(textColor = if (enabled) colorHex else null)
        }
    }

    private fun updateBlockTextStyle(blockId: String, textStyle: RichTextBlockStyle, cursorOffset: Int) = intent {
        val blocks = state.detailBlocks
        val index = blocks.indexOfFirst { it.id == blockId }
        if (index < 0) return@intent
        val block = blocks[index]
        val updated = blocks.toMutableList()

        // 같은 스타일 재탭은 PARAGRAPH로
        if (block.textStyle == textStyle) {
            updated[index] = block.copy(textStyle = RichTextBlockStyle.PARAGRAPH)
            reduce { state.copy(detailBlocks = updated) }
            return@intent
        }

        // 현재 줄만 변환, 블록 전체 변환 금지
        val text = block.text
        val cursor = cursorOffset.coerceIn(0, text.length)
        // 맨 앞 커서는 0, 첫 글자가 줄바꿈이면 범위 초과
        val lineStart = if (cursor == 0) 0 else text.lastIndexOf('\n', cursor - 1) + 1
        val lineEnd = text.indexOf('\n', cursor).let { if (it < 0) text.length else it }

        // 단일 줄은 블록 전체 변환
        if (lineStart == 0 && lineEnd == text.length) {
            updated[index] = block.copy(textStyle = textStyle)
            reduce { state.copy(detailBlocks = updated) }
            return@intent
        }

        val fullFormats = formatsOfLength(text, block.formats)
        val hasBefore = lineStart > 0           // 앞 줄들: \n 앞까지를 한 문단으로
        val hasAfter = lineEnd < text.length    // 뒷 줄들: \n 다음부터를 한 문단으로
        val beforeEnd = (lineStart - 1).coerceAtLeast(0)
        val afterStart = (lineEnd + 1).coerceAtMost(text.length)

        val newBlocks = mutableListOf<EditorBlock>()
        if (hasBefore) {
            newBlocks.add(
                EditorBlock(
                    type = EditorBlockType.TEXT,
                    regionId = block.regionId,  // 스타일만 분리, 같은 글 영역
                    text = text.substring(0, beforeEnd),
                    formats = fullFormats.subList(0, beforeEnd).toList(),
                    textStyle = block.textStyle,
                    textAlignment = block.textAlignment
                )
            )
        }
        // 현재 줄은 원래 id 유지
        val currentBlock = block.copy(
            text = text.substring(lineStart, lineEnd),
            formats = fullFormats.subList(lineStart, lineEnd).toList(),
            textStyle = textStyle
        )
        newBlocks.add(currentBlock)
        if (hasAfter) {
            newBlocks.add(
                EditorBlock(
                    type = EditorBlockType.TEXT,
                    regionId = block.regionId,  // 스타일만 분리, 같은 글 영역
                    text = text.substring(afterStart, text.length),
                    formats = fullFormats.subList(afterStart, text.length).toList(),
                    textStyle = block.textStyle,
                    textAlignment = block.textAlignment
                )
            )
        }

        updated.removeAt(index)
        updated.addAll(index, newBlocks)
        reduce {
            state.copy(
                detailBlocks = updated,
                focusedBlockId = currentBlock.id,
                pendingFocusBlockId = currentBlock.id
            )
        }
    }

    private fun splitListItem(blockId: String, splitOffset: Int) = intent {
        val blocks = state.detailBlocks
        val index = blocks.indexOfFirst { it.id == blockId }
        if (index < 0) return@intent
        val block = blocks[index]
        // splitOffset 앞 줄바꿈에서 가름
        val p = (splitOffset - 1).coerceIn(0, block.text.length)
        val beforeText = block.text.substring(0, p)
        val afterText = block.text.substring(p)

        val newBlocks = blocks.toMutableList()
        // 빈 항목 엔터는 PARAGRAPH
        if (beforeText.isEmpty() && afterText.isEmpty()) {
            newBlocks[index] = block.copy(textStyle = RichTextBlockStyle.PARAGRAPH)
            reduce { state.copy(detailBlocks = newBlocks) }
            return@intent
        }

        val fullFormats = formatsOfLength(block.text, block.formats)
        val beforeBlock = block.copy(text = beforeText, formats = fullFormats.subList(0, p).toList())
        val afterBlock = EditorBlock(
            type = EditorBlockType.TEXT,
            regionId = block.regionId,        // 리스트 항목만 분리, 같은 글 영역
            text = afterText,
            formats = fullFormats.subList(p, block.text.length).toList(),
            textStyle = block.textStyle,      // 같은 리스트 스타일로 다음 항목 시작
            textAlignment = block.textAlignment
        )
        newBlocks[index] = beforeBlock
        newBlocks.add(index + 1, afterBlock)
        reduce {
            state.copy(
                detailBlocks = newBlocks,
                focusedBlockId = afterBlock.id,
                pendingFocusBlockId = afterBlock.id
            )
        }
    }

    private fun exitQuote(blockId: String, splitOffset: Int) = intent {
        val blocks = state.detailBlocks
        val index = blocks.indexOfFirst { it.id == blockId }
        if (index < 0) return@intent
        val block = blocks[index]
        val len = block.text.length
        // 빈 줄 줄바꿈 제거, 앞은 인용·뒤는 새 문단
        val stripPos = (splitOffset - 2).coerceIn(0, len)
        val afterStart = (stripPos + 1).coerceIn(0, len)
        val beforeText = block.text.substring(0, stripPos)
        val afterText = block.text.substring(afterStart)
        val fullFormats = formatsOfLength(block.text, block.formats)

        val newBlocks = blocks.toMutableList()
        if (beforeText.isEmpty()) {
            // 인용이 비어 있으면 이 블록 자체를 일반 문단으로 전환
            newBlocks[index] = block.copy(
                text = afterText,
                formats = fullFormats.subList(afterStart, len).toList(),
                textStyle = RichTextBlockStyle.PARAGRAPH
            )
            reduce {
                state.copy(detailBlocks = newBlocks, focusedBlockId = block.id, pendingFocusBlockId = block.id)
            }
            return@intent
        }
        val quoteBlock = block.copy(text = beforeText, formats = fullFormats.subList(0, stripPos).toList())
        val paragraphBlock = EditorBlock(
            type = EditorBlockType.TEXT,
            regionId = block.regionId,  // 인용 다음 문단, 같은 글 영역
            text = afterText,
            formats = fullFormats.subList(afterStart, len).toList(),
            textStyle = RichTextBlockStyle.PARAGRAPH,
            textAlignment = block.textAlignment
        )
        newBlocks[index] = quoteBlock
        newBlocks.add(index + 1, paragraphBlock)
        reduce {
            state.copy(
                detailBlocks = newBlocks,
                focusedBlockId = paragraphBlock.id,
                pendingFocusBlockId = paragraphBlock.id
            )
        }
    }

    private fun updateBlockAlign(blockId: String, alignment: RichTextAlignment) = intent {
        reduce {
            state.copy(
                detailBlocks = state.detailBlocks.map { block ->
                    if (block.id == blockId) block.copy(textAlignment = alignment) else block
                }
            )
        }
    }

    private suspend fun Syntax<CreateProductState, CreateProductSideEffect>.toggleInlineFormat(
        blockId: String,
        start: Int,
        end: Int,
        isEnabled: (CharFormat) -> Boolean,
        update: (CharFormat, Boolean) -> CharFormat
    ) {
        reduce {
            state.copy(
                detailBlocks = state.detailBlocks.map { block ->
                    if (block.id == blockId) {
                        if (start == end) return@map block
                        val fmts = block.formats.toMutableList()
                        val allEnabled = (start until end).all { i -> i < fmts.size && isEnabled(fmts[i]) }
                        for (i in start until end.coerceAtMost(fmts.size)) {
                            fmts[i] = update(fmts[i], !allEnabled)
                        }
                        block.copy(formats = fmts)
                    } else block
                },
                typingFormats = state.typingFormats.toMutableMap().apply {
                    val targetBlock = state.detailBlocks.firstOrNull { it.id == blockId }
                    if (targetBlock != null && start == end) {
                        val baseFormat = this[blockId] ?: currentFormatAt(targetBlock.formats, start)
                        val nextEnabled = !isEnabled(baseFormat)
                        this[blockId] = update(baseFormat, nextEnabled)
                    }
                }
            )
        }
    }

    private fun adjustFormats(
        oldText: String,
        newText: String,
        oldFormats: List<CharFormat>,
        cursorPosition: Int,
        typingFormat: CharFormat? = null
    ): List<CharFormat> {
        val delta = newText.length - oldText.length
        if (delta == 0 && oldFormats.size == newText.length) return oldFormats

        val formats = oldFormats.toMutableList()
        while (formats.size < oldText.length) formats.add(CharFormat())

        if (delta > 0) {
            val insertPos = (cursorPosition - delta).coerceIn(0, formats.size)
            val inheritFormat = typingFormat ?: if (insertPos > 0) formats[insertPos - 1] else CharFormat()
            repeat(delta) { formats.add(insertPos, inheritFormat) }
        } else if (delta < 0) {
            val deleteStart = cursorPosition.coerceIn(0, formats.size)
            repeat((-delta).coerceAtMost(formats.size - deleteStart)) {
                if (deleteStart < formats.size) formats.removeAt(deleteStart)
            }
        }

        while (formats.size > newText.length) formats.removeAt(formats.lastIndex)
        while (formats.size < newText.length) formats.add(CharFormat())
        return formats
    }

    private fun currentFormatAt(formats: List<CharFormat>, cursorPosition: Int): CharFormat {
        if (formats.isEmpty()) return CharFormat()
        val index = when {
            cursorPosition in formats.indices -> cursorPosition
            cursorPosition - 1 in formats.indices -> cursorPosition - 1
            else -> formats.lastIndex
        }
        return formats.getOrElse(index) { CharFormat() }
    }

    /** 연속 텍스트 블록 병합 */
    private fun mergeDetailContentToBlocks(
        blocks: List<DetailContentBlock>
    ): List<EditorBlock> {
        val result = mutableListOf<EditorBlock>()
        val currentText = StringBuilder()
        val currentFormats = mutableListOf<CharFormat>()
        var currentBlockId: String? = null
        var currentTextStyle = RichTextBlockStyle.PARAGRAPH
        var currentTextAlignment = RichTextAlignment.START
        // 이미지 전까지 연속 텍스트는 같은 regionId
        var currentRegionId = UUID.randomUUID().toString()

        fun flushText() {
            if (currentText.isNotEmpty()) {
                result.add(
                    EditorBlock(
                        id = currentBlockId ?: UUID.randomUUID().toString(),
                        regionId = currentRegionId,
                        type = EditorBlockType.TEXT,
                        text = currentText.toString(),
                        formats = currentFormats.toList(),
                        textStyle = currentTextStyle,
                        textAlignment = currentTextAlignment
                    )
                )
                currentText.clear()
                currentFormats.clear()
                currentBlockId = null
                currentTextStyle = RichTextBlockStyle.PARAGRAPH
                currentTextAlignment = RichTextAlignment.START
            }
        }

        for (block in blocks) {
            when (block.type) {
                "text" -> {
                    val content = block.content ?: ""
                    val shouldFlush = currentText.isNotEmpty() && (
                        (block.blockId != null && currentBlockId != null && block.blockId != currentBlockId) ||
                            block.textStyle != currentTextStyle ||
                            block.textAlignment != currentTextAlignment
                        )
                    if (shouldFlush) flushText()
                    if (currentText.isEmpty()) {
                        currentBlockId = block.blockId
                        currentTextStyle = block.textStyle
                        currentTextAlignment = block.textAlignment
                    }
                    val format = CharFormat(
                        bold = block.bold,
                        italic = block.italic,
                        underline = block.underline,
                        strikethrough = block.strikethrough,
                        textColor = block.textColor
                    )
                    currentText.append(content)
                    repeat(content.length) { currentFormats.add(format) }
                }
                "image" -> {
                    flushText()
                    block.url?.let {
                        result.add(
                            EditorBlock(
                                type = EditorBlockType.IMAGE,
                                imageUrl = it,
                                imageWidth = block.width,
                                imageHeight = block.height
                            )
                        )
                    }
                    // 이미지 다음 텍스트런은 새 영역
                    currentRegionId = UUID.randomUUID().toString()
                }
            }
        }
        flushText()
        return result
    }

    // 태그
    private fun openTagBottomSheet() = intent {
        reduce {
            state.copy(
                showTagBottomSheet = true,
                pendingTags = state.tags,
                pendingSectionTagId = state.selectedSectionTagId
            )
        }
    }

    private fun closeTagBottomSheet() = intent {
        reduce {
            state.copy(
                showTagBottomSheet = false,
                pendingTags = emptyList(),
                pendingSectionTagId = null
            )
        }
    }

    private fun confirmTagChanges() = intent {
        reduce {
            state.copy(
                showTagBottomSheet = false,
                tags = state.pendingTags,
                selectedSectionTagId = state.pendingSectionTagId,
                pendingTags = emptyList(),
                pendingSectionTagId = null
            )
        }
    }

    private fun addTag(tag: String) = intent {
        val trimmedTag = tag.trim()
        if (trimmedTag.isBlank()) return@intent

        if (state.pendingTags.size >= MAX_TAGS) {
            postSideEffect(CreateProductSideEffect.ShowToast("태그는 최대 ${MAX_TAGS}개까지 추가할 수 있습니다"))
            return@intent
        }

        if (state.pendingTags.any { it.equals(trimmedTag, ignoreCase = true) }) {
            postSideEffect(CreateProductSideEffect.ShowToast("이미 추가된 태그입니다"))
            return@intent
        }

        reduce {
            state.copy(pendingTags = state.pendingTags + trimmedTag)
        }
    }

    private fun removeTag(tag: String) = intent {
        reduce {
            state.copy(pendingTags = state.pendingTags - tag)
        }
    }

    private fun selectSectionTag(tagId: Int?) = intent {
        reduce {
            state.copy(pendingSectionTagId = tagId)
        }
    }

    private fun createSectionTag(name: String) = intent {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return@intent
        // 로드·편집 중엔 막음, 늦은 로드가 새 태그를 덮음
        if (state.isLoadingSectionTags) return@intent

        if (state.sectionTags.size >= MAX_SECTION_TAGS) {
            postSideEffect(CreateProductSideEffect.ShowToast("섹션 태그는 최대 ${MAX_SECTION_TAGS}개까지 추가할 수 있습니다"))
            return@intent
        }

        reduce { state.copy(isLoadingSectionTags = true) }

        createTagUseCase(
            name = trimmedName,
            targetType = TagTargetType.PRODUCT,
            isSectionEnabled = true
        ).onSuccess { newTag ->
            reduce {
                state.copy(
                    sectionTags = state.sectionTags + newTag,
                    isLoadingSectionTags = false
                )
            }
            postSideEffect(CreateProductSideEffect.ShowToast("섹션 태그가 추가되었습니다"))
        }.onFailure { error ->
            reduce { state.copy(isLoadingSectionTags = false) }
            postSideEffect(CreateProductSideEffect.ShowToast(error.message ?: "섹션 태그 추가 실패"))
        }
    }

    private fun updateSectionTag(tagId: Int, name: String) = intent {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return@intent
        if (state.isLoadingSectionTags) return@intent

        reduce { state.copy(isLoadingSectionTags = true) }

        updateTagUseCase(
            tagId = tagId,
            name = trimmedName
        ).onSuccess { updatedTag ->
            reduce {
                state.copy(
                    sectionTags = state.sectionTags.map { if (it.id == tagId) updatedTag else it },
                    isLoadingSectionTags = false
                )
            }
            postSideEffect(CreateProductSideEffect.ShowToast("섹션 태그가 수정되었습니다"))
        }.onFailure { error ->
            reduce { state.copy(isLoadingSectionTags = false) }
            postSideEffect(CreateProductSideEffect.ShowToast(error.message ?: "섹션 태그 수정 실패"))
        }
    }

    private fun deleteSectionTag(tagId: Int) = intent {
        if (state.isLoadingSectionTags) return@intent
        reduce { state.copy(isLoadingSectionTags = true) }

        deleteTagUseCase(tagId).onSuccess {
            reduce {
                state.copy(
                    sectionTags = state.sectionTags.filter { it.id != tagId },
                    selectedSectionTagId = if (state.selectedSectionTagId == tagId) null else state.selectedSectionTagId,
                    pendingSectionTagId = if (state.pendingSectionTagId == tagId) null else state.pendingSectionTagId,
                    isLoadingSectionTags = false
                )
            }
            postSideEffect(CreateProductSideEffect.ShowToast("섹션 태그가 삭제되었습니다"))
        }.onFailure { error ->
            reduce { state.copy(isLoadingSectionTags = false) }
            postSideEffect(CreateProductSideEffect.ShowToast(error.message ?: "섹션 태그 삭제 실패"))
        }
    }

    private fun dismissTagToast() = intent {
        reduce { state.copy(showTagToast = false, tagToastMessage = "") }
    }

    private fun buildDetailContentBlocks(blocks: List<EditorBlock>, draftIds: ArrayDeque<Int>): List<DetailContentBlock> {
        val result = mutableListOf<DetailContentBlock>()
        for (block in blocks) {
            when (block.type) {
                EditorBlockType.TEXT -> {
                    if (block.text.isNotBlank()) {
                        val segments = splitByFormat(block.text, block.formats)
                        segments.forEach { (text, format) ->
                            // 서식 경계의 공백·줄바꿈 구간도 보존
                            if (text.isNotEmpty()) {
                                result.add(
                                    DetailContentBlock(
                                        type = "text",
                                        blockId = block.id,
                                        content = text,
                                        textStyle = block.textStyle,
                                        textAlignment = block.textAlignment,
                                        bold = format.bold,
                                        italic = format.italic,
                                        underline = format.underline,
                                        strikethrough = format.strikethrough,
                                        textColor = format.textColor
                                    )
                                )
                            }
                        }
                    }
                }
                EditorBlockType.IMAGE -> {
                    if (block.imageUrl != null) {
                        // 원본 치수 함께 전송
                        result.add(
                            DetailContentBlock(
                                type = "image",
                                blockId = block.id,
                                url = block.imageUrl,
                                width = block.imageWidth,
                                height = block.imageHeight
                            )
                        )
                    } else if (block.imageUri != null) {
                        // 새 이미지는 초안 id, 서버가 주소로 바꿈
                        result.add(DetailContentBlock(type = "image", blockId = block.id, draftId = draftIds.removeFirst()))
                    }
                }
            }
        }
        return result
    }

    private fun splitByFormat(text: String, formats: List<CharFormat>): List<Pair<String, CharFormat>> {
        if (text.isEmpty()) return emptyList()
        val result = mutableListOf<Pair<String, CharFormat>>()
        var currentFormat = formats.getOrElse(0) { CharFormat() }
        var start = 0
        for (i in text.indices) {
            val fmt = formats.getOrElse(i) { CharFormat() }
            if (fmt != currentFormat) {
                result.add(text.substring(start, i) to currentFormat)
                currentFormat = fmt
                start = i
            }
        }
        result.add(text.substring(start) to currentFormat)
        return result
    }

    private fun loadSectionTags() = intent {
        // 프로필 조회 중에도 태그 편집 막음
        reduce { state.copy(isLoadingSectionTags = true) }
        // 캐시가 비었으면 조회, 없으면 섹션 태그가 안 뜸
        val myProfile = getCachedMyProfileUseCase() ?: getMyProfileUseCase().getOrNull()
        if (myProfile == null || !myProfile.isCreator) {
            reduce { state.copy(isLoadingSectionTags = false) }
            return@intent
        }

        getCreatorTagsUseCase(
            creatorId = myProfile.userId,
            targetType = TagTargetType.PRODUCT
        ).onSuccess { tags ->
            reduce {
                state.copy(
                    sectionTags = tags.filter { it.isSectionEnabled },
                    isLoadingSectionTags = false
                )
            }
        }.onFailure {
            reduce { state.copy(isLoadingSectionTags = false) }
        }
    }
}
