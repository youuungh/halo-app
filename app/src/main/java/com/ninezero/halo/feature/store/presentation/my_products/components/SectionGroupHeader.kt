package com.ninezero.halo.feature.store.presentation.my_products.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun SectionGroupHeader(
    tagName: String,
    itemCount: Int,
    isCollapsed: Boolean,
    groupColor: Color?,
    onToggleCollapse: () -> Unit,
    onColorPickerClick: () -> Unit,
    showColorPicker: Boolean,
    onColorSelected: (Color?) -> Unit,
    onDismissColorPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isCollapsed) -90f else 0f,
        label = "chevron_rotation"
    )
    val barColor = groupColor?.let { barColorFor(it) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggleCollapse() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 좌측 색상 막대
        if (barColor != null) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(barColor)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tagName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "$itemCount",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .offset(y = 1.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                painter = painterResource(R.drawable.ic_chevron_down),
                contentDescription = if (isCollapsed) "펼치기" else "접기",
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotationAngle),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            Box {
                Icon(
                    painter = painterResource(R.drawable.ic_theme),
                    contentDescription = "그룹 색상 변경",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onColorPickerClick() },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (showColorPicker) {
                    ColorPickerPopup(
                        selectedColor = groupColor,
                        onColorSelected = onColorSelected,
                        onDismiss = onDismissColorPicker
                    )
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SectionGroupHeaderPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                SectionGroupHeader(
                    tagName = "이어폰",
                    itemCount = 12,
                    isCollapsed = false,
                    groupColor = GroupColors.first(),
                    onToggleCollapse = {},
                    onColorPickerClick = {},
                    showColorPicker = false,
                    onColorSelected = {},
                    onDismissColorPicker = {}
                )
                SectionGroupHeader(
                    tagName = "색상 미지정 그룹",
                    itemCount = 3,
                    isCollapsed = true,
                    groupColor = null,
                    onToggleCollapse = {},
                    onColorPickerClick = {},
                    showColorPicker = false,
                    onColorSelected = {},
                    onDismissColorPicker = {}
                )
            }
        }
    }
}
