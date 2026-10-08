package com.ninezero.halo.feature.auth.presentation.reset_password

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.isValidPassword
import com.ninezero.halo.core.util.stripPasswordWhitespace
import com.ninezero.halo.feature.auth.domain.model.AuthValidationMessages
import com.ninezero.halo.feature.auth.domain.usecase.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel(), OrbitContainerHost<ResetPasswordState, ResetPasswordState, ResetPasswordSideEffect> {

    private val resetToken: String = savedStateHandle.get<String>("resetToken").orEmpty()

    override val container: OrbitContainer<ResetPasswordState, ResetPasswordState, ResetPasswordSideEffect> =
        orbitContainer(ResetPasswordState())

    fun onIntent(intent: ResetPasswordIntent) {
        when (intent) {
            is ResetPasswordIntent.PasswordChanged -> onPasswordChanged(intent.password)
            is ResetPasswordIntent.ConfirmPasswordChanged -> onConfirmPasswordChanged(intent.confirmPassword)
            is ResetPasswordIntent.TogglePasswordVisibility -> togglePasswordVisibility()
            is ResetPasswordIntent.ToggleConfirmPwVisibility -> toggleConfirmPwVisibility()
            is ResetPasswordIntent.Submit -> submit()
            is ResetPasswordIntent.NavigateBack -> navigateBack()
            is ResetPasswordIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun onPasswordChanged(password: String) = blockingIntent {
        reduce {
            state.copy(
                password = password.stripPasswordWhitespace(),
                passwordError = null,
                confirmPasswordError = null
            )
        }
    }

    private fun onConfirmPasswordChanged(confirmPassword: String) = blockingIntent {
        reduce {
            state.copy(
                confirmPassword = confirmPassword.stripPasswordWhitespace(),
                confirmPasswordError = null
            )
        }
    }

    private fun togglePasswordVisibility() = intent {
        reduce {
            state.copy(isPasswordVisible = !state.isPasswordVisible)
        }
    }

    private fun toggleConfirmPwVisibility() = intent {
        reduce {
            state.copy(isConfirmPwVisible = !state.isConfirmPwVisible)
        }
    }

    private fun submit() = intent {
        var hasError = false

        if (state.password.isBlank()) {
            reduce { state.copy(passwordError = AuthValidationMessages.PASSWORD_REQUIRED) }
            hasError = true
        } else if (!state.password.isValidPassword()) {
            reduce { state.copy(passwordError = AuthValidationMessages.PASSWORD_INVALID_SHORT) }
            hasError = true
        }

        if (state.confirmPassword.isBlank()) {
            reduce { state.copy(confirmPasswordError = AuthValidationMessages.PASSWORD_CONFIRM_REQUIRED) }
            hasError = true
        } else if (state.password != state.confirmPassword) {
            reduce { state.copy(confirmPasswordError = AuthValidationMessages.PASSWORD_MISMATCH) }
            hasError = true
        }

        if (hasError) {
            return@intent
        }

        postSideEffect(ResetPasswordSideEffect.ClearFocus)
        reduce { state.copy(isLoading = true) }

        resetPasswordUseCase(resetToken, state.password)
            .onSuccess {
                reduce { state.copy(isLoading = false) }
                postSideEffect(ResetPasswordSideEffect.ShowToast("비밀번호가 변경되었습니다. 새 비밀번호로 로그인해주세요"))
                postSideEffect(ResetPasswordSideEffect.NavigateToLogin)
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        snackbarState = SnackbarState(error.message ?: "비밀번호 변경에 실패했습니다")
                    )
                }
            }
    }

    private fun navigateBack() = intent {
        postSideEffect(ResetPasswordSideEffect.NavigateBack)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
