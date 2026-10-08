package com.ninezero.halo.feature.auth.presentation.welcome

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.fcm.FcmRegistrationManager
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.auth.domain.model.SocialLoginResult
import com.ninezero.halo.feature.auth.domain.usecase.SocialLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val socialLoginUseCase: SocialLoginUseCase,
    private val fcmRegistrationManager: FcmRegistrationManager
) : ViewModel(), OrbitContainerHost<WelcomeState, WelcomeState, WelcomeSideEffect> {

    override val container: OrbitContainer<WelcomeState, WelcomeState, WelcomeSideEffect> =
        orbitContainer(WelcomeState())

    fun onIntent(intent: WelcomeIntent) {
        when (intent) {
            is WelcomeIntent.SocialCredentialReceived -> socialLogin(intent.provider, intent.token)
            is WelcomeIntent.SocialSignInFailed -> showError(intent.message)
            is WelcomeIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun socialLogin(provider: String, token: String) = intent {
        reduce { state.copy(loadingProvider = provider) }

        val fid = fcmRegistrationManager.getFid()

        socialLoginUseCase(
            provider = provider,
            token = token,
            fid = fid
        ).onSuccess { result ->
            reduce { state.copy(loadingProvider = null) }
            when (result) {
                is SocialLoginResult.Success -> {
                    postSideEffect(WelcomeSideEffect.NavigateToHome)
                }
                is SocialLoginResult.NeedsUsername -> {
                    postSideEffect(
                        WelcomeSideEffect.NavigateToUsernameOnboarding(
                            signupToken = result.signupToken,
                            suggestedUsername = result.suggestedUsername
                        )
                    )
                }
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    loadingProvider = null,
                    snackbarState = SnackbarState(error.message ?: "로그인에 실패했습니다")
                )
            }
        }
    }

    private fun showError(message: String) = intent {
        reduce { state.copy(snackbarState = SnackbarState(message)) }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
