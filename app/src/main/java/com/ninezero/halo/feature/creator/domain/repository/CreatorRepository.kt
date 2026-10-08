package com.ninezero.halo.feature.creator.domain.repository

import com.ninezero.halo.feature.creator.domain.model.CreatorApplication

interface CreatorRepository {
    /** 크리에이터 신청 */
    suspend fun submitApplication(reason: String, portfolioUrl: String?): Result<CreatorApplication>
    /** 내 신청 조회 */
    suspend fun getMyApplication(): Result<CreatorApplication>
    /** 신청 취소 */
    suspend fun cancelApplication(): Result<Unit>
    /** 크리에이터 셀프 해제 */
    suspend fun revokeCreator(): Result<Unit>
}
