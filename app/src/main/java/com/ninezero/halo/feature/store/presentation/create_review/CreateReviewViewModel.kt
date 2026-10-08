package com.ninezero.halo.feature.store.presentation.create_review

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.feature.profile.domain.usecase.GetCachedMyProfileUseCase
import com.ninezero.halo.feature.store.domain.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class CreateReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reviewRepository: ReviewRepository,
    private val getCachedMyProfileUseCase: GetCachedMyProfileUseCase,
    @param:ApplicationContext private val context: Context
) : ViewModel(), OrbitContainerHost<CreateReviewState, CreateReviewState, CreateReviewSideEffect> {

    private val productId: Int = savedStateHandle.get<Int>("productId") ?: 0
    private val orderId: Int = savedStateHandle.get<Int>("orderId") ?: 0
    private val productName: String = savedStateHandle.get<String>("productName") ?: ""
    private val productImageUrl: String? = savedStateHandle.get<String>("productImageUrl")

    // 수정 모드
    private val reviewId: Int? = savedStateHandle.get<Int>("reviewId")
    private val initialRating: Int = savedStateHandle.get<Int>("rating") ?: 0
    private val initialContent: String = savedStateHandle.get<String>("content") ?: ""

    private val myProfile = getCachedMyProfileUseCase()

    override val container: OrbitContainer<CreateReviewState, CreateReviewState, CreateReviewSideEffect> = orbitContainer(
        CreateReviewState(
            productId = productId,
            orderId = orderId,
            productName = productName,
            productImageUrl = productImageUrl,
            isEditMode = reviewId != null,
            isImagesLoading = reviewId != null,
            editReviewId = reviewId,
            rating = if (reviewId != null) initialRating else 0,
            content = if (reviewId != null) initialContent else "",
            myAvatarUrl = myProfile?.avatarUrl,
            myDisplayName = myProfile?.displayName?.takeIf { it.isNotBlank() } ?: myProfile?.username ?: ""
        )
    )

    private var originalImageUrls: List<String>? = null

    init {
        if (reviewId != null) {
            loadExistingImages(reviewId)
        }
    }

    private fun loadExistingImages(reviewId: Int) = intent {
        reduce { state.copy(isImagesLoading = true, imagesError = null) }
        reviewRepository.getReviewById(reviewId)
            .onSuccess { review ->
                reduce { state.copy(isImagesLoading = false, selectedMediaUris = review.images.map(Uri::parse)) }
                originalImageUrls = review.images  // 목록 반영 뒤에 둬야 저장이 기존 사진을 지우지 않음
            }
            .onFailure {
                reduce { state.copy(isImagesLoading = false, imagesError = "기존 사진을 불러오지 못했습니다") }
            }
    }

    private fun retryLoadImages() = intent {
        val reviewId = reviewId ?: return@intent
        if (state.isImagesLoading) return@intent
        loadExistingImages(reviewId)
    }

    private fun Uri.isRemote(): Boolean = scheme == "http" || scheme == "https"  // content·file URI와 구분

    fun onIntent(intent: CreateReviewIntent) {
        when (intent) {
            is CreateReviewIntent.UpdateRating -> updateRating(intent.rating)
            is CreateReviewIntent.UpdateContent -> updateContent(intent.content)
            is CreateReviewIntent.AddMedia -> addMedia(intent.uris)
            is CreateReviewIntent.RemoveMedia -> removeMedia(intent.uri)
            is CreateReviewIntent.RetryLoadImages -> retryLoadImages()
            is CreateReviewIntent.Submit -> submit()
            is CreateReviewIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun updateRating(rating: Int) = intent {
        reduce { state.copy(rating = rating) }
    }

    private fun updateContent(content: String) = blockingIntent {
        reduce { state.copy(content = content) }
    }

    private fun addMedia(uris: List<Uri>) = intent {
        if (!state.canEditImages) return@intent
        val currentUris = state.selectedMediaUris
        val availableSlots = MAX_REVIEW_IMAGES - currentUris.size

        if (availableSlots <= 0) {
            reduce {
                state.copy(snackbarState = SnackbarState("이미지는 최대 ${MAX_REVIEW_IMAGES}개까지 첨부 가능합니다"))
            }
            return@intent
        }

        val validUris = uris.take(availableSlots).filter { uri ->
            val fileInfo = MediaUtils.getFileInfo(context, uri) ?: return@filter false

            if (fileInfo.fileSize > MAX_IMAGE_SIZE) {
                reduce {
                    state.copy(snackbarState = SnackbarState("이미지는 최대 10MB까지 업로드 가능합니다"))
                }
                return@filter false
            }
            true
        }

        if (validUris.isEmpty()) return@intent

        // 검사하는 사이 지운 항목이 되살아나지 않게 최신 목록에 붙임
        reduce {
            val newUris = (state.selectedMediaUris + validUris).distinct()
            if (newUris.size > MAX_REVIEW_IMAGES) {
                state.copy(
                    selectedMediaUris = newUris.take(MAX_REVIEW_IMAGES),
                    snackbarState = SnackbarState("이미지는 최대 ${MAX_REVIEW_IMAGES}개까지 첨부 가능합니다")
                )
            } else {
                state.copy(selectedMediaUris = newUris)
            }
        }
    }

    private fun removeMedia(uri: Uri) = intent {
        if (!state.canEditImages) return@intent
        reduce { state.copy(selectedMediaUris = state.selectedMediaUris - uri) }
    }

    private fun submit() = intent {
        if (state.rating == 0) {
            reduce { state.copy(snackbarState = SnackbarState("별점을 선택해주세요")) }
            return@intent
        }
        if (state.content.isBlank()) {
            reduce { state.copy(snackbarState = SnackbarState("리뷰 내용을 입력해주세요")) }
            return@intent
        }

        reduce { state.copy(isSubmitting = true) }

        if (state.isEditMode && state.editReviewId != null) {
            submitUpdate()
        } else {
            submitCreate()
        }
    }

    private fun submitCreate() = intent {
        val trimmedContent = state.content.trim()

        // 목록은 한 번만 읽음, 변환 중 바뀌면 개수 비교가 틀어짐
        val uris = state.selectedMediaUris
        val parts = uris.mapIndexedNotNull { index, uri ->
            MediaUtils.createUploadPart(context, uri, "images", "image_$index")
        }

        if (parts.size != uris.size) {
            reduce {
                state.copy(
                    isSubmitting = false,
                    snackbarState = SnackbarState("이미지를 처리하지 못했습니다")
                )
            }
            return@intent
        }

        reviewRepository.createReviewWithImages(
            productId = state.productId,
            orderId = state.orderId,
            rating = state.rating,
            content = trimmedContent,
            images = parts
        ).onSuccess {
            reduce { state.copy(isSubmitting = false) }
            postSideEffect(CreateReviewSideEffect.ReviewSaved)
        }.onFailure { error ->
            reduce {
                state.copy(
                    isSubmitting = false,
                    snackbarState = SnackbarState(error.message ?: "리뷰 등록에 실패했습니다")
                )
            }
        }
    }

    private fun submitUpdate() = intent {
        val reviewId = state.editReviewId ?: return@intent
        val trimmedContent = state.content.trim()

        // 원격 URL = 남길 것, 로컬 URI = 새로 올릴 것
        val current = state.selectedMediaUris
        val keepImages = originalImageUrls?.let { current.filter { uri -> uri.isRemote() }.map { it.toString() } }
        val addedUris = current.filterNot { it.isRemote() }
        val newParts = addedUris.mapIndexedNotNull { index, uri -> MediaUtils.createUploadPart(context, uri, "images", "image_$index") }

        if (newParts.size != addedUris.size) {
            reduce {
                state.copy(
                    isSubmitting = false,
                    snackbarState = SnackbarState("이미지를 처리하지 못했습니다")
                )
            }
            return@intent
        }

        reviewRepository.updateReviewWithImages(
            reviewId = reviewId,
            rating = state.rating,
            content = trimmedContent,
            keepImages = keepImages,
            newImages = newParts
        ).onSuccess {
            reduce { state.copy(isSubmitting = false) }
            postSideEffect(CreateReviewSideEffect.ReviewSaved)
        }.onFailure { error ->
            reduce {
                state.copy(
                    isSubmitting = false,
                    snackbarState = SnackbarState(error.message ?: "리뷰 수정에 실패했습니다")
                )
            }
        }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    companion object {
        private const val MAX_REVIEW_IMAGES = 5
        private const val MAX_IMAGE_SIZE = 10 * 1024 * 1024L  // 10MB
    }
}
