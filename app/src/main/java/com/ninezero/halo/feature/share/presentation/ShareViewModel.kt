package com.ninezero.halo.feature.share.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.feature.profile.domain.repository.FollowRepository
import com.ninezero.halo.feature.share.domain.repository.ShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import java.util.UUID
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ShareViewModel @Inject constructor(
    private val followRepository: FollowRepository,
    private val shareRepository: ShareRepository
) : ViewModel(), OrbitContainerHost<ShareState, ShareState, ShareSideEffect> {

    override val container: OrbitContainer<ShareState, ShareState, ShareSideEffect> =
        orbitContainer(ShareState())

    private val searchQueryFlow = MutableStateFlow("")

    init {
        setupDebouncedSearch()
    }

    private fun setupDebouncedSearch() {
        searchQueryFlow
            .debounce(300)
            .distinctUntilChanged()
            .filter { it.isValidSearchQuery() }
            .onEach { query -> performSearch(query) }
            .launchIn(viewModelScope)
    }

    private fun String.isValidSearchQuery(): Boolean {
        val trimmed = trim()
        if (trimmed.isBlank()) return false
        return !trimmed.matches(KOREAN_JAMO_ONLY)
    }

    companion object {
        private val KOREAN_JAMO_ONLY = Regex("^[ㄱ-ㅎㅏ-ㅣ]+$")
    }

    fun onIntent(intent: ShareIntent) {
        when (intent) {
            is ShareIntent.LoadFollowing -> loadFollowing()
            is ShareIntent.SearchUsers -> searchUsers(intent.query)
            is ShareIntent.ToggleUser -> toggleUser(intent.userId)
            is ShareIntent.UpdateMessage -> updateMessage(intent.text)
            is ShareIntent.SharePost -> sharePost(intent.postId)
            is ShareIntent.ShareProduct -> shareProduct(intent.productId)
            is ShareIntent.Reset -> reset()
        }
    }

    private fun reset() = intent {
        searchQueryFlow.value = ""
        reduce { ShareState() }
    }

    private fun loadFollowing() = intent {
        reduce { state.copy(isLoading = true) }
        followRepository.getMyFollowingWithSearch(cursor = null, limit = 100, search = null)
            .onSuccess { result ->
                reduce {
                    state.copy(
                        isLoading = false,
                        followingUsers = result.items,
                        filteredUsers = result.items
                    )
                }
            }
            .onFailure {
                reduce { state.copy(isLoading = false) }
            }
    }

    private fun searchUsers(query: String) {
        blockingIntent {
            reduce { state.copy(searchQuery = query) }
            if (query.isBlank()) {
                reduce { state.copy(filteredUsers = state.followingUsers) }
            }
        }
        searchQueryFlow.value = query
    }

    private fun performSearch(query: String) = intent {
        reduce { state.copy(isLoading = true) }
        // 서버는 팔로잉에게만 보내므로 검색도 팔로잉 안에서
        followRepository.getMyFollowingWithSearch(cursor = null, limit = 30, search = query)
            .onSuccess { result ->
                // 검색어가 바뀌었으면 버림, 지웠으면 팔로잉 유지
                if (state.searchQuery != query) {
                    reduce { state.copy(isLoading = false) }
                    return@intent
                }
                reduce { state.copy(isLoading = false, filteredUsers = result.items) }
            }
            .onFailure {
                reduce { state.copy(isLoading = false) }
            }
    }

    private fun toggleUser(userId: Int) = intent {
        reduce {
            val newSelected = if (userId in state.selectedUserIds) {
                state.selectedUserIds - userId
            } else {
                state.selectedUserIds + userId
            }
            state.copy(selectedUserIds = newSelected)
        }
    }

    private fun updateMessage(text: String) = blockingIntent {
        reduce { state.copy(message = text) }
    }

    private fun sharePost(postId: Int) = intent {
        if (state.selectedUserIds.isEmpty()) return@intent
        if (state.isSending) return@intent  // 연타 가드

        // 카드 id는 대상별, 한 마디 id는 한 마디별로 유지
        // 한 마디는 서버처럼 앞뒤 공백을 뺀 값으로 비교
        val target = "post:$postId"
        val sameTarget = state.shareTempTarget == target
        val tempId = state.shareTempId?.takeIf { sameTarget } ?: UUID.randomUUID().toString()
        val message = state.message.trim().takeIf { it.isNotEmpty() }
        val messageTempId = message?.let {
            state.messageTempId?.takeIf { sameTarget && state.shareTempMessage == message } ?: UUID.randomUUID().toString()
        }
        reduce {
            state.copy(
                isSending = true,
                shareTempId = tempId,
                shareTempTarget = target,
                messageTempId = messageTempId ?: state.messageTempId.takeIf { sameTarget },
                shareTempMessage = message ?: state.shareTempMessage.takeIf { sameTarget }
            )
        }
        shareRepository.sharePost(
            postId = postId,
            recipientUserIds = state.selectedUserIds.toList(),
            message = message,
            clientTempId = tempId,
            messageTempId = messageTempId
        ).onSuccess { result ->
            reduce { state.copy(isSending = false) }
            postSideEffect(ShareSideEffect.ShareSuccess(result))
        }.onFailure { e ->
            reduce { state.copy(isSending = false) }
            postSideEffect(ShareSideEffect.ShareError(e.message ?: "공유에 실패했습니다"))
        }
    }

    private fun shareProduct(productId: Int) = intent {
        if (state.selectedUserIds.isEmpty()) return@intent
        if (state.isSending) return@intent  // 연타 가드

        // 카드 id는 대상별, 한 마디 id는 한 마디별로 유지
        // 한 마디는 서버처럼 앞뒤 공백을 뺀 값으로 비교
        val target = "product:$productId"
        val sameTarget = state.shareTempTarget == target
        val tempId = state.shareTempId?.takeIf { sameTarget } ?: UUID.randomUUID().toString()
        val message = state.message.trim().takeIf { it.isNotEmpty() }
        val messageTempId = message?.let {
            state.messageTempId?.takeIf { sameTarget && state.shareTempMessage == message } ?: UUID.randomUUID().toString()
        }
        reduce {
            state.copy(
                isSending = true,
                shareTempId = tempId,
                shareTempTarget = target,
                messageTempId = messageTempId ?: state.messageTempId.takeIf { sameTarget },
                shareTempMessage = message ?: state.shareTempMessage.takeIf { sameTarget }
            )
        }
        shareRepository.shareProduct(
            productId = productId,
            recipientUserIds = state.selectedUserIds.toList(),
            message = message,
            clientTempId = tempId,
            messageTempId = messageTempId
        ).onSuccess { result ->
            reduce { state.copy(isSending = false) }
            postSideEffect(ShareSideEffect.ShareSuccess(result))
        }.onFailure { e ->
            reduce { state.copy(isSending = false) }
            postSideEffect(ShareSideEffect.ShareError(e.message ?: "공유에 실패했습니다"))
        }
    }
}
