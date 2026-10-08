package com.ninezero.halo.feature.creator.presentation

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.creator.domain.model.CreatorApplication

data class CreatorApplicationState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val application: CreatorApplication? = null,
    val reason: String = "",
    val portfolioUrl: String = "",
    val error: String? = null,
    val showCancelDialog: Boolean = false,
    val snackbarState: SnackbarState? = null
)

sealed interface CreatorApplicationIntent {
    data object LoadApplication : CreatorApplicationIntent
    data class UpdateReason(val reason: String) : CreatorApplicationIntent
    data class UpdatePortfolioUrl(val url: String) : CreatorApplicationIntent
    data object Submit : CreatorApplicationIntent
    data object StartReapply : CreatorApplicationIntent
    data object ShowCancelDialog : CreatorApplicationIntent
    data object DismissCancelDialog : CreatorApplicationIntent
    data object ConfirmCancel : CreatorApplicationIntent
    data object NavigateBack : CreatorApplicationIntent
    data object SnackbarDismissed : CreatorApplicationIntent
}

sealed interface CreatorApplicationSideEffect {
    data object NavigateBack : CreatorApplicationSideEffect
}
