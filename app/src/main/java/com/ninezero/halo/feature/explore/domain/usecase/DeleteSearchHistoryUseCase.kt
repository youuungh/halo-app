package com.ninezero.halo.feature.explore.domain.usecase

import com.ninezero.halo.feature.explore.domain.repository.SearchRepository
import javax.inject.Inject

class DeleteSearchHistoryUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(id: Int): Result<Unit> {
        return searchRepository.deleteSearchHistory(id)
    }
}
