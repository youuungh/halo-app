package com.ninezero.halo.feature.explore.domain.usecase

import com.ninezero.halo.feature.explore.domain.model.SearchHistory
import com.ninezero.halo.feature.explore.domain.repository.SearchRepository
import javax.inject.Inject

class GetSearchHistoryUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20
    ): Result<List<SearchHistory>> {
        return searchRepository.getSearchHistory(page, limit)
    }
}
