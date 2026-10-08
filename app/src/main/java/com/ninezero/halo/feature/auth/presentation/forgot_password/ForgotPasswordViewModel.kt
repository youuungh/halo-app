package com.ninezero.halo.feature.auth.presentation.forgot_password

import androidx.lifecycle.ViewModel
import com.ninezero.halo.feature.auth.domain.usecase.RequestPasswordResetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase
) : ViewModel(), OrbitContainerHost<ForgotPasswordState, ForgotPasswordState, ForgotPasswordSideEffect> {

    override val container: OrbitContainer<ForgotPasswordState, ForgotPasswordState, ForgotPasswordSideEffect> =
        orbitContainer(ForgotPasswordState())

    fun onIntent(intent: ForgotPasswordIntent) {
        when (intent) {
            is ForgotPasswordIntent.EmailChanged -> updateEmail(intent.email)
            is ForgotPasswordIntent.SendResetCode -> sendResetCode()
            is ForgotPasswordIntent.NavigateBack -> navigateBack()
        }
    }

    private fun updateEmail(email: String) = blockingIntent {
        reduce { state.copy(email = email, emailError = null) }
    }

    private fun sendResetCode() = intent {
        postSideEffect(ForgotPasswordSideEffect.ClearFocus)
        reduce { state.copy(isLoading = true, emailError = null) }

        requestPasswordResetUseCase(state.email)
            .onSuccess {
                reduce { state.copy(isLoading = false) }
                postSideEffect(ForgotPasswordSideEffect.NavigateToResetCode(state.email))
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        emailError = error.message ?: "인증 코드 발송에 실패했습니다"
                    )
                }
            }
    }

    private fun navigateBack() = intent {
        postSideEffect(ForgotPasswordSideEffect.NavigateBack)
    }
}
