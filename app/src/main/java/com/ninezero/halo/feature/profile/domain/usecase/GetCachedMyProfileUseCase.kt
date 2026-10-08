package com.ninezero.halo.feature.profile.domain.usecase

import com.ninezero.halo.feature.profile.domain.model.UserProfile
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetCachedMyProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): UserProfile? {
        return repository.getCachedMyProfile()
    }
}
