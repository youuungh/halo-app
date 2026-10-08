package com.ninezero.halo.feature.auth.presentation.reset_password

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

data class ResetPasswordState(
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPwVisible: Boolean = false,
    val isLoading: Boolean = false,
    val snackbarState: SnackbarState? = null,

    // 입력값 검증 에러
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

sealed interface ResetPasswordIntent {
    data class PasswordChanged(val password: String) : ResetPasswordIntent
    data class ConfirmPasswordChanged(val confirmPassword: String) : ResetPasswordIntent
    data object TogglePasswordVisibility : ResetPasswordIntent
    data object ToggleConfirmPwVisibility : ResetPasswordIntent
    data object Submit : ResetPasswordIntent
    data object NavigateBack : ResetPasswordIntent
    data object SnackbarDismissed : ResetPasswordIntent
}

sealed interface ResetPasswordSideEffect {
    data object ClearFocus : ResetPasswordSideEffect
    data class ShowToast(val message: String) : ResetPasswordSideEffect
    data object NavigateBack : ResetPasswordSideEffect
    data object NavigateToLogin : ResetPasswordSideEffect
}
