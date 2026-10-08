package com.ninezero.halo.feature.explore.presentation.search

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.explore.domain.model.SearchHistory

internal fun previewSearchState(): SearchState {
    return SearchState(
        recommendations = listOf(
            previewUserSummary(
                id = 1,
                username = "creator1",
                displayName = "크리에이터1",
                role = "CREATOR",
                followerCount = 1234
            ),
            previewUserSummary(
                id = 2,
                username = "user2",
                displayName = "크리에이터2",
                followerCount = 567
            )
        )
    )
}

internal fun previewSearchFocusedState(): SearchState {
    return SearchState(
        isSearchFocused = true,
        searchHistory = listOf(
            SearchHistory(1, "creator"),
            SearchHistory(2, "user")
        )
    )
}

internal fun previewSearchEmptyState(): SearchState {
    return SearchState(
        isSearchFocused = true,
        searchQuery = "creator",
        isSearchQueryValid = true
    )
}

internal fun previewSearchLoadingState(): SearchState {
    return SearchState(
        isSearchFocused = true,
        searchQuery = "creator",
        isSearchQueryValid = true,
        isSearching = true
    )
}

@HaloLightDarkPreview
@Composable
private fun SearchScreenPreview() {
    HaloScreenPreview {
        SearchScreenContent(
            state = previewSearchState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SearchScreenFocusedPreview() {
    HaloScreenPreview {
        SearchScreenContent(
            state = previewSearchFocusedState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SearchScreenLoadingPreview() {
    HaloScreenPreview {
        SearchScreenContent(
            state = previewSearchLoadingState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SearchScreenEmptyPreview() {
    HaloScreenPreview {
        SearchScreenContent(
            state = previewSearchEmptyState(),
            onIntent = {}
        )
    }
}
