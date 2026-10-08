package com.ninezero.halo.feature.notification.presentation.notification_settings

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.notification.domain.model.UpdateNotificationPreference
import com.ninezero.halo.feature.notification.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

private const val ERROR_UPDATE_FAILED = "설정 변경에 실패했습니다"

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel(), OrbitContainerHost<NotificationSettingsState, NotificationSettingsState, NotificationSettingsSideEffect> {

    override val container: OrbitContainer<NotificationSettingsState, NotificationSettingsState, NotificationSettingsSideEffect> =
        orbitContainer(NotificationSettingsState())

    init {
        loadPreferences()
    }

    fun onIntent(intent: NotificationSettingsIntent) {
        when (intent) {
            is NotificationSettingsIntent.Retry -> loadPreferences()
            is NotificationSettingsIntent.ToggleAll -> toggleAll(intent.enabled)
            is NotificationSettingsIntent.ToggleSocial -> toggleCategory(socialEnabled = intent.enabled)
            is NotificationSettingsIntent.ToggleCreatorActivity -> toggleCategory(
                creatorActivityEnabled = intent.enabled
            )

            is NotificationSettingsIntent.ToggleCommerce -> toggleCategory(commerceEnabled = intent.enabled)
            is NotificationSettingsIntent.ToggleChat -> toggleCategory(chatEnabled = intent.enabled)
            is NotificationSettingsIntent.ToggleSystem -> toggleCategory(systemEnabled = intent.enabled)
            is NotificationSettingsIntent.OpenCreatorNotifications ->
                intent { postSideEffect(NotificationSettingsSideEffect.NavigateToCreatorNotifications) }

            is NotificationSettingsIntent.SnackbarDismissed -> dismissSnackbar()
            is NotificationSettingsIntent.NavigateBack -> intent {
                postSideEffect(
                    NotificationSettingsSideEffect.NavigateBack
                )
            }
        }
    }

    private fun toggleAll(enabled: Boolean) = intent {
        if (state.isLoading) return@intent

        val previousState = state

        if (enabled) {
            val shouldRestoreSavedState = previousState.savedSocialEnabled ||
                    previousState.savedCreatorActivityEnabled ||
                    previousState.savedCommerceEnabled ||
                    previousState.savedChatEnabled ||
                    previousState.savedSystemEnabled

            val restored = previousState.copy(
                masterEnabled = true,
                socialEnabled = if (shouldRestoreSavedState) previousState.savedSocialEnabled else true,
                creatorActivityEnabled = if (shouldRestoreSavedState) previousState.savedCreatorActivityEnabled else true,
                commerceEnabled = if (shouldRestoreSavedState) previousState.savedCommerceEnabled else true,
                chatEnabled = if (shouldRestoreSavedState) previousState.savedChatEnabled else true,
                systemEnabled = if (shouldRestoreSavedState) previousState.savedSystemEnabled else true
            )
            reduce { restored }

            repository.updatePreferences(
                UpdateNotificationPreference(
                    socialEnabled = restored.socialEnabled,
                    creatorActivityEnabled = restored.creatorActivityEnabled,
                    commerceEnabled = restored.commerceEnabled,
                    chatEnabled = restored.chatEnabled,
                    systemEnabled = restored.systemEnabled
                )
            ).onFailure {
                reduce { state.rolledBack(sent = restored, previous = previousState) }
            }
        } else {
            val cleared = previousState.copy(
                masterEnabled = false,
                savedSocialEnabled = previousState.socialEnabled,
                savedCreatorActivityEnabled = previousState.creatorActivityEnabled,
                savedCommerceEnabled = previousState.commerceEnabled,
                savedChatEnabled = previousState.chatEnabled,
                savedSystemEnabled = previousState.systemEnabled,
                socialEnabled = false,
                creatorActivityEnabled = false,
                commerceEnabled = false,
                chatEnabled = false,
                systemEnabled = false
            )
            reduce { cleared }

            repository.updatePreferences(
                UpdateNotificationPreference(
                    socialEnabled = false,
                    creatorActivityEnabled = false,
                    commerceEnabled = false,
                    chatEnabled = false,
                    systemEnabled = false
                )
            ).onFailure {
                reduce { state.rolledBack(sent = cleared, previous = previousState) }
            }
        }
    }

    // 실패 시 이 요청이 바꾼 칸만 되돌림
    private fun NotificationSettingsState.rolledBack(
        sent: NotificationSettingsState,
        previous: NotificationSettingsState
    ): NotificationSettingsState = copy(
        socialEnabled = if (socialEnabled == sent.socialEnabled) previous.socialEnabled else socialEnabled,
        creatorActivityEnabled = if (creatorActivityEnabled == sent.creatorActivityEnabled) previous.creatorActivityEnabled else creatorActivityEnabled,
        commerceEnabled = if (commerceEnabled == sent.commerceEnabled) previous.commerceEnabled else commerceEnabled,
        chatEnabled = if (chatEnabled == sent.chatEnabled) previous.chatEnabled else chatEnabled,
        systemEnabled = if (systemEnabled == sent.systemEnabled) previous.systemEnabled else systemEnabled,
        savedSocialEnabled = previous.savedSocialEnabled,
        savedCreatorActivityEnabled = previous.savedCreatorActivityEnabled,
        savedCommerceEnabled = previous.savedCommerceEnabled,
        savedChatEnabled = previous.savedChatEnabled,
        savedSystemEnabled = previous.savedSystemEnabled,
        snackbarState = SnackbarState(ERROR_UPDATE_FAILED)
    ).withMasterSynced()

    private fun loadPreferences() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        repository.getPreferences()
            .onSuccess { prefs ->
                val allOff = !prefs.socialEnabled && !prefs.creatorActivityEnabled &&
                        !prefs.commerceEnabled && !prefs.chatEnabled && !prefs.systemEnabled
                reduce {
                    state.copy(
                        isLoading = false,
                        masterEnabled = !allOff,
                        socialEnabled = prefs.socialEnabled,
                        creatorActivityEnabled = prefs.creatorActivityEnabled,
                        commerceEnabled = prefs.commerceEnabled,
                        chatEnabled = prefs.chatEnabled,
                        systemEnabled = prefs.systemEnabled,
                        savedSocialEnabled = prefs.socialEnabled,
                        savedCreatorActivityEnabled = prefs.creatorActivityEnabled,
                        savedCommerceEnabled = prefs.commerceEnabled,
                        savedChatEnabled = prefs.chatEnabled,
                        savedSystemEnabled = prefs.systemEnabled
                    )
                }
            }
            .onFailure {
                reduce { state.copy(isLoading = false, error = "설정을 불러올 수 없습니다") }
            }
    }

    private fun toggleCategory(
        socialEnabled: Boolean? = null,
        creatorActivityEnabled: Boolean? = null,
        commerceEnabled: Boolean? = null,
        chatEnabled: Boolean? = null,
        systemEnabled: Boolean? = null
    ) = intent {
        if (state.isLoading) return@intent

        val previousState = state

        // 바꾼 칸만 반영·롤백해 응답 전 다른 토글을 덮지 않음
        reduce {
            state.copy(
                socialEnabled = socialEnabled ?: state.socialEnabled,
                creatorActivityEnabled = creatorActivityEnabled ?: state.creatorActivityEnabled,
                commerceEnabled = commerceEnabled ?: state.commerceEnabled,
                chatEnabled = chatEnabled ?: state.chatEnabled,
                systemEnabled = systemEnabled ?: state.systemEnabled
            ).withMasterSynced()
        }

        repository.updatePreferences(
            UpdateNotificationPreference(
                socialEnabled = socialEnabled,
                creatorActivityEnabled = creatorActivityEnabled,
                commerceEnabled = commerceEnabled,
                chatEnabled = chatEnabled,
                systemEnabled = systemEnabled
            )
        ).onFailure {
            reduce {
                state.copy(
                    socialEnabled = if (socialEnabled != null) previousState.socialEnabled else state.socialEnabled,
                    creatorActivityEnabled = if (creatorActivityEnabled != null) previousState.creatorActivityEnabled else state.creatorActivityEnabled,
                    commerceEnabled = if (commerceEnabled != null) previousState.commerceEnabled else state.commerceEnabled,
                    chatEnabled = if (chatEnabled != null) previousState.chatEnabled else state.chatEnabled,
                    systemEnabled = if (systemEnabled != null) previousState.systemEnabled else state.systemEnabled,
                    snackbarState = SnackbarState(ERROR_UPDATE_FAILED)
                ).withMasterSynced()
            }
        }
    }

    private fun NotificationSettingsState.withMasterSynced(): NotificationSettingsState =
        copy(masterEnabled = socialEnabled || creatorActivityEnabled || commerceEnabled || chatEnabled || systemEnabled)

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
