package com.ninezero.halo.feature.profile.domain.usecase

import android.net.Uri
import com.ninezero.halo.feature.profile.domain.model.UserProfile
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(avatarUri: Uri): Result<UserProfile> {
        return repository.uploadAvatar(avatarUri)
    }
}
