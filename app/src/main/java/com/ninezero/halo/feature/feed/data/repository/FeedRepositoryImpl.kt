package com.ninezero.halo.feature.feed.data.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.model.PaginatedResult
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.feature.feed.data.remote.api.FeedApi
import com.ninezero.halo.feature.feed.data.remote.dto.toDomain
import com.ninezero.halo.feature.feed.domain.model.FeedType
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.repository.FeedRepository
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val api: FeedApi
) : FeedRepository {

    override suspend fun getHomeFeed(cursor: String?, limit: Int): Result<CursorPage<Post>> {
        return safeApiCall { api.getHomeFeed(cursor, limit) }
            .map { response -> CursorPage(response.items.map { it.toDomain() }, response.nextCursor) }
    }

    override suspend fun getFeed(type: FeedType, page: Int, limit: Int): Result<PaginatedResult<Post>> {
        return safeApiCall {
            when (type) {
                FeedType.EXPLORE -> api.getExploreFeed(page, limit)
                FeedType.TRENDING -> api.getTrendingFeed(page, limit)
            }
        }.map { feedResponse ->
            PaginatedResult(
                items = feedResponse.posts.map { it.toDomain() },
                hasNext = feedResponse.hasNext,
                totalCount = feedResponse.totalCount,
                currentPage = feedResponse.page,
                totalPages = feedResponse.totalPages
            )
        }
    }
}
