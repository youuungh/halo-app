package com.ninezero.halo.feature.profile.domain.model

import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.Comment as FeedComment

/** 답글 탭 댓글을 피드 댓글로 변환 */
fun ThreadCommentData.toFeedComment(postId: Int, parentCommentId: Int?): FeedComment {
    return FeedComment(
        id = id,
        postId = postId,
        author = Author(
            id = author.id,
            username = author.username,
            displayName = author.displayName,
            avatarUrl = author.avatarUrl,
            avatarThumbUrl = author.avatarThumbUrl,
            role = author.role,
            isFollowing = author.isFollowing,
            isDeleted = author.isDeleted
        ),
        content = content,
        mediaAttachments = mediaAttachments,
        parentCommentId = parentCommentId,
        replyCount = replyCount,
        likeCount = likeCount,
        isLiked = isLiked,
        isBookmarked = isBookmarked,
        isBlocked = isBlocked,
        isBlinded = isBlinded,
        isDeleted = isDeleted,
        firstReply = null,
        replyPreviewUsers = emptyList(),
        createdAt = createdAt,
    )
}

fun CommentThreadData.toCacheableFeedComments(): List<FeedComment> {  // 맨 앞 부모는 자기 부모를 모름
    if (comments.size == 1) return listOf(comments.first().toFeedComment(post.id, parentCommentId = null))
    return comments.zipWithNext { parent, reply -> reply.toFeedComment(post.id, parentCommentId = parent.id) }
}

fun ThreadCommentData.mergeCached(cached: FeedComment?): ThreadCommentData {
    if (cached == null) return this
    return copy(
        content = cached.content,
        mediaAttachments = cached.mediaAttachments,
        author = author.copy(
            username = cached.author.username,
            displayName = cached.author.displayName,
            avatarUrl = cached.author.avatarUrl,
            isFollowing = cached.author.isFollowing
        ),
        createdAt = cached.createdAt,
        likeCount = cached.likeCount,
        replyCount = cached.replyCount,
        isLiked = cached.isLiked,
        isBookmarked = cached.isBookmarked,
        isBlocked = cached.isBlocked,
        isDeleted = cached.isDeleted,
        isBlinded = cached.isBlinded
    )
}
