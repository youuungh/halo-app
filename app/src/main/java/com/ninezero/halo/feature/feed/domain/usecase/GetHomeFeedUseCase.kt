package com.ninezero.halo.feature.feed.domain.usecase

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.repository.FeedRepository
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import javax.inject.Inject

class GetHomeFeedUseCase @Inject constructor(
    private val repository: FeedRepository,
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(cursor: String? = null, limit: Int = 20): Result<CursorPage<Post>> {
        return repository.getHomeFeed(cursor, limit)
            .onSuccess { result ->
                postRepository.cachePosts(result.items)
            }
    }
}
