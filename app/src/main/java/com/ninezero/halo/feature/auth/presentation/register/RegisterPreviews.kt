package com.ninezero.halo.feature.auth.presentation.register
import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun RegisterScreenPreview() {
    HaloScreenPreview {
        RegisterScreenContent(
            state = RegisterState(),
            onIntent = {},
            onBackClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun RegisterScreenLoadingPreview() {
    HaloScreenPreview {
        RegisterScreenContent(
            state = RegisterState(isLoading = true),
            onIntent = {},
            onBackClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun RegisterScreenFieldErrorsPreview() {
    HaloScreenPreview {
        RegisterScreenContent(
            state = RegisterState(
                username = "abc",
                usernameError = "3자 이상 입력해주세요",
                email = "invalid",
                emailError = "올바른 이메일 형식이 아닙니다",
                password = "123",
                passwordError = "8자 이상 입력해주세요"
            ),
            onIntent = {},
            onBackClick = {}
        )
    }
}
