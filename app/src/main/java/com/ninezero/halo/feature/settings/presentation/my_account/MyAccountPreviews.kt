package com.ninezero.halo.feature.settings.presentation.my_account

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.auth.domain.model.Device

private val previewRootState = MyAccountState(
    displayName = "테스트",
    username = "test",
    hasPassword = true,
    socialProviders = listOf("GOOGLE", "KAKAO")
)

private val previewDevices = listOf(
    Device(
        id = "session-1",
        deviceName = "Pixel 8",
        os = "Android 15",
        ipAddress = "192.168.0.12",
        location = "Seoul, KR",
        lastActiveAt = System.currentTimeMillis() - 60_000L,
        isCurrent = true,
    ),
    Device(
        id = "session-2",
        deviceName = "SM-S928N",
        os = "Android 14",
        ipAddress = "172.30.1.54",
        location = null,
        lastActiveAt = System.currentTimeMillis() - 3 * 60 * 60_000L,
        isCurrent = false,
    ),
    Device(
        id = "session-3",
        deviceName = "Unknown OS",
        os = "Unknown OS",
        ipAddress = "121.140.22.7",
        location = "Busan, KR",
        lastActiveAt = System.currentTimeMillis() - 2 * 24 * 60 * 60_000L,
        isCurrent = false,
    )
)

@HaloLightDarkPreview
@Composable
private fun MyAccountRootPreview() {
    HaloScreenPreview {
        MyAccountRootContent(
            state = previewRootState,
            onIntent = {},
            onNavigateToCreatorApplication = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyAccountRootCreatorPreview() {
    HaloScreenPreview {
        MyAccountRootContent(
            state = previewRootState.copy(isCreator = true),
            onIntent = {},
            onNavigateToCreatorApplication = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ChangePasswordPreview() {
    HaloScreenPreview {
        ChangePasswordContent(
            state = previewRootState.copy(
                page = MyAccountPage.CHANGE_PASSWORD,
                currentPassword = "password1!",
                newPassword = "newpass1!",
                confirmPassword = "newpass2!",
                confirmPasswordError = "새 비밀번호가 일치하지 않습니다"
            ),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun DevicesPreview() {
    HaloScreenPreview {
        DevicesContent(
            state = previewRootState.copy(
                page = MyAccountPage.DEVICES,
                devices = previewDevices
            ),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun DevicesErrorPreview() {
    HaloScreenPreview {
        DevicesContent(
            state = previewRootState.copy(
                page = MyAccountPage.DEVICES,
                devicesError = "기기 목록을 불러오지 못했습니다"
            ),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun DeviceLogoutDialogPreview() {
    HaloScreenPreview {
        ConfirmDialog(
            title = "이 기기에서 로그아웃할까요?",
            message = previewDevices[1].deviceName,
            confirmText = "로그아웃",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun DeleteAccountDialogPreview() {
    HaloScreenPreview {
        DeleteAccountDialog(
            isAgreed = true,
            onAgreementChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}
