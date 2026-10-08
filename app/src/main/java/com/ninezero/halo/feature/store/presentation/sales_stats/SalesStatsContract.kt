package com.ninezero.halo.feature.store.presentation.sales_stats

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.CreatorMonthlySales
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class SalesStatsState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val monthly: CreatorMonthlySales? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val registrationMonth: YearMonth = YearMonth.now(),
    val showMonthPicker: Boolean = false,
    val error: String? = null,
    val snackbarState: SnackbarState? = null
) {
    val selectedMonthDisplay: String
        get() = "${selectedMonth.year}년 ${selectedMonth.monthValue}월"

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

sealed interface SalesStatsIntent {
    data object LoadData : SalesStatsIntent
    data object RefreshData : SalesStatsIntent
    data object GoToPreviousMonth : SalesStatsIntent
    data object GoToNextMonth : SalesStatsIntent
    data class SelectMonth(val month: YearMonth) : SalesStatsIntent
    data object ShowMonthPicker : SalesStatsIntent
    data object HideMonthPicker : SalesStatsIntent
    data object NavigateBack : SalesStatsIntent
    data object SnackbarDismissed : SalesStatsIntent
}

sealed interface SalesStatsSideEffect {
    data object NavigateBack : SalesStatsSideEffect
}
