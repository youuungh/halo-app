package com.ninezero.halo.feature.profile.domain.usecase

import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class DeleteCommunityPostUseCase @Inject constructor(
    private val repository: ProfileRepository,
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: Int): Result<Unit> {
        // 캐시에서도 빼서 모든 목록에서 제거
        return repository.deleteCommunityPost(postId)
            .onSuccess { postRepository.removeCachedPost(postId) }
    }
}
