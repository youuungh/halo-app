package com.ninezero.halo.feature.store.presentation.payment_methods

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.store.domain.model.BillingCard
import com.ninezero.halo.feature.store.domain.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel(), OrbitContainerHost<PaymentMethodsState, PaymentMethodsState, PaymentMethodsSideEffect> {

    override val container: OrbitContainer<PaymentMethodsState, PaymentMethodsState, PaymentMethodsSideEffect> =
        orbitContainer(PaymentMethodsState())

    init {
        load()
    }

    fun onIntent(intent: PaymentMethodsIntent) {
        when (intent) {
            is PaymentMethodsIntent.Load -> load()
            is PaymentMethodsIntent.Refresh -> refresh()
            is PaymentMethodsIntent.NavigateBack -> intent { postSideEffect(PaymentMethodsSideEffect.NavigateBack) }
            is PaymentMethodsIntent.AddCard -> addCard()
            is PaymentMethodsIntent.RegisterReturned -> registerReturned(intent.authKey)
            is PaymentMethodsIntent.RegisterDismissed -> intent {
                reduce {
                    state.copy(
                        showRegisterWebView = false
                    )
                }
            }

            is PaymentMethodsIntent.ShowDeleteDialog -> intent {
                reduce {
                    state.copy(
                        showDeleteDialog = true
                    )
                }
            }

            is PaymentMethodsIntent.DismissDeleteDialog -> intent {
                reduce {
                    state.copy(
                        showDeleteDialog = false
                    )
                }
            }

            is PaymentMethodsIntent.ConfirmDelete -> confirmDelete()
            is PaymentMethodsIntent.SnackbarDismissed -> intent { reduce { state.copy(snackbarState = null) } }
        }
    }

    private fun load() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        billingRepository.getMyBillingKey()
            .onSuccess { card -> reduce { state.copy(isLoading = false, billingCard = card) } }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "결제수단을 불러오지 못했습니다"
                    )
                }
            }
    }

    private fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }
        billingRepository.getMyBillingKey()
            .onSuccess { card ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        billingCard = card,
                        error = null
                    )
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        snackbarState = SnackbarState(e.message ?: "새로고침에 실패했습니다")
                    )
                }
            }
    }

    private fun addCard() = intent {
        reduce { state.copy() }
        val customerKey = billingRepository.getCustomerKey().getOrElse { e ->
            reduce { state.copy(snackbarState = SnackbarState(e.message ?: "결제 준비에 실패했습니다")) }
            return@intent
        }
        reduce {
            state.copy(
                registerWebViewUrl = Constants.billingAuthUrl(customerKey),
                showRegisterWebView = true
            )
        }
    }

    private fun registerReturned(authKey: String) = intent {
        reduce { state.copy(showRegisterWebView = false) }
        billingRepository.issueBillingKey(authKey)
            .onSuccess { card ->
                reduce {
                    state.copy(billingCard = card, snackbarState = SnackbarState("결제수단이 등록되었습니다"))
                }
            }
            .onFailure { e ->
                reduce { state.copy(snackbarState = SnackbarState(e.message ?: "카드 등록에 실패했습니다")) }
            }
    }

    private fun confirmDelete() = intent {
        reduce { state.copy(showDeleteDialog = false) }
        billingRepository.deleteBillingKey()
            .onSuccess {
                reduce {
                    state.copy(
                        billingCard = BillingCard(registered = false),
                        snackbarState = SnackbarState("결제수단이 삭제되었습니다")
                    )
                }
            }
            .onFailure { e ->
                reduce { state.copy(snackbarState = SnackbarState(e.message ?: "삭제에 실패했습니다")) }
            }
    }
}
