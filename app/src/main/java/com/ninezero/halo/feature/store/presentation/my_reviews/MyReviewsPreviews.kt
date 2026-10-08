package com.ninezero.halo.feature.store.presentation.my_reviews

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

@HaloLightDarkPreview
@Composable
private fun MyReviewsScreenPreview() {
    HaloScreenPreview {
        MyReviewsScreenContent(
            state = MyReviewsState(isLoading = false)
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyReviewsScreenLoadingPreview() {
    HaloScreenPreview {
        MyReviewsScreenContent(
            state = MyReviewsState(isLoading = true)
        )
    }
}
