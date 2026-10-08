package com.ninezero.halo.feature.point.domain.repository

import com.ninezero.halo.feature.point.domain.model.PointBalance
import com.ninezero.halo.feature.point.domain.model.PointHistoryPage
import com.ninezero.halo.feature.point.domain.model.PointType

interface PointRepository {
    /** 포인트 잔액 조회 */
    suspend fun getBalance(): Result<PointBalance>

    /** 포인트 내역 조회 */
    suspend fun getHistory(
        type: PointType? = null,
        page: Int = 1,
        limit: Int = 20,
        startDate: String? = null,
        endDate: String? = null
    ): Result<PointHistoryPage>
}
