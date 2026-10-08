package com.ninezero.halo.feature.explore.domain.repository

import com.ninezero.halo.feature.explore.domain.model.Banner
import com.ninezero.halo.feature.explore.domain.model.Creator
import com.ninezero.halo.feature.explore.domain.model.SearchCreatorsResult
import com.ninezero.halo.feature.explore.domain.model.SearchHistory

interface SearchRepository {
    /** 크리에이터 검색 */
    suspend fun searchCreators(
        query: String,
        page: Int,
        limit: Int,
        saveHistory: Boolean = true
    ): Result<SearchCreatorsResult>

    /** 추천 크리에이터 조회 */
    suspend fun getPopularCreators(limit: Int): Result<List<Creator>>

    /** 검색 기록 조회 */
    suspend fun getSearchHistory(page: Int, limit: Int): Result<List<SearchHistory>>

    /** 검색 기록 삭제 */
    suspend fun deleteSearchHistory(id: Int): Result<Unit>

    /** 검색 기록 전체 삭제 */
    suspend fun clearSearchHistory(): Result<Unit>

    /** 배너 목록 조회 */
    suspend fun getBanners(limit: Int? = null): Result<List<Banner>>
}
