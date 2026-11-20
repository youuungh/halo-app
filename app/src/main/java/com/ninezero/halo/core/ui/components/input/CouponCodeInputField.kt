package com.ninezero.halo.core.ui.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

/** 쿠폰 코드 입력칸과 받기 버튼 */
@Composable
internal fun CouponCodeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onClaimClick: () -> Unit,
    isClaiming: Boolean,
    modifier: Modifier = Modifier
) {
    val isClaimEnabled = value.isNotBlank() && !isClaiming

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .inputBorder(MaterialTheme.shapes.small)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue.uppercase())
            },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            enabled = !isClaiming,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (isClaimEnabled) onClaimClick() }
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = "쿠폰 코드 입력",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (value.isNotEmpty() && !isClaiming) {
            InputClearButton(onClick = { onValueChange("") })
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isClaimEnabled) {
                        MaterialTheme.colorScheme.inverseSurface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .clickable(enabled = isClaimEnabled, onClick = onClaimClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isClaiming) {
                LoadingIndicator(
                    size = LoadingIndicatorSize.Small,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            } else {
                Text(
                    text = "등록",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isClaimEnabled) {
                        MaterialTheme.colorScheme.inverseOnSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponCodeInputFieldPreview() {
    HaloSurfacePreview {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CouponCodeInputField(value = "", onValueChange = {}, onClaimClick = {}, isClaiming = false)
            CouponCodeInputField(value = "WELCOME10", onValueChange = {}, onClaimClick = {}, isClaiming = false)
        }
    }
}
