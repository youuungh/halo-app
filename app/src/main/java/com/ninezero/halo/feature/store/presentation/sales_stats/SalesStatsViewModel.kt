package com.ninezero.halo.feature.store.presentation.sales_stats

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import com.ninezero.halo.feature.store.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class SalesStatsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel(), OrbitContainerHost<SalesStatsState, SalesStatsState, SalesStatsSideEffect> {

    override val container: OrbitContainer<SalesStatsState, SalesStatsState, SalesStatsSideEffect> =
        orbitContainer(SalesStatsState())

    init {
        loadRegistrationMonth()
        loadData()
    }

    fun onIntent(intent: SalesStatsIntent) {
        when (intent) {
            is SalesStatsIntent.LoadData -> loadData()
            is SalesStatsIntent.RefreshData -> refreshData()
            is SalesStatsIntent.GoToPreviousMonth -> goToPreviousMonth()
            is SalesStatsIntent.GoToNextMonth -> goToNextMonth()
            is SalesStatsIntent.SelectMonth -> selectMonth(intent.month)
            is SalesStatsIntent.ShowMonthPicker -> showMonthPicker()
            is SalesStatsIntent.HideMonthPicker -> hideMonthPicker()
            is SalesStatsIntent.NavigateBack -> navigateBack()
            is SalesStatsIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadRegistrationMonth() = intent {
        authRepository.getCurrentUser().onSuccess { user ->
            val registrationMonth = Instant.ofEpochMilli(user.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .let { YearMonth.from(it) }
            reduce { state.copy(registrationMonth = registrationMonth) }
        }
    }

    private fun loadData() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        val month = state.selectedMonth

        orderRepository.getCreatorMonthlySales(month.toString())
            .onSuccess { monthly ->
                if (state.selectedMonth != month) return@intent
                reduce { state.copy(isLoading = false, monthly = monthly) }
            }
            .onFailure { error ->
                if (state.selectedMonth != month) return@intent
                reduce {
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "매출 통계를 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun refreshData() = intent {
        reduce { state.copy(isRefreshing = true) }
        val month = state.selectedMonth

        orderRepository.getCreatorMonthlySales(month.toString())
            .onSuccess { monthly ->
                if (state.selectedMonth != month) {  // 그사이 달이 바뀜, 스피너만 해제
                    reduce { state.copy(isRefreshing = false) }
                    return@intent
                }
                reduce { state.copy(isRefreshing = false, monthly = monthly, error = null) }
            }
            .onFailure { error ->
                if (state.selectedMonth != month) {
                    reduce { state.copy(isRefreshing = false) }
                    return@intent
                }
                reduce {
                    state.copy(
                        isRefreshing = false,
                        snackbarState = SnackbarState(error.message ?: "새로고침 실패")
                    )
                }
            }
    }

    private fun goToPreviousMonth() = intent {
        if (!state.canGoPreviousMonth) return@intent
        changeMonth(state.selectedMonth.minusMonths(1))
    }

    private fun goToNextMonth() = intent {
        if (!state.canGoNextMonth) return@intent
        changeMonth(state.selectedMonth.plusMonths(1))
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
                monthly = null,
                error = null
            )
        }

        orderRepository.getCreatorMonthlySales(month.toString())
            .onSuccess { monthly ->
                if (state.selectedMonth != month) return@intent
                reduce { state.copy(isLoading = false, monthly = monthly) }
            }
            .onFailure { error ->
                if (state.selectedMonth != month) return@intent
                reduce {
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "매출 통계를 불러올 수 없습니다"
                    )
                }
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
        postSideEffect(SalesStatsSideEffect.NavigateBack)
    }
}
