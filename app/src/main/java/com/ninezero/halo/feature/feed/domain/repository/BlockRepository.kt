package com.ninezero.halo.feature.feed.domain.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.feature.feed.domain.model.BlockRelation

interface BlockRepository {
    /** 사용자 차단/해제 토글 */
    suspend fun toggleBlock(userId: Int): Result<Boolean>

    /** 양쪽 차단 관계 확인 */
    suspend fun getBlockRelation(userId: Int): Result<BlockRelation>

    /** 차단 사용자 목록 조회 */
    suspend fun getMyBlockedUsers(cursor: String?, limit: Int): Result<CursorPage<UserSummary>>
}
