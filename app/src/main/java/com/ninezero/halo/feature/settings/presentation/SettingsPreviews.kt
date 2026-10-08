package com.ninezero.halo.feature.settings.presentation

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun SettingsScreenPreview() {
    HaloScreenPreview {
        SettingsScreenContent(
            state = SettingsState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SettingsScreenLoadingPreview() {
    HaloScreenPreview {
        SettingsScreenContent(
            state = SettingsState(isLoading = true),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SettingsScreenLogoutDialogPreview() {
    HaloScreenPreview {
        SettingsScreenContent(
            state = SettingsState(showLogoutDialog = true),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}
