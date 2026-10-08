package com.ninezero.halo.feature.feed.domain.model

sealed interface CommentUploadEvent {
    data class Success(
        val postId: Int,
        val parentCommentId: Int?,
        val comment: Comment
    ) : CommentUploadEvent

    data class Failure(
        val postId: Int,
        val parentCommentId: Int?,
        val errorMessage: String
    ) : CommentUploadEvent
}
