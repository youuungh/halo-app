package com.ninezero.halo.feature.feed.domain.usecase

import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.NewMediaItem
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.repository.CommentRepository
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import javax.inject.Inject

class GetCachedPostUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(postId: Int): Post? {
        return repository.getCachedPost(postId)
    }
}

class RemoveCachedPostUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(postId: Int) {
        repository.removeCachedPost(postId)
    }
}

class ObservePostUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(postId: Int): Flow<Post?> {
        return repository.observePost(postId)
    }
}

class ObserveCachedPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(): Flow<List<Post>> {
        return repository.observeCachedPosts()
    }
}

class UpdatePostUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(
        postId: Int,
        content: String,
        keepMediaIds: List<Int>? = null,
        newFiles: List<MultipartBody.Part> = emptyList(),
        reorder: List<Int>? = null,
        items: List<NewMediaItem>? = null
    ): Result<Post> {
        return if (keepMediaIds != null || reorder != null || newFiles.isNotEmpty() || items != null) {
            repository.updatePostWithMedia(
                postId = postId,
                content = content,
                keepMediaIds = keepMediaIds,
                newFiles = newFiles,
                reorder = reorder,
                items = items
            )
        } else {
            repository.updatePost(
                postId = postId,
                content = content
            )
        }
    }
}

class GetCommentThreadUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(commentId: Int): Result<List<Comment>> {
        return repository.getCommentThread(commentId)
    }
}

class GetCachedCommentUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    operator fun invoke(commentId: Int): Comment? {
        return repository.getCachedComment(commentId)
    }
}

class ObserveCachedCommentsUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    operator fun invoke(): Flow<Map<Int, Comment>> {
        return repository.observeCachedComments()
    }
}

class CacheCommentsUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    // 스레드 정보 없는 댓글, 캐시의 답글 정보 유지
    operator fun invoke(comments: List<Comment>) {
        repository.cacheComments(
            comments.map { fresh ->
                repository.getCachedComment(fresh.id)?.let { cached ->
                    fresh.copy(
                        firstReply = cached.firstReply,
                        replyPreviewUsers = cached.replyPreviewUsers
                    )
                } ?: fresh
            }
        )
    }
}

class GetParentChainUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    operator fun invoke(commentId: Int): List<Comment> {
        return repository.getParentChain(commentId)
    }
}

class GetPostByIdUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(postId: Int): Result<Post> {
        return repository.getPostById(postId)
    }
}

class GetPostCommentsUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(
        postId: Int,
        page: Int = 1,
        limit: Int = 20,
        sortBy: String = "POPULAR"
    ): Result<List<Comment>> {
        return repository.getCommentsByPostId(postId, page, limit, sortBy)
    }
}

class GetCommentRepliesUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(
        commentId: Int,
        page: Int = 1,
        limit: Int = 20,
        sortBy: String = "POPULAR"
    ): Result<List<Comment>> {
        return repository.getRepliesByCommentId(commentId, page, limit, sortBy)
    }
}

class DeleteCommentUseCase @Inject constructor(  // 캐시·commentCount 정리
    private val repository: CommentRepository,
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(commentId: Int): Result<Unit> {
        // 삭제 전에 읽어 둠, 인라인 답글 수도 반영
        val deleted = repository.getCachedCommentOrInlineReply(commentId)

        return repository.deleteComment(commentId).onSuccess {
            // 캐시에서 빼야 다른 화면도 제거
            repository.applyCommentDeletedToCache(commentId, deleted?.parentCommentId)

            // 댓글 수도 캐시 반영
            deleted?.postId?.let { postId ->
                postRepository.getCachedPost(postId)?.let { post ->
                    postRepository.updateCachedPost(
                        post.copy(commentCount = maxOf(0, post.commentCount - 1))
                    )
                }
            }
        }
    }
}
