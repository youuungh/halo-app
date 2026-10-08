package com.ninezero.halo.core.ui.components.input

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

/** 인증 화면 입력 필드 */
@Composable
fun AuthTextField(
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
    singleLine: Boolean = true
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
                .padding(start = 16.dp, end = if (isPassword) 8.dp else 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
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
                    .padding(top = if (showSmallLabel) 8.dp else 0.dp)
            )

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(if (showSmallLabel) Alignment.BottomStart else Alignment.CenterStart)
                    .padding(
                        bottom = if (showSmallLabel) 8.dp else 0.dp,
                        end = if (isPassword) 48.dp else 0.dp
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

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun AuthTextFieldPreview() {
    HaloTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AuthTextField(
                value = "",
                onValueChange = {},
                label = "이메일"
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun AuthTextFieldWithValuePreview() {
    HaloTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AuthTextField(
                value = "test@example.com",
                onValueChange = {},
                label = "이메일"
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun AuthTextFieldErrorPreview() {
    HaloTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AuthTextField(
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
private fun AuthTextFieldPasswordPreview() {
    HaloTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AuthTextField(
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
