package com.ninezero.halo.feature.auth.presentation.login
import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun LoginScreenPreview() {
    LoginScreenPreviewContent(state = LoginState())
}

@HaloLightDarkPreview
@Composable
private fun LoginScreenLoadingPreview() {
    LoginScreenPreviewContent(state = LoginState(isLoading = true))
}

@Composable
private fun LoginScreenPreviewContent(
    state: LoginState
) {
    HaloScreenPreview {
        LoginScreenContent(
            state = state,
            onIntent = {},
            onBackClick = {}
        )
    }
}
