package com.ninezero.halo.feature.feed.domain.repository

import com.ninezero.halo.feature.feed.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    // 캐시 관련

    /** 캐시 댓글 조회 */
    fun getCachedComment(commentId: Int): Comment?

    /** 캐시 댓글 조회 */
    fun getCachedCommentOrInlineReply(commentId: Int): Comment?  // 부모 안의 인라인 첫 답글까지

    /** 댓글 캐시 저장 */
    fun cacheComment(comment: Comment)

    /** 댓글 일괄 캐시 저장 */
    fun cacheComments(comments: List<Comment>)

    /** 댓글 캐시 관찰 */
    fun observeCachedComments(): Flow<Map<Int, Comment>>

    /** 캐시 작성자 팔로우 상태 갱신 */
    fun updateCachedFollowState(authorId: Int, isFollowing: Boolean)

    /** 작성자 댓글 캐시 제거 */
    fun removeCachedCommentsByAuthor(authorId: Int)

    /** 댓글 캐시 전체 삭제 */
    fun clearCachedComments()

    /** 댓글 삭제 캐시 반영 */
    fun applyCommentDeletedToCache(commentId: Int, parentCommentId: Int?)

    /** 답글 작성 캐시 반영 */
    fun applyReplyCreatedToCache(parentCommentId: Int, newReply: Comment)

    /** 조상 댓글 체인 조회 */
    fun getParentChain(commentId: Int): List<Comment>  // 최상위 부모부터 대상 댓글까지

    // API 호출

    /** 댓글 조상 체인 조회 */
    suspend fun getCommentThread(commentId: Int): Result<List<Comment>>

    /** 포스트 댓글 목록 조회 */
    suspend fun getCommentsByPostId(
        postId: Int,
        page: Int,
        limit: Int,
        sortBy: String = "createdAt"
    ): Result<List<Comment>>

    /** 대댓글 목록 조회 */
    suspend fun getRepliesByCommentId(
        commentId: Int,
        page: Int,
        limit: Int,
        sortBy: String = "POPULAR"
    ): Result<List<Comment>>

    /** 댓글 작성 */
    suspend fun createComment(
        postId: Int,
        content: String,
        parentCommentId: Int? = null,
        mediaDraftIds: List<Int> = emptyList(),  // 표시 순서대로
        clientRequestId: String? = null  // 같은 요청이 반복되면 서버가 만든 댓글을 그대로 응답
    ): Result<Comment>

    /** 댓글 수정 */
    suspend fun updateComment(
        commentId: Int,
        content: String
    ): Result<Comment>

    /** 댓글 삭제 */
    suspend fun deleteComment(commentId: Int): Result<Unit>
}
