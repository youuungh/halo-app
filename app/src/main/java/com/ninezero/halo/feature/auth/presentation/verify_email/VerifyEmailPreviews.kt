package com.ninezero.halo.feature.auth.presentation.verify_email
import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun VerifyEmailScreenPreview() {
    HaloScreenPreview {
        VerifyEmailScreenContent(
            state = VerifyEmailState(
                email = "test@example.com",
                code = "A3F"
            ),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun VerifyEmailScreenErrorPreview() {
    HaloScreenPreview {
        VerifyEmailScreenContent(
            state = VerifyEmailState(
                email = "test@example.com",
                codeError = "인증 코드가 올바르지 않습니다.",
                remainingSeconds = 42,
                resendCooldownSeconds = 0
            ),
            onIntent = {}
        )
    }
}
