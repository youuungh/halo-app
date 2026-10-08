package com.ninezero.halo.feature.settings.presentation.my_account

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.auth.domain.model.Device
import com.ninezero.halo.feature.creator.domain.model.CreatorApplicationStatus

enum class MyAccountPage {
    ROOT,
    CHANGE_PASSWORD,
    DEVICES
}

data class MyAccountState(
    val page: MyAccountPage = MyAccountPage.ROOT,

    // 계정 정보, 받기 전엔 카드 스켈레톤
    val isAccountLoaded: Boolean = false,
    val displayName: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    val hasPassword: Boolean = true,
    val socialProviders: List<String> = emptyList(),

    // 크리에이터 배너
    val isCreator: Boolean = false,
    val applicationStatus: CreatorApplicationStatus? = null,

    // 비밀번호 변경 폼
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSubmitting: Boolean = false,

    // 계정 삭제
    val showDeleteDialog: Boolean = false,
    val isDeleteAgreed: Boolean = false,
    val isDeleting: Boolean = false,

    // 크리에이터 해제
    val showRevokeCreatorDialog: Boolean = false,
    val isRevokeAgreed: Boolean = false,
    val isRevokingCreator: Boolean = false,

    // 로그인된 기기
    val devices: List<Device> = emptyList(),
    val isDevicesLoading: Boolean = false,
    val devicesError: String? = null,
    val deviceToLogout: Device? = null,
    val isLoggingOutDevice: Boolean = false,

    val snackbarState: SnackbarState? = null
)

sealed interface MyAccountIntent {
    data object Load : MyAccountIntent

    // 페이지 전환
    data object ChangePasswordClick : MyAccountIntent
    data object BackToRoot : MyAccountIntent

    // 비밀번호 변경 폼
    data class CurrentPasswordChanged(val value: String) : MyAccountIntent
    data class NewPasswordChanged(val value: String) : MyAccountIntent
    data class ConfirmPasswordChanged(val value: String) : MyAccountIntent
    data object ToggleCurrentPasswordVisibility : MyAccountIntent
    data object ToggleNewPasswordVisibility : MyAccountIntent
    data object ToggleConfirmPasswordVisibility : MyAccountIntent
    data object SubmitPasswordChange : MyAccountIntent

    // 계정 삭제
    data object DeleteAccountClick : MyAccountIntent
    data object DismissDeleteDialog : MyAccountIntent
    data class DeleteAgreementChanged(val agreed: Boolean) : MyAccountIntent
    data object ConfirmDeleteAccount : MyAccountIntent

    // 크리에이터 해제
    data object RevokeCreatorClick : MyAccountIntent
    data object DismissRevokeDialog : MyAccountIntent
    data class RevokeAgreementChanged(val agreed: Boolean) : MyAccountIntent
    data object ConfirmRevokeCreator : MyAccountIntent

    // 로그인된 기기
    data object DevicesClick : MyAccountIntent
    data object RetryLoadDevices : MyAccountIntent
    data class RequestDeviceLogout(val device: Device) : MyAccountIntent
    data object DismissDeviceLogoutDialog : MyAccountIntent
    data object ConfirmDeviceLogout : MyAccountIntent


    data object SnackbarDismissed : MyAccountIntent
}

sealed interface MyAccountSideEffect {
    data object ClearFocus : MyAccountSideEffect
    data object AccountDeleted : MyAccountSideEffect
}
