package com.ninezero.halo.feature.explore.domain.usecase

import com.ninezero.halo.feature.explore.domain.model.SearchCreatorsResult
import com.ninezero.halo.feature.explore.domain.repository.SearchRepository
import javax.inject.Inject

class SearchCreatorsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(
        query: String,
        page: Int = 1,
        limit: Int = 20,
        saveHistory: Boolean = true
    ): Result<SearchCreatorsResult> {
        return searchRepository.searchCreators(query, page, limit, saveHistory)
    }
}
