package com.ninezero.halo.feature.explore.domain.usecase

import com.ninezero.halo.feature.explore.domain.model.Creator
import com.ninezero.halo.feature.explore.domain.repository.SearchRepository
import javax.inject.Inject

class GetPopularCreatorsUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(limit: Int = 10): Result<List<Creator>> {
        return repository.getPopularCreators(limit)
    }
}
