package com.ninezero.halo.feature.share.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.share.data.remote.dto.SharePostRequest
import com.ninezero.halo.feature.share.data.remote.dto.ShareProductRequest
import com.ninezero.halo.feature.share.data.remote.dto.ShareResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ShareApi {
    /** 포스트 공유 */
    @POST("api/v1/share/post")
    suspend fun sharePost(@Body request: SharePostRequest): Response<ApiResponse<ShareResponseDto>>

    /** 상품 공유 */
    @POST("api/v1/share/product")
    suspend fun shareProduct(@Body request: ShareProductRequest): Response<ApiResponse<ShareResponseDto>>
}
