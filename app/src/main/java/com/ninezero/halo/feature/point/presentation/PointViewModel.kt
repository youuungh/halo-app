package com.ninezero.halo.feature.point.presentation

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import com.ninezero.halo.feature.point.domain.model.PointType
import com.ninezero.halo.feature.point.domain.repository.PointRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class PointViewModel @Inject constructor(
    private val pointRepository: PointRepository,
    private val authRepository: AuthRepository
) : ViewModel(), OrbitContainerHost<PointState, PointState, PointSideEffect> {

    override val container: OrbitContainer<PointState, PointState, PointSideEffect> =
        orbitContainer(PointState())

    init {
        loadRegistrationMonth()
        loadData()
    }

    fun onIntent(intent: PointIntent) {
        when (intent) {
            is PointIntent.RefreshData -> refreshData()
            is PointIntent.LoadMoreHistory -> loadMoreHistory()
            is PointIntent.NavigateBack -> navigateBack()
            is PointIntent.FilterByType -> filterByType(intent.type)
            is PointIntent.GoToPreviousMonth -> goToPreviousMonth()
            is PointIntent.GoToNextMonth -> goToNextMonth()
            is PointIntent.SelectMonth -> selectMonth(intent.month)
            is PointIntent.ShowMonthPicker -> showMonthPicker()
            is PointIntent.HideMonthPicker -> hideMonthPicker()
            is PointIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadRegistrationMonth() = intent {
        authRepository.getCurrentUser()
            .onSuccess { user ->
                val registrationMonth = Instant.ofEpochMilli(user.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .let { YearMonth.from(it) }
                reduce { state.copy(registrationMonth = registrationMonth) }
            }
    }

    private fun dateRangeOf(month: YearMonth): Pair<String, String> {
        val startDate = "${month.atDay(1)}T00:00:00"
        val endDate = "${month.atEndOfMonth()}T23:59:59"
        return startDate to endDate
    }

    private fun loadData() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        val month = state.selectedMonth
        val type = state.selectedType
        val (startDate, endDate) = dateRangeOf(month)

        val (balanceResult, historyResult) = coroutineScope {
            val balanceDeferred = async { pointRepository.getBalance() }
            val historyDeferred = async {
                pointRepository.getHistory(
                    type = type,
                    page = 1,
                    limit = 20,
                    startDate = startDate,
                    endDate = endDate
                )
            }
            balanceDeferred.await() to historyDeferred.await()
        }

        balanceResult
            .onSuccess { balance ->
                reduce { state.copy(balance = balance) }
            }
            .onFailure { error ->
                // 내역까지 실패하면 에러 화면만 보여줌
                if (historyResult.isSuccess) {
                    reduce { state.copy(snackbarState = SnackbarState(error.message ?: "포인트 정보를 불러올 수 없습니다")) }
                }
            }

        if (state.selectedMonth != month || state.selectedType != type) return@intent

        historyResult
            .onSuccess { result ->
                reduce {
                    state.copy(
                        isLoading = false,
                        histories = result.histories,
                        page = result.page,
                        hasMore = result.hasMore
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "포인트 내역을 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun refreshData() = intent {
        reduce { state.copy(isRefreshing = true) }

        val month = state.selectedMonth
        val type = state.selectedType
        val (startDate, endDate) = dateRangeOf(month)

        val (balanceResult, historyResult) = coroutineScope {
            val balanceDeferred = async { pointRepository.getBalance() }
            val historyDeferred = async {
                pointRepository.getHistory(
                    type = type,
                    page = 1,
                    limit = 20,
                    startDate = startDate,
                    endDate = endDate
                )
            }
            balanceDeferred.await() to historyDeferred.await()
        }

        balanceResult.onSuccess { balance ->
            reduce { state.copy(balance = balance) }
        }

        if (state.selectedMonth != month || state.selectedType != type) {  // 그사이 달·유형이 바뀜, 스피너만 해제
            reduce { state.copy(isRefreshing = false) }
            return@intent
        }

        historyResult
            .onSuccess { result ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        histories = result.histories,
                        page = result.page,
                        hasMore = result.hasMore,
                        error = null)
                }
            }
            .onFailure { error ->
                reduce { state.copy(isRefreshing = false, snackbarState = SnackbarState(error.message ?: "새로고침 실패")) }
            }
    }

    private fun loadMoreHistory() = intent {
        if (!state.hasMore || state.isLoading) return@intent

        reduce { state.copy(isLoading = true) }

        val nextPage = state.page + 1
        val month = state.selectedMonth
        val type = state.selectedType
        val (startDate, endDate) = dateRangeOf(month)

        pointRepository.getHistory(
            type = type,
            page = nextPage,
            limit = 20,
            startDate = startDate,
            endDate = endDate
        ).onSuccess { result ->
            if (state.selectedMonth != month || state.selectedType != type) return@intent
            reduce {
                state.copy(
                    isLoading = false,
                    histories = (state.histories + result.histories).distinctBy { it.id },
                    page = result.page,
                    hasMore = result.hasMore
                )
            }
        }.onFailure { error ->
            reduce { state.copy(isLoading = false, snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")) }
        }
    }

    private fun filterByType(type: PointType?) = intent {
        if (state.selectedType == type) return@intent

        reduce {
            state.copy(
                selectedType = type,
                isLoading = true,
                histories = emptyList(),
                page = 1,
                hasMore = true,
                error = null
            )
        }

        val month = state.selectedMonth
        val (startDate, endDate) = dateRangeOf(month)

        pointRepository.getHistory(
            type = type,
            page = 1,
            limit = 20,
            startDate = startDate,
            endDate = endDate
        ).onSuccess { result ->
            if (state.selectedType != type || state.selectedMonth != month) return@intent
            reduce {
                state.copy(
                    isLoading = false,
                    histories = result.histories,
                    page = result.page,
                    hasMore = result.hasMore
                )
            }
        }.onFailure { error ->
            if (state.selectedType != type || state.selectedMonth != month) return@intent
            reduce { state.copy(isLoading = false, error = error.message ?: "포인트 내역을 불러올 수 없습니다") }
        }
    }

    private fun goToPreviousMonth() = intent {
        if (!state.canGoPreviousMonth) return@intent
        val newMonth = state.selectedMonth.minusMonths(1)
        changeMonth(newMonth)
    }

    private fun goToNextMonth() = intent {
        if (!state.canGoNextMonth) return@intent
        val newMonth = state.selectedMonth.plusMonths(1)
        changeMonth(newMonth)
    }

    private fun selectMonth(month: YearMonth) = intent {
        reduce { state.copy(showMonthPicker = false) }
        if (state.selectedMonth == month) return@intent
        changeMonth(month)
    }

    private fun changeMonth(month: YearMonth) = intent {
        reduce {
            state.copy(
                selectedMonth = month,
                isLoading = true,
                histories = emptyList(),
                page = 1,
                hasMore = true,
                error = null
            )
        }

        val (startDate, endDate) = dateRangeOf(month)

        // 잔액은 월과 무관해 첫 로드·새로고침에서만 받음
        val type = state.selectedType
        val historyResult = pointRepository.getHistory(
            type = type,
            page = 1,
            limit = 20,
            startDate = startDate,
            endDate = endDate
        )
        if (state.selectedMonth != month || state.selectedType != type) return@intent

        historyResult
            .onSuccess { result ->
                reduce {
                    state.copy(
                        isLoading = false,
                        histories = result.histories,
                        page = result.page,
                        hasMore = result.hasMore)
                }
            }
            .onFailure { error ->
                reduce { state.copy(isLoading = false, error = error.message ?: "포인트 내역을 불러올 수 없습니다") }
            }
    }

    private fun showMonthPicker() = intent {
        reduce { state.copy(showMonthPicker = true) }
    }

    private fun hideMonthPicker() = intent {
        reduce { state.copy(showMonthPicker = false) }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    private fun navigateBack() = intent {
        postSideEffect(PointSideEffect.NavigateBack)
    }
}
