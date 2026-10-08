package com.ninezero.halo.feature.store.presentation.create_product

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.tag.domain.model.Tag

@HaloLightDarkPreview
@Composable
private fun CreateProductPreview() {
    HaloScreenPreview {
        CreateProductScreenContent(
            state = CreateProductState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreateProductEditModePreview() {
    HaloScreenPreview {
        CreateProductScreenContent(
            state = CreateProductState(
                isEditMode = true,
                productId = 1,
                name = "프리미엄 블루투스 이어폰",
                description = "고음질 블루투스 이어폰입니다. ANC 지원.",
                price = "89000",
                originalPrice = "110000",
                stock = "25",
                requiredTier = SubscriptionPlanTier.FREE.name
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreateProductWithImagesPreview() {
    HaloScreenPreview {
        CreateProductScreenContent(
            state = CreateProductState(
                isEditMode = true,
                productId = 1,
                name = "프리미엄 블루투스 이어폰",
                description = "고음질 블루투스 이어폰입니다.",
                price = "89000",
                originalPrice = "110000",
                stock = "25",
                existingImageUrls = listOf(
                    "https://picsum.photos/200/200",
                    "https://picsum.photos/201/200",
                    "https://picsum.photos/202/200"
                ),
                tags = listOf("음향", "프리미엄"),
                sectionTags = listOf(
                    Tag(
                        id = 1, name = "전자기기", isSectionEnabled = true
                    )
                ),
                selectedSectionTagId = 1
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreateProductValidationErrorPreview() {
    HaloScreenPreview {
        CreateProductScreenContent(
            state = CreateProductState(
                name = "A",
                nameError = "상품명은 2자 이상 입력해주세요",
                descriptionError = "상품 설명을 입력해주세요",
                priceError = "가격을 입력해주세요",
                stockError = "재고를 입력해주세요"
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreateProductTextFormatBarPreview() {
    val focusedBlock = EditorBlock(
        id = "focused-block",
        type = EditorBlockType.TEXT,
        text = "강조할 텍스트를 선택한 상태의 프리뷰입니다.",
        formats = List("강조할 텍스트를 선택한 상태의 프리뷰입니다.".length) { index ->
            when (index) {
                in 0..2 -> CharFormat(bold = true)
                in 10..14 -> CharFormat(underline = true)
                else -> CharFormat()
            }
        }
    )

    HaloScreenPreview {
        CreateProductScreenContent(
            state = CreateProductState(
                name = "프리미엄 블루투스 이어폰",
                description = "고음질 블루투스 이어폰입니다.",
                price = "89000",
                stock = "25",
                detailBlocks = listOf(
                    focusedBlock,
                    EditorBlock(
                        type = EditorBlockType.IMAGE,
                        imageUrl = "https://picsum.photos/400/240"
                    ),
                    EditorBlock(
                        type = EditorBlockType.TEXT,
                        text = "이미지 아래에 이어서 설명을 작성하는 흐름입니다.",
                        formats = List("이미지 아래에 이어서 설명을 작성하는 흐름입니다.".length) { CharFormat() }
                    )
                ),
                focusedBlockId = focusedBlock.id,
                showTextFormatBar = true
            ),
            previewFocusedSelection = focusedBlock.id to TextRange(0, 7)
        )
    }
}
