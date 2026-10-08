package com.ninezero.halo.feature.auth.presentation.welcome

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun WelcomeScreenPreview() {
    HaloScreenPreview {
        WelcomeScreenContent(
            state = WelcomeState(),
            onKakaoClick = {},
            onNaverClick = {},
            onGoogleClick = {},
            onRegisterClick = {},
            onLoginClick = {}
        )
    }
}
