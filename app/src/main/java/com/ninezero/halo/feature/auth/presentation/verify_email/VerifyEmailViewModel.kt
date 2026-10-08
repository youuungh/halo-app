package com.ninezero.halo.feature.auth.presentation.verify_email

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.fcm.FcmRegistrationManager
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.auth.domain.usecase.ResendVerificationUseCase
import com.ninezero.halo.feature.auth.domain.usecase.VerifyEmailCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val verifyEmailCodeUseCase: VerifyEmailCodeUseCase,
    private val resendVerificationUseCase: ResendVerificationUseCase,
    private val fcmRegistrationManager: FcmRegistrationManager
) : ViewModel(), OrbitContainerHost<VerifyEmailState, VerifyEmailState, VerifyEmailSideEffect> {

    private val email: String = savedStateHandle.get<String>("email").orEmpty()

    override val container: OrbitContainer<VerifyEmailState, VerifyEmailState, VerifyEmailSideEffect> =
        orbitContainer(VerifyEmailState(email = email))

    private var validityTimerJob: Job? = null
    private var resendCooldownJob: Job? = null

    init {
        // 가입 폼 제출로 코드가 막 발송된 상태로 진입
        startValidityTimer()
        startResendCooldown()
    }

    fun onIntent(intent: VerifyEmailIntent) {
        when (intent) {
            is VerifyEmailIntent.CodeChanged -> updateCode(intent.code)
            is VerifyEmailIntent.VerifyCode -> verifyCode()
            is VerifyEmailIntent.ResendCode -> resendCode()
            is VerifyEmailIntent.NavigateBack -> navigateBack()
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

        postSideEffect(VerifyEmailSideEffect.ClearFocus)
        reduce { state.copy(isLoading = true, codeError = null) }

        val fid = fcmRegistrationManager.getFid()

        verifyEmailCodeUseCase(state.email, state.code, fid)
            .onSuccess {
                reduce { state.copy(isLoading = false) }
                postSideEffect(VerifyEmailSideEffect.NavigateToHome)
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

        resendVerificationUseCase(state.email)
            .onSuccess {
                reduce {
                    state.copy(
                        isLoading = false,
                        code = "",
                        remainingSeconds = VerifyEmailState.CODE_VALIDITY_SECONDS,
                        resendCooldownSeconds = VerifyEmailState.RESEND_COOLDOWN_SECONDS
                    )
                }
                startValidityTimer()
                startResendCooldown()
                postSideEffect(VerifyEmailSideEffect.ShowToast("인증 코드가 재전송되었습니다"))
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
        postSideEffect(VerifyEmailSideEffect.NavigateBack)
    }
}
