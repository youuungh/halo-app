package com.ninezero.halo.core.ui.components.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.time.YearMonth

/** 월 선택 시트 */
@Composable
fun MonthPickerBottomSheet(
    availableMonths: List<YearMonth>,
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    FloatingBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        showDragHandle = false  // 헤더 안에 있음
    ) {
        MonthPickerContent(
            availableMonths = availableMonths,
            selectedMonth = selectedMonth,
            onMonthSelected = onMonthSelected,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun MonthPickerContent(
    availableMonths: List<YearMonth>,
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val hazeState = remember { HazeState() }
    val backgroundColor = MaterialTheme.colorScheme.surface
    var headerHeight by remember { mutableStateOf(0.dp) }

    Box(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)  // 4줄 반만 보여 아래 줄이 잘려 스크롤되는 게 보임
                .hazeSource(hazeState)
                .blockSheetFling(),
            contentPadding = PaddingValues(
                top = headerHeight + 8.dp,
                bottom = 16.dp
            ),
            overscrollEffect = null
        ) {
            items(
                items = availableMonths,
                key = { it.toString() }
            ) { month ->
                SheetOptionItem(
                    text = "${month.year}년 ${month.monthValue}월",
                    isSelected = month == selectedMonth,
                    onClick = { onMonthSelected(month) }
                )
                if (month != availableMonths.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        SheetHeader(
            title = "조회 기간",
            onClose = onDismiss,
            hazeState = hazeState,
            onHeightChange = { headerHeight = it }
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MonthPickerContentPreview() {
    val months = buildList {
        var current = YearMonth.of(2026, 2)
        val start = YearMonth.of(2025, 9)
        while (!current.isBefore(start)) {
            add(current)
            current = current.minusMonths(1)
        }
    }

    HaloTheme {
        Surface {
            MonthPickerContent(
                availableMonths = months,
                selectedMonth = YearMonth.of(2026, 2),
                onMonthSelected = {},
                onDismiss = {}
            )
        }
    }
}
