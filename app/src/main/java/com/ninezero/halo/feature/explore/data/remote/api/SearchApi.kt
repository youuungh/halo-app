package com.ninezero.halo.feature.explore.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.explore.data.remote.dto.BannerListResponseDto
import com.ninezero.halo.feature.explore.data.remote.dto.CreatorSearchListResponseDto
import com.ninezero.halo.feature.explore.data.remote.dto.SearchHistoryListResponseDto
import retrofit2.Response
import retrofit2.http.*

interface SearchApi {
    /** 크리에이터 검색 (공개) */
    @GET("api/v1/search/creators")
    suspend fun searchCreators(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("saveHistory") saveHistory: Boolean = true
    ): Response<ApiResponse<CreatorSearchListResponseDto>>

    /** 추천 크리에이터 조회 (공개) */
    @GET("api/v1/search/creators/popular")
    suspend fun getPopularCreators(
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<CreatorSearchListResponseDto>>

    /** 검색 기록 조회 */
    @GET("api/v1/search/history")
    suspend fun getSearchHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<SearchHistoryListResponseDto>>

    /** 검색 기록 삭제 */
    @DELETE("api/v1/search/history/{id}")
    suspend fun deleteSearchHistory(
        @Path("id") id: Int
    ): Response<Unit>

    /** 검색 기록 전체 삭제 */
    @DELETE("api/v1/search/history/clear")
    suspend fun clearSearchHistory(): Response<Unit>

    /** 배너 목록 조회 (공개) */
    @GET("api/v1/banners")
    suspend fun getBanners(
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<BannerListResponseDto>>
}
