package com.ninezero.halo.core.ui.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

/** 주 버튼 모양, 채움 · 테두리 · 옅은 채움 */
enum class PrimaryButtonStyle {
    Filled,
    Outlined,
    Tonal
}

/** 화면 · 시트의 주 버튼, 높이 48 알약 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PrimaryButtonStyle = PrimaryButtonStyle.Filled,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = CircleShape
) {
    val buttonModifier = modifier
        .fillMaxWidth()
        .height(48.dp)
        .then(if (enabled && !isLoading) Modifier.bounceClick() else Modifier)

    val content: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 글자는 labelLarge · SemiBold
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )

            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    size = LoadingIndicatorSize.Small,
                    color = when (style) {
                        PrimaryButtonStyle.Filled -> MaterialTheme.colorScheme.onPrimary
                        PrimaryButtonStyle.Outlined -> MaterialTheme.colorScheme.primary
                        PrimaryButtonStyle.Tonal -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }

    when (style) {
        PrimaryButtonStyle.Filled -> {
            Button(
                onClick = { if (!isLoading) onClick() },
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                colors = filledColors()
            ) {
                content()
            }
        }
        PrimaryButtonStyle.Outlined -> {
            OutlinedButton(
                onClick = { if (!isLoading) onClick() },
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
            ) {
                content()
            }
        }
        PrimaryButtonStyle.Tonal -> {
            Button(
                onClick = { if (!isLoading) onClick() },
                modifier = buttonModifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = null
            ) {
                content()
            }
        }
    }
}

/** 금액 카운터 · 로딩처럼 글자 대신 다른 내용을 넣는 주 버튼 */
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .then(if (enabled) Modifier.bounceClick() else Modifier),
        enabled = enabled,
        shape = shape,
        colors = filledColors(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        content = content
    )
}

// 비활성은 앱 전체에서 회색 바탕 · 회색 글자
@Composable
private fun filledColors() = ButtonDefaults.buttonColors(
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
)

@HaloLightDarkPreview
@Composable
private fun PrimaryButtonPreview() {
    HaloSurfacePreview {
        Column(modifier = Modifier.padding(16.dp)) {
            PrimaryButton(
                text = "보내기",
                onClick = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                PrimaryButton(
                    text = "장바구니",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    style = PrimaryButtonStyle.Outlined
                )
                Spacer(modifier = Modifier.width(8.dp))
                PrimaryButton(
                    text = "구매하기",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(
                text = "보내기",
                onClick = {},
                isLoading = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(
                text = "사용하기",
                onClick = {},
                enabled = false
            )
        }
    }
}
