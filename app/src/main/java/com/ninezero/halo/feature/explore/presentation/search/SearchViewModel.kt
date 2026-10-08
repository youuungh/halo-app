package com.ninezero.halo.feature.explore.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.explore.domain.usecase.ClearSearchHistoryUseCase
import com.ninezero.halo.feature.explore.domain.usecase.DeleteSearchHistoryUseCase
import com.ninezero.halo.feature.explore.domain.usecase.GetSearchHistoryUseCase
import com.ninezero.halo.feature.explore.domain.usecase.SearchCreatorsUseCase
import com.ninezero.halo.feature.feed.domain.delegate.PostMutationDelegate
import com.ninezero.halo.feature.feed.domain.event.FollowEvent
import com.ninezero.halo.feature.feed.domain.event.FollowEventBus
import com.ninezero.halo.feature.profile.domain.usecase.GetFollowRecommendationsUseCase
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
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCreatorsUseCase: SearchCreatorsUseCase,
    private val getRecommendsUseCase: GetFollowRecommendationsUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val deleteSearchHistoryUseCase: DeleteSearchHistoryUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase,
    private val postMutationDelegate: PostMutationDelegate,
    private val followEventBus: FollowEventBus
) : ViewModel(), OrbitContainerHost<SearchState, SearchState, SearchSideEffect> {

    override val container: OrbitContainer<SearchState, SearchState, SearchSideEffect> =
        orbitContainer(SearchState())

    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadRecommendations()
        setupDebouncedSearch()
        observeFollowEvents()
    }

    private fun observeFollowEvents() {
        followEventBus.events
            .onEach { event ->
                intent {
                    val newIsFollowing = event is FollowEvent.Followed
                    val targetId = event.userId
                    val updatedSearchResults = state.searchResults.map { creator ->
                        if (creator.id == targetId) creator.copy(isFollowing = newIsFollowing) else creator
                    }
                    val updatedRecommendations = if (newIsFollowing) {
                        state.recommendations.filter { it.id != targetId }
                    } else {
                        state.recommendations.map { creator ->
                            if (creator.id == targetId) creator.copy(isFollowing = false) else creator
                        }
                    }
                    if (
                        updatedSearchResults != state.searchResults ||
                        updatedRecommendations != state.recommendations
                    ) {
                        reduce {
                            state.copy(
                                searchResults = updatedSearchResults,
                                recommendations = updatedRecommendations
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun setupDebouncedSearch() {
        searchQueryFlow
            .debounce(300)
            .distinctUntilChanged()
            .filter { it.isValidSearchQuery() }
            .onEach { query -> search(query, saveHistory = false) }
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

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateSearchQuery -> updateSearchQuery(intent.query)
            is SearchIntent.Search -> search(intent.query, saveHistory = true)
            is SearchIntent.LoadMoreSearchResults -> loadMoreSearchResults()
            is SearchIntent.ClearSearch -> clearSearch()
            is SearchIntent.FocusSearchBar -> focusSearchBar()
            is SearchIntent.RefreshRecommendations -> refreshRecommendations()
            is SearchIntent.ToggleFollow -> toggleFollow(intent.userId)
            is SearchIntent.DeleteSearchHistoryItem -> deleteSearchHistoryItem(intent.id)
            is SearchIntent.ClearSearchHistory -> clearSearchHistory()
            is SearchIntent.ToggleEditHistory -> toggleEditHistory()
            is SearchIntent.SelectHistoryItem -> selectHistoryItem(intent.keyword)
            is SearchIntent.NavigateToProfile -> navigateToProfile(intent.userId)
            is SearchIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun updateSearchQuery(query: String) = blockingIntent {
        val isValid = query.isValidSearchQuery()
        reduce {
            state.copy(
                searchQuery = query,
                isSearchQueryValid = isValid,
                searchResults = if (query.isBlank()) emptyList() else state.searchResults,
                isSearching = if (query.isBlank()) false else state.isSearching
            )
        }
        searchQueryFlow.value = query
    }

    private fun search(query: String, saveHistory: Boolean) = intent {
        if (query.isBlank()) return@intent

        reduce {
            state.copy(
                searchQuery = query,
                isSearchQueryValid = query.isValidSearchQuery(),
                isSearching = true,
                searchResults = emptyList(),
                searchPage = 1
            )
        }

        searchCreatorsUseCase(query = query, page = 1, limit = 20, saveHistory = saveHistory)
            .onSuccess { result ->
                if (state.searchQuery != query) return@intent
                reduce {
                    state.copy(
                        isSearching = false,
                        searchResults = result.creators,
                        searchHasMore = result.hasMore,
                        searchPage = 1
                    )
                }

                if (saveHistory) {
                    loadSearchHistory()
                }
            }
            .onFailure { error ->
                if (state.searchQuery != query) return@intent
                // 결과 자리는 스켈레톤 그대로
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "검색 실패")) }
            }
    }

    private fun loadMoreSearchResults() = intent {
        if (state.isSearching || !state.searchHasMore || state.searchQuery.isBlank()) return@intent

        val nextPage = state.searchPage + 1
        val query = state.searchQuery
        reduce { state.copy(isSearching = true) }

        searchCreatorsUseCase(query = query, page = nextPage, limit = 20, saveHistory = false)
            .onSuccess { result ->
                if (state.searchQuery != query) return@intent
                reduce {
                    state.copy(
                        isSearching = false,
                        searchResults = (state.searchResults + result.creators).distinctBy { it.id },
                        searchHasMore = result.hasMore,
                        searchPage = nextPage
                    )
                }
            }
            .onFailure { error ->
                reduce { state.copy(isSearching = false, snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")) }
            }
    }

    private fun clearSearch() = intent {
        searchQueryFlow.value = ""  // 같은 검색어를 다시 쳐도 디바운스 검색이 다시 돌게
        reduce {
            state.copy(
                searchQuery = "",
                isSearchQueryValid = false,
                isSearching = false,
                searchResults = emptyList(),
                searchPage = 1,
                searchHasMore = true,
                isSearchFocused = false
            )
        }
    }

    private fun focusSearchBar() = intent {
        reduce { state.copy(isSearchFocused = true) }
        loadSearchHistory()
    }

    private fun loadRecommendations() = intent {
        reduce { state.copy(isRecommendationsLoading = true) }

        getRecommendsUseCase(limit = 20)
            .onSuccess { recommendations ->
                reduce {
                    state.copy(
                        isRecommendationsLoading = false,
                        recommendations = recommendations
                    )
                }
            }
            .onFailure {
                reduce { state.copy(isRecommendationsLoading = false) }
            }
    }

    private fun refreshRecommendations() = intent {
        reduce { state.copy(isRefreshing = true) }

        getRecommendsUseCase(limit = 20)
            .onSuccess { recommendations ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        recommendations = recommendations
                    )
                }
            }
            .onFailure {
                reduce { state.copy(isRefreshing = false) }
            }
    }

    private fun toggleFollow(userId: Int) = intent {
        val currentIsFollowing = state.recommendations.find { it.id == userId }?.isFollowing
            ?: state.searchResults.find { it.id == userId }?.isFollowing
            ?: return@intent
        val shouldFollow = !currentIsFollowing

        postMutationDelegate.toggleAuthorFollow(userId, shouldFollow)
            ?.onSuccess {
                if (shouldFollow) {
                    reduce { state.copy(snackbarState = SnackbarState("팔로우했습니다")) }
                }
            }
            ?.onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "팔로우 실패"))
                }
            }
    }

    private fun loadSearchHistory() = intent {
        reduce { state.copy(isHistoryLoading = true) }

        getSearchHistoryUseCase(page = 1, limit = 20)
            .onSuccess { history ->
                reduce {
                    state.copy(
                        isHistoryLoading = false,
                        searchHistory = history
                    )
                }
            }
            .onFailure {
                reduce { state.copy(isHistoryLoading = false) }
            }
    }

    private fun deleteSearchHistoryItem(id: Int) = intent {
        deleteSearchHistoryUseCase(id)
            .onSuccess {
                reduce {
                    state.copy(
                        searchHistory = state.searchHistory.filter { it.id != id }
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "삭제 실패"))
                }
            }
    }

    private fun clearSearchHistory() = intent {
        clearSearchHistoryUseCase()
            .onSuccess {
                reduce {
                    state.copy(
                        searchHistory = emptyList(),
                        isEditingHistory = false,
                        snackbarState = SnackbarState("검색 기록이 삭제되었습니다")
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "삭제 실패"))
                }
            }
    }

    private fun toggleEditHistory() = intent {
        reduce { state.copy(isEditingHistory = !state.isEditingHistory) }
    }

    private fun selectHistoryItem(keyword: String) = intent {
        reduce { state.copy(isEditingHistory = false) }
        search(keyword, saveHistory = true)
    }

    private fun navigateToProfile(userId: Int) = intent {
        postSideEffect(SearchSideEffect.NavigateToProfile(userId))

        val query = state.searchQuery
        if (query.isNotBlank() && state.isSearchQueryValid) {
            searchCreatorsUseCase(query = query, page = 1, limit = 1, saveHistory = true)
                .onSuccess { loadSearchHistory() }
        }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
