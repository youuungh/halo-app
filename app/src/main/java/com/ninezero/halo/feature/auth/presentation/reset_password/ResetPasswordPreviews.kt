package com.ninezero.halo.feature.auth.presentation.reset_password
import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun ResetPasswordScreenPreview() {
    HaloScreenPreview {
        ResetPasswordScreenContent(
            state = ResetPasswordState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ResetPasswordScreenErrorPreview() {
    HaloScreenPreview {
        ResetPasswordScreenContent(
            state = ResetPasswordState(
                password = "password123!",
                confirmPassword = "password123",
                confirmPasswordError = "비밀번호가 일치하지 않습니다"
            ),
            onIntent = {}
        )
    }
}
