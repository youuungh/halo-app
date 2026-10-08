package com.ninezero.halo.feature.store.presentation.create_product

import android.net.Uri
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.RichTextAlignment
import com.ninezero.halo.feature.store.domain.model.RichTextBlockStyle
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.tag.domain.model.Tag
import java.util.UUID

data class CharFormat(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val textColor: String? = null
)

/** 에디터 블록 */
data class EditorBlock(  // 새 이미지는 uri, 기존 이미지는 url
    val id: String = UUID.randomUUID().toString(),
    // 재정렬 영역 식별자, 같은 글 영역은 같은 값
    val regionId: String = UUID.randomUUID().toString(),
    val type: EditorBlockType = EditorBlockType.TEXT,
    val text: String = "",
    val formats: List<CharFormat> = emptyList(),
    val textStyle: RichTextBlockStyle = RichTextBlockStyle.PARAGRAPH,
    val textAlignment: RichTextAlignment = RichTextAlignment.START,
    val imageUri: Uri? = null,
    val imageUrl: String? = null,
    // 기존 이미지 원본 치수 보존
    val imageWidth: Int? = null,
    val imageHeight: Int? = null
)

enum class EditorBlockType { TEXT, IMAGE }

/** 드래그 재정렬 영역 그룹핑 */
fun List<EditorBlock>.toDetailRegions(): List<List<EditorBlock>> {  // 같은 regionId 연속 텍스트만 한 영역
    val regions = mutableListOf<List<EditorBlock>>()
    val run = mutableListOf<EditorBlock>()
    fun flush() {
        if (run.isNotEmpty()) {
            regions.add(run.toList())
            run.clear()
        }
    }
    forEach { block ->
        if (block.type == EditorBlockType.TEXT) {
            if (run.isNotEmpty() && run.first().regionId != block.regionId) flush()
            run.add(block)
        } else {
            flush()
            regions.add(listOf(block))
        }
    }
    flush()
    return regions
}

data class CreateProductState(
    val isLoading: Boolean = false,
    val error: String? = null,  // 로드 실패 시 재시도 화면
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val productId: Int? = null,
    // 폼 필드
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val originalPrice: String = "",
    val stock: String = "",
    val brandName: String = "",
    val requiredTier: String = SubscriptionPlanTier.FREE.name,
    val planTiers: Set<SubscriptionPlanTier> = emptySet(),  // 활성 플랜이 있는 등급
    // 수정 폼을 연 때 값, 바뀐 경우만 보냄
    val loadedStock: Int? = null,
    val loadedRequiredTier: String? = null,
    // 대표 이미지
    val selectedImageUris: List<Uri> = emptyList(),
    val existingImageUrls: List<String> = emptyList(),
    val removedImageUrls: List<String> = emptyList(),
    // 상세 콘텐츠
    val detailBlocks: List<EditorBlock> = listOf(EditorBlock()),
    val focusedBlockId: String? = null,
    // 프로그램 포커스 대상 블록
    val pendingFocusBlockId: String? = null,
    val showTextFormatBar: Boolean = false,
    val typingFormats: Map<String, CharFormat> = emptyMap(),
    // 태그
    val showTagBottomSheet: Boolean = false,
    val tags: List<String> = emptyList(),
    val pendingTags: List<String> = emptyList(),
    val sectionTags: List<Tag> = emptyList(),
    val selectedSectionTagId: Int? = null,
    val pendingSectionTagId: Int? = null,
    val isLoadingSectionTags: Boolean = false,
    val showTagToast: Boolean = false,
    val tagToastMessage: String = "",
    // 검증
    val nameError: String? = null,
    val descriptionError: String? = null,
    val priceError: String? = null,
    val stockError: String? = null,
    // 섹션 접기
    val collapsedSections: Set<String> = emptySet(),

    val snackbarState: SnackbarState? = null
)

sealed interface CreateProductIntent {
    data object NavigateBack : CreateProductIntent
    data class UpdateName(val name: String) : CreateProductIntent
    data class UpdateDescription(val description: String) : CreateProductIntent
    data class UpdatePrice(val price: String) : CreateProductIntent
    data class UpdateOriginalPrice(val originalPrice: String) : CreateProductIntent
    data class UpdateStock(val stock: String) : CreateProductIntent
    data class UpdateRequiredTier(val tier: String) : CreateProductIntent
    data class AddImages(val uris: List<Uri>) : CreateProductIntent
    data class RemoveImage(val uri: Uri) : CreateProductIntent
    data class RemoveExistingImage(val url: String) : CreateProductIntent
    data object Save : CreateProductIntent
    data object SnackbarDismissed : CreateProductIntent
    // 상세 콘텐츠 블록 에디터
    data class UpdateBlockText(val blockId: String, val text: String, val cursorPosition: Int) : CreateProductIntent
    data class RemoveBlock(val blockId: String) : CreateProductIntent
    // 영역 단위 드래그 재정렬
    data class MoveRegion(val fromBlockId: String, val toIndex: Int) : CreateProductIntent
    // 이미지 탭 토글
    data class ToggleTextAfterImage(val imageId: String) : CreateProductIntent
    data class InsertDetailImages(val uris: List<Uri>, val cursorOffset: Int? = null) : CreateProductIntent  // null이면 끝에 삽입
    data class MergeBlockBackward(val blockId: String) : CreateProductIntent
    // 리스트 블록 엔터는 새 항목
    data class SplitListItem(val blockId: String, val splitOffset: Int) : CreateProductIntent
    // 인용 더블 엔터는 새 문단
    data class ExitQuote(val blockId: String, val splitOffset: Int) : CreateProductIntent
    data class FocusBlock(val blockId: String?) : CreateProductIntent
    data object ToggleTextFormatBar : CreateProductIntent
    data class ToggleBold(val blockId: String, val start: Int, val end: Int) : CreateProductIntent
    data class ToggleItalic(val blockId: String, val start: Int, val end: Int) : CreateProductIntent
    data class ToggleUnderline(val blockId: String, val start: Int, val end: Int) : CreateProductIntent
    data class ToggleStrikethrough(val blockId: String, val start: Int, val end: Int) : CreateProductIntent
    data class SetTextColor(val blockId: String, val start: Int, val end: Int, val colorHex: String?) : CreateProductIntent
    data class UpdateBlockTextStyle(val blockId: String, val textStyle: RichTextBlockStyle, val cursorOffset: Int) : CreateProductIntent  // 커서 줄에만 적용
    data class UpdateBlockTextAlignment(val blockId: String, val alignment: RichTextAlignment) : CreateProductIntent
    // 태그
    data object OpenTagBottomSheet : CreateProductIntent
    data object CloseTagBottomSheet : CreateProductIntent
    data object ConfirmTagChanges : CreateProductIntent
    data class AddTag(val tag: String) : CreateProductIntent
    data class RemoveTag(val tag: String) : CreateProductIntent
    data class SelectSectionTag(val tagId: Int?) : CreateProductIntent
    data class CreateSectionTag(val name: String) : CreateProductIntent
    data class UpdateSectionTag(val tagId: Int, val name: String) : CreateProductIntent
    data class DeleteSectionTag(val tagId: Int) : CreateProductIntent
    data object DismissTagToast : CreateProductIntent
    // 섹션 접기
    data class ToggleSectionCollapse(val section: String) : CreateProductIntent
    data object Retry : CreateProductIntent
}

sealed interface CreateProductSideEffect {
    data object NavigateBack : CreateProductSideEffect
    data object SaveSuccess : CreateProductSideEffect
    data class ShowToast(val message: String) : CreateProductSideEffect
}
