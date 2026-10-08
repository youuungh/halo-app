package com.ninezero.halo.feature.feed.domain.delegate

import com.ninezero.halo.feature.feed.domain.model.LikeResult
import com.ninezero.halo.feature.feed.domain.usecase.BlockUserUseCase
import com.ninezero.halo.feature.feed.domain.usecase.HidePostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ToggleAuthorFollowUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ToggleCommentBookmarkUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ToggleCommentLikeUseCase
import com.ninezero.halo.feature.feed.domain.usecase.TogglePostBookmarkUseCase
import com.ninezero.halo.feature.feed.domain.usecase.TogglePostLikeUseCase
import javax.inject.Inject

/** 포스트·댓글 공통 mutation delegate */
class PostMutationDelegate @Inject constructor(  // null 반환은 진행 중 스킵
    private val togglePostLikeUseCase: TogglePostLikeUseCase,
    private val toggleCommentLikeUseCase: ToggleCommentLikeUseCase,
    private val togglePostBookmarkUseCase: TogglePostBookmarkUseCase,
    private val toggleCommentBookmarkUseCase: ToggleCommentBookmarkUseCase,
    private val toggleAuthorFollowUseCase: ToggleAuthorFollowUseCase,
    private val hidePostUseCase: HidePostUseCase,
    private val blockUserUseCase: BlockUserUseCase
) {
    private val postLikeInProgress = mutableSetOf<Int>()
    private val commentLikeInProgress = mutableSetOf<Int>()
    private val postBookmarkInProgress = mutableSetOf<Int>()
    private val commentBookmarkInProgress = mutableSetOf<Int>()
    private val authorFollowInProgress = mutableSetOf<Int>()

    suspend fun togglePostLike(postId: Int, shouldLike: Boolean): Result<LikeResult>? {
        if (!postLikeInProgress.add(postId)) return null
        return try {
            togglePostLikeUseCase(postId, shouldLike)
        } finally {
            postLikeInProgress.remove(postId)
        }
    }

    suspend fun toggleCommentLike(commentId: Int, shouldLike: Boolean): Result<LikeResult>? {
        if (!commentLikeInProgress.add(commentId)) return null
        return try {
            toggleCommentLikeUseCase(commentId, shouldLike)
        } finally {
            commentLikeInProgress.remove(commentId)
        }
    }

    suspend fun togglePostBookmark(postId: Int, shouldBookmark: Boolean): Result<Boolean>? {
        if (!postBookmarkInProgress.add(postId)) return null
        return try {
            togglePostBookmarkUseCase(postId, shouldBookmark)
        } finally {
            postBookmarkInProgress.remove(postId)
        }
    }

    suspend fun toggleCommentBookmark(commentId: Int, shouldBookmark: Boolean): Result<Boolean>? {
        if (!commentBookmarkInProgress.add(commentId)) return null
        return try {
            toggleCommentBookmarkUseCase(commentId, shouldBookmark)
        } finally {
            commentBookmarkInProgress.remove(commentId)
        }
    }

    suspend fun toggleAuthorFollow(authorId: Int, shouldFollow: Boolean): Result<Boolean>? {
        if (!authorFollowInProgress.add(authorId)) return null
        return try {
            toggleAuthorFollowUseCase(authorId, shouldFollow)
        } finally {
            authorFollowInProgress.remove(authorId)
        }
    }

    suspend fun hidePost(postId: Int): Result<Boolean> {
        return hidePostUseCase(postId)
    }

    suspend fun blockUser(userId: Int): Result<Boolean> {
        return blockUserUseCase(userId)
    }

}
