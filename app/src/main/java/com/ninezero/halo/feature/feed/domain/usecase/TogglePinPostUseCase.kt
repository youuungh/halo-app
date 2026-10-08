package com.ninezero.halo.feature.feed.domain.usecase

import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import javax.inject.Inject

class TogglePinPostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: Int, shouldPin: Boolean): Result<Boolean> {
        val previous = postRepository.getCachedPost(postId)
        if (previous != null) {
            postRepository.updateCachedPost(previous.copy(isPinned = shouldPin))
        }

        val result = postRepository.togglePinPost(postId)
        result
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

        return result
    }
}
