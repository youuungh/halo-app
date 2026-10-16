package com.ninezero.halo.feature.store.presentation.create_product.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.feature.store.domain.model.RichTextAlignment
import com.ninezero.halo.feature.store.domain.model.RichTextBlockStyle
import com.ninezero.halo.feature.store.presentation.create_product.CharFormat
import com.ninezero.halo.feature.store.presentation.create_product.EditorBlock
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState

@Composable
fun RichTextActionBar(
    block: EditorBlock,
    selection: TextRange,
    typingFormat: CharFormat?,
    hazeState: HazeState,
    backgroundColor: Color,
    onToggleBold: (TextRange) -> Unit,
    onToggleItalic: (TextRange) -> Unit,
    onToggleUnderline: (TextRange) -> Unit,
    onToggleStrikethrough: (TextRange) -> Unit,
    onSetTextColor: (String?) -> Unit,
    onUpdateTextStyle: (RichTextBlockStyle) -> Unit,
    onUpdateTextAlignment: (RichTextAlignment) -> Unit
) {
    val hasSelection = selection.start != selection.end
    val activeFormat = activeInlineFormat(block, selection, typingFormat)
    val selectedColor = activeFormat.textColor
    val allBold = if (hasSelection) isFormatEnabled(block, selection) { it.bold } else activeFormat.bold
    val allItalic = if (hasSelection) isFormatEnabled(block, selection) { it.italic } else activeFormat.italic
    val allUnderline = if (hasSelection) isFormatEnabled(block, selection) { it.underline } else activeFormat.underline
    val allStrikethrough = if (hasSelection) isFormatEnabled(block, selection) { it.strikethrough } else activeFormat.strikethrough
    var showColorPalette by rememberSaveable(block.id) { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .haloBlur(state = hazeState) {
                style = HaloBlurStyle(
                    backgroundColor = backgroundColor.copy(alpha = 0.84f),
                    tint = HaloBlurTint(backgroundColor.copy(alpha = 0.88f)),
                    noiseFactor = 0f
                )
                progressive = HazeProgressive.verticalGradient(
                    startIntensity = 0.35f,
                    endIntensity = 0f
                )
            }
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.52f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(14.dp)
            )
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RichTextIconChip(
            selected = block.textAlignment == RichTextAlignment.START,
            onClick = { onUpdateTextAlignment(RichTextAlignment.START) },
            icon = painterResource(R.drawable.ic_format_align_left),
            contentDescription = "왼쪽 정렬"
        )
        RichTextIconChip(
            selected = block.textAlignment == RichTextAlignment.CENTER,
            onClick = { onUpdateTextAlignment(RichTextAlignment.CENTER) },
            icon = painterResource(R.drawable.ic_format_align_center),
            contentDescription = "가운데 정렬"
        )
        RichTextIconChip(
            selected = block.textAlignment == RichTextAlignment.END,
            onClick = { onUpdateTextAlignment(RichTextAlignment.END) },
            icon = painterResource(R.drawable.ic_format_align_right),
            contentDescription = "오른쪽 정렬"
        )
        VerticalDividerChip()
        RichTextIconChip(
            selected = allBold,
            onClick = { onToggleBold(selection) },
            icon = painterResource(R.drawable.ic_format_bold),
            contentDescription = "굵게"
        )
        RichTextIconChip(
            selected = allItalic,
            onClick = { onToggleItalic(selection) },
            icon = painterResource(R.drawable.ic_format_italic),
            contentDescription = "기울임"
        )
        RichTextIconChip(
            selected = allUnderline,
            onClick = { onToggleUnderline(selection) },
            icon = painterResource(R.drawable.ic_format_underlined),
            contentDescription = "밑줄"
        )
        RichTextIconChip(
            selected = allStrikethrough,
            onClick = { onToggleStrikethrough(selection) },
            icon = painterResource(R.drawable.ic_strikethrough_s),
            contentDescription = "취소선"
        )
        VerticalDividerChip()
        CompactTextStyleSelector(
            currentStyle = block.textStyle,
            onSelect = onUpdateTextStyle
        )
        VerticalDividerChip()
        RichTextIconChip(
            selected = showColorPalette || selectedColor != null,
            onClick = { showColorPalette = !showColorPalette },
            iconContent = { tint ->
                Text(
                    text = "A",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tint,
                    fontWeight = FontWeight.Bold
                )
            },
            tintOverride = richTextColorOrNull(selectedColor)
        )
        if (showColorPalette) {
            RichTextColorChip(
                colorHex = null,
                selected = selectedColor == null,
                onClick = { onSetTextColor(null) }
            )
            listOf("#111111", "#D14343", "#1D4ED8", "#15803D", "#C2410C", "#7C3AED").forEach { colorHex ->
                RichTextColorChip(
                    colorHex = colorHex,
                    selected = selectedColor == colorHex,
                    onClick = { onSetTextColor(colorHex) }
                )
            }
        }
        VerticalDividerChip()
        RichTextIconChip(
            selected = block.textStyle == RichTextBlockStyle.QUOTE,
            onClick = { onUpdateTextStyle(RichTextBlockStyle.QUOTE) },
            icon = painterResource(R.drawable.ic_format_quote),
            contentDescription = "인용"
        )
        RichTextIconChip(
            selected = block.textStyle == RichTextBlockStyle.BULLET,
            onClick = { onUpdateTextStyle(RichTextBlockStyle.BULLET) },
            icon = painterResource(R.drawable.ic_format_list_bulleted),
            contentDescription = "글머리표 목록"
        )
        RichTextIconChip(
            selected = block.textStyle == RichTextBlockStyle.NUMBERED,
            onClick = { onUpdateTextStyle(RichTextBlockStyle.NUMBERED) },
            icon = painterResource(R.drawable.ic_format_list_numbered),
            contentDescription = "번호 목록"
        )
    }
}

@Composable
private fun VerticalDividerChip() {
    Box(
        modifier = Modifier
            .height(20.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}

@Composable
private fun FormatToggleChip(
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable (Color) -> Unit
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant
    val chipShape = RoundedCornerShape(6.dp)

    Box(
        modifier = Modifier
            .clip(chipShape)
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = chipShape
            )
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        content(contentColor)
    }
}

@Composable
private fun RichTextIconChip(
    selected: Boolean,
    onClick: () -> Unit,
    icon: Painter? = null,
    contentDescription: String? = null,
    enabled: Boolean = true,
    tintOverride: Color? = null,
    iconContent: (@Composable (Color) -> Unit)? = null
) {
    FormatToggleChip(selected = selected, enabled = enabled, onClick = onClick) { contentColor ->
        iconContent?.invoke(tintOverride ?: contentColor) ?: Icon(
            painter = requireNotNull(icon),
            contentDescription = contentDescription,
            tint = tintOverride ?: contentColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CompactTextStyleSelector(
    currentStyle: RichTextBlockStyle,
    onSelect: (RichTextBlockStyle) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactTextStyleButton(
            label = "P",
            selected = currentStyle == RichTextBlockStyle.PARAGRAPH,
            onClick = { onSelect(RichTextBlockStyle.PARAGRAPH) }
        )
        CompactTextStyleButton(
            label = "H1",
            selected = currentStyle == RichTextBlockStyle.HEADING1,
            onClick = { onSelect(RichTextBlockStyle.HEADING1) }
        )
        CompactTextStyleButton(
            label = "H2",
            selected = currentStyle == RichTextBlockStyle.HEADING2,
            onClick = { onSelect(RichTextBlockStyle.HEADING2) }
        )
    }
}

@Composable
private fun CompactTextStyleButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun RichTextColorChip(
    colorHex: String?,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    FormatToggleChip(selected = selected, enabled = enabled, onClick = onClick) { contentColor ->
        if (colorHex == null) {
            Text(
                text = "기본",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(richTextColorOrNull(colorHex) ?: MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

private fun isFormatEnabled(
    block: EditorBlock,
    selection: TextRange,
    predicate: (CharFormat) -> Boolean
): Boolean {
    return (selection.start until selection.end).all { index ->
        index < block.formats.size && predicate(block.formats[index])
    }
}

private fun activeInlineFormat(
    block: EditorBlock,
    selection: TextRange,
    typingFormat: CharFormat?
): CharFormat {
    if (selection.start == selection.end) {
        return typingFormat ?: currentInlineFormatAt(block, selection.start)
    }

    val colors = (selection.start until selection.end)
        .map { index -> block.formats.getOrElse(index) { CharFormat() }.textColor }
        .distinct()
    val color = colors.singleOrNull()

    return CharFormat(
        bold = isFormatEnabled(block, selection) { it.bold },
        italic = isFormatEnabled(block, selection) { it.italic },
        underline = isFormatEnabled(block, selection) { it.underline },
        strikethrough = isFormatEnabled(block, selection) { it.strikethrough },
        textColor = color
    )
}

private fun currentInlineFormatAt(block: EditorBlock, cursorPosition: Int): CharFormat {
    if (block.formats.isEmpty()) return CharFormat()
    // 마지막 글자 뒤 커서는 앞 글자 서식을 물려받음
    // 그래야 문장 끝에서 이어 타이핑할 때 서식이 풀리지 않음
    val index = when {
        cursorPosition in block.formats.indices -> cursorPosition
        cursorPosition - 1 in block.formats.indices -> cursorPosition - 1
        else -> block.formats.lastIndex
    }
    return block.formats.getOrElse(index) { CharFormat() }
}
