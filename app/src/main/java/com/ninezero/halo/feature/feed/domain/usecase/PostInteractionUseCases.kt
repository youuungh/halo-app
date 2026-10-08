package com.ninezero.halo.feature.feed.domain.usecase

import com.ninezero.halo.feature.feed.domain.event.FollowEventBus
import com.ninezero.halo.feature.feed.domain.model.LikeResult
import com.ninezero.halo.feature.feed.domain.repository.BlockRepository
import com.ninezero.halo.feature.feed.domain.repository.BookmarkRepository
import com.ninezero.halo.feature.feed.domain.repository.CommentRepository
import com.ninezero.halo.feature.feed.domain.repository.HiddenPostRepository
import com.ninezero.halo.feature.feed.domain.repository.LikeRepository
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import com.ninezero.halo.feature.profile.domain.repository.FollowRepository
import javax.inject.Inject

/** 낙관적 갱신 공통 패턴 */
private suspend fun <T : Any, R> applyMutation(  // 실패 시 previous로 롤백
    getCurrent: () -> T?,
    save: (T) -> Unit,
    optimistic: (T) -> T,
    onSynced: (current: T, synced: R) -> T,
    onRollback: (current: T, previous: T) -> T,
    remote: suspend () -> Result<R>
): Result<R> {
    val previous = getCurrent()
    previous?.let { save(optimistic(it)) }
    val result = remote()
    result
        .onSuccess { synced ->
            (getCurrent() ?: previous)?.let { save(onSynced(it, synced)) }
        }
        .onFailure {
            if (previous != null) {
                save(onRollback(getCurrent() ?: previous, previous))
            }
        }
    return result
}

class TogglePostLikeUseCase @Inject constructor(
    private val likeRepository: LikeRepository,
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: Int, shouldLike: Boolean): Result<LikeResult> =
        applyMutation(
            getCurrent = { postRepository.getCachedPost(postId) },
            save = { postRepository.updateCachedPost(it) },
            optimistic = { it.copy(
                isLiked = shouldLike,
                likeCount = if (shouldLike) it.likeCount + 1 else maxOf(0, it.likeCount - 1)
            )},
            onSynced = { current, synced ->
                current.copy(isLiked = synced.isLiked, likeCount = synced.likeCount)
            },
            onRollback = { current, prev ->
                current.copy(isLiked = prev.isLiked, likeCount = prev.likeCount)
            },
            remote = { likeRepository.togglePostLike(postId) }
        )
}

class TogglePostBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: Int, shouldBookmark: Boolean): Result<Boolean> =
        applyMutation(
            getCurrent = { postRepository.getCachedPost(postId) },
            save = { postRepository.updateCachedPost(it) },
            optimistic = { it.copy(isBookmarked = shouldBookmark) },
            onSynced = { current, synced -> current.copy(isBookmarked = synced) },
            onRollback = { current, prev -> current.copy(isBookmarked = prev.isBookmarked) },
            remote = { bookmarkRepository.togglePostBookmark(postId) }
        )
}

class ToggleCommentLikeUseCase @Inject constructor(
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(commentId: Int, shouldLike: Boolean): Result<LikeResult> =
        applyMutation(
            getCurrent = { commentRepository.getCachedComment(commentId) },
            save = { commentRepository.cacheComment(it) },
            optimistic = { it.copy(
                isLiked = shouldLike,
                likeCount = if (shouldLike) it.likeCount + 1 else maxOf(0, it.likeCount - 1)
            )},
            onSynced = { current, synced ->
                current.copy(isLiked = synced.isLiked, likeCount = synced.likeCount)
            },
            onRollback = { current, prev ->
                current.copy(isLiked = prev.isLiked, likeCount = prev.likeCount)
            },
            remote = { likeRepository.toggleCommentLike(commentId) }
        )
}

class ToggleCommentBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(commentId: Int, shouldBookmark: Boolean): Result<Boolean> =
        applyMutation(
            getCurrent = { commentRepository.getCachedComment(commentId) },
            save = { commentRepository.cacheComment(it) },
            optimistic = { it.copy(isBookmarked = shouldBookmark) },
            onSynced = { current, synced -> current.copy(isBookmarked = synced) },
            onRollback = { current, prev -> current.copy(isBookmarked = prev.isBookmarked) },
            remote = { bookmarkRepository.toggleCommentBookmark(commentId) }
        )
}

class IncrementPostShareCountUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    operator fun invoke(postId: Int) {
        val previous = postRepository.getCachedPost(postId) ?: return
        postRepository.updateCachedPost(
            previous.copy(shareCount = previous.shareCount + 1)
        )
    }
}

class ToggleAuthorFollowUseCase @Inject constructor(  // 포스트·댓글 캐시 동시 갱신
    private val followRepository: FollowRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val followEventBus: FollowEventBus
) {
    suspend operator fun invoke(authorId: Int, shouldFollow: Boolean): Result<Boolean> {
        postRepository.updateCachedFollowState(authorId, shouldFollow)
        commentRepository.updateCachedFollowState(authorId, shouldFollow)

        val result = if (shouldFollow) {
            followRepository.followUser(authorId).map { true }
        } else {
            followRepository.unfollowUser(authorId).map { false }
        }

        result
            .onSuccess { syncedIsFollowing ->
                postRepository.updateCachedFollowState(authorId, syncedIsFollowing)
                commentRepository.updateCachedFollowState(authorId, syncedIsFollowing)
                if (syncedIsFollowing) {
                    followEventBus.emitFollowed(authorId)
                } else {
                    followEventBus.emitUnfollowed(authorId)
                }
            }
            .onFailure {
                postRepository.updateCachedFollowState(authorId, !shouldFollow)
                commentRepository.updateCachedFollowState(authorId, !shouldFollow)
            }

        return result
    }
}

class BlockUserUseCase @Inject constructor(  // 작성자 캐시 일괄 제거
    private val repository: BlockRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(userId: Int): Result<Boolean> {
        return repository.toggleBlock(userId).onSuccess { isBlocked ->
            if (isBlocked) {
                postRepository.removeCachedPostsByAuthor(userId)
                commentRepository.removeCachedCommentsByAuthor(userId)
            }
        }
    }
}

class HidePostUseCase @Inject constructor(  // 숨김 시 캐시 제거
    private val repository: HiddenPostRepository,
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: Int): Result<Boolean> {
        return repository.toggleHidden(postId).onSuccess { isHidden ->
            if (isHidden) {
                postRepository.removeCachedPost(postId)
            }
        }
    }
}

class DeletePostUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(postId: Int): Result<Unit> {
        return repository.deletePost(postId)
    }
}
