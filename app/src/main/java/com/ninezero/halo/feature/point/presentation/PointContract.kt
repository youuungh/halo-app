package com.ninezero.halo.feature.point.presentation

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.point.domain.model.PointBalance
import com.ninezero.halo.feature.point.domain.model.PointHistory
import com.ninezero.halo.feature.point.domain.model.PointType
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class PointState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val balance: PointBalance? = null,
    val histories: List<PointHistory> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
    // 필터
    val selectedType: PointType? = null,  // null = 전체
    // 월별 조회
    val selectedMonth: YearMonth = YearMonth.now(),
    val registrationMonth: YearMonth = YearMonth.now(),
    val showMonthPicker: Boolean = false,
    val error: String? = null,
    val snackbarState: SnackbarState? = null
) {
    val selectedMonthDisplay: String
        get() = "${selectedMonth.monthValue}월"

    val selectedMonthDateRange: String
        get() {
            val start = selectedMonth.atDay(1)
            val end = selectedMonth.atEndOfMonth()
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
            return "${start.format(formatter)} ~ ${end.format(formatter)}"
        }

    val canGoPreviousMonth: Boolean
        get() = selectedMonth.isAfter(registrationMonth)

    val canGoNextMonth: Boolean
        get() = selectedMonth.isBefore(YearMonth.now())

    val availableMonths: List<YearMonth>
        get() = generateSequence(YearMonth.now()) { it.minusMonths(1) }
            .takeWhile { !it.isBefore(registrationMonth) }
            .toList()
}

sealed interface PointIntent {
    data object RefreshData : PointIntent
    data object LoadMoreHistory : PointIntent
    data object NavigateBack : PointIntent
    // 필터
    data class FilterByType(val type: PointType?) : PointIntent
    // 월별 조회
    data object GoToPreviousMonth : PointIntent
    data object GoToNextMonth : PointIntent
    data class SelectMonth(val month: YearMonth) : PointIntent
    data object ShowMonthPicker : PointIntent
    data object HideMonthPicker : PointIntent
    data object SnackbarDismissed : PointIntent
}

sealed interface PointSideEffect {
    data object NavigateBack : PointSideEffect
}
