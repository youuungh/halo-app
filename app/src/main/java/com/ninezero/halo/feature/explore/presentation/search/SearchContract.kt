package com.ninezero.halo.feature.explore.presentation.search

import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.explore.domain.model.Creator
import com.ninezero.halo.feature.explore.domain.model.SearchHistory

data class SearchState(
    // 검색
    val searchQuery: String = "",
    val isSearchQueryValid: Boolean = false,
    val isSearchFocused: Boolean = false,
    val isSearching: Boolean = false,
    val searchResults: List<Creator> = emptyList(),
    val searchHasMore: Boolean = true,
    val searchPage: Int = 1,

    // 팔로우 추천 기본 화면
    val recommendations: List<UserSummary> = emptyList(),
    val isRecommendationsLoading: Boolean = false,
    val isRefreshing: Boolean = false,

    // 검색 기록
    val searchHistory: List<SearchHistory> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val isEditingHistory: Boolean = false,

    // 공통
    val snackbarState: SnackbarState? = null
)

sealed interface SearchIntent {
    // 검색
    data class UpdateSearchQuery(val query: String) : SearchIntent
    data class Search(val query: String) : SearchIntent
    data object LoadMoreSearchResults : SearchIntent
    data object ClearSearch : SearchIntent
    data object FocusSearchBar : SearchIntent

    // 팔로우 추천
    data object RefreshRecommendations : SearchIntent

    // 팔로우 토글
    data class ToggleFollow(val userId: Int) : SearchIntent

    // 검색 기록
    data class DeleteSearchHistoryItem(val id: Int) : SearchIntent
    data object ClearSearchHistory : SearchIntent
    data object ToggleEditHistory : SearchIntent
    data class SelectHistoryItem(val keyword: String) : SearchIntent

    // 네비게이션
    data class NavigateToProfile(val userId: Int) : SearchIntent

    data object SnackbarDismissed : SearchIntent
}

sealed interface SearchSideEffect {
    data class NavigateToProfile(val userId: Int) : SearchSideEffect
}
