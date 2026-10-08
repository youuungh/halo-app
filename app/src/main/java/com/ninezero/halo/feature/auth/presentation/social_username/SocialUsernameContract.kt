package com.ninezero.halo.feature.auth.presentation.social_username

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

data class SocialUsernameState(
    val username: String = "",
    val isLoading: Boolean = false,
    val usernameError: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface SocialUsernameIntent {
    data class UsernameChanged(val username: String) : SocialUsernameIntent
    data object SubmitClicked : SocialUsernameIntent
    data object NavigateBack : SocialUsernameIntent
    data object SnackbarDismissed : SocialUsernameIntent
}

sealed interface SocialUsernameSideEffect {
    data object ClearFocus : SocialUsernameSideEffect
    data object NavigateToHome : SocialUsernameSideEffect
    data object NavigateBack : SocialUsernameSideEffect
}
