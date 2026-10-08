package com.ninezero.halo.feature.feed.domain.repository

import com.ninezero.halo.core.model.ReportTargetType

interface ReportRepository {
    /** 신고 */
    suspend fun report(targetType: ReportTargetType, targetId: Int): Result<Unit>
}
