package com.ninezero.halo.feature.feed.domain.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.feature.feed.domain.model.BookmarkItem

interface BookmarkRepository {
    /** 포스트 북마크 토글 */
    suspend fun togglePostBookmark(postId: Int): Result<Boolean>

    /** 댓글 북마크 토글 */
    suspend fun toggleCommentBookmark(commentId: Int): Result<Boolean>

    /** 내 북마크 목록 조회 */
    suspend fun getMyBookmarks(
        targetType: String? = null,  // POST·COMMENT, null이면 전체
        cursor: String? = null,
        limit: Int = 20
    ): Result<CursorPage<BookmarkItem>>  // 서버가 비밀글 댓글을 거른 뒤라 개수로 끝을 판단하면 안 됨
}
