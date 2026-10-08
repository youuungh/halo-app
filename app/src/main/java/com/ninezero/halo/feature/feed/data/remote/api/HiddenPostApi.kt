package com.ninezero.halo.feature.feed.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.feed.data.remote.dto.HiddenPostResponse
import retrofit2.Response
import retrofit2.http.*

interface HiddenPostApi {
    /** 포스트 숨김 */
    @POST("api/v1/posts/hidden/{postId}")
    suspend fun toggleHiddenPost(
        @Path("postId") postId: Int
    ): Response<ApiResponse<HiddenPostResponse>>
}
