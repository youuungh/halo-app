package com.ninezero.halo.feature.notification.presentation.notification_settings

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

internal fun previewNotificationSettingsState(): NotificationSettingsState {
    return NotificationSettingsState(
        isLoading = false,
        masterEnabled = true,
        socialEnabled = true,
        creatorActivityEnabled = true,
        commerceEnabled = false,
        chatEnabled = true,
        systemEnabled = true
    )
}

internal fun previewNotificationSettingsLoadingState(): NotificationSettingsState {
    return NotificationSettingsState(isLoading = true)
}

internal fun previewNotificationSettingsErrorState(): NotificationSettingsState {
    return NotificationSettingsState(
        isLoading = false,
        error = "설정을 불러올 수 없습니다"
    )
}

@HaloLightDarkPreview
@Composable
private fun NotificationSettingsScreenPreview() {
    HaloScreenPreview {
        NotificationSettingsScreenContent(
            state = previewNotificationSettingsState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun NotificationSettingsScreenLoadingPreview() {
    HaloScreenPreview {
        NotificationSettingsScreenContent(
            state = previewNotificationSettingsLoadingState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun NotificationSettingsScreenErrorPreview() {
    HaloScreenPreview {
        NotificationSettingsScreenContent(
            state = previewNotificationSettingsErrorState()
        )
    }
}
