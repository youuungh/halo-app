package com.ninezero.halo.feature.store.presentation.create_review

import android.net.Uri
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

data class CreateReviewState(
    val productId: Int = 0,
    val orderId: Int = 0,
    val productName: String = "",
    val productImageUrl: String? = null,
    val rating: Int = 0,
    val content: String = "",
    val selectedMediaUris: List<Uri> = emptyList(),
    val isImagesLoading: Boolean = false,  // 수정 모드 기존 사진, 받기 전엔 사진 편집 막음
    val imagesError: String? = null,
    val isSubmitting: Boolean = false,
    val snackbarState: SnackbarState? = null,
    val isEditMode: Boolean = false,
    val editReviewId: Int? = null,
    val myAvatarUrl: String? = null,
    val myDisplayName: String = ""
) {
    val canEditImages: Boolean
        get() = !isImagesLoading && imagesError == null
}

sealed interface CreateReviewIntent {
    data class UpdateRating(val rating: Int) : CreateReviewIntent
    data class UpdateContent(val content: String) : CreateReviewIntent
    data class AddMedia(val uris: List<Uri>) : CreateReviewIntent
    data class RemoveMedia(val uri: Uri) : CreateReviewIntent
    data object RetryLoadImages : CreateReviewIntent
    data object Submit : CreateReviewIntent
    data object SnackbarDismissed : CreateReviewIntent
}

sealed interface CreateReviewSideEffect {
    data object ReviewSaved : CreateReviewSideEffect
}
