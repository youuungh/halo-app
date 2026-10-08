package com.ninezero.halo.feature.auth.presentation.register

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

data class RegisterState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPwVisible: Boolean = false,
    val isLoading: Boolean = false,
    val snackbarState: SnackbarState? = null,

    // 입력값 검증 에러
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

sealed interface RegisterIntent {
    data class EmailChanged(val email: String) : RegisterIntent
    data class UsernameChanged(val username: String) : RegisterIntent
    data class PasswordChanged(val password: String) : RegisterIntent
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterIntent
    data object TogglePasswordVisibility : RegisterIntent
    data object ToggleConfirmPwVisibility : RegisterIntent
    data object RegisterClicked : RegisterIntent
    data object LoginClicked : RegisterIntent
    data object SnackbarDismissed : RegisterIntent
}

sealed interface RegisterSideEffect {
    data object ClearFocus : RegisterSideEffect
    data object NavigateToLogin : RegisterSideEffect

    data class NavigateToVerifyEmail(val email: String) : RegisterSideEffect  // 계정은 코드 검증 후 생성
}
