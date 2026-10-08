package com.ninezero.halo.feature.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.chat.domain.repository.ChatRepository
import com.ninezero.halo.feature.feed.domain.model.PostUploadEvent
import com.ninezero.halo.feature.feed.domain.repository.PostUploadEventRepository
import com.ninezero.halo.feature.notification.domain.event.NotificationSyncBus
import com.ninezero.halo.feature.notification.domain.model.NotificationEvent
import com.ninezero.halo.feature.notification.domain.repository.NotificationEventRepository
import com.ninezero.halo.feature.notification.domain.repository.NotificationRepository
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val postUploadEventRepository: PostUploadEventRepository,
    private val notificationEventRepository: NotificationEventRepository,
    private val notificationRepository: NotificationRepository,
    private val notificationSyncBus: NotificationSyncBus,
    private val chatRepository: ChatRepository
) : ViewModel(), OrbitContainerHost<MainState, MainState, MainSideEffect> {

    override val container: OrbitContainer<MainState, MainState, MainSideEffect> =
        orbitContainer(MainState())

    init {
        preloadMyProfile()
        observePostUploadEvents()
        observeNotificationEvents()
        observeNotificationSync()
        refreshUnreadCounts()
        observeChatUnread()
    }

    private fun observePostUploadEvents() {
        postUploadEventRepository.uploadEvents
            .onEach { event ->
                when (event) {
                    is PostUploadEvent.Success -> if (event.creatorId == null) {
                        intent {
                            reduce { state.copy(snackbarState = SnackbarState("게시됨")) }
                        }
                    }
                    is PostUploadEvent.Failure -> if (event.creatorId == null) {
                        intent {
                            reduce { state.copy(snackbarState = SnackbarState("게시 실패")) }
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.SelectTab -> selectTab(intent.tab)
            is MainIntent.RefreshUnreadCounts -> refreshUnreadCounts()
            is MainIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun selectTab(tab: MainTab) = intent {
        if (tab == MainTab.CREATE) {
            postSideEffect(MainSideEffect.NavigateToCreatePost)
        } else {
            reduce { state.copy(selectedTab = tab) }
        }
    }

    // 연결 수명은 HaloApplication 소유
    private fun observeNotificationEvents() {
        notificationEventRepository.events
            .onEach { event ->
                when (event) {
                    is NotificationEvent.NewNotification -> {
                        intent {
                            reduce { state.copy(hasUnreadNotification = true) }
                        }
                    }
                    NotificationEvent.RefreshRequired -> refreshUnreadCounts()
                    is NotificationEvent.Connected -> {
                        Timber.d("알림 WebSocket 연결됨")

                        refreshUnreadCounts()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeNotificationSync() {
        notificationSyncBus.events
            .onEach {
                refreshUnreadCounts()
            }
            .launchIn(viewModelScope)
    }

    private fun refreshUnreadCounts() = intent {
        notificationRepository.getUnreadCount()
            .onSuccess { count ->
                reduce { state.copy(hasUnreadNotification = count > 0) }
            }
    }

    // 서버 동기화 후 로컬 SSOT로 배지 갱신
    private fun observeChatUnread() {
        viewModelScope.launch { chatRepository.syncChatRooms() }
        chatRepository.observeChatRooms()
            .onEach { rooms ->
                val hasUnread = rooms.any { it.unreadCount > 0 }
                intent { reduce { state.copy(hasUnreadMessage = hasUnread) } }
            }
            .launchIn(viewModelScope)
    }

    private fun preloadMyProfile() = intent {
        getMyProfileUseCase()
            .onSuccess { profile ->
                reduce { state.copy(currentUserId = profile.userId) }
            }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
