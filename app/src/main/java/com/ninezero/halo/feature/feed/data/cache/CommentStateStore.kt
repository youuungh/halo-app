package com.ninezero.halo.feature.feed.data.cache

import com.ninezero.halo.feature.feed.domain.model.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** 댓글 SSOT 캐시 */
interface CommentStateStore {
    val comments: StateFlow<Map<Int, Comment>>

    fun getComment(commentId: Int): Comment?
    fun upsert(comment: Comment)
    fun upsertAll(comments: List<Comment>)
    fun updateAuthorFollowState(authorId: Int, isFollowing: Boolean)
    fun removeByAuthor(authorId: Int)
    fun remove(commentId: Int)
    fun getParentChain(commentId: Int): List<Comment>
    fun clear()
}

@Singleton
class CommentStateStoreImpl @Inject constructor() : CommentStateStore {
    // 동시 수정 유실 방지로 update만
    private val _comments = MutableStateFlow<Map<Int, Comment>>(emptyMap())
    override val comments: StateFlow<Map<Int, Comment>> = _comments.asStateFlow()

    override fun getComment(commentId: Int): Comment? = comments.value[commentId]

    override fun upsert(comment: Comment) {
        _comments.update { syncNestedFirstReplies(it + (comment.id to comment)) }
    }

    override fun upsertAll(comments: List<Comment>) {
        if (comments.isEmpty()) return
        _comments.update { current -> syncNestedFirstReplies(current + comments.associateBy { it.id }) }
    }

    override fun updateAuthorFollowState(authorId: Int, isFollowing: Boolean) {
        _comments.update { current ->
            current.mapValues { (_, comment) ->
                var next = comment

                if (next.author.id == authorId && next.author.isFollowing != isFollowing) {
                    next = next.copy(author = next.author.copy(isFollowing = isFollowing))
                }

                val firstReply = next.firstReply
                if (firstReply != null &&
                    firstReply.author.id == authorId &&
                    firstReply.author.isFollowing != isFollowing
                ) {
                    next = next.copy(
                        firstReply = firstReply.copy(
                            author = firstReply.author.copy(isFollowing = isFollowing)
                        )
                    )
                }

                next
            }
        }
    }

    override fun remove(commentId: Int) {
        _comments.update { it - commentId }
    }

    override fun removeByAuthor(authorId: Int) {
        _comments.update { current ->
            // 남는 댓글에 인라인으로 붙은 그 작성자의 첫 답글도 뺌
            syncNestedFirstReplies(
                current
                    .filterValues { comment -> comment.author.id != authorId }
                    .mapValues { (_, comment) ->
                        if (comment.firstReply?.author?.id == authorId) comment.copy(firstReply = null) else comment
                    }
            )
        }
    }

    override fun getParentChain(commentId: Int): List<Comment> {
        val chain = mutableListOf<Comment>()
        var current = getComment(commentId)

        while (current != null) {
            chain.add(0, current)
            current = current.parentCommentId?.let(::getComment)
        }

        return chain
    }

    override fun clear() {
        _comments.value = emptyMap()
    }

    /** 부모의 firstReply 사본을 최신본으로 교체 */
    private fun syncNestedFirstReplies(cache: Map<Int, Comment>): Map<Int, Comment> {
        return cache.mapValues { (_, comment) ->
            val firstReply = comment.firstReply ?: return@mapValues comment
            val synced = cache[firstReply.id] ?: firstReply
            if (synced == firstReply) comment else comment.copy(firstReply = synced)
        }
    }
}
