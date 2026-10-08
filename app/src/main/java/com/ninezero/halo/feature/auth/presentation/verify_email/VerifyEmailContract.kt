package com.ninezero.halo.feature.auth.presentation.verify_email

data class VerifyEmailState(
    val email: String = "",
    val code: String = "",
    val codeError: String? = null,
    val isLoading: Boolean = false,
    val remainingSeconds: Int = CODE_VALIDITY_SECONDS,
    val resendCooldownSeconds: Int = RESEND_COOLDOWN_SECONDS
) {
    companion object {
        const val CODE_VALIDITY_SECONDS = 5 * 60
        const val RESEND_COOLDOWN_SECONDS = 60
    }
}

sealed interface VerifyEmailIntent {
    data class CodeChanged(val code: String) : VerifyEmailIntent
    data object VerifyCode : VerifyEmailIntent
    data object ResendCode : VerifyEmailIntent
    data object NavigateBack : VerifyEmailIntent
}

sealed interface VerifyEmailSideEffect {
    data object ClearFocus : VerifyEmailSideEffect
    data class ShowToast(val message: String) : VerifyEmailSideEffect
    data object NavigateBack : VerifyEmailSideEffect

    data object NavigateToHome : VerifyEmailSideEffect  // 코드 검증이 곧 로그인
}
