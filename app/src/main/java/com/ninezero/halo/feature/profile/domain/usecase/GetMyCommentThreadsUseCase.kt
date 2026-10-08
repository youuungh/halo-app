package com.ninezero.halo.feature.profile.domain.usecase

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.feature.profile.domain.model.CommentThreadData
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetMyCommentThreadsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val getCachedMyProfileUseCase: GetCachedMyProfileUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase
) {
    suspend operator fun invoke(
        cursor: String? = null,
        limit: Int = 20,
        sort: String? = null,
        contextType: String? = null
    ): Result<CursorPage<CommentThreadData>> {
        // 내 id는 캐시 우선, 없을 때만 조회
        val userId = getCachedMyProfileUseCase()?.userId
            ?: getMyProfileUseCase().getOrElse { return Result.failure(it) }.userId
        return profileRepository.getUserCommentThreads(userId, cursor, limit, sort, contextType)
    }
}
