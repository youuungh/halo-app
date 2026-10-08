package com.ninezero.halo.feature.auth.presentation.social_username

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.fcm.FcmRegistrationManager
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.auth.domain.model.AuthValidationMessages
import com.ninezero.halo.feature.auth.domain.usecase.SocialCompleteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class SocialUsernameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val socialCompleteUseCase: SocialCompleteUseCase,
    private val fcmRegistrationManager: FcmRegistrationManager
) : ViewModel(), OrbitContainerHost<SocialUsernameState, SocialUsernameState, SocialUsernameSideEffect> {

    private val signupToken: String = savedStateHandle.get<String>("signupToken").orEmpty()

    private val suggestedUsername: String = savedStateHandle.get<String>("suggestedUsername").orEmpty()

    override val container: OrbitContainer<SocialUsernameState, SocialUsernameState, SocialUsernameSideEffect> =
        orbitContainer(SocialUsernameState(username = suggestedUsername))

    fun onIntent(intent: SocialUsernameIntent) {
        when (intent) {
            is SocialUsernameIntent.UsernameChanged -> onUsernameChanged(intent.username)
            is SocialUsernameIntent.SubmitClicked -> submit()
            is SocialUsernameIntent.NavigateBack -> navigateBack()
            is SocialUsernameIntent.SnackbarDismissed -> dismissSnackbar()
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

    private fun submit() = intent {
        if (state.username.isBlank()) {
            reduce { state.copy(usernameError = AuthValidationMessages.USERNAME_REQUIRED) }
            return@intent
        }
        if (state.username.trim().length < Constants.Auth.MIN_USERNAME_LENGTH) {
            reduce { state.copy(usernameError = AuthValidationMessages.USERNAME_TOO_SHORT) }
            return@intent
        }

        postSideEffect(SocialUsernameSideEffect.ClearFocus)
        reduce { state.copy(isLoading = true, usernameError = null) }

        val fid = fcmRegistrationManager.getFid()

        socialCompleteUseCase(
            signupToken = signupToken,
            username = state.username,
            fid = fid
        ).onSuccess {
            reduce { state.copy(isLoading = false) }
            postSideEffect(SocialUsernameSideEffect.NavigateToHome)
        }.onFailure { error ->
            reduce {
                state.copy(
                    isLoading = false,
                    snackbarState = SnackbarState(error.message ?: "회원가입에 실패했습니다")
                )
            }
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(SocialUsernameSideEffect.NavigateBack)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
