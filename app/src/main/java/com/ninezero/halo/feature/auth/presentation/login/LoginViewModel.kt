package com.ninezero.halo.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.fcm.FcmRegistrationManager
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.stripPasswordWhitespace
import com.ninezero.halo.feature.auth.domain.model.AuthValidationMessages
import com.ninezero.halo.feature.auth.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val fcmRegistrationManager: FcmRegistrationManager
) : ViewModel(), OrbitContainerHost<LoginState, LoginState, LoginSideEffect> {

    override val container: OrbitContainer<LoginState, LoginState, LoginSideEffect> =
        orbitContainer(LoginState())

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> onEmailChanged(intent.email)
            is LoginIntent.PasswordChanged -> onPasswordChanged(intent.password)
            is LoginIntent.TogglePasswordVisibility -> togglePasswordVisibility()
            is LoginIntent.LoginClicked -> login()
            is LoginIntent.RegisterClicked -> navigateToRegister()
            is LoginIntent.ForgotPasswordClicked -> navigateToForgotPassword()
            is LoginIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun onEmailChanged(email: String) = blockingIntent {
        reduce { state.copy(email = email, emailError = null) }
    }

    private fun onPasswordChanged(password: String) = blockingIntent {
        reduce { state.copy(password = password.stripPasswordWhitespace(), passwordError = null) }
    }

    private fun togglePasswordVisibility() = intent {
        reduce {
            state.copy(isPasswordVisible = !state.isPasswordVisible)
        }
    }

    private fun login() = intent {
        if (state.email.isBlank()) {
            reduce { state.copy(emailError = AuthValidationMessages.EMAIL_REQUIRED) }
            return@intent
        }
        if (state.password.isBlank()) {
            reduce { state.copy(passwordError = AuthValidationMessages.PASSWORD_REQUIRED) }
            return@intent
        }

        postSideEffect(LoginSideEffect.ClearFocus)
        reduce { state.copy(isLoading = true, emailError = null, passwordError = null) }

        val fid = fcmRegistrationManager.getFid()

        loginUseCase(
            email = state.email,
            password = state.password,
            fid = fid
        ).onSuccess {
            reduce { state.copy(isLoading = false) }
            postSideEffect(LoginSideEffect.NavigateToHome)
        }.onFailure { error ->
            reduce {
                state.copy(
                    isLoading = false,
                    snackbarState = SnackbarState(error.message ?: "로그인에 실패했습니다")
                )
            }
        }
    }

    private fun navigateToRegister() = intent {
        postSideEffect(LoginSideEffect.NavigateToRegister)
    }

    private fun navigateToForgotPassword() = intent {
        postSideEffect(LoginSideEffect.NavigateToForgotPassword)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
