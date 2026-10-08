package com.ninezero.halo.core.ui.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

/** 태그 입력 필드 */
@Composable
fun TagInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "태그 입력",
    buttonText: String = "추가",
    isSubtleButton: Boolean = false,
    maxLength: Int = 20,
    enabled: Boolean = true
) {
    val isAddEnabled = enabled && value.isNotBlank() && value.length <= maxLength

    // placeholder 줄 높이 정규화
    val inputTextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        ),
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.length <= maxLength) {
                    // 공백·# 제거
                    val filtered = newValue.replace(Regex("[\\s#]"), "")
                    onValueChange(filtered)
                }
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            enabled = enabled,
            textStyle = inputTextStyle,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { if (isAddEnabled) onAddClick() }
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = inputTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (value.isNotEmpty() && enabled) {
            Spacer(modifier = Modifier.width(8.dp))
            InputClearButton(onClick = { onValueChange("") })
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    when {
                        !isAddEnabled -> MaterialTheme.colorScheme.surfaceVariant
                        isSubtleButton -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.inverseSurface
                    }
                )
                .clickable(enabled = isAddEnabled, onClick = onAddClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = when {
                    !isAddEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    isSubtleButton -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.inverseOnSurface
                }
            )
        }

        Spacer(modifier = Modifier.width(4.dp))
    }
}

@HaloLightDarkPreview
@Composable
private fun TagInputFieldPreview() {
    HaloTheme {
        Surface {
            TagInputField(
                value = "일상",
                onValueChange = {},
                onAddClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun TagInputFieldEmptyPreview() {
    HaloTheme {
        Surface {
            TagInputField(
                value = "",
                onValueChange = {},
                onAddClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
