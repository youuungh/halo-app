package com.ninezero.halo.feature.feed.domain.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.feature.feed.domain.model.LikeResult
import com.ninezero.halo.feature.feed.domain.model.LikedItem

interface LikeRepository {
    /** 포스트 좋아요 토글 */
    suspend fun togglePostLike(postId: Int): Result<LikeResult>

    /** 댓글 좋아요 토글 */
    suspend fun toggleCommentLike(commentId: Int): Result<LikeResult>

    /** 내가 좋아요한 항목 목록 조회 */
    suspend fun getMyLikedItems(
        targetType: String? = null,  // POST·COMMENT, null이면 전체
        cursor: String? = null,
        limit: Int = 20
    ): Result<CursorPage<LikedItem>>  // 서버가 비밀글 댓글을 거른 뒤라 개수로 끝을 판단하면 안 됨
}
