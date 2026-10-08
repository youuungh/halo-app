package com.ninezero.halo.feature.feed.data.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.feature.feed.data.remote.api.BookmarkApi
import com.ninezero.halo.feature.feed.data.remote.dto.toDomain
import com.ninezero.halo.feature.feed.domain.model.BookmarkItem
import com.ninezero.halo.feature.feed.domain.repository.BookmarkRepository
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val api: BookmarkApi
) : BookmarkRepository {

    override suspend fun togglePostBookmark(postId: Int): Result<Boolean> {
        return safeApiCall {
            api.togglePostBookmark(postId)
        }.map { response ->
            response.isBookmarked
        }
    }

    override suspend fun toggleCommentBookmark(commentId: Int): Result<Boolean> {
        return safeApiCall {
            api.toggleCommentBookmark(commentId)
        }.map { response ->
            response.isBookmarked
        }
    }

    override suspend fun getMyBookmarks(
        targetType: String?,
        cursor: String?,
        limit: Int
    ): Result<CursorPage<BookmarkItem>> {
        return safeApiCall {
            api.getMyBookmarks(targetType, cursor, limit)
        }.map { response ->
            CursorPage(response.items.map { it.toDomain() }, response.nextCursor)
        }
    }
}
