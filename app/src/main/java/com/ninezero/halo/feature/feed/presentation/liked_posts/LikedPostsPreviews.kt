package com.ninezero.halo.feature.feed.presentation.liked_posts

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.feed.presentation.preview.previewLikedItems

@HaloLightDarkPreview
@Composable
private fun LikedPostsScreenPreview() {
    LikedPostsPreviewContent(state = LikedPostsState(items = previewLikedItems))
}

@HaloLightDarkPreview
@Composable
private fun LikedPostsLoadingPreview() {
    LikedPostsPreviewContent(state = LikedPostsState(isLoading = true))
}

@HaloLightDarkPreview
@Composable
private fun LikedPostsEmptyPreview() {
    LikedPostsPreviewContent(state = LikedPostsState())
}

@HaloLightDarkPreview
@Composable
private fun LikedPostsErrorPreview() {
    LikedPostsPreviewContent(state = LikedPostsState(error = "네트워크 연결을 확인해주세요"))
}

@Composable
private fun LikedPostsPreviewContent(
    state: LikedPostsState
) {
    HaloScreenPreview {
        LikedPostsScreenContent(
            state = state,
            onNavigateBack = {},
            onIntent = {}
        )
    }
}
