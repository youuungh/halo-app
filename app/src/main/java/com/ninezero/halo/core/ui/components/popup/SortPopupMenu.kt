package com.ninezero.halo.core.ui.components.popup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState

/** 정렬 기준 글자 + 펼침 메뉴 */
@Composable
fun <T> SortPopupMenu(
    hazeState: HazeState?,
    options: List<T>,
    currentSort: T,
    label: (T) -> String,
    onSortChange: (T) -> Unit,
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
                text = label(currentSort),
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
            HaloPopupMenu(
                hazeState = hazeState,
                onDismiss = { expanded = false },
                offset = IntOffset(-12, 80)
            ) {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSortChange(option)
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label(option),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (option == currentSort) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (option == currentSort) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (index < options.lastIndex) {
                        HorizontalDivider(
                            thickness = 0.25.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SortPopupMenuPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SortPopupMenu(
                hazeState = null,
                options = listOf("인기순", "최신순"),
                currentSort = "인기순",
                label = { it },
                onSortChange = {}
            )
        }
    }
}
