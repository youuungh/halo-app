package com.ninezero.halo.feature.creator.presentation.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun CreatorFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minHeight: Dp = 56.dp,
    maxLength: Int = Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
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
            isFocused -> MaterialTheme.colorScheme.primary
            showSmallLabel -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        },
        animationSpec = tween(150),
        label = "labelColor"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (singleLine) Modifier.height(56.dp)
                    else Modifier.height(minHeight)
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp)
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
                onValueChange = { if (it.length <= maxLength) onValueChange(it) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = singleLine,
                maxLines = maxLines,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(if (singleLine) Alignment.CenterStart else Alignment.TopStart)
                    .padding(
                        top = if (singleLine) 8.dp else 24.dp,
                        bottom = if (singleLine) 0.dp else 8.dp
                    ),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty() && !showSmallLabel) {
                            // label이 placeholder 역할
                        } else if (value.isEmpty() && showSmallLabel) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        if (maxLength < Int.MAX_VALUE) {
            Text(
                text = "${value.length}/$maxLength",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp, end = 4.dp)
            )
        }
    }
}
