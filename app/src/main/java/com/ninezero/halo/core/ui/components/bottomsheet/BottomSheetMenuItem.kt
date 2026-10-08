package com.ninezero.halo.core.ui.components.bottomsheet

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

private val MenuItemShape = RoundedCornerShape(20.dp)

data class BottomSheetMenuItemData(
    val text: String,
    @param:DrawableRes val iconRes: Int,
    val onClick: () -> Unit,
    val textColor: Color? = null,
    val iconTint: Color? = null
)

/** 시트 메뉴 그룹 */
@Composable
fun BottomSheetMenuItemGroup(
    items: List<BottomSheetMenuItemData>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MenuItemShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            items.forEachIndexed { index, item ->
                MenuItemContent(
                    text = item.text,
                    iconRes = item.iconRes,
                    onClick = item.onClick,
                    textColor = item.textColor ?: MaterialTheme.colorScheme.onSurface,
                    iconTint = item.iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/** 시트 메뉴 항목 */
@Composable
fun BottomSheetMenuItem(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MenuItemShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        MenuItemContent(
            text = text,
            iconRes = iconRes,
            onClick = onClick,
            textColor = textColor,
            iconTint = iconTint
        )
    }
}

@Composable
private fun MenuItemContent(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    textColor: Color,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .bounceClick(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(iconRes),
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = iconTint
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun BottomSheetMenuItemPreview() {
    BottomSheetMenuItemPreviewContainer {
        BottomSheetMenuItem(
            text = "수정하기",
            iconRes = R.drawable.ic_edit,
            onClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun BottomSheetMenuItemGroupPreview() {
    BottomSheetMenuItemPreviewContainer {
        BottomSheetMenuItemGroup(
            items = listOf(
                BottomSheetMenuItemData(
                    text = "수정하기",
                    iconRes = R.drawable.ic_edit,
                    onClick = {}
                ),
                BottomSheetMenuItemData(
                    text = "삭제하기",
                    iconRes = R.drawable.ic_delete,
                    onClick = {}
                )
            )
        )
    }
}

@Composable
private fun BottomSheetMenuItemPreviewContainer(
    content: @Composable () -> Unit
) {
    HaloSurfacePreview {
        content()
    }
}
