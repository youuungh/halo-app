package com.ninezero.halo.core.ui.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

enum class SheetActionButtonStyle {
    Filled,
    Outlined
}

/** 시트 CTA 버튼 */
@Composable
fun SheetActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SheetActionButtonStyle = SheetActionButtonStyle.Filled,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(12.dp)
    val buttonModifier = modifier
        .fillMaxWidth()
        .height(48.dp)
        .then(if (enabled && !isLoading) Modifier.bounceClick() else Modifier)

    val content: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center)
            )

            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    size = LoadingIndicatorSize.Small,
                    color = when (style) {
                        SheetActionButtonStyle.Filled -> MaterialTheme.colorScheme.onPrimary
                        SheetActionButtonStyle.Outlined -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }

    when (style) {
        SheetActionButtonStyle.Filled -> {
            Button(
                onClick = { if (!isLoading) onClick() },
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape
            ) {
                content()
            }
        }
        SheetActionButtonStyle.Outlined -> {
            OutlinedButton(
                onClick = { if (!isLoading) onClick() },
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                content()
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SheetActionButtonPreview() {
    HaloSurfacePreview {
        Column(modifier = Modifier.padding(16.dp)) {
            SheetActionButton(
                text = "보내기",
                onClick = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                SheetActionButton(
                    text = "장바구니",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    style = SheetActionButtonStyle.Outlined
                )
                Spacer(modifier = Modifier.width(8.dp))
                SheetActionButton(
                    text = "구매하기",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SheetActionButton(
                text = "보내기",
                onClick = {},
                isLoading = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            SheetActionButton(
                text = "사용하기",
                onClick = {},
                enabled = false
            )
        }
    }
}
