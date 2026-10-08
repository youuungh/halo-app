package com.ninezero.halo.feature.auth.presentation.login

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent
    data class PasswordChanged(val password: String) : LoginIntent
    data object TogglePasswordVisibility : LoginIntent
    data object LoginClicked : LoginIntent
    data object RegisterClicked : LoginIntent
    data object ForgotPasswordClicked : LoginIntent
    data object SnackbarDismissed : LoginIntent
}

sealed interface LoginSideEffect {
    data object ClearFocus : LoginSideEffect
    data object NavigateToHome : LoginSideEffect
    data object NavigateToRegister : LoginSideEffect
    data object NavigateToForgotPassword : LoginSideEffect
}
