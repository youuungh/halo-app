package com.ninezero.halo.feature.feed.domain.usecase

import com.ninezero.halo.core.model.PaginatedResult
import com.ninezero.halo.feature.feed.domain.model.FeedType
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.repository.FeedRepository
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import javax.inject.Inject

class GetFeedUseCase @Inject constructor(
    private val repository: FeedRepository,
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        type: FeedType,
        page: Int = 1,
        limit: Int = 20
    ): Result<PaginatedResult<Post>> {
        return repository.getFeed(type, page, limit)
            .onSuccess { result ->
                postRepository.cachePosts(result.items)
            }
    }
}
