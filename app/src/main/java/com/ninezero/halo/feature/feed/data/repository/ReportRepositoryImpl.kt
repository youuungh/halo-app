package com.ninezero.halo.feature.feed.data.repository

import com.ninezero.halo.core.model.ReportTargetType
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.feature.feed.data.remote.api.ReportApi
import com.ninezero.halo.feature.feed.data.remote.dto.ReportRequestDto
import com.ninezero.halo.feature.feed.domain.repository.ReportRepository
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val api: ReportApi
) : ReportRepository {

    override suspend fun report(
        targetType: ReportTargetType,
        targetId: Int
    ): Result<Unit> {
        return safeApiCall {
            api.report(
                ReportRequestDto(
                    targetType = targetType.name,
                    targetId = targetId
                )
            )
        }.map { }
    }
}
