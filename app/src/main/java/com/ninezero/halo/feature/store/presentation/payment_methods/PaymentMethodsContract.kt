package com.ninezero.halo.feature.store.presentation.payment_methods

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.store.domain.model.BillingCard

data class PaymentMethodsState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val billingCard: BillingCard? = null,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    // 카드 등록 WebView
    val showRegisterWebView: Boolean = false,
    val registerWebViewUrl: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface PaymentMethodsIntent {
    data object Load : PaymentMethodsIntent
    data object Refresh : PaymentMethodsIntent
    data object NavigateBack : PaymentMethodsIntent
    data object AddCard : PaymentMethodsIntent
    data class RegisterReturned(val authKey: String) : PaymentMethodsIntent
    data object RegisterDismissed : PaymentMethodsIntent
    data object ShowDeleteDialog : PaymentMethodsIntent
    data object DismissDeleteDialog : PaymentMethodsIntent
    data object ConfirmDelete : PaymentMethodsIntent
    data object SnackbarDismissed : PaymentMethodsIntent
}

sealed interface PaymentMethodsSideEffect {
    data object NavigateBack : PaymentMethodsSideEffect
}
