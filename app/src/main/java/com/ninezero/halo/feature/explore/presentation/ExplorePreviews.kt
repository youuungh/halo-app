package com.ninezero.halo.feature.explore.presentation

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewAuthor
import com.ninezero.halo.core.ui.preview.previewPost
import com.ninezero.halo.feature.explore.domain.model.Banner

internal fun previewExploreState(): ExploreState {
    return ExploreState(
        banners = listOf(
            Banner(
                imageUrl = "https://example.com/banner1.jpg",
                title = "인기 크리에이터 특집",
                description = "이번 주 가장 핫한 크리에이터를 만나보세요",
                link = "/creators/popular"
            )
        ),
        trendingPosts = listOf(
            previewPost(
                id = 1,
                author = previewAuthor(1, "username1", "displayName1", "avatarUrl"),
                content = "내용내용내용내용내용내용내용",
                likeCount = 12,
                commentCount = 3,
                isLiked = true,
                createdAtOffset = 3_600_000L
            ),
            previewPost(
                id = 2,
                author = previewAuthor(2, "username2", "displayName2", "avatarUrl"),
                content = "내용내용내용내용내용내용내용내용내용내용내용",
                likeCount = 1234,
                commentCount = 56,
                isBookmarked = true,
                createdAtOffset = 86_400_000L
            ),
            previewPost(
                id = 3,
                author = previewAuthor(3, "username3", "displayName3", "avatarUrl"),
                content = "내용내용내용내용내용내용내용내용내용내용내용내용내용",
                likeCount = 123,
                commentCount = 45,
                isLiked = true,
                createdAtOffset = 5_400_000L
            )
        )
    )
}

internal fun previewExploreLoadingState(): ExploreState {
    return ExploreState(
        isBannersLoading = true,
        isTrendingLoading = true
    )
}

internal fun previewExploreEmptyState(): ExploreState {
    return ExploreState(trendingPosts = emptyList())
}

internal fun previewExploreErrorState(): ExploreState {
    return ExploreState(
        trendingError = "네트워크 연결을 확인해주세요",
        trendingPosts = emptyList()
    )
}

@HaloLightDarkPreview
@Composable
private fun ExploreScreenPreview() {
    HaloScreenPreview {
        ExploreScreenContent(
            state = previewExploreState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ExploreScreenLoadingPreview() {
    HaloScreenPreview {
        ExploreScreenContent(
            state = previewExploreLoadingState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ExploreScreenEmptyPreview() {
    HaloScreenPreview {
        ExploreScreenContent(
            state = previewExploreEmptyState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ExploreScreenErrorPreview() {
    HaloScreenPreview {
        ExploreScreenContent(
            state = previewExploreErrorState(),
            onIntent = {}
        )
    }
}
