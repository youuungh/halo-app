package com.ninezero.halo.feature.profile.domain.usecase

import com.ninezero.halo.feature.profile.domain.model.UserProfile
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import okhttp3.MultipartBody
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(avatarPart: MultipartBody.Part): Result<UserProfile> {
        return repository.uploadAvatar(avatarPart)
    }
}
