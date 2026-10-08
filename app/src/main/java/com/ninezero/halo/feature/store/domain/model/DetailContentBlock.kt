package com.ninezero.halo.feature.store.domain.model

enum class RichTextBlockStyle(val wireValue: String) {
    PARAGRAPH("paragraph"),
    HEADING1("heading1"),
    HEADING2("heading2"),
    QUOTE("quote"),
    BULLET("bullet"),
    NUMBERED("numbered");

    companion object {
        fun fromWire(value: String?): RichTextBlockStyle {
            return entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: PARAGRAPH
        }
    }
}

enum class RichTextAlignment(val wireValue: String) {
    START("start"),
    CENTER("center"),
    END("end");

    companion object {
        fun fromWire(value: String?): RichTextAlignment {
            return entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: START
        }
    }
}

/** 상품 상세 콘텐츠 블록 */
data class DetailContentBlock(  // text는 content+스타일, image는 url+크기
    val type: String,
    val blockId: String? = null,
    val content: String? = null,
    val url: String? = null,
    val draftId: Int? = null,  // 새 이미지는 저장 요청에서만 초안 ID
    val width: Int? = null,
    val height: Int? = null,
    val textStyle: RichTextBlockStyle = RichTextBlockStyle.PARAGRAPH,
    val textAlignment: RichTextAlignment = RichTextAlignment.START,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val textColor: String? = null
)
