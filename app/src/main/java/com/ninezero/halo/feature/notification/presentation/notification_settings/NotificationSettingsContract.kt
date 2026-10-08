package com.ninezero.halo.feature.notification.presentation.notification_settings

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

data class NotificationSettingsState(
    val isLoading: Boolean = true,
    val masterEnabled: Boolean = true,
    val socialEnabled: Boolean = true,
    val creatorActivityEnabled: Boolean = true,
    val commerceEnabled: Boolean = true,
    val chatEnabled: Boolean = true,
    val systemEnabled: Boolean = true,
    // 마스터 토글 끄기 전 개별 설정 백업
    val savedSocialEnabled: Boolean = true,
    val savedCreatorActivityEnabled: Boolean = true,
    val savedCommerceEnabled: Boolean = true,
    val savedChatEnabled: Boolean = true,
    val savedSystemEnabled: Boolean = true,
    val error: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface NotificationSettingsIntent {
    data object Retry : NotificationSettingsIntent
    data class ToggleAll(val enabled: Boolean) : NotificationSettingsIntent
    data class ToggleSocial(val enabled: Boolean) : NotificationSettingsIntent
    data class ToggleCreatorActivity(val enabled: Boolean) : NotificationSettingsIntent
    data class ToggleCommerce(val enabled: Boolean) : NotificationSettingsIntent
    data class ToggleChat(val enabled: Boolean) : NotificationSettingsIntent
    data class ToggleSystem(val enabled: Boolean) : NotificationSettingsIntent
    data object OpenCreatorNotifications : NotificationSettingsIntent
    data object SnackbarDismissed : NotificationSettingsIntent
    data object NavigateBack : NotificationSettingsIntent
}

sealed interface NotificationSettingsSideEffect {
    data object NavigateBack : NotificationSettingsSideEffect
    data object NavigateToCreatorNotifications : NotificationSettingsSideEffect
}
