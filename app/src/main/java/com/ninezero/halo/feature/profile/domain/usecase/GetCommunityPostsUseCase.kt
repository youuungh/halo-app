package com.ninezero.halo.feature.profile.domain.usecase

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

/** 커뮤니티 글 목록, 다른 화면 변경이 반영되게 캐시에 저장 */
class GetCommunityPostsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(creatorId: Int, cursor: String? = null, limit: Int = 20): Result<CursorPage<Post>> {
        return profileRepository.getCommunityPosts(creatorId, cursor, limit)
            .onSuccess { postRepository.cachePosts(it.items) }
    }
}
