package com.ninezero.halo.core.ui.components.input

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bringIntoViewOnFocus
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme

/** 라벨이 칸 안에서 위로 올라가는 입력칸 */
@Composable
fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minHeight: Dp = 56.dp,
    leadingIcon: Painter? = null,
    maxLength: Int = 100,
    showCounter: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

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

    // 칸 아래 오류 · 글자 수까지 키보드 위로
    Column(modifier = modifier.fillMaxWidth().bringIntoViewOnFocus(isFocused)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (singleLine) 56.dp else minHeight)
                .inputBorder(MaterialTheme.shapes.medium, isFocused = isFocused, isError = isError)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(
                    start = if (leadingIcon != null) 12.dp else 16.dp,
                    end = if (isPassword) 8.dp else 16.dp
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
                                    bottom = if (showSmallLabel) 8.dp else 0.dp,
                                    end = if (isPassword) 48.dp else 0.dp
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
                enabled = enabled,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = if (showSmallLabel) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0f)
                    }
                ),
                singleLine = singleLine,
                maxLines = if (singleLine) 1 else maxLines,
                visualTransformation = if (isPassword && !isPasswordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                interactionSource = interactionSource,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )

            if (isPassword) {
                IconButton(
                    onClick = { onPasswordVisibilityToggle?.invoke() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off
                        ),
                        contentDescription = if (isPasswordVisible) {
                            "비밀번호 숨기기"
                        } else {
                            "비밀번호 보기"
                        },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (isError && errorMessage != null || showCounter) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = if (showCounter) 4.dp else 0.dp)
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
private fun LabeledTextFieldPreview() {
    HaloTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LabeledTextField(
                value = "",
                onValueChange = {},
                label = "이메일"
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun LabeledTextFieldWithValuePreview() {
    HaloTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LabeledTextField(
                value = "test@example.com",
                onValueChange = {},
                label = "이메일"
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun LabeledTextFieldErrorPreview() {
    HaloTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LabeledTextField(
                value = "invalid",
                onValueChange = {},
                label = "이메일",
                isError = true,
                errorMessage = "올바른 이메일 형식이 아닙니다"
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun LabeledTextFieldPasswordPreview() {
    HaloTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LabeledTextField(
                value = "password123",
                onValueChange = {},
                label = "비밀번호",
                isPassword = true,
                isPasswordVisible = false,
                onPasswordVisibilityToggle = {}
            )
        }
    }
}
