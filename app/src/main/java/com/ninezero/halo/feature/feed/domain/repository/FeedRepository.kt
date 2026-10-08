package com.ninezero.halo.feature.feed.domain.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.model.PaginatedResult
import com.ninezero.halo.feature.feed.domain.model.FeedType
import com.ninezero.halo.feature.feed.domain.model.Post

interface FeedRepository {
    /** 홈 피드 조회 */
    suspend fun getHomeFeed(cursor: String?, limit: Int): Result<CursorPage<Post>>

    /** 탐색·트렌딩 피드 조회 */
    suspend fun getFeed(type: FeedType, page: Int, limit: Int): Result<PaginatedResult<Post>>
}
