package com.ninezero.halo.feature.profile.presentation.edit_profile.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun EditProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    maxLength: Int,
    modifier: Modifier = Modifier,
    showCounter: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minHeight: Dp = 56.dp,
    leadingIcon: Painter? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val interactionSource = remember { MutableInteractionSource() }
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
                .padding(
                    start = if (leadingIcon != null) 12.dp else 16.dp,
                    end = 16.dp
                )
        ) {
            if (leadingIcon != null) {
                Icon(
                    painter = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .align(if (singleLine) Alignment.CenterStart else Alignment.TopStart)
                        .then(
                            if (!singleLine) Modifier.padding(top = 16.dp)
                            else Modifier
                        ),
                    tint = if (isFocused) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val textStartPadding = if (leadingIcon != null) 28.dp else 0.dp

            Text(
                text = label,
                style = if (showSmallLabel) {
                    MaterialTheme.typography.labelSmall
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = labelColor,
                modifier = Modifier
                    .align(if (showSmallLabel) Alignment.TopStart else Alignment.CenterStart)
                    .padding(
                        start = textStartPadding,
                        top = if (showSmallLabel) 8.dp else 0.dp
                    )
            )

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (singleLine) {
                            Modifier
                                .align(if (showSmallLabel) Alignment.BottomStart else Alignment.CenterStart)
                                .padding(
                                    start = textStartPadding,
                                    bottom = if (showSmallLabel) 8.dp else 0.dp
                                )
                        } else {
                            Modifier
                                .align(Alignment.TopStart)
                                .padding(
                                    start = textStartPadding,
                                    top = if (showSmallLabel) 24.dp else 16.dp,
                                    bottom = 8.dp
                                )
                        }
                    ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = if (showSmallLabel) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0f)
                    }
                ),
                singleLine = singleLine,
                maxLines = if (singleLine) 1 else maxLines,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                interactionSource = interactionSource,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        }

        // 에러 메시지 & 카운터
        if (isError && errorMessage != null || showCounter) {
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
