package com.ninezero.halo.feature.chat.presentation.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.chat.domain.model.Chat
import com.ninezero.halo.feature.chat.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel(), OrbitContainerHost<ChatListState, ChatListState, ChatListSideEffect> {

    // init에서 즉시 로드하므로 초기 상태를 로딩으로 둠
    override val container: OrbitContainer<ChatListState, ChatListState, ChatListSideEffect> =
        orbitContainer(ChatListState(isLoading = true))

    init {
        observeChatRoomsFromDb()
        syncOnStartup()
    }

    private fun observeChatRoomsFromDb() {
        chatRepository.observeChatRooms()
            .onEach { rooms ->
                intent {
                    reduce {
                        state.copy(
                            chatRooms = rooms,
                            // 빈 캐시 첫 방출엔 동기화 끝까지 로딩 유지
                            isLoading = if (rooms.isEmpty()) state.isLoading else false,
                            error = null
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun syncOnStartup() = intent {
        reduce { state.copy(isLoading = state.chatRooms.isEmpty()) }
        chatRepository.syncChatRooms()
            .onSuccess { reduce { state.copy(isLoading = false) } }  // 방이 하나도 없으면 방출이 없음
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = if (state.chatRooms.isEmpty()) {
                            error.message ?: "채팅 목록을 불러올 수 없습니다"
                        } else null,
                        snackbarState = if (state.chatRooms.isNotEmpty()) {
                            SnackbarState(error.message ?: "새로고침 실패")
                        } else null
                    )
                }
            }
    }

    fun onIntent(intent: ChatListIntent) {
        when (intent) {
            is ChatListIntent.LoadChatRooms -> loadChatRooms()
            is ChatListIntent.RefreshChatRooms -> refreshChatRooms()
            is ChatListIntent.LoadMoreChatRooms -> loadMoreChatRooms()
            is ChatListIntent.NavigateToChatRoom -> navigateToChatRoom(intent.roomId)
            is ChatListIntent.SnackbarDismissed -> dismissSnackbar()
            is ChatListIntent.SelectChatForArchive -> selectChatForArchive(intent.chat)
            is ChatListIntent.DismissArchiveSheet -> dismissArchiveSheet()
            is ChatListIntent.ArchiveSelectedChat -> archiveSelectedChat()
            is ChatListIntent.DismissArchiveToast -> dismissArchiveToast()
            is ChatListIntent.OpenDeleteDialog -> openDeleteDialog()
            is ChatListIntent.DismissDeleteDialog -> dismissDeleteDialog()
            is ChatListIntent.DeleteSelectedChat -> deleteSelectedChat()
        }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    // 명시적 sync
    private fun loadChatRooms() = intent {
        chatRepository.syncChatRooms()
            .onFailure { error ->
                reduce {
                    state.copy(
                        error = if (state.chatRooms.isEmpty()) {
                            error.message ?: "채팅 목록을 불러올 수 없습니다"
                        } else null,
                        snackbarState = SnackbarState(error.message ?: "새로고침 실패")
                    )
                }
            }
    }

    private fun refreshChatRooms() = intent {
        reduce { state.copy(isRefreshing = true) }
        chatRepository.syncChatRooms()
            .onSuccess {
                reduce { state.copy(isRefreshing = false, error = null) }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        snackbarState = SnackbarState(error.message ?: "새로고침 실패")
                    )
                }
            }
    }

    private fun loadMoreChatRooms() = intent {
        // 페이지네이션 없음
    }

    private fun navigateToChatRoom(roomId: Int) = intent {
        postSideEffect(ChatListSideEffect.NavigateToChatRoom(roomId))
    }

    private fun selectChatForArchive(chat: Chat) = intent {
        reduce { state.copy(selectedChat = chat) }
    }

    private fun dismissArchiveSheet() = intent {
        reduce { state.copy(selectedChat = null) }
    }

    private fun archiveSelectedChat() = intent {
        val chat = state.selectedChat ?: return@intent
        reduce { state.copy(selectedChat = null) }

        chatRepository.toggleArchive(chat.id, archived = true)
            .onSuccess {
                reduce {
                    state.copy(
                        showArchiveToast = true,
                        archiveToastMessage = "${chat.otherUser.displayName ?: chat.otherUser.username}님과의 대화를 보관했습니다"
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        snackbarState = SnackbarState(error.message ?: "보관 처리에 실패했습니다")
                    )
                }
            }
    }

    private fun dismissArchiveToast() = intent {
        reduce { state.copy(showArchiveToast = false) }
    }

    private fun openDeleteDialog() = intent {
        if (state.selectedChat == null) return@intent
        reduce { state.copy(showDeleteDialog = true) }
    }

    private fun dismissDeleteDialog() = intent {
        reduce {
            state.copy(
                showDeleteDialog = false,
                selectedChat = null
            )
        }
    }

    private fun deleteSelectedChat() = intent {
        val chat = state.selectedChat ?: return@intent
        reduce {
            state.copy(
                showDeleteDialog = false,
                selectedChat = null
            )
        }

        chatRepository.clearChatRoom(chat.id)
            .onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "대화 삭제에 실패했습니다"))
                }
            }
    }
}
