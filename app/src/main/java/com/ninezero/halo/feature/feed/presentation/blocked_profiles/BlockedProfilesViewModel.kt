package com.ninezero.halo.feature.feed.presentation.blocked_profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.feature.feed.domain.event.BlockEvent
import com.ninezero.halo.feature.feed.domain.event.BlockEventBus
import com.ninezero.halo.feature.feed.domain.repository.BlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class BlockedProfilesViewModel @Inject constructor(
    private val blockRepository: BlockRepository,
    private val blockEventBus: BlockEventBus
) : ViewModel(), OrbitContainerHost<BlockedProfilesState, BlockedProfilesState, BlockedProfilesSideEffect> {

    // init에서 즉시 로드하므로 초기 상태를 로딩으로 둠
    override val container: OrbitContainer<BlockedProfilesState, BlockedProfilesState, BlockedProfilesSideEffect> =
        orbitContainer(BlockedProfilesState(isLoading = true))

    private val listGeneration = ListGeneration()

    init {
        loadBlockedUsers()
        observeBlockEvents()
    }

    private fun observeBlockEvents() {
        blockEventBus.events
            .onEach { event ->
                when (event) {
                    is BlockEvent.Unblocked -> intent {
                        if (state.blockedUsers.any { it.id == event.userId }) {
                            reduce {
                                state.copy(blockedUsers = state.blockedUsers.filter { it.id != event.userId })
                            }
                        }
                    }

                    is BlockEvent.Blocked -> refreshBlockedUsers()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: BlockedProfilesIntent) {
        when (intent) {
            is BlockedProfilesIntent.LoadBlockedUsers -> loadBlockedUsers()
            is BlockedProfilesIntent.RefreshBlockedUsers -> refreshBlockedUsers()
            is BlockedProfilesIntent.LoadMoreBlockedUsers -> loadMoreBlockedUsers()
            is BlockedProfilesIntent.NavigateToUserProfile -> navigateToUserProfile(intent.userId)
            is BlockedProfilesIntent.UnblockClick -> showUnblockDialog(intent.user)
            is BlockedProfilesIntent.ConfirmUnblock -> confirmUnblock()
            is BlockedProfilesIntent.DismissUnblockDialog -> dismissUnblockDialog()
            is BlockedProfilesIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadBlockedUsers() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        blockRepository.getMyBlockedUsers(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    state.copy(
                        blockedUsers = result.items,
                        isLoading = false,
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null
                    )
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "차단 목록을 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun refreshBlockedUsers() = intent {
        reduce { state.copy(isRefreshing = true, error = null) }

        blockRepository.getMyBlockedUsers(cursor = null, limit = 20)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    state.copy(
                        blockedUsers = result.items,
                        isRefreshing = false,
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null
                    )
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        snackbarState = SnackbarState(message = e.message ?: "새로고침 실패")
                    )
                }
            }
    }

    private fun loadMoreBlockedUsers(): Job = intent {  // 자기를 다시 불러 타입 명시
        if (state.isLoading || state.isRefreshing || !state.hasMore) return@intent

        reduce { state.copy(isLoading = true) }

        val generation = listGeneration.current()
        blockRepository.getMyBlockedUsers(cursor = state.nextCursor, limit = 20)
            .onSuccess { result ->
                if (listGeneration.current() != generation) {
                    reduce { state.copy(isLoading = false) }
                    // 목록 수가 그대로면 트리거가 안 걸려 다시 요청
                    if (state.hasMore) loadMoreBlockedUsers()
                    return@intent
                }
                reduce {
                    state.copy(
                        blockedUsers = (state.blockedUsers + result.items).distinctBy { it.id },
                        isLoading = false,
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null
                    )
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isLoading = false,
                        snackbarState = SnackbarState(message = e.message ?: "더 불러오기 실패")
                    )
                }
            }
    }

    private fun showUnblockDialog(user: UserSummary) = intent {
        reduce { state.copy(showUnblockDialog = true, selectedUser = user) }
    }

    private fun confirmUnblock() = intent {
        val user = state.selectedUser ?: return@intent
        reduce { state.copy(showUnblockDialog = false) }

        blockRepository.toggleBlock(user.id)
            .onSuccess { isBlocked ->
                if (!isBlocked) {
                    reduce {
                        state.copy(
                            blockedUsers = state.blockedUsers.filter { it.id != user.id },
                            selectedUser = null,
                            snackbarState = SnackbarState(message = "차단이 해제되었습니다")
                        )
                    }
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        selectedUser = null,
                        snackbarState = SnackbarState(message = e.message ?: "차단 해제 실패")
                    )
                }
            }
    }

    private fun dismissUnblockDialog() = intent {
        reduce { state.copy(showUnblockDialog = false, selectedUser = null) }
    }

    private fun navigateToUserProfile(userId: Int) = intent {
        postSideEffect(BlockedProfilesSideEffect.NavigateToUserProfile(userId))
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
