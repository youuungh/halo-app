package com.ninezero.halo.core.ui.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.bringIntoViewOnFocus
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

/** 폼 입력 필드 */
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onNext: (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLength: Int = 100,
    prefix: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    showCounter: Boolean = false,
    numberFormat: Boolean = false,
    visualTransformation: VisualTransformation? = null,  // numberFormat보다 우선
    bringIntoViewReserve: Dp = 0.dp,  // 내용 위에 떠 있는 하단 바 높이
    showClearButton: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val fieldTextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = if (enabled) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        ),
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    Column(modifier = modifier.bringIntoViewOnFocus(isFocused, bringIntoViewReserve)) {
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= maxLength) onValueChange(it) },
            visualTransformation = visualTransformation ?: if (numberFormat) CommaVisualTransformation else VisualTransformation.None,
            modifier = Modifier
                .fillMaxWidth()
                .inputBorder(RoundedCornerShape(10.dp), isFocused = isFocused, isError = isError)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            textStyle = fieldTextStyle,
            singleLine = singleLine,
            minLines = minLines,
            enabled = enabled,
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext?.invoke() }
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // ₩ · %는 글자 높이가 달라 가운데가 아닌 기준선으로 맞춤
                    if (prefix != null) {
                        Text(
                            text = prefix,
                            style = fieldTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.alignByBaseline()
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(if (prefix != null) Modifier.alignByBaseline() else Modifier)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = fieldTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                    // 입력 지우기, 단일 라인만
                    if (showClearButton && singleLine && enabled && value.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        InputClearButton(onClick = { onValueChange("") })
                    }
                }
            }
        )

        if ((isError && errorMessage != null) || showCounter) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (isError && errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }

                if (showCounter) {
                    Text(
                        text = "${value.length}/${maxLength}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (value.length >= maxLength) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun FormTextFieldPreview() {
    HaloSurfacePreview {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FormTextField(value = "", onValueChange = {}, placeholder = "상품 이름을 입력하세요")
            FormTextField(
                value = "89000",
                onValueChange = {},
                placeholder = "판매가",
                keyboardType = KeyboardType.Number,
                numberFormat = true
            )
            FormTextField(value = "", onValueChange = {}, placeholder = "재고", isError = true, errorMessage = "재고를 입력하세요")
        }
    }
}
