package com.ninezero.halo.feature.store.presentation.my_products.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

val GroupColors = listOf(
    Color(0xFFEF5350),  // Red
    Color(0xFFEC407A),  // Pink
    Color(0xFFAB47BC),  // Purple
    Color(0xFF7E57C2),  // Deep Purple
    Color(0xFF5C6BC0),  // Indigo
    Color(0xFF42A5F5),  // Blue
    Color(0xFF29B6F6),  // Light Blue
    Color(0xFF26C6DA),  // Cyan
    Color(0xFF26A69A),  // Teal
    Color(0xFF66BB6A),  // Green
    Color(0xFF9CCC65),  // Light Green
    Color(0xFFFFCA28),  // Amber
    Color(0xFFFFA726),  // Orange
    Color(0xFF78909C)  // Blue Grey
)

// 막대/배경용 흐린 색상
val GroupBarColors = listOf(
    Color(0xFFE0A0A0),  // Soft Red
    Color(0xFFDEA0BD),  // Soft Pink
    Color(0xFFCBAADB),  // Soft Purple
    Color(0xFFB8AEDD),  // Soft Deep Purple
    Color(0xFFACB4DD),  // Soft Indigo
    Color(0xFFA4C8E8),  // Soft Blue
    Color(0xFF9DD4ED),  // Soft Light Blue
    Color(0xFF99D8DE),  // Soft Cyan
    Color(0xFF9DCCC6),  // Soft Teal
    Color(0xFFA8D5AB),  // Soft Green
    Color(0xFFC5DDA0),  // Soft Light Green
    Color(0xFFEBDDA0),  // Soft Amber
    Color(0xFFE8CCA0),  // Soft Orange
    Color(0xFFB0BEC5)  // Soft Blue Grey
)

fun barColorFor(color: Color): Color {
    val index = GroupColors.indexOf(color)
    return if (index >= 0) GroupBarColors[index] else color.copy(alpha = 0.4f)
}

@Composable
fun ColorPickerPopup(
    selectedColor: Color?,
    onColorSelected: (Color?) -> Unit,
    onDismiss: () -> Unit
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(top = 32.dp)
        ) {
            FlowRow(
                modifier = Modifier
                    .widthIn(max = 250.dp)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isNoneSelected = selectedColor == null
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(1.dp, outlineColor, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onColorSelected(null) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isNoneSelected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                GroupColors.forEach { color ->
                    val isSelected = selectedColor == color
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(color, CircleShape)
                            .then(
                                if (isSelected) Modifier.border(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    CircleShape
                                ) else Modifier
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onColorSelected(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ColorPickerPopupPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                ColorPickerPopup(
                    selectedColor = GroupColors.first(),
                    onColorSelected = {},
                    onDismiss = {}
                )
            }
        }
    }
}
