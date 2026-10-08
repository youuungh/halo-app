package com.ninezero.halo.feature.creator.data.repository

import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallNoData
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.feature.creator.data.remote.api.CreatorApi
import com.ninezero.halo.feature.creator.data.remote.dto.CreatorApplicationRequest
import com.ninezero.halo.feature.creator.data.remote.dto.toDomain
import com.ninezero.halo.feature.creator.domain.model.CreatorApplication
import com.ninezero.halo.feature.creator.domain.repository.CreatorRepository
import javax.inject.Inject

class CreatorRepositoryImpl @Inject constructor(
    private val api: CreatorApi
) : CreatorRepository {

    override suspend fun submitApplication(reason: String, portfolioUrl: String?): Result<CreatorApplication> {
        return safeApiCall {
            api.submitApplication(CreatorApplicationRequest(reason, portfolioUrl))
        }.map { it.toDomain() }
    }

    override suspend fun getMyApplication(): Result<CreatorApplication> {
        return safeApiCall {
            api.getMyApplication()
        }.map { it.toDomain() }
    }

    override suspend fun cancelApplication(): Result<Unit> {
        return safeApiCallUnit {
            api.cancelApplication()
        }
    }

    override suspend fun revokeCreator(): Result<Unit> {
        return safeApiCallNoData {
            api.revokeCreator()
        }
    }
}
