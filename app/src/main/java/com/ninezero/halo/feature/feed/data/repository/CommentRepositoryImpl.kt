package com.ninezero.halo.feature.feed.data.repository

import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.feature.feed.data.cache.CommentStateStore
import com.ninezero.halo.feature.feed.data.remote.api.CommentApi
import com.ninezero.halo.feature.feed.data.remote.dto.CommentRequest
import com.ninezero.halo.feature.feed.data.remote.dto.UpdateCommentRequest
import com.ninezero.halo.feature.feed.data.remote.dto.toDomain
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val api: CommentApi,
    private val commentStateStore: CommentStateStore
) : CommentRepository {
    // 캐시

    override fun getCachedComment(commentId: Int): Comment? {
        return commentStateStore.getComment(commentId)
    }

    override fun getCachedCommentOrInlineReply(commentId: Int): Comment? {
        // 인라인 첫 답글은 부모 안에만 있어 거기서 찾음
        return commentStateStore.getComment(commentId)
            ?: commentStateStore.comments.value.values.firstNotNullOfOrNull { cached ->
                cached.firstReply?.takeIf { it.id == commentId }
            }
    }

    override fun cacheComment(comment: Comment) {
        commentStateStore.upsert(comment)
    }

    override fun cacheComments(comments: List<Comment>) {
        commentStateStore.upsertAll(comments)
    }

    override fun observeCachedComments(): Flow<Map<Int, Comment>> {
        return commentStateStore.comments
    }

    override fun updateCachedFollowState(authorId: Int, isFollowing: Boolean) {
        commentStateStore.updateAuthorFollowState(authorId, isFollowing)
    }

    override fun removeCachedCommentsByAuthor(authorId: Int) {
        commentStateStore.removeByAuthor(authorId)
    }

    override fun clearCachedComments() {
        commentStateStore.clear()
    }

    override fun applyCommentDeletedToCache(commentId: Int, parentCommentId: Int?) {
        val deleted = getCachedCommentOrInlineReply(commentId)

        // 답글이 남으면 자리만 남김, 부모 답글 수는 그대로
        if (deleted != null && deleted.replyCount > 0) {
            commentStateStore.upsert(
                deleted.copy(
                    isDeleted = true,
                    content = "",
                    mediaAttachments = emptyList(),
                    isLiked = false,
                    isBookmarked = false
                )
            )
            return
        }

        commentStateStore.remove(commentId)

        // 답글이 모두 사라진 삭제 자리는 위로 따라가며 함께 제거
        var removedId = commentId
        var parentId = deleted?.parentCommentId ?: parentCommentId
        while (parentId != null) {
            val parent = commentStateStore.getComment(parentId) ?: return
            val updated = parent.copy(
                replyCount = maxOf(0, parent.replyCount - 1),
                // 삭제된 답글이 부모의 미리보기였으면 비움
                firstReply = parent.firstReply?.takeIf { it.id != removedId }
            )
            if (!updated.isDeleted || updated.replyCount > 0) {
                commentStateStore.upsert(updated)
                return
            }
            commentStateStore.remove(parentId)
            removedId = parentId
            parentId = parent.parentCommentId
        }
    }

    override fun applyReplyCreatedToCache(parentCommentId: Int, newReply: Comment) {
        val parent = commentStateStore.getComment(parentCommentId) ?: return
        commentStateStore.upsert(
            parent.copy(
                replyCount = parent.replyCount + 1,
                firstReply = parent.firstReply ?: newReply
            )
        )
    }

    override fun getParentChain(commentId: Int): List<Comment> {
        return commentStateStore.getParentChain(commentId)
    }

    // API 호출

    override suspend fun getCommentThread(commentId: Int): Result<List<Comment>> {
        return safeApiCall {
            api.getCommentThread(commentId)
        }.map { dtos -> dtos.map { it.toDomain() } }.also { result ->
            result.getOrNull()?.forEach { cacheComment(it) }
        }
    }

    override suspend fun getCommentsByPostId(
        postId: Int,
        page: Int,
        limit: Int,
        sortBy: String
    ): Result<List<Comment>> {
        return safeApiCall {
            api.getCommentsByPostId(postId, page, limit, sortBy)
        }.map { response ->
            response.items.map { it.toDomain() }
        }.also { result ->
            // CommentDetail에서 재사용하기 위해 캐시에 저장
            result.getOrNull()?.let { cacheComments(it) }
        }
    }

    override suspend fun getRepliesByCommentId(
        commentId: Int,
        page: Int,
        limit: Int,
        sortBy: String
    ): Result<List<Comment>> {
        return safeApiCall {
            api.getRepliesByCommentId(commentId, page, limit, sortBy)
        }.map { response ->
            response.items.map { it.toDomain() }
        }.also { result ->
            result.getOrNull()?.let { cacheComments(it) }
        }
    }

    override suspend fun createComment(
        postId: Int,
        content: String,
        parentCommentId: Int?,
        mediaDraftIds: List<Int>,
        clientRequestId: String?
    ): Result<Comment> {
        return safeApiCall {
            api.createComment(
                CommentRequest(
                    postId = postId,
                    content = content,
                    parentCommentId = parentCommentId,
                    mediaDraftIds = mediaDraftIds,
                    clientRequestId = clientRequestId
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun updateComment(
        commentId: Int,
        content: String
    ): Result<Comment> {
        return safeApiCall {
            api.updateComment(
                commentId,
                UpdateCommentRequest(content = content)
            )
        }.map { it.toDomain() }
    }

    override suspend fun deleteComment(commentId: Int): Result<Unit> {
        return safeApiCallUnit {
            api.deleteComment(commentId)
        }
    }
}
