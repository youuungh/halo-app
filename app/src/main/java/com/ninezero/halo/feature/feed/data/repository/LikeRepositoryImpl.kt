package com.ninezero.halo.feature.feed.data.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.feature.feed.data.remote.api.LikeApi
import com.ninezero.halo.feature.feed.data.remote.dto.toDomain
import com.ninezero.halo.feature.feed.domain.model.LikeResult
import com.ninezero.halo.feature.feed.domain.model.LikedItem
import com.ninezero.halo.feature.feed.domain.repository.LikeRepository
import javax.inject.Inject

class LikeRepositoryImpl @Inject constructor(
    private val api: LikeApi
) : LikeRepository {

    override suspend fun togglePostLike(postId: Int): Result<LikeResult> {
        return safeApiCall {
            api.togglePostLike(postId)
        }.map { response ->
            LikeResult(
                isLiked = response.isLiked,
                likeCount = response.likeCount
            )
        }
    }

    override suspend fun toggleCommentLike(commentId: Int): Result<LikeResult> {
        return safeApiCall {
            api.toggleCommentLike(commentId)
        }.map { response ->
            LikeResult(
                isLiked = response.isLiked,
                likeCount = response.likeCount
            )
        }
    }

    override suspend fun getMyLikedItems(
        targetType: String?,
        cursor: String?,
        limit: Int
    ): Result<CursorPage<LikedItem>> {
        return safeApiCall {
            api.getMyLikedItems(targetType, cursor, limit)
        }.map { response ->
            CursorPage(response.items.map { it.toDomain() }, response.nextCursor)
        }
    }
}
