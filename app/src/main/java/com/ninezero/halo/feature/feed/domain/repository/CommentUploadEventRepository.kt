package com.ninezero.halo.feature.feed.domain.repository

import com.ninezero.halo.feature.feed.domain.model.CommentUploadEvent
import kotlinx.coroutines.flow.SharedFlow

interface CommentUploadEventRepository {
    val uploadEvents: SharedFlow<CommentUploadEvent>

    /** 활성 댓글 상세 화면 등록 */
    fun registerActiveCommentDetail(commentId: Int)
    /** 활성 댓글 상세 화면 해제 */
    fun unregisterActiveCommentDetail(commentId: Int)
    /** 댓글 상세 화면 활성 여부 */
    fun isCommentDetailActive(commentId: Int): Boolean
}
