package com.ninezero.halo.feature.profile.domain.usecase

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.feature.profile.domain.model.CommentThreadData
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetUserCommentThreadsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        userId: Int,
        cursor: String? = null,
        limit: Int = 20,
        sort: String? = null,
        contextType: String? = null
    ): Result<CursorPage<CommentThreadData>> {
        return profileRepository.getUserCommentThreads(userId, cursor, limit, sort, contextType)
    }
}
