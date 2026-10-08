package com.ninezero.halo.feature.settings.presentation

import com.ninezero.halo.core.local.ThemeMode
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

data class SettingsState(
    val isLoading: Boolean = false,
    val showMyAccountSheet: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showThemeSheet: Boolean = false,
    val themeMode: Int = ThemeMode.SYSTEM,
    val snackbarState: SnackbarState? = null
)

sealed interface SettingsIntent {
    // 메뉴
    data object MyAccountClick : SettingsIntent
    data object DismissMyAccountSheet : SettingsIntent
    data object NotificationClick : SettingsIntent
    data object SavedPostsClick : SettingsIntent
    data object LikedPostsClick : SettingsIntent
    data object ArchivedChatsClick : SettingsIntent
    data object BlockedProfilesClick : SettingsIntent
    data object PaymentMethodsClick : SettingsIntent
    data object InfoClick : SettingsIntent
    data object ThemeClick : SettingsIntent
    data class ChangeTheme(val mode: Int) : SettingsIntent
    data object DismissThemeSheet : SettingsIntent

    // 로그아웃
    data object LogoutClick : SettingsIntent
    data object ConfirmLogout : SettingsIntent
    data object DismissLogoutDialog : SettingsIntent
    data object SnackbarDismissed : SettingsIntent
}

sealed interface SettingsSideEffect {
    data object NavigateToNotificationSettings : SettingsSideEffect
    data object NavigateToSavedPosts : SettingsSideEffect
    data object NavigateToLikedPosts : SettingsSideEffect
    data object NavigateToArchivedChats : SettingsSideEffect
    data object NavigateToBlockedProfiles : SettingsSideEffect
    data object NavigateToPaymentMethods : SettingsSideEffect
    data object NavigateToInfo : SettingsSideEffect
    data object NavigateToLogin : SettingsSideEffect
}
