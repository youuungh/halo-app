package com.ninezero.halo.feature.settings.presentation.my_account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.network.ApiException
import com.ninezero.halo.core.network.NetworkMonitor
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.isValidPassword
import com.ninezero.halo.core.util.stripPasswordWhitespace
import com.ninezero.halo.feature.auth.domain.model.AuthValidationMessages
import com.ninezero.halo.feature.auth.domain.model.Device
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import com.ninezero.halo.feature.auth.domain.repository.DeviceRepository
import com.ninezero.halo.feature.auth.domain.usecase.ChangePasswordUseCase
import com.ninezero.halo.feature.auth.domain.usecase.DeleteAccountUseCase
import com.ninezero.halo.feature.auth.presentation.social.disconnectNaver
import com.ninezero.halo.feature.creator.domain.repository.CreatorRepository
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class MyAccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val creatorRepository: CreatorRepository,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val deviceRepository: DeviceRepository,
    private val profileRepository: ProfileRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel(), OrbitContainerHost<MyAccountState, MyAccountState, MyAccountSideEffect> {

    override val container: OrbitContainer<MyAccountState, MyAccountState, MyAccountSideEffect> =
        orbitContainer(MyAccountState())

    // 연결 실패 뒤 네트워크 복귀 때 다시 받음
    private var networkRestoredJob: Job? = null

    private fun loadAccountInfo(): Job = intent {  // 네트워크 복귀 때 자기 호출이라 타입 명시
        var offline = false
        fun Throwable.isOffline() = (this as? ApiException)?.code == 0

        // 앱이 받아 둔 내 프로필로 카드부터, 오프라인 빈 칸 방지
        val cached = profileRepository.getCachedMyProfile()
        if (cached != null && !state.isAccountLoaded) {
            reduce {
                state.copy(
                    isAccountLoaded = true,
                    displayName = cached.displayName,
                    username = cached.username,
                    avatarUrl = cached.avatarThumbUrl ?: cached.avatarUrl,
                    isCreator = cached.isCreator
                )
            }
        }

        authRepository.getCurrentUser()
            .onSuccess { user ->
                reduce {
                    state.copy(
                        username = user.username,
                        hasPassword = user.hasPassword,
                        socialProviders = user.socialProviders
                    )
                }
            }
            .onFailure { if (it.isOffline()) offline = true }

        getMyProfileUseCase()
            .onSuccess { profile ->
                reduce {
                    state.copy(
                        isAccountLoaded = true,
                        displayName = profile.displayName,
                        avatarUrl = profile.avatarThumbUrl ?: profile.avatarUrl,
                        isCreator = profile.isCreator
                    )
                }
            }
            .onFailure { if (it.isOffline()) offline = true }

        // 배너 문구
        creatorRepository.getMyApplication()
            .onSuccess { application ->
                reduce { state.copy(applicationStatus = application.status) }
            }
            .onFailure { if (it.isOffline()) offline = true }

        if (offline) {
            if (networkRestoredJob == null) {
                networkRestoredJob = viewModelScope.launch {
                    networkMonitor.restored.first()
                    networkRestoredJob = null
                    loadAccountInfo()
                }
            }
        } else {
            networkRestoredJob?.cancel()
            networkRestoredJob = null
        }
    }

    fun onIntent(intent: MyAccountIntent) {
        when (intent) {
            is MyAccountIntent.Load -> loadAccountInfo()
            is MyAccountIntent.ChangePasswordClick -> navigateToChangePassword()
            is MyAccountIntent.BackToRoot -> backToRoot()
            is MyAccountIntent.CurrentPasswordChanged -> onCurrentPasswordChanged(intent.value)
            is MyAccountIntent.NewPasswordChanged -> onNewPasswordChanged(intent.value)
            is MyAccountIntent.ConfirmPasswordChanged -> onConfirmPasswordChanged(intent.value)
            is MyAccountIntent.ToggleCurrentPasswordVisibility -> toggleCurrentPasswordVisibility()
            is MyAccountIntent.ToggleNewPasswordVisibility -> toggleNewPasswordVisibility()
            is MyAccountIntent.ToggleConfirmPasswordVisibility -> toggleConfirmPasswordVisibility()
            is MyAccountIntent.SubmitPasswordChange -> submitPasswordChange()
            is MyAccountIntent.DeleteAccountClick -> showDeleteDialog()
            is MyAccountIntent.DismissDeleteDialog -> dismissDeleteDialog()
            is MyAccountIntent.DeleteAgreementChanged -> changeDeleteAgreement(intent.agreed)
            is MyAccountIntent.ConfirmDeleteAccount -> deleteAccount()
            is MyAccountIntent.RevokeCreatorClick -> showRevokeDialog()
            is MyAccountIntent.DismissRevokeDialog -> dismissRevokeDialog()
            is MyAccountIntent.RevokeAgreementChanged -> changeRevokeAgreement(intent.agreed)
            is MyAccountIntent.ConfirmRevokeCreator -> revokeCreator()
            is MyAccountIntent.DevicesClick -> navigateToDevices()
            is MyAccountIntent.RetryLoadDevices -> loadDevices()
            is MyAccountIntent.RequestDeviceLogout -> requestDeviceLogout(intent.device)
            is MyAccountIntent.DismissDeviceLogoutDialog -> dismissDeviceLogoutDialog()
            is MyAccountIntent.ConfirmDeviceLogout -> confirmDeviceLogout()
            is MyAccountIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun toggleCurrentPasswordVisibility() = intent {
        reduce { state.copy(isCurrentPasswordVisible = !state.isCurrentPasswordVisible) }
    }

    private fun toggleNewPasswordVisibility() = intent {
        reduce { state.copy(isNewPasswordVisible = !state.isNewPasswordVisible) }
    }

    private fun toggleConfirmPasswordVisibility() = intent {
        reduce { state.copy(isConfirmPasswordVisible = !state.isConfirmPasswordVisible) }
    }

    private fun showDeleteDialog() = intent {
        reduce { state.copy(showDeleteDialog = true, isDeleteAgreed = false) }
    }

    private fun dismissDeleteDialog() = intent {
        reduce { state.copy(showDeleteDialog = false) }
    }

    private fun changeDeleteAgreement(agreed: Boolean) = intent {
        reduce { state.copy(isDeleteAgreed = agreed) }
    }

    private fun showRevokeDialog() = intent {
        reduce { state.copy(showRevokeCreatorDialog = true, isRevokeAgreed = false) }
    }

    private fun dismissRevokeDialog() = intent {
        reduce { state.copy(showRevokeCreatorDialog = false) }
    }

    private fun changeRevokeAgreement(agreed: Boolean) = intent {
        reduce { state.copy(isRevokeAgreed = agreed) }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    private fun navigateToChangePassword() = intent {
        reduce { state.copy(page = MyAccountPage.CHANGE_PASSWORD) }
    }

    private fun navigateToDevices() = intent {
        reduce { state.copy(page = MyAccountPage.DEVICES) }
        loadDevices()
    }

    private fun loadDevices() = intent {
        reduce { state.copy(isDevicesLoading = true, devicesError = null) }
        deviceRepository.getActiveDevices()
            .onSuccess { devices ->
                reduce {
                    state.copy(
                        devices = devices.sortedByDescending { it.isCurrent },
                        isDevicesLoading = false
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isDevicesLoading = false,
                        devicesError = error.message ?: "기기 목록을 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun requestDeviceLogout(device: Device) = intent {
        reduce { state.copy(deviceToLogout = device) }
    }

    private fun dismissDeviceLogoutDialog() = intent {
        reduce { state.copy(deviceToLogout = null) }
    }

    private fun confirmDeviceLogout() = intent {
        val device = state.deviceToLogout ?: return@intent
        reduce { state.copy(deviceToLogout = null, isLoggingOutDevice = true) }

        deviceRepository.logoutDevice(device.id)
            .onSuccess {
                reduce {
                    state.copy(
                        isLoggingOutDevice = false,
                        devices = state.devices.filterNot { it.id == device.id },
                        snackbarState = SnackbarState("해당 기기에서 로그아웃되었습니다")
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoggingOutDevice = false,
                        snackbarState = SnackbarState(error.message ?: "기기 로그아웃에 실패했습니다")
                    )
                }
            }
    }

    private fun backToRoot() = intent {
        reduce {
            state.copy(
                page = MyAccountPage.ROOT,
                currentPassword = "",
                newPassword = "",
                confirmPassword = "",
                isCurrentPasswordVisible = false,
                isNewPasswordVisible = false,
                isConfirmPasswordVisible = false,
                currentPasswordError = null,
                newPasswordError = null,
                confirmPasswordError = null
            )
        }
    }

    private fun onCurrentPasswordChanged(value: String) = blockingIntent {
        reduce { state.copy(currentPassword = value.stripPasswordWhitespace(), currentPasswordError = null) }
    }

    private fun onNewPasswordChanged(value: String) = blockingIntent {
        reduce {
            state.copy(
                newPassword = value.stripPasswordWhitespace(),
                newPasswordError = null,
                confirmPasswordError = null
            )
        }
    }

    private fun onConfirmPasswordChanged(value: String) = blockingIntent {
        reduce { state.copy(confirmPassword = value.stripPasswordWhitespace(), confirmPasswordError = null) }
    }

    private fun submitPasswordChange() = intent {
        var hasError = false

        if (state.currentPassword.isBlank()) {
            reduce { state.copy(currentPasswordError = AuthValidationMessages.CURRENT_PASSWORD_REQUIRED) }
            hasError = true
        }

        if (state.newPassword.isBlank()) {
            reduce { state.copy(newPasswordError = AuthValidationMessages.PASSWORD_REQUIRED) }
            hasError = true
        } else if (!state.newPassword.isValidPassword()) {
            reduce { state.copy(newPasswordError = AuthValidationMessages.PASSWORD_INVALID_SHORT) }
            hasError = true
        } else if (state.newPassword == state.currentPassword) {
            reduce { state.copy(newPasswordError = AuthValidationMessages.PASSWORD_SAME_AS_CURRENT) }
            hasError = true
        }

        if (state.confirmPassword.isBlank()) {
            reduce { state.copy(confirmPasswordError = AuthValidationMessages.PASSWORD_CONFIRM_REQUIRED) }
            hasError = true
        } else if (state.newPassword != state.confirmPassword) {
            reduce { state.copy(confirmPasswordError = AuthValidationMessages.PASSWORD_MISMATCH) }
            hasError = true
        }

        if (hasError) {
            return@intent
        }

        postSideEffect(MyAccountSideEffect.ClearFocus)
        reduce { state.copy(isSubmitting = true) }

        changePasswordUseCase(state.currentPassword, state.newPassword)
            .onSuccess {
                reduce {
                    state.copy(
                        isSubmitting = false,
                        page = MyAccountPage.ROOT,
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        isCurrentPasswordVisible = false,
                        isNewPasswordVisible = false,
                        isConfirmPasswordVisible = false,
                        snackbarState = SnackbarState("비밀번호가 변경되었습니다")
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isSubmitting = false,
                        snackbarState = SnackbarState(error.message ?: "비밀번호 변경에 실패했습니다")
                    )
                }
            }
    }

    private fun revokeCreator() = intent {
        reduce { state.copy(isRevokingCreator = true) }

        creatorRepository.revokeCreator()
            .onSuccess {
                reduce {
                    state.copy(
                        isRevokingCreator = false,
                        showRevokeCreatorDialog = false,
                        snackbarState = SnackbarState("크리에이터가 해제되었습니다")
                    )
                }
                loadAccountInfo()
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isRevokingCreator = false,
                        showRevokeCreatorDialog = false,
                        snackbarState = SnackbarState(error.message ?: "크리에이터 해제에 실패했습니다")
                    )
                }
            }
    }

    private fun deleteAccount() = intent {
        reduce { state.copy(isDeleting = true) }

        // 탈퇴 뒤엔 네이버 해제 불가라 먼저, 실패해도 진행
        disconnectNaver()
        deleteAccountUseCase()
            .onSuccess {
                reduce { state.copy(isDeleting = false, showDeleteDialog = false) }
                postSideEffect(MyAccountSideEffect.AccountDeleted)
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isDeleting = false,
                        showDeleteDialog = false,
                        snackbarState = SnackbarState(error.message ?: "회원 탈퇴에 실패했습니다")
                    )
                }
            }
    }
}
