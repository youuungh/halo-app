package com.ninezero.halo.feature.auth.presentation.social_username

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun SocialUsernamePreview() {
    HaloScreenPreview {
        SocialUsernameScreenContent(
            state = SocialUsernameState(username = "test1234"),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SocialUsernameErrorPreview() {
    HaloScreenPreview {
        SocialUsernameScreenContent(
            state = SocialUsernameState(
                username = "test1234",
                usernameError = "이미 사용 중인 사용자 이름입니다"
            ),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SocialUsernameLoadingPreview() {
    HaloScreenPreview {
        SocialUsernameScreenContent(
            state = SocialUsernameState(
                username = "test1234",
                isLoading = true
            ),
            onIntent = {}
        )
    }
}
