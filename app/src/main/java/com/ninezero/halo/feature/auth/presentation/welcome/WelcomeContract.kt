package com.ninezero.halo.feature.auth.presentation.welcome

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

data class WelcomeState(
    val loadingProvider: String? = null,
    val snackbarState: SnackbarState? = null
) {
    val isSocialLoading: Boolean get() = loadingProvider != null
}

sealed interface WelcomeIntent {
    data class SocialCredentialReceived(val provider: String, val token: String) : WelcomeIntent  // 토큰 수신
    data class SocialSignInFailed(val message: String) : WelcomeIntent
    data object SnackbarDismissed : WelcomeIntent
}

sealed interface WelcomeSideEffect {
    data object NavigateToHome : WelcomeSideEffect
    data class NavigateToUsernameOnboarding(
        val signupToken: String,
        val suggestedUsername: String
    ) : WelcomeSideEffect
}
