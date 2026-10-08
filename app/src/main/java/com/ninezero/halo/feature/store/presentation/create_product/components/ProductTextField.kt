package com.ninezero.halo.feature.store.presentation.create_product.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun ProductTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    errorAbove: Boolean = false,
    errorAboveTopPadding: Dp = 0.dp,
    errorAboveBottomPadding: Dp = 4.dp,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minHeight: Dp = 40.dp,
    hintCenterVertically: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val interactionSource = remember { MutableInteractionSource() }

    // outlined 필드
    if (outlined) {
        val isFocused by interactionSource.collectIsFocusedAsState()
        val borderColor by animateColorAsState(
            targetValue = when {
                isError -> MaterialTheme.colorScheme.error
                isFocused -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            },
            animationSpec = tween(150),
            label = "borderColor"
        )
        val hasContent = value.isNotEmpty()
        val showSmallLabel = isFocused || hasContent
        val labelColor by animateColorAsState(
            targetValue = when {
                isError -> MaterialTheme.colorScheme.error
                isFocused -> MaterialTheme.colorScheme.primary
                showSmallLabel -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            animationSpec = tween(150),
            label = "labelColor"
        )

        Column(modifier = modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = label,
                    style = if (showSmallLabel) MaterialTheme.typography.labelSmall
                    else MaterialTheme.typography.bodyLarge,
                    color = labelColor,
                    modifier = Modifier
                        .align(if (showSmallLabel) Alignment.TopStart else Alignment.CenterStart)
                        .padding(top = if (showSmallLabel) 8.dp else 0.dp)
                )

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(if (showSmallLabel) Alignment.BottomStart else Alignment.CenterStart)
                        .padding(bottom = if (showSmallLabel) 8.dp else 0.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = if (showSmallLabel) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0f)
                    ),
                    singleLine = singleLine,
                    maxLines = if (singleLine) 1 else maxLines,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource
                )
            }

            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        return
    }

    // border 없는 placeholder 박스 + 외부 divider
    val shouldCenterVertically = hintCenterVertically || singleLine
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = if (enabled) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        ),
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
    val hintStyle = textStyle.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (errorAbove && isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = errorAboveTopPadding, bottom = errorAboveBottomPadding)
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight),
            textStyle = textStyle,
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else maxLines,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (shouldCenterVertically) Alignment.CenterStart else Alignment.TopStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = label,
                            style = hintStyle
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (!errorAbove && isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductTextFieldPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProductTextField(value = "", onValueChange = {}, label = "상품명")
                ProductTextField(value = "프리미엄 무선 이어폰", onValueChange = {}, label = "상품명")
                ProductTextField(value = "89000", onValueChange = {}, label = "가격", outlined = true)
                ProductTextField(
                    value = "",
                    onValueChange = {},
                    label = "재고",
                    isError = true,
                    errorMessage = "재고를 입력해주세요"
                )
                ProductTextField(value = "수정 불가", onValueChange = {}, label = "비활성", enabled = false)
            }
        }
    }
}
