package com.ninezero.halo.feature.tag.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.tag.data.remote.dto.TagDto
import com.ninezero.halo.feature.tag.data.remote.dto.TagListResponseDto
import com.ninezero.halo.feature.tag.data.remote.dto.TagRequest
import com.ninezero.halo.feature.tag.data.remote.dto.UpdateTagRequest
import retrofit2.Response
import retrofit2.http.*

interface TagApi {
    /** 크리에이터 태그 목록 조회 (공개) */
    @GET("api/v1/tags/creator/{creatorId}")
    suspend fun getCreatorTags(
        @Path("creatorId") creatorId: Int,
        @Query("targetType") targetType: String  // POST·PRODUCT
    ): Response<ApiResponse<TagListResponseDto>>

    /** 태그 생성 (Creator) */
    @POST("api/v1/tags")
    suspend fun createTag(
        @Body request: TagRequest
    ): Response<ApiResponse<TagDto>>

    /** 태그 수정 (Creator) */
    @PUT("api/v1/tags/{tagId}")
    suspend fun updateTag(
        @Path("tagId") tagId: Int,
        @Body request: UpdateTagRequest
    ): Response<ApiResponse<TagDto>>

    /** 태그 삭제 (Creator) */
    @DELETE("api/v1/tags/{tagId}")
    suspend fun deleteTag(
        @Path("tagId") tagId: Int
    ): Response<Unit>
}
