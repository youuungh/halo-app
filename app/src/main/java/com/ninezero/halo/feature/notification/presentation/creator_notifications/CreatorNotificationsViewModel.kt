package com.ninezero.halo.feature.notification.presentation.creator_notifications

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.profile.domain.model.FollowedCreator
import com.ninezero.halo.feature.profile.domain.repository.FollowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

private const val PAGE_SIZE = 20

@HiltViewModel
class CreatorNotificationsViewModel @Inject constructor(
    private val followRepository: FollowRepository
) : ViewModel(), OrbitContainerHost<CreatorNotificationsState, CreatorNotificationsState, CreatorNotificationsSideEffect> {

    // 크리에이터별 토글 중복 요청 방지
    private val notifyToggleInProgress = mutableSetOf<Int>()

    override val container: OrbitContainer<CreatorNotificationsState, CreatorNotificationsState, CreatorNotificationsSideEffect> =
        orbitContainer(CreatorNotificationsState())

    init {
        loadCreators()
    }

    fun onIntent(intent: CreatorNotificationsIntent) {
        when (intent) {
            is CreatorNotificationsIntent.LoadMore -> loadMore()
            is CreatorNotificationsIntent.Retry -> loadCreators()
            is CreatorNotificationsIntent.ToggleNotify -> toggleNotify(intent.creator)
            is CreatorNotificationsIntent.NavigateBack -> navigateBack()
            is CreatorNotificationsIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadCreators() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        followRepository.getFollowedCreators(page = 1, limit = PAGE_SIZE)
            .onSuccess { result ->
                reduce {
                    state.copy(
                        creators = result.items,
                        isLoading = false,
                        page = 1,
                        hasMore = result.hasNext
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "크리에이터 목록을 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun loadMore() = intent {
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return@intent
        reduce { state.copy(isLoadingMore = true) }

        val nextPage = state.page + 1
        followRepository.getFollowedCreators(page = nextPage, limit = PAGE_SIZE)
            .onSuccess { result ->
                reduce {
                    state.copy(
                        creators = (state.creators + result.items).distinctBy { it.id },
                        isLoadingMore = false,
                        page = nextPage,
                        hasMore = result.hasNext
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoadingMore = false,
                        snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")
                    )
                }
            }
    }

    // 낙관적 갱신
    private fun toggleNotify(creator: FollowedCreator) = intent {
        if (!notifyToggleInProgress.add(creator.id)) return@intent

        try {
            val enabled = !creator.isNotifyEnabled
            reduce {
                state.copy(
                    creators = state.creators.map {
                        if (it.id == creator.id) it.copy(isNotifyEnabled = enabled) else it
                    }
                )
            }

            followRepository.setCreatorNotifications(creator.id, enabled)
                .onFailure { error ->
                    reduce {
                        state.copy(
                            creators = state.creators.map {
                                if (it.id == creator.id) it.copy(isNotifyEnabled = creator.isNotifyEnabled) else it
                            },
                            snackbarState = SnackbarState(error.message ?: "알림 설정 실패")
                        )
                    }
                }
        } finally {
            notifyToggleInProgress.remove(creator.id)
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(CreatorNotificationsSideEffect.NavigateBack)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
