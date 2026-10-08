package com.ninezero.halo.feature.store.data.remote.dto

import com.ninezero.halo.feature.store.domain.model.PaymentPrepareInfo
import kotlinx.serialization.Serializable

@Serializable
data class PaymentPrepareResponseDto(
    val orderId: String,
    val orderName: String,
    val amount: String
)

@Serializable
data class TossConfirmRequestDto(
    val paymentKey: String,
    val orderId: Int,  // 앱 내부 주문 ID
    val amount: Long
)

fun PaymentPrepareResponseDto.toDomain(): PaymentPrepareInfo = PaymentPrepareInfo(
    orderId = orderId,
    orderName = orderName,
    amount = amount.toLongOrNull() ?: 0L
)
