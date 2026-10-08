package com.ninezero.halo.feature.store.presentation.review_list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.store.presentation.review_list.ReviewSortType
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun ReviewSortPopupMenu(
    currentSort: ReviewSortType,
    onSortChange: (ReviewSortType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentSort.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_down),
                contentDescription = "정렬 선택",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        if (expanded) {
            Popup(
                onDismissRequest = { expanded = false },
                offset = IntOffset(-12, 80),
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .widthIn(min = 130.dp)
                ) {
                    Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                        ReviewSortType.entries.forEachIndexed { index, sortType ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSortChange(sortType)
                                        expanded = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sortType.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (sortType == currentSort) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (sortType == currentSort) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_check),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            if (index < ReviewSortType.entries.size - 1) {
                                HorizontalDivider(
                                    thickness = 0.3.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ReviewSortPopupMenuRecentPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ReviewSortPopupMenu(
                currentSort = ReviewSortType.RECENT,
                onSortChange = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ReviewSortPopupMenuItemsPreview() {
    HaloTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .widthIn(min = 130.dp)
            ) {
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    ReviewSortType.entries.forEachIndexed { index, sortType ->
                        val isSelected = sortType == ReviewSortType.RECENT
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sortType.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        if (index < ReviewSortType.entries.size - 1) {
                            HorizontalDivider(
                                thickness = 0.3.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
