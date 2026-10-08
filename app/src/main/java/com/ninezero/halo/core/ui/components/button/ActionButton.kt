package com.ninezero.halo.core.ui.components.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

enum class ActionButtonStyle {
    Primary,
    Text
}

/** 기본 액션 버튼 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ActionButtonStyle = ActionButtonStyle.Primary,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    contentColor: Color = when (style) {
        ActionButtonStyle.Primary -> MaterialTheme.colorScheme.onPrimary
        ActionButtonStyle.Text -> MaterialTheme.colorScheme.primary
    }
) {
    val buttonModifier = modifier
        .fillMaxWidth()
        .height(48.dp)
        .then(if (enabled && !isLoading) Modifier.bounceClick() else Modifier)
    val isEnabled = enabled && !isLoading

    val content: @Composable () -> Unit = {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text)

            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    size = LoadingIndicatorSize.Small,
                    color = contentColor
                )
            }
        }
    }

    when (style) {
        ActionButtonStyle.Primary -> {
            Button(
                onClick = { if (!isLoading) onClick() },
                modifier = buttonModifier,
                enabled = isEnabled
            ) {
                content()
            }
        }
        ActionButtonStyle.Text -> {
            TextButton(
                onClick = { if (!isLoading) onClick() },
                modifier = buttonModifier,
                enabled = isEnabled,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor
                )
            ) {
                content()
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ActionButtonPreview() {
    ActionButtonPreviewContainer {
        Column(modifier = Modifier.padding(16.dp)) {
            ActionButton(
                text = "신청하기",
                onClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ActionButtonLoadingPreview() {
    ActionButtonPreviewContainer {
        Column(modifier = Modifier.padding(16.dp)) {
            ActionButton(
                text = "신청하기",
                onClick = {},
                isLoading = true
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ActionButtonDisabledPreview() {
    ActionButtonPreviewContainer {
        Column(modifier = Modifier.padding(16.dp)) {
            ActionButton(
                text = "신청하기",
                onClick = {},
                enabled = false
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun TextActionButtonPreview() {
    ActionButtonPreviewContainer {
        Column(modifier = Modifier.padding(16.dp)) {
            ActionButton(
                text = "신청하기",
                onClick = {},
                style = ActionButtonStyle.Primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            ActionButton(
                text = "신청 취소",
                onClick = {},
                style = ActionButtonStyle.Text,
                contentColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ActionButtonPreviewContainer(
    content: @Composable () -> Unit
) {
    HaloSurfacePreview {
        content()
    }
}
