package com.ninezero.halo.feature.profile.domain.usecase

import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.feature.profile.domain.repository.FollowRepository
import javax.inject.Inject

class GetFollowRecommendationsUseCase @Inject constructor(
    private val followRepository: FollowRepository
) {
    suspend operator fun invoke(limit: Int = 20): Result<List<UserSummary>> {
        return followRepository.getFollowRecommendations(limit = limit)
    }
}
