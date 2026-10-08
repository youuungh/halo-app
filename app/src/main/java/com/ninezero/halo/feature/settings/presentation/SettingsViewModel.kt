package com.ninezero.halo.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.local.PreferencesManager
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.auth.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel(), OrbitContainerHost<SettingsState, SettingsState, SettingsSideEffect> {

    override val container: OrbitContainer<SettingsState, SettingsState, SettingsSideEffect> =
        orbitContainer(SettingsState())

    init {
        observeThemeMode()
    }

    private fun observeThemeMode() {
        viewModelScope.launch {
            preferencesManager.observeThemeMode().collect { mode ->
                intent { reduce { state.copy(themeMode = mode) } }
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.MyAccountClick -> showMyAccountSheet()
            is SettingsIntent.DismissMyAccountSheet -> dismissMyAccountSheet()
            is SettingsIntent.NotificationClick -> navigateToNotificationSettings()
            is SettingsIntent.SavedPostsClick -> navigateToSavedPosts()
            is SettingsIntent.LikedPostsClick -> navigateToLikedPosts()
            is SettingsIntent.ArchivedChatsClick -> navigateToArchivedChats()
            is SettingsIntent.BlockedProfilesClick -> navigateToBlockedProfiles()
            is SettingsIntent.PaymentMethodsClick -> navigateToPaymentMethods()
            is SettingsIntent.InfoClick -> navigateToInfo()
            is SettingsIntent.ThemeClick -> showThemeSheet()
            is SettingsIntent.ChangeTheme -> changeTheme(intent.mode)
            is SettingsIntent.DismissThemeSheet -> dismissThemeSheet()
            is SettingsIntent.LogoutClick -> showLogoutDialog()
            is SettingsIntent.ConfirmLogout -> logout()
            is SettingsIntent.DismissLogoutDialog -> dismissLogoutDialog()
            is SettingsIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun showMyAccountSheet() = intent {
        reduce { state.copy(showMyAccountSheet = true) }
    }

    private fun dismissMyAccountSheet() = intent {
        reduce { state.copy(showMyAccountSheet = false) }
    }

    private fun navigateToNotificationSettings() = intent {
        postSideEffect(SettingsSideEffect.NavigateToNotificationSettings)
    }

    private fun navigateToSavedPosts() = intent {
        postSideEffect(SettingsSideEffect.NavigateToSavedPosts)
    }

    private fun navigateToLikedPosts() = intent {
        postSideEffect(SettingsSideEffect.NavigateToLikedPosts)
    }

    private fun navigateToArchivedChats() = intent {
        postSideEffect(SettingsSideEffect.NavigateToArchivedChats)
    }

    private fun navigateToBlockedProfiles() = intent {
        postSideEffect(SettingsSideEffect.NavigateToBlockedProfiles)
    }

    private fun navigateToPaymentMethods() = intent {
        postSideEffect(SettingsSideEffect.NavigateToPaymentMethods)
    }

    private fun navigateToInfo() = intent {
        postSideEffect(SettingsSideEffect.NavigateToInfo)
    }

    private fun showThemeSheet() = intent {
        reduce { state.copy(showThemeSheet = true) }
    }

    private fun dismissThemeSheet() = intent {
        reduce { state.copy(showThemeSheet = false) }
    }

    private fun changeTheme(mode: Int) = intent {
        reduce { state.copy(showThemeSheet = false, themeMode = mode) }
        preferencesManager.setThemeMode(mode)
    }

    private fun showLogoutDialog() = intent {
        reduce { state.copy(showLogoutDialog = true) }
    }

    private fun dismissLogoutDialog() = intent {
        reduce { state.copy(showLogoutDialog = false) }
    }

    private fun logout() = intent {
        reduce { state.copy(isLoading = true, showLogoutDialog = false) }

        logoutUseCase()
            .onSuccess {
                postSideEffect(SettingsSideEffect.NavigateToLogin)
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isLoading = false,
                        snackbarState = SnackbarState(e.message ?: "로그아웃에 실패했습니다")
                    )
                }
            }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
