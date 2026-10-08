package com.ninezero.halo.feature.main.presentation

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

data class MainState(
    val selectedTab: MainTab = MainTab.FEED,
    val currentUserId: Int? = null,
    val hasUnreadMessage: Boolean = false,
    val hasUnreadNotification: Boolean = false,
    val snackbarState: SnackbarState? = null
)

enum class MainTab {
    FEED,
    EXPLORE,
    CREATE,
    CHAT,
    PROFILE
}

sealed interface MainIntent {
    data class SelectTab(val tab: MainTab) : MainIntent
    data object RefreshUnreadCounts : MainIntent
    data object SnackbarDismissed : MainIntent
}

sealed interface MainSideEffect {
    data object NavigateToCreatePost : MainSideEffect
}
