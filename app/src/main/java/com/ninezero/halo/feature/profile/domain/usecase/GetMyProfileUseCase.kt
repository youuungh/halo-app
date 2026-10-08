package com.ninezero.halo.feature.profile.domain.usecase

import com.ninezero.halo.feature.profile.domain.model.UserProfile
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetMyProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Result<UserProfile> {
        return repository.getMyProfile()
    }
}
