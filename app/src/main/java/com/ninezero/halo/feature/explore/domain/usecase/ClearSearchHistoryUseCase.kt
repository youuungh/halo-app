package com.ninezero.halo.feature.explore.domain.usecase

import com.ninezero.halo.feature.explore.domain.repository.SearchRepository
import javax.inject.Inject

class ClearSearchHistoryUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return searchRepository.clearSearchHistory()
    }
}
