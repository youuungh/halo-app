package com.ninezero.halo.feature.point.data.repository

import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.feature.point.data.remote.api.PointApi
import com.ninezero.halo.feature.point.data.remote.dto.toDomain
import com.ninezero.halo.feature.point.domain.model.PointBalance
import com.ninezero.halo.feature.point.domain.model.PointHistoryPage
import com.ninezero.halo.feature.point.domain.model.PointType
import com.ninezero.halo.feature.point.domain.repository.PointRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PointRepositoryImpl @Inject constructor(
    private val api: PointApi
) : PointRepository {

    override suspend fun getBalance(): Result<PointBalance> {
        return safeApiCall {
            api.getBalance()
        }.map { it.toDomain() }
    }

    override suspend fun getHistory(
        type: PointType?,
        page: Int,
        limit: Int,
        startDate: String?,
        endDate: String?
    ): Result<PointHistoryPage> {
        return safeApiCall {
            api.getHistory(type?.name, page, limit, startDate, endDate)
        }.map { it.toDomain() }
    }
}
