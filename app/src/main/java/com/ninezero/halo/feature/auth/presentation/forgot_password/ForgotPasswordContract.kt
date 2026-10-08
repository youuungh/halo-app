package com.ninezero.halo.feature.auth.presentation.forgot_password

data class ForgotPasswordState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false
)

sealed interface ForgotPasswordIntent {
    data class EmailChanged(val email: String) : ForgotPasswordIntent
    data object SendResetCode : ForgotPasswordIntent
    data object NavigateBack : ForgotPasswordIntent
}

sealed interface ForgotPasswordSideEffect {
    data object ClearFocus : ForgotPasswordSideEffect
    data object NavigateBack : ForgotPasswordSideEffect
    data class NavigateToResetCode(val email: String) : ForgotPasswordSideEffect
}
