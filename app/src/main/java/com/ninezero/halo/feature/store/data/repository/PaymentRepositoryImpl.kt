package com.ninezero.halo.feature.store.data.repository

import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.feature.store.data.remote.api.PaymentApi
import com.ninezero.halo.feature.store.data.remote.dto.TossConfirmRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.toDomain
import com.ninezero.halo.feature.store.domain.model.PaymentPrepareInfo
import com.ninezero.halo.feature.store.domain.repository.PaymentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val api: PaymentApi
) : PaymentRepository {

    override suspend fun processPayment(orderId: Int): Result<String> {
        return safeApiCall { api.processPayment(orderId) }
    }

    override suspend fun prepareTossPayment(orderId: Int): Result<PaymentPrepareInfo> {
        return safeApiCall { api.prepareTossPayment(orderId) }.map { it.toDomain() }
    }

    override suspend fun confirmTossPayment(
        orderId: Int,
        paymentKey: String,
        amount: Long
    ): Result<Unit> {
        return safeApiCall {
            api.confirmTossPayment(
                TossConfirmRequestDto(paymentKey = paymentKey, orderId = orderId, amount = amount)
            )
        }.map { }
    }
}
