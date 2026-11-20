package com.ninezero.halo.feature.store.presentation.create_product.components

import android.os.SystemClock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.feature.store.domain.model.RichTextAlignment
import com.ninezero.halo.feature.store.domain.model.RichTextBlockStyle
import com.ninezero.halo.feature.store.presentation.components.richTextColorOrNull
import com.ninezero.halo.feature.store.presentation.create_product.CharFormat
import com.ninezero.halo.feature.store.presentation.create_product.EditorBlock
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState

/** 서식 버튼 오른쪽 알약, 토글과 위 메뉴 */
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
    onUpdateTextAlignment: (RichTextAlignment) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasSelection = selection.start != selection.end
    val activeFormat = activeInlineFormat(block, selection, typingFormat)
    val selectedColor = activeFormat.textColor
    val allBold = if (hasSelection) isFormatEnabled(block, selection) { it.bold } else activeFormat.bold
    val allItalic = if (hasSelection) isFormatEnabled(block, selection) { it.italic } else activeFormat.italic
    val allUnderline = if (hasSelection) isFormatEnabled(block, selection) { it.underline } else activeFormat.underline
    val allStrikethrough = if (hasSelection) isFormatEnabled(block, selection) { it.strikethrough } else activeFormat.strikethrough
    var openMenu by remember(block.id) { mutableStateOf<RichTextMenu?>(null) }
    // 방금 닫힌 메뉴의 버튼이면 다시 열지 않음
    var lastDismissed by remember { mutableStateOf<Pair<RichTextMenu, Long>?>(null) }
    val toggleMenu: (RichTextMenu) -> Unit = { menu ->
        val justDismissed = lastDismissed?.let { (m, at) -> m == menu && SystemClock.uptimeMillis() - at < 300 } == true
        openMenu = if (openMenu == menu || justDismissed) null else menu
        lastDismissed = null
    }
    val dismissMenu: () -> Unit = {
        openMenu?.let { lastDismissed = it to SystemClock.uptimeMillis() }
        openMenu = null
    }

    Row(
        modifier = modifier
            .height(RichTextBarHeight)
            .clip(CircleShape)
            .haloBlur(
                hazeState,
                HaloBlurStyle(
                    backgroundColor = backgroundColor.copy(alpha = 0.84f),
                    tint = backgroundColor.copy(alpha = 0.88f),
                    noiseFactor = 0f,
                    progressive = HazeProgressive.verticalGradient(startIntensity = 0.35f, endIntensity = 0f)
                )
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.52f))
            .cardBorder(CircleShape)
            .horizontalScroll(rememberScrollState())
            // 둥근 끝에 아이콘이 잘리지 않게 양옆 여유
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 문단 스타일
        Box {
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable { toggleMenu(RichTextMenu.STYLE) }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = block.textStyle.menuLabel(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (openMenu == RichTextMenu.STYLE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_down_small),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (openMenu == RichTextMenu.STYLE) {
                RichTextMenuPopup(onDismiss = dismissMenu) {
                    RichTextBlockStyle.entries.forEach { style ->
                        StyleMenuItem(
                            style = style,
                            selected = block.textStyle == style,
                            onClick = {
                                onUpdateTextStyle(style)
                                openMenu = null
                            }
                        )
                    }
                }
            }
        }
        BarDivider()
        BarIcon(allBold, R.drawable.ic_format_bold, "굵게") { onToggleBold(selection) }
        BarIcon(allItalic, R.drawable.ic_format_italic, "기울임") { onToggleItalic(selection) }
        BarIcon(allUnderline, R.drawable.ic_format_underlined, "밑줄") { onToggleUnderline(selection) }
        BarIcon(allStrikethrough, R.drawable.ic_strikethrough_s, "취소선") { onToggleStrikethrough(selection) }
        BarDivider()
        // 글자색
        Box {
            BarButton(
                selected = openMenu == RichTextMenu.COLOR,
                onClick = { toggleMenu(RichTextMenu.COLOR) }
            ) { tint ->
                Text(
                    text = "A",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = richTextColorOrNull(selectedColor) ?: tint,
                    textDecoration = TextDecoration.Underline
                )
            }
            if (openMenu == RichTextMenu.COLOR) {
                RichTextMenuPopup(onDismiss = dismissMenu) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (listOf<String?>(null) + RichTextColors).forEach { hex ->
                            ColorDot(
                                colorHex = hex,
                                selected = selectedColor == hex,
                                onClick = {
                                    onSetTextColor(hex)
                                    openMenu = null
                                }
                            )
                        }
                    }
                }
            }
        }
        // 정렬
        Box {
            BarButton(
                selected = openMenu == RichTextMenu.ALIGN,
                onClick = { toggleMenu(RichTextMenu.ALIGN) }
            ) { tint ->
                Icon(
                    painter = painterResource(block.textAlignment.iconRes()),
                    contentDescription = "정렬",
                    modifier = Modifier.size(18.dp),
                    tint = tint
                )
            }
            if (openMenu == RichTextMenu.ALIGN) {
                RichTextMenuPopup(onDismiss = dismissMenu) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RichTextAlignment.entries.forEach { alignment ->
                            BarIcon(block.textAlignment == alignment, alignment.iconRes(), alignment.label()) {
                                onUpdateTextAlignment(alignment)
                                openMenu = null
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class RichTextMenu { STYLE, COLOR, ALIGN }

private val RichTextBarHeight = 32.dp  // 하단 바 버튼과 같아 펼쳐도 바 높이가 안 변함
private val RichTextColors = listOf("#111111", "#D14343", "#1D4ED8", "#15803D", "#C2410C", "#7C3AED")

private fun RichTextBlockStyle.menuLabel(): String = when (this) {
    RichTextBlockStyle.PARAGRAPH -> "기본"
    RichTextBlockStyle.HEADING1 -> "제목 1"
    RichTextBlockStyle.HEADING2 -> "제목 2"
    RichTextBlockStyle.QUOTE -> "인용"
    RichTextBlockStyle.BULLET -> "글머리표"
    RichTextBlockStyle.NUMBERED -> "번호"
}

private fun RichTextAlignment.iconRes(): Int = when (this) {
    RichTextAlignment.START -> R.drawable.ic_format_align_left
    RichTextAlignment.CENTER -> R.drawable.ic_format_align_center
    RichTextAlignment.END -> R.drawable.ic_format_align_right
}

private fun RichTextAlignment.label(): String = when (this) {
    RichTextAlignment.START -> "왼쪽 정렬"
    RichTextAlignment.CENTER -> "가운데 정렬"
    RichTextAlignment.END -> "오른쪽 정렬"
}

@Composable
private fun BarDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .height(16.dp)
            .width(0.5.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun BarButton(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable (Color) -> Unit
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(MaterialTheme.shapes.small)
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content(tint)
    }
}

@Composable
private fun BarIcon(selected: Boolean, iconRes: Int, description: String, onClick: () -> Unit) {
    BarButton(selected = selected, onClick = onClick) { tint ->
        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            modifier = Modifier.size(18.dp),
            tint = tint
        )
    }
}

/** 버튼 위 작은 메뉴, 포커스를 가져가지 않음 */
@Composable
private fun RichTextMenuPopup(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val gap = with(LocalDensity.current) { 8.dp.roundToPx() }
    val edge = with(LocalDensity.current) { 16.dp.roundToPx() }  // 화면 양옆 여백
    val positionProvider = remember(gap, edge) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val x = anchorBounds.left.coerceIn(edge, (windowSize.width - popupContentSize.width - edge).coerceAtLeast(edge))
                val y = (anchorBounds.top - popupContentSize.height - gap).coerceAtLeast(0)
                return IntOffset(x, y)
            }
        }
    }
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(4.dp)) { content() }
        }
    }
}

@Composable
private fun StyleMenuItem(
    style: RichTextBlockStyle,
    selected: Boolean,
    onClick: () -> Unit
) {
    val textStyle = when (style) {
        RichTextBlockStyle.HEADING1 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        RichTextBlockStyle.HEADING2 -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        else -> MaterialTheme.typography.bodyMedium
    }
    Row(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = style.menuLabel(),
            style = textStyle,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ColorDot(colorHex: String?, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .border(if (selected) 2.dp else 0.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (colorHex == null) {
            Text(
                text = "기본",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
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
