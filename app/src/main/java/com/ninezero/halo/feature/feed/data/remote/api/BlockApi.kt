package com.ninezero.halo.feature.feed.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.feed.data.remote.dto.BlockRelationResponse
import com.ninezero.halo.feature.feed.data.remote.dto.BlockResponse
import com.ninezero.halo.feature.feed.data.remote.dto.BlockedUserListResponse
import retrofit2.Response
import retrofit2.http.*

interface BlockApi {
    /** 사용자 차단/해제 토글 */
    @POST("api/v1/users/block/{userId}")
    suspend fun toggleBlock(
        @Path("userId") userId: Int
    ): Response<ApiResponse<BlockResponse>>

    /** 양쪽 차단 관계 확인 */
    @GET("api/v1/users/block/{userId}/relation")
    suspend fun getBlockRelation(
        @Path("userId") userId: Int
    ): Response<ApiResponse<BlockRelationResponse>>

    /** 차단 사용자 목록 조회 */
    @GET("api/v1/users/block/my")
    suspend fun getMyBlockedUsers(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<BlockedUserListResponse>>
}
