package com.ninezero.halo.feature.feed.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.core.network.CursorDto
import com.ninezero.halo.feature.feed.data.remote.dto.FeedResponse
import com.ninezero.halo.feature.feed.data.remote.dto.PostDto
import retrofit2.Response
import retrofit2.http.*

interface FeedApi {
    /** 홈 피드 */
    @GET("api/v1/feeds/home")
    suspend fun getHomeFeed(
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int
    ): Response<ApiResponse<CursorDto<PostDto>>>

    /** 탐색 피드 (공개) */
    @GET("api/v1/feeds/explore")
    suspend fun getExploreFeed(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ApiResponse<FeedResponse>>

    /** 트렌딩 피드 (공개) */
    @GET("api/v1/feeds/trending")
    suspend fun getTrendingFeed(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ApiResponse<FeedResponse>>
}
