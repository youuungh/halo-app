package com.ninezero.halo.feature.feed.domain.repository

import com.ninezero.halo.feature.feed.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface PendingCommentRepository {
    /** 업로드 중 댓글 추가 */
    suspend fun addPendingComment(
        postId: Int,
        content: String,
        parentCommentId: Int?,
        authorId: Int,
        authorUsername: String,
        authorDisplayName: String?,
        authorAvatarUrl: String?,
        mediaUris: List<String>? = null
    ): Long

    /** 포스트의 업로드 중 댓글 관찰 */
    fun getPendingCommentsByPostId(postId: Int): Flow<List<Comment>>

    /** 부모별 업로드 중 답글 관찰 */
    fun getPendingCommentsByParentId(parentCommentId: Int): Flow<List<Comment>>
}
