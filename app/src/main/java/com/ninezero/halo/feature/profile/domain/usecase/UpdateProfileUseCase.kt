package com.ninezero.halo.feature.profile.domain.usecase

import com.ninezero.halo.feature.profile.domain.model.UpdateProfileRequest
import com.ninezero.halo.feature.profile.domain.model.UserProfile
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(request: UpdateProfileRequest): Result<UserProfile> {
        return repository.updateProfile(request)
    }
}
