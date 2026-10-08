package com.ninezero.halo.feature.auth.presentation.forgot_password
import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun ForgotPasswordScreenPreview() {
    HaloScreenPreview {
        ForgotPasswordScreenContent(
            state = ForgotPasswordState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ForgotPasswordScreenLoadingPreview() {
    HaloScreenPreview {
        ForgotPasswordScreenContent(
            state = ForgotPasswordState(
                email = "test@example.com",
                isLoading = true
            ),
            onIntent = {}
        )
    }
}
