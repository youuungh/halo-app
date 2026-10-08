package com.ninezero.halo.feature.feed.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.core.network.dto.UploadPrepareDto
import com.ninezero.halo.feature.feed.data.remote.dto.MediaDraftDto
import com.ninezero.halo.feature.feed.data.remote.dto.VideoDraftRequest
import com.ninezero.halo.feature.feed.data.remote.dto.VideoUploadPrepareRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface MediaDraftApi {
    /** 초안 조회 */
    @GET("api/v1/media/drafts/{id}")
    suspend fun getDraft(
        @Path("id") draftId: Int
    ): Response<ApiResponse<MediaDraftDto>>

    /** 이미지 초안 생성 */
    @Multipart
    @POST("api/v1/media/drafts/images")
    suspend fun createImageDrafts(
        @Query("targetType") targetType: String,  // POST/COMMENT/PRODUCT
        @Part mediaFiles: List<MultipartBody.Part>
    ): Response<ApiResponse<List<MediaDraftDto>>>

    /** 초안 영상 업로드 준비 */
    @POST("api/v1/media/drafts/videos/uploads")
    suspend fun prepareVideoUpload(
        @Query("targetType") targetType: String,
        @Body request: VideoUploadPrepareRequest
    ): Response<ApiResponse<UploadPrepareDto>>

    /** 영상 초안 생성 */
    @POST("api/v1/media/drafts/videos")
    suspend fun createVideoDraft(
        @Query("targetType") targetType: String,
        @Body request: VideoDraftRequest
    ): Response<ApiResponse<MediaDraftDto>>
}
