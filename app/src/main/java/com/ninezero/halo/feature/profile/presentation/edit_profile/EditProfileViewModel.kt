package com.ninezero.halo.feature.profile.presentation.edit_profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.feature.profile.domain.model.UpdateProfileRequest
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.UpdateProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.UploadAvatarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val application: Application,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase
) : ViewModel(), OrbitContainerHost<EditProfileState, EditProfileState, EditProfileSideEffect> {

    override val container: OrbitContainer<EditProfileState, EditProfileState, EditProfileSideEffect> =
        orbitContainer(EditProfileState(isLoading = true))

    init {
        loadProfile()
    }

    fun onIntent(intent: EditProfileIntent) {
        when (intent) {
            is EditProfileIntent.LoadProfile -> loadProfile()
            is EditProfileIntent.DisplayNameChanged -> updateDisplayName(intent.displayName)
            is EditProfileIntent.BioChanged -> updateBio(intent.bio)
            is EditProfileIntent.LocationChanged -> updateLocation(intent.location)
            is EditProfileIntent.WebsiteChanged -> updateWebsite(intent.website)
            is EditProfileIntent.AvatarSelected -> updateAvatar(intent.uri)
            is EditProfileIntent.SaveProfile -> saveProfile()
            is EditProfileIntent.NavigateBack -> navigateBack()
            is EditProfileIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    private fun loadProfile() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        getMyProfileUseCase()
            .onSuccess { profile ->
                reduce {
                    state.copy(
                        displayName = profile.displayName,
                        bio = profile.bio ?: "",
                        avatarUrl = profile.avatarUrl,
                        location = profile.location ?: "",
                        website = profile.website ?: "",
                        isLoading = false,
                        originalDisplayName = profile.displayName,
                        originalBio = profile.bio ?: "",
                        originalLocation = profile.location ?: "",
                        originalWebsite = profile.website ?: ""
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "프로필을 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun updateDisplayName(displayName: String) = blockingIntent {
        if (displayName.length > 30) return@blockingIntent
        reduce {
            state.copy(
                displayName = displayName,
                displayNameError = null
            )
        }
    }

    private fun updateBio(bio: String) = blockingIntent {
        if (bio.length > 300) return@blockingIntent
        reduce {
            state.copy(
                bio = bio,
                bioError = null
            )
        }
    }

    private fun updateLocation(location: String) = blockingIntent {
        if (location.length > 100) return@blockingIntent
        reduce {
            state.copy(
                location = location,
                locationError = null
            )
        }
    }

    private fun updateWebsite(website: String) = blockingIntent {
        if (website.length > 200) return@blockingIntent
        reduce {
            state.copy(
                website = website,
                websiteError = null
            )
        }
    }

    private fun updateAvatar(uri: Uri) = intent {
        reduce { state.copy(selectedAvatarUri = uri) }
    }

    private fun saveProfile() = intent {
        var hasError = false

        if (state.displayName.isBlank()) {
            reduce { state.copy(displayNameError = "닉네임을 입력해주세요") }
            hasError = true
        }

        if (state.bio.length > 300) {
            reduce { state.copy(bioError = "소개는 300자 이하로 입력해주세요") }
            hasError = true
        }

        if (state.location.length > 100) {
            reduce { state.copy(locationError = "위치는 100자 이하로 입력해주세요") }
            hasError = true
        }

        if (state.website.isNotBlank() &&
            !state.website.startsWith("http://") &&
            !state.website.startsWith("https://")
        ) {
            reduce { state.copy(websiteError = "http:// 또는 https://로 시작해야 합니다") }
            hasError = true
        }

        if (hasError) return@intent

        reduce { state.copy(isSaving = true) }

        // 아바타 업로드
        var avatarUploadFailed = false
        state.selectedAvatarUri?.let { uri ->
            val part = MediaUtils.createUploadPart(application, uri, "avatar", "avatar")
            if (part != null) {
                uploadAvatarUseCase(part)
                    .onSuccess { profile ->
                        // 즉시 navigate back할 거라 state 정리는 불필요
                        reduce { state.copy(avatarUrl = profile.avatarUrl) }
                    }
                    .onFailure { error ->
                        avatarUploadFailed = true
                        reduce {
                            state.copy(
                                snackbarState = SnackbarState(
                                    message = error.message ?: "아바타 업로드 실패"
                                )
                            )
                        }
                    }
            } else {
                avatarUploadFailed = true
                reduce {
                    state.copy(
                        snackbarState = SnackbarState(message = "이미지를 읽을 수 없습니다")
                    )
                }
            }
        }

        // 아바타가 실패해도 저장, 빈 칸은 서버에서 지움
        val request = UpdateProfileRequest(
            displayName = state.displayName,
            bio = state.bio.trim(),
            location = state.location.trim(),
            website = state.website.trim()
        )

        updateProfileUseCase(request)
            .onSuccess {
                val message = if (avatarUploadFailed) {
                    "프로필이 저장되었습니다 (아바타 업로드 실패)"
                } else {
                    "프로필이 저장되었습니다"
                }
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditProfileSideEffect.ShowToast(message))
                postSideEffect(EditProfileSideEffect.NavigateBack)
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isSaving = false,
                        snackbarState = SnackbarState(
                            message = error.message ?: "프로필 저장 실패"
                        )
                    )
                }
            }
    }

    private fun navigateBack() = intent {
        postSideEffect(EditProfileSideEffect.NavigateBack)
    }
}
