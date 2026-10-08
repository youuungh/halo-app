package com.ninezero.halo.feature.auth.presentation.reset_code
import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun ResetCodeScreenPreview() {
    HaloScreenPreview {
        ResetCodeScreenContent(
            state = ResetCodeState(
                email = "test@example.com",
                code = "A3F"
            ),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ResetCodeScreenErrorPreview() {
    HaloScreenPreview {
        ResetCodeScreenContent(
            state = ResetCodeState(
                email = "test@example.com",
                codeError = "인증 코드가 올바르지 않습니다.",
                remainingSeconds = 42,
                resendCooldownSeconds = 0
            ),
            onIntent = {}
        )
    }
}
