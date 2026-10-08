package com.ninezero.halo.feature.explore.domain.usecase

import com.ninezero.halo.feature.explore.domain.model.Banner
import com.ninezero.halo.feature.explore.domain.repository.SearchRepository
import javax.inject.Inject

class GetBannersUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(limit: Int? = null): Result<List<Banner>> {
        return repository.getBanners(limit)
    }
}
