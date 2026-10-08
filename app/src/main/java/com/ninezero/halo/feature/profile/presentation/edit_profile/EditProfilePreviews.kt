package com.ninezero.halo.feature.profile.presentation.edit_profile

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun EditProfileScreenPreview() {
    HaloScreenPreview {
        EditProfileScreenContent(
            state = EditProfileState(
                displayName = "홍길동",
                bio = "안녕하세요! 반갑습니다.",
                location = "서울, 대한민국",
                website = "https://example.com",
                originalDisplayName = "홍길동",
                originalBio = "안녕하세요!",
                originalLocation = "서울, 대한민국",
                originalWebsite = "https://example.com"
            ),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun EditProfileScreenEmptyPreview() {
    HaloScreenPreview {
        EditProfileScreenContent(
            state = EditProfileState(
                displayName = "",
                bio = "",
                location = "",
                website = ""
            ),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun EditProfileScreenErrorPreview() {
    HaloScreenPreview {
        EditProfileScreenContent(
            state = EditProfileState(
                displayName = "",
                bio = "소개글입니다",
                location = "",
                website = "invalid-url",
                displayNameError = "닉네임을 입력해주세요",
                websiteError = "http:// 또는 https://로 시작해야 합니다",
                originalDisplayName = "홍길동"
            ),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun EditProfileScreenSavingPreview() {
    HaloScreenPreview {
        EditProfileScreenContent(
            state = EditProfileState(
                displayName = "홍길동",
                bio = "자기소개",
                isSaving = true,
                originalDisplayName = "홍길동"
            ),
            onIntent = {}
        )
    }
}
