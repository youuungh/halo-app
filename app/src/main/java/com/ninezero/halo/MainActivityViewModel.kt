package com.ninezero.halo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.datastore.PreferencesManager
import com.ninezero.halo.core.datastore.ThemeMode
import com.ninezero.halo.core.network.auth.SessionEventBus
import com.ninezero.halo.feature.auth.data.local.TokenManager
import com.ninezero.halo.feature.notification.domain.event.NotificationSyncBus
import com.ninezero.halo.feature.notification.domain.model.NotificationType
import com.ninezero.halo.feature.notification.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    preferencesManager: PreferencesManager,
    sessionEventBus: SessionEventBus,
    private val notificationRepository: NotificationRepository,
    private val notificationSyncBus: NotificationSyncBus
) : ViewModel() {

    // 첫 값을 읽기 전엔 null, 스플래시가 기다림
    val themeMode: StateFlow<ThemeMode?> = preferencesManager.observeThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val sessionExpired: SharedFlow<Unit> = sessionEventBus.sessionExpired

    val isLoggedIn: Boolean get() = tokenManager.loggedIn.value

    suspend fun hasToken(): Boolean = tokenManager.hasToken()

    fun markNotificationRead(notificationId: Int?, type: String?, targetId: Int?) {
        if (type == NotificationType.CHAT_MESSAGE) return  // 채팅은 알림 목록에서 제외
        if (notificationId == null && (type == null || targetId == null)) return

        viewModelScope.launch {
            if (!tokenManager.hasToken()) return@launch
            val result = when {
                notificationId != null -> notificationRepository.markAsRead(notificationId)
                type != null && targetId != null -> notificationRepository.markAsReadByTarget(type, targetId)
                else -> return@launch
            }
            result.onSuccess { notificationSyncBus.notifyChanged() }
        }
    }
}
