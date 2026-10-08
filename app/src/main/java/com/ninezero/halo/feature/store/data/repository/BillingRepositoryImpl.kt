package com.ninezero.halo.feature.store.data.repository

import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.feature.store.data.remote.api.BillingApi
import com.ninezero.halo.feature.store.data.remote.dto.BillingIssueRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.BillingPayRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.toDomain
import com.ninezero.halo.feature.store.domain.model.BillingCard
import com.ninezero.halo.feature.store.domain.repository.BillingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val api: BillingApi
) : BillingRepository {

    override suspend fun getCustomerKey(): Result<String> =
        safeApiCall { api.getCustomerKey() }.map { it.customerKey }

    override suspend fun issueBillingKey(authKey: String): Result<BillingCard> =
        safeApiCall { api.issueBillingKey(BillingIssueRequestDto(authKey)) }.map { it.toDomain() }

    override suspend fun getMyBillingKey(): Result<BillingCard> =
        safeApiCall { api.getMyBillingKey() }.map { it.toDomain() }

    override suspend fun deleteBillingKey(): Result<Unit> =
        safeApiCallUnit { api.deleteBillingKey() }

    override suspend fun payByBilling(orderId: Int): Result<Unit> =
        safeApiCall { api.payByBilling(BillingPayRequestDto(orderId)) }.map { }
}
