package com.ninezero.halo.feature.auth.presentation.reset_code

data class ResetCodeState(
    val email: String = "",
    val code: String = "",
    val codeError: String? = null,
    val isLoading: Boolean = false,
    val remainingSeconds: Int = CODE_EXPIRE_SECONDS,
    val resendWaitSeconds: Int = RESEND_WAIT_SECONDS
) {
    companion object {
        const val CODE_EXPIRE_SECONDS = 5 * 60
        const val RESEND_WAIT_SECONDS = 60
    }
}

sealed interface ResetCodeIntent {
    data class CodeChanged(val code: String) : ResetCodeIntent
    data object VerifyCode : ResetCodeIntent
    data object ResendCode : ResetCodeIntent
    data object NavigateBack : ResetCodeIntent
}

sealed interface ResetCodeSideEffect {
    data object ClearFocus : ResetCodeSideEffect
    data class ShowToast(val message: String) : ResetCodeSideEffect
    data object NavigateBack : ResetCodeSideEffect
    data class NavigateToResetPassword(val resetToken: String) : ResetCodeSideEffect
}
