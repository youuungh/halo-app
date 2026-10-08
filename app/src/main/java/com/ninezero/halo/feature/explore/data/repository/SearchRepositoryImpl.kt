package com.ninezero.halo.feature.explore.data.repository

import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.feature.explore.data.remote.api.SearchApi
import com.ninezero.halo.feature.explore.data.remote.dto.toDomain
import com.ninezero.halo.feature.explore.domain.model.Banner
import com.ninezero.halo.feature.explore.domain.model.Creator
import com.ninezero.halo.feature.explore.domain.model.SearchCreatorsResult
import com.ninezero.halo.feature.explore.domain.model.SearchHistory
import com.ninezero.halo.feature.explore.domain.repository.SearchRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val api: SearchApi
) : SearchRepository {

    override suspend fun searchCreators(query: String, page: Int, limit: Int, saveHistory: Boolean): Result<SearchCreatorsResult> {
        return safeApiCall { api.searchCreators(query, page, limit, saveHistory) }
            .map { response ->
                SearchCreatorsResult(
                    creators = response.items.map { it.toDomain() },
                    hasMore = response.hasNext
                )
            }
    }

    override suspend fun getPopularCreators(limit: Int): Result<List<Creator>> {
        return safeApiCall { api.getPopularCreators(limit) }
            .map { response -> response.items.map { it.toDomain() } }
    }

    override suspend fun getSearchHistory(page: Int, limit: Int): Result<List<SearchHistory>> {
        return safeApiCall { api.getSearchHistory(page, limit) }
            .map { it.items.map { dto -> dto.toDomain() } }
    }

    override suspend fun deleteSearchHistory(id: Int): Result<Unit> {
        return safeApiCallUnit {
            api.deleteSearchHistory(id)
        }
    }

    override suspend fun clearSearchHistory(): Result<Unit> {
        return safeApiCallUnit {
            api.clearSearchHistory()
        }
    }

    override suspend fun getBanners(limit: Int?): Result<List<Banner>> {
        return safeApiCall { api.getBanners(limit) }
            .map { response -> response.banners.map { it.toDomain() } }
    }
}
