package com.ninezero.halo.feature.feed.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.feed.data.remote.dto.BookmarkListResponse
import com.ninezero.halo.feature.feed.data.remote.dto.BookmarkResponse
import retrofit2.Response
import retrofit2.http.*

interface BookmarkApi {
    /** 포스트 북마크 토글 */
    @POST("api/v1/bookmarks/posts/{postId}")
    suspend fun togglePostBookmark(
        @Path("postId") postId: Int
    ): Response<ApiResponse<BookmarkResponse>>

    /** 댓글 북마크 토글 */
    @POST("api/v1/bookmarks/comments/{commentId}")
    suspend fun toggleCommentBookmark(
        @Path("commentId") commentId: Int
    ): Response<ApiResponse<BookmarkResponse>>

    /** 내 북마크 목록 조회 */
    @GET("api/v1/bookmarks/my")
    suspend fun getMyBookmarks(
        @Query("targetType") targetType: String? = null,  // POST/COMMENT, null이면 전체
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<BookmarkListResponse>>
}
