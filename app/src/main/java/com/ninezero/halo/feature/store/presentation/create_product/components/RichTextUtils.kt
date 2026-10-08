package com.ninezero.halo.feature.store.presentation.create_product.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.graphics.toColorInt
import com.ninezero.halo.feature.store.domain.model.RichTextAlignment
import com.ninezero.halo.feature.store.domain.model.RichTextBlockStyle

@Composable
fun richEditorTextStyle(textStyle: RichTextBlockStyle): TextStyle = when (textStyle) {
    RichTextBlockStyle.PARAGRAPH -> MaterialTheme.typography.bodyLarge
    RichTextBlockStyle.HEADING1 -> MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
    RichTextBlockStyle.HEADING2 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
    RichTextBlockStyle.QUOTE -> MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic)
    RichTextBlockStyle.BULLET,
    RichTextBlockStyle.NUMBERED -> MaterialTheme.typography.bodyLarge
}

fun richTextAlign(alignment: RichTextAlignment): TextAlign = when (alignment) {
    RichTextAlignment.START -> TextAlign.Start
    RichTextAlignment.CENTER -> TextAlign.Center
    RichTextAlignment.END -> TextAlign.End
}

fun richTextColorOrNull(colorHex: String?): Color? {
    return colorHex?.let {
        runCatching { Color(it.toColorInt()) }.getOrNull()
    }
}

fun richTextDecoration(underline: Boolean, strikethrough: Boolean): TextDecoration {
    val decorations = buildList {
        if (underline) add(TextDecoration.Underline)
        if (strikethrough) add(TextDecoration.LineThrough)
    }
    return if (decorations.isEmpty()) TextDecoration.None else TextDecoration.combine(decorations)
}
