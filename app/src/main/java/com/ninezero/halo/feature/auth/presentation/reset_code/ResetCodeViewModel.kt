package com.ninezero.halo.feature.auth.presentation.reset_code

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.auth.domain.usecase.RequestPasswordResetUseCase
import com.ninezero.halo.feature.auth.domain.usecase.VerifyResetCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class ResetCodeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val verifyResetCodeUseCase: VerifyResetCodeUseCase,
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase
) : ViewModel(), OrbitContainerHost<ResetCodeState, ResetCodeState, ResetCodeSideEffect> {

    private val email: String = savedStateHandle.get<String>("email").orEmpty()

    override val container: OrbitContainer<ResetCodeState, ResetCodeState, ResetCodeSideEffect> =
        orbitContainer(ResetCodeState(email = email))

    private var validityTimerJob: Job? = null
    private var resendCooldownJob: Job? = null

    init {
        // 직전 화면에서 코드가 막 발송된 상태로 진입
        startValidityTimer()
        startResendCooldown()
    }

    fun onIntent(intent: ResetCodeIntent) {
        when (intent) {
            is ResetCodeIntent.CodeChanged -> updateCode(intent.code)
            is ResetCodeIntent.VerifyCode -> verifyCode()
            is ResetCodeIntent.ResendCode -> resendCode()
            is ResetCodeIntent.NavigateBack -> navigateBack()
        }
    }

    private fun updateCode(raw: String) = blockingIntent {
        val filtered = raw.uppercase()
            .filter { it in 'A'..'Z' || it in '0'..'9' }
            .take(Constants.Auth.VERIFICATION_CODE_LENGTH)

        reduce { state.copy(code = filtered, codeError = null) }

        // 마지막 자리까지 입력하면 자동 제출
        if (filtered.length == Constants.Auth.VERIFICATION_CODE_LENGTH && !state.isLoading) {
            verifyCode()
        }
    }

    private fun verifyCode() = intent {
        if (state.isLoading || state.code.length != Constants.Auth.VERIFICATION_CODE_LENGTH) return@intent

        postSideEffect(ResetCodeSideEffect.ClearFocus)
        reduce { state.copy(isLoading = true, codeError = null) }

        verifyResetCodeUseCase(state.email, state.code)
            .onSuccess { resetToken ->
                reduce { state.copy(isLoading = false) }
                postSideEffect(ResetCodeSideEffect.NavigateToResetPassword(resetToken))
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        code = "",
                        codeError = error.message ?: "인증에 실패했습니다"
                    )
                }
            }
    }

    private fun resendCode() = intent {
        if (state.resendCooldownSeconds > 0 || state.isLoading) return@intent

        reduce { state.copy(isLoading = true, codeError = null) }

        requestPasswordResetUseCase(state.email)
            .onSuccess {
                reduce {
                    state.copy(
                        isLoading = false,
                        code = "",
                        remainingSeconds = ResetCodeState.CODE_VALIDITY_SECONDS,
                        resendCooldownSeconds = ResetCodeState.RESEND_COOLDOWN_SECONDS
                    )
                }
                startValidityTimer()
                startResendCooldown()
                postSideEffect(ResetCodeSideEffect.ShowToast("인증 코드가 재전송되었습니다"))
            }
            .onFailure { error ->
                reduce { state.copy(isLoading = false, codeError = error.message ?: "인증 코드 재전송에 실패했습니다") }
            }
    }

    private fun startValidityTimer() {
        validityTimerJob?.cancel()
        validityTimerJob = intent {
            while (state.remainingSeconds > 0) {
                delay(1_000)
                reduce { state.copy(remainingSeconds = state.remainingSeconds - 1) }
            }
        }
    }

    private fun startResendCooldown() {
        resendCooldownJob?.cancel()
        resendCooldownJob = intent {
            while (state.resendCooldownSeconds > 0) {
                delay(1_000)
                reduce { state.copy(resendCooldownSeconds = state.resendCooldownSeconds - 1) }
            }
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(ResetCodeSideEffect.NavigateBack)
    }
}
