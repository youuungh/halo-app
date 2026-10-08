package com.ninezero.halo.feature.profile.domain.usecase

import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class TogglePinCommunityPostUseCase @Inject constructor(
    private val repository: ProfileRepository,
    private val postRepository: PostRepository
) {
    // 캐시를 먼저 뒤집고 서버 결과로 맞춤
    suspend operator fun invoke(postId: Int): Result<Boolean> {
        val previous = postRepository.getCachedPost(postId)
        if (previous != null) {
            postRepository.updateCachedPost(previous.copy(isPinned = !previous.isPinned))
        }

        return repository.togglePinCommunityPost(postId)
            .onSuccess { syncedIsPinned ->
                val base = postRepository.getCachedPost(postId) ?: previous
                if (base != null) {
                    postRepository.updateCachedPost(base.copy(isPinned = syncedIsPinned))
                }
            }
            .onFailure {
                if (previous != null) {
                    val base = postRepository.getCachedPost(postId) ?: previous
                    postRepository.updateCachedPost(base.copy(isPinned = previous.isPinned))
                }
            }
    }
}
