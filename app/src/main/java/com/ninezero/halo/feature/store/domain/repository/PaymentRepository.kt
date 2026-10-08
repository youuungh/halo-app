package com.ninezero.halo.feature.store.domain.repository

import com.ninezero.halo.feature.store.domain.model.PaymentPrepareInfo

interface PaymentRepository {
    /** Mock 결제 */
    suspend fun processPayment(orderId: Int): Result<String>

    /** 토스 결제 준비 */
    suspend fun prepareTossPayment(orderId: Int): Result<PaymentPrepareInfo>

    /** 토스 결제 승인 */
    suspend fun confirmTossPayment(orderId: Int, paymentKey: String, amount: Long): Result<Unit>
}
