package com.ninezero.halo.feature.auth.presentation.register

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.isValidEmail
import com.ninezero.halo.core.util.isValidPassword
import com.ninezero.halo.core.util.stripPasswordWhitespace
import com.ninezero.halo.feature.auth.domain.model.AuthValidationMessages
import com.ninezero.halo.feature.auth.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel(), OrbitContainerHost<RegisterState, RegisterState, RegisterSideEffect> {

    override val container: OrbitContainer<RegisterState, RegisterState, RegisterSideEffect> =
        orbitContainer(RegisterState())

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.EmailChanged -> onEmailChanged(intent.email)
            is RegisterIntent.UsernameChanged -> onUsernameChanged(intent.username)
            is RegisterIntent.PasswordChanged -> onPasswordChanged(intent.password)
            is RegisterIntent.ConfirmPasswordChanged -> onConfirmPasswordChanged(intent.confirmPassword)
            is RegisterIntent.TogglePasswordVisibility -> togglePasswordVisibility()
            is RegisterIntent.ToggleConfirmPwVisibility -> toggleConfirmPwVisibility()
            is RegisterIntent.RegisterClicked -> register()
            is RegisterIntent.LoginClicked -> navigateToLogin()
            is RegisterIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun onEmailChanged(email: String) = blockingIntent {
        reduce {
            state.copy(
                email = email,
                emailError = null
            )
        }
    }

    private fun onUsernameChanged(username: String) = blockingIntent {
        reduce {
            state.copy(
                username = username.lowercase(),  // 서버처럼 소문자로 표시
                usernameError = null
            )
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

    private fun register() = intent {
        var hasError = false

        if (state.email.isBlank()) {
            reduce { state.copy(emailError = AuthValidationMessages.EMAIL_REQUIRED) }
            hasError = true
        } else if (!state.email.isValidEmail()) {
            reduce { state.copy(emailError = AuthValidationMessages.EMAIL_INVALID) }
            hasError = true
        }

        if (state.username.isBlank()) {
            reduce { state.copy(usernameError = AuthValidationMessages.USERNAME_REQUIRED) }
            hasError = true
        } else if (state.username.trim().length < Constants.Auth.MIN_USERNAME_LENGTH) {
            reduce { state.copy(usernameError = AuthValidationMessages.USERNAME_TOO_SHORT) }
            hasError = true
        }

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

        postSideEffect(RegisterSideEffect.ClearFocus)
        reduce { state.copy(isLoading = true) }

        registerUseCase(
            email = state.email,
            username = state.username,
            password = state.password,
            confirmPassword = state.confirmPassword
        ).onSuccess {
            // 안내는 인증 화면이 담당
            reduce { state.copy(isLoading = false) }
            postSideEffect(RegisterSideEffect.NavigateToVerifyEmail(state.email))
        }.onFailure { error ->
            reduce {
                state.copy(
                    isLoading = false,
                    snackbarState = SnackbarState(error.message ?: "회원가입에 실패했습니다")
                )
            }
        }
    }

    private fun navigateToLogin() = intent {
        postSideEffect(RegisterSideEffect.NavigateToLogin)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
