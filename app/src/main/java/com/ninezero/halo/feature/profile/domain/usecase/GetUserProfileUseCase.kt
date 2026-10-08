package com.ninezero.halo.feature.profile.domain.usecase

import com.ninezero.halo.feature.profile.domain.model.UserProfile
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: Int): Result<UserProfile> {
        if (userId <= 0) {
            return Result.failure(IllegalArgumentException("유효하지 않은 사용자 ID입니다"))
        }
        return repository.getUserProfile(userId)
    }
}
