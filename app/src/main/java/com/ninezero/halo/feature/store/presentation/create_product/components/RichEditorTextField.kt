@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.ninezero.halo.feature.store.presentation.create_product.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.feature.store.domain.model.RichTextBlockStyle
import com.ninezero.halo.feature.store.presentation.create_product.CharFormat
import com.ninezero.halo.feature.store.presentation.create_product.EditorBlock

@Composable
fun RichEditorTextField(
    block: EditorBlock,
    isFocused: Boolean,
    onTextChange: (String, Int) -> Unit,
    onSelectionChange: (TextRange) -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier,
    selectionOverride: TextRange? = null,
    showPlaceholder: Boolean = true,
    pendingFocus: Boolean = false,
    onRequestMergeBackward: () -> Unit = {},
    onSplitListItem: (Int) -> Unit = {},
    onExitQuote: (Int) -> Unit = {},
    listNumber: Int? = null,
    bottomReserve: Dp = 0.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val editorTextStyle = richEditorTextStyle(block.textStyle).copy(
        textAlign = richTextAlign(block.textAlignment),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        ),
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
    // 빈 블록도 한 줄 높이
    val annotatedString = remember(block.text, block.formats) {
        buildAnnotatedString {
            block.text.forEachIndexed { index, char ->
                val fmt = block.formats.getOrElse(index) { CharFormat() }
                withStyle(
                    SpanStyle(
                        fontWeight = if (fmt.bold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (fmt.italic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = richTextDecoration(fmt.underline, fmt.strikethrough),
                        color = richTextColorOrNull(fmt.textColor) ?: Color.Unspecified
                    )
                ) {
                    append(char)
                }
            }
        }
    }

    var selection by remember(block.id) { mutableStateOf(TextRange(block.text.length)) }
    val currentSelection = selectionOverride ?: selection

    val safeSelection = TextRange(
        currentSelection.start.coerceIn(0, block.text.length),
        currentSelection.end.coerceIn(0, block.text.length)
    )

    val textFieldValue = TextFieldValue(
        annotatedString = annotatedString,
        selection = safeSelection
    )

    val density = LocalDensity.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }
    var textLayoutResult by remember(block.id) { mutableStateOf<TextLayoutResult?>(null) }
    // 예약 높이는 state로만 읽어 effect 재실행 방지
    val reservePxState = rememberUpdatedState(with(density) { bottomReserve.toPx() })
    val imeBottom = WindowInsets.ime.getBottom(density)
    // 분리·병합 뒤 포커스 이동
    LaunchedEffect(pendingFocus) {
        if (pendingFocus) {
            runCatching { focusRequester.requestFocus() }
            onFocus()
        }
    }

    LaunchedEffect(isFocused, block.text, safeSelection.start, imeBottom) {
        if (!isFocused) return@LaunchedEffect
        // 텍스트 변경 직후엔 한 프레임 뒤 커서 사용
        withFrameNanos {}
        val cursorRect = textLayoutResult?.let { layout ->
            val offset = safeSelection.start.coerceIn(0, layout.layoutInput.text.length)
            runCatching { layout.getCursorRect(offset) }.getOrNull()
        } ?: return@LaunchedEffect
        // 데코 블록은 top 패딩만큼 커서 보정
        val topPad = with(density) {
            when (block.textStyle) {
                RichTextBlockStyle.QUOTE -> 6.dp
                RichTextBlockStyle.BULLET, RichTextBlockStyle.NUMBERED -> 2.dp
                else -> 0.dp
            }.toPx()
        }
        val r = cursorRect.translate(0f, topPad)
        bringIntoViewRequester.bringIntoView(Rect(r.left, r.top, r.right, r.bottom + reservePxState.value))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val isListBlock = block.textStyle == RichTextBlockStyle.BULLET ||
                    block.textStyle == RichTextBlockStyle.NUMBERED
                val isQuoteBlock = block.textStyle == RichTextBlockStyle.QUOTE
                val cursor = newValue.selection.start
                val isNewlineInsert = newValue.text.length == block.text.length + 1 &&
                    cursor in 1..newValue.text.length &&
                    newValue.text[cursor - 1] == '\n'
                // 빈 줄 엔터 = 더블 엔터
                val isEmptyLineEnter = isNewlineInsert &&
                    (cursor - 1 == 0 || newValue.text.getOrNull(cursor - 2) == '\n')
                when {
                    // 리스트: 엔터마다 새 항목 블록
                    isListBlock && isNewlineInsert -> onSplitListItem(cursor)
                    // 인용은 더블 엔터로 빠져나감
                    isQuoteBlock && isEmptyLineEnter -> onExitQuote(cursor)
                    else -> {
                        selection = newValue.selection
                        onSelectionChange(newValue.selection)
                        if (newValue.text != block.text) {
                            onTextChange(newValue.text, newValue.selection.start)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused && !isFocused) onFocus() }
                .onPreviewKeyEvent { event ->
                    // 맨 앞 백스페이스는 앞 블록과 병합
                    if (event.type == KeyEventType.KeyDown &&
                        event.key == Key.Backspace &&
                        safeSelection.collapsed &&
                        safeSelection.start == 0
                    ) {
                        onRequestMergeBackward()
                        true
                    } else {
                        false
                    }
                },
            textStyle = editorTextStyle.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            interactionSource = interactionSource,
            onTextLayout = { textLayoutResult = it },
            decorationBox = { innerTextField ->
                RichEditorDecoration(
                    block = block,
                    showPlaceholder = showPlaceholder,
                    listNumber = listNumber,
                    placeholderStyle = editorTextStyle.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    ),
                    contentStyle = editorTextStyle,
                    innerTextField = innerTextField
                )
            }
        )
    }
}

@Composable
private fun RichEditorDecoration(
    block: EditorBlock,
    showPlaceholder: Boolean,
    listNumber: Int?,
    placeholderStyle: TextStyle,
    contentStyle: TextStyle,
    innerTextField: @Composable () -> Unit
) {
    val placeholder: @Composable () -> Unit = {
        if (showPlaceholder && block.text.isEmpty()) {
            Text(
                text = "상세 내용을 입력하세요",
                style = placeholderStyle
            )
        }
    }

    when (block.textStyle) {
        RichTextBlockStyle.QUOTE -> {
            val quoteBarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 6.dp)
                    // 인용 막대는 전체 높이 drawBehind
                    .drawBehind {
                        drawRect(
                            color = quoteBarColor,
                            topLeft = Offset.Zero,
                            size = Size(3.dp.toPx(), size.height)
                        )
                    }
                    .padding(start = 13.dp),
                propagateMinConstraints = true
            ) {
                placeholder()
                innerTextField()
            }
        }
        RichTextBlockStyle.BULLET,
        RichTextBlockStyle.NUMBERED -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = if (block.textStyle == RichTextBlockStyle.BULLET) "\u2022" else "${listNumber ?: 1}.",
                    style = contentStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Box(modifier = Modifier.weight(1f), propagateMinConstraints = true) {
                    placeholder()
                    innerTextField()
                }
            }
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                // 전체 너비 강제, 정렬 기준 통일
                propagateMinConstraints = true
            ) {
                placeholder()
                innerTextField()
            }
        }
    }
}
