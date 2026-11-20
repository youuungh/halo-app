package com.ninezero.halo.feature.profile.domain.model

import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier

data class ThreadUser(
    val id: Int,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val avatarThumbUrl: String? = null,
    val role: String = "USER",
    val isFollowing: Boolean = false,
    val isDeleted: Boolean = false  // 탈퇴한 사용자, 남은 글·댓글의 작성자
)

data class ThreadPost(
    val id: Int,
    val excerpt: String,
    val mediaAttachments: List<MediaItem> = emptyList(),
    val author: ThreadUser,
    val createdAt: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val isDeleted: Boolean = false,
    val isHidden: Boolean = false,  // 크리에이터 해제로 숨김, isDeleted와 구분
    val requiredTier: SubscriptionPlanTier = SubscriptionPlanTier.FREE,
    val isSecret: Boolean = false,
    val canAccess: Boolean = true
)

data class ThreadComment(
    val id: Int,
    val content: String,
    val mediaAttachments: List<MediaItem> = emptyList(),
    val author: ThreadUser,
    val replyTo: ThreadUser?,
    val createdAt: Long,
    val likeCount: Int,
    val replyCount: Int,
    val isLiked: Boolean,
    val isBookmarked: Boolean = false,
    val isProfileUser: Boolean,
    val isBlocked: Boolean = false,
    val isDeleted: Boolean = false,
    val isBlinded: Boolean = false
)

data class CommentThread(
    val comments: List<ThreadComment>,
    val post: ThreadPost
)

/** 스레드 안 댓글 한 개 갱신 */
fun List<CommentThread>.updateComment(
    commentId: Int,
    transform: (ThreadComment) -> ThreadComment
): List<CommentThread> = map { thread ->
    if (thread.comments.none { it.id == commentId }) thread
    else thread.copy(comments = thread.comments.map { if (it.id == commentId) transform(it) else it })
}

/** 스레드 원글 갱신, 같은 글의 스레드 전부 */
fun List<CommentThread>.updatePost(
    postId: Int,
    transform: (ThreadPost) -> ThreadPost
): List<CommentThread> = map { if (it.post.id == postId) it.copy(post = transform(it.post)) else it }

/** 팔로우 이벤트 반영 */
fun ThreadUser.applyFollow(authorId: Int, isFollowing: Boolean): ThreadUser =
    if (id == authorId && this.isFollowing != isFollowing) copy(isFollowing = isFollowing) else this

fun ThreadPost.applyFollow(authorId: Int, isFollowing: Boolean): ThreadPost =
    copy(author = author.applyFollow(authorId, isFollowing))

fun ThreadComment.applyFollow(authorId: Int, isFollowing: Boolean): ThreadComment =
    copy(
        author = author.applyFollow(authorId, isFollowing),
        replyTo = replyTo?.applyFollow(authorId, isFollowing)
    )

fun CommentThread.applyFollow(authorId: Int, isFollowing: Boolean): CommentThread =
    copy(
        post = post.applyFollow(authorId, isFollowing),
        comments = comments.map { it.applyFollow(authorId, isFollowing) }
    )

/** 팔로우 이벤트 반영 */
fun Post.applyFollow(authorId: Int, isFollowing: Boolean): Post =
    if (author.id == authorId && author.isFollowing != isFollowing)
        copy(author = author.copy(isFollowing = isFollowing))
    else this
