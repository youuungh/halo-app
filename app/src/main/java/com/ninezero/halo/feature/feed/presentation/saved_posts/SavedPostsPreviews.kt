package com.ninezero.halo.feature.feed.presentation.saved_posts

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.feed.presentation.preview.previewSavedItems

@HaloLightDarkPreview
@Composable
private fun SavedPostsScreenPreview() {
    SavedPostsPreviewContent(state = SavedPostsState(items = previewSavedItems))
}

@HaloLightDarkPreview
@Composable
private fun SavedPostsLoadingPreview() {
    SavedPostsPreviewContent(state = SavedPostsState(isLoading = true))
}

@HaloLightDarkPreview
@Composable
private fun SavedPostsEmptyPreview() {
    SavedPostsPreviewContent(state = SavedPostsState())
}

@HaloLightDarkPreview
@Composable
private fun SavedPostsErrorPreview() {
    SavedPostsPreviewContent(state = SavedPostsState(error = "네트워크 연결을 확인해주세요"))
}

@Composable
private fun SavedPostsPreviewContent(
    state: SavedPostsState
) {
    HaloScreenPreview {
        SavedPostsScreenContent(
            state = state,
            onNavigateBack = {},
            onIntent = {}
        )
    }
}
