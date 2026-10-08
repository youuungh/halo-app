package com.ninezero.halo.feature.feed.domain.model

data class Comment(
    val id: Int,
    val postId: Int,
    val author: Author,
    val content: String,
    val mediaAttachments: List<MediaItem> = emptyList(),
    val parentCommentId: Int?,
    val replyCount: Int,
    val likeCount: Int,
    val isLiked: Boolean,
    val isBookmarked: Boolean = false,
    val isBlocked: Boolean = false,
    val isBlinded: Boolean = false,   // 신고 누적으로 가려짐, 본인에게만
    val isDeleted: Boolean = false,   // 작성자가 지웠지만 답글이 남아 자리만 표시
    val firstReply: Comment? = null,
    val replyPreviewUsers: List<UserPreview> = emptyList(),
    val createdAt: Long,
    val pendingId: Long? = null  // null이 아니면 업로드 중인 댓글
) {
    val isUploading: Boolean get() = pendingId != null
}

data class UserPreview(
    val id: Int,
    val avatarUrl: String?,
    val avatarThumbUrl: String? = null
)
