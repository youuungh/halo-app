package com.ninezero.halo.feature.profile.domain.usecase

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import javax.inject.Inject

class GetUserPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        userId: Int,
        cursor: String? = null,
        limit: Int = 20,
        sort: String? = null,
        contextType: String? = null
    ): Result<CursorPage<Post>> {
        return postRepository.getUserPosts(userId, cursor, limit, sort, contextType)
    }
}
