package com.ninezero.halo.feature.chat.presentation.archived_chats

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.chat.domain.model.Chat
import com.ninezero.halo.feature.chat.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class ArchivedChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel(), OrbitContainerHost<ArchivedChatsState, ArchivedChatsState, ArchivedChatsSideEffect> {

    override val container: OrbitContainer<ArchivedChatsState, ArchivedChatsState, ArchivedChatsSideEffect> =
        orbitContainer(ArchivedChatsState(isLoading = true))

    init {
        loadArchivedChats()
    }

    fun onIntent(intent: ArchivedChatsIntent) {
        when (intent) {
            is ArchivedChatsIntent.LoadArchivedChats -> loadArchivedChats()
            is ArchivedChatsIntent.RefreshArchivedChats -> refreshArchivedChats()
            is ArchivedChatsIntent.LoadMoreArchivedChats -> loadMoreArchivedChats()
            is ArchivedChatsIntent.NavigateToChatRoom -> navigateToChatRoom(intent.roomId)
            is ArchivedChatsIntent.SnackbarDismissed -> dismissSnackbar()
            is ArchivedChatsIntent.SelectChatForUnarchive -> selectChatForUnarchive(intent.chat)
            is ArchivedChatsIntent.DismissUnarchiveSheet -> dismissUnarchiveSheet()
            is ArchivedChatsIntent.UnarchiveSelectedChat -> unarchiveSelectedChat()
            is ArchivedChatsIntent.DismissUnarchiveToast -> dismissUnarchiveToast()
            is ArchivedChatsIntent.OpenDeleteDialog -> openDeleteDialog()
            is ArchivedChatsIntent.DismissDeleteDialog -> dismissDeleteDialog()
            is ArchivedChatsIntent.DeleteSelectedChat -> deleteSelectedChat()
        }
    }

    private fun loadArchivedChats() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        chatRepository.getChatRooms(
            page = Constants.INITIAL_PAGE,
            limit = Constants.DEFAULT_PAGE_SIZE,
            includeArchived = true
        ).onSuccess { chatRooms ->
            val archivedOnly = chatRooms.filter { it.isArchived }
            reduce {
                state.copy(
                    isLoading = false,
                    archivedChats = archivedOnly,
                    currentPage = Constants.INITIAL_PAGE,
                    hasMore = chatRooms.size >= Constants.DEFAULT_PAGE_SIZE,
                    error = null
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isLoading = false,
                    error = error.message ?: "보관된 채팅을 불러올 수 없습니다"
                )
            }
        }
    }

    private fun refreshArchivedChats() = intent {
        reduce { state.copy(isRefreshing = true) }

        chatRepository.getChatRooms(
            page = Constants.INITIAL_PAGE,
            limit = Constants.DEFAULT_PAGE_SIZE,
            includeArchived = true
        ).onSuccess { chatRooms ->
            val archivedOnly = chatRooms.filter { it.isArchived }
            reduce {
                state.copy(
                    isRefreshing = false,
                    archivedChats = archivedOnly,
                    currentPage = Constants.INITIAL_PAGE,
                    hasMore = chatRooms.size >= Constants.DEFAULT_PAGE_SIZE,
                    error = null
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isRefreshing = false,
                    snackbarState = SnackbarState(error.message ?: "새로고침에 실패했습니다")
                )
            }
        }
    }

    private fun loadMoreArchivedChats() = intent {
        if (state.isLoading || !state.hasMore) return@intent

        val nextPage = state.currentPage + 1
        reduce { state.copy(isLoading = true) }

        chatRepository.getChatRooms(
            page = nextPage,
            limit = Constants.DEFAULT_PAGE_SIZE,
            includeArchived = true
        ).onSuccess { chatRooms ->
            val archivedOnly = chatRooms.filter { it.isArchived }
            reduce {
                state.copy(
                    isLoading = false,
                    archivedChats = (state.archivedChats + archivedOnly).distinctBy { it.id },
                    currentPage = nextPage,
                    hasMore = chatRooms.size >= Constants.DEFAULT_PAGE_SIZE
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isLoading = false,
                    snackbarState = SnackbarState(error.message ?: "더 불러오는데 실패했습니다")
                )
            }
        }
    }

    private fun navigateToChatRoom(roomId: Int) = intent {
        postSideEffect(ArchivedChatsSideEffect.NavigateToChatRoom(roomId))
    }

    private fun selectChatForUnarchive(chat: Chat) = intent {
        reduce { state.copy(selectedChat = chat) }
    }

    private fun dismissUnarchiveSheet() = intent {
        reduce { state.copy(selectedChat = null) }
    }

    private fun unarchiveSelectedChat() = intent {
        val chat = state.selectedChat ?: return@intent
        reduce { state.copy(selectedChat = null) }

        chatRepository.toggleArchive(chat.id, archived = false)
            .onSuccess {
                reduce {
                    state.copy(
                        archivedChats = state.archivedChats.filter { it.id != chat.id },
                        showUnarchiveToast = true,
                        unarchiveToastMessage = "${chat.otherUser.displayName ?: chat.otherUser.username}님과의 대화를 보관 해제했습니다"
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        snackbarState = SnackbarState(error.message ?: "보관 해제에 실패했습니다")
                    )
                }
            }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    private fun dismissUnarchiveToast() = intent {
        reduce { state.copy(showUnarchiveToast = false) }
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
            .onSuccess {
                reduce {
                    state.copy(archivedChats = state.archivedChats.filter { it.id != chat.id })
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "대화 삭제에 실패했습니다"))
                }
            }
    }
}
