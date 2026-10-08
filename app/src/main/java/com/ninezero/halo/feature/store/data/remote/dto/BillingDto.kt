package com.ninezero.halo.feature.store.data.remote.dto

import com.ninezero.halo.feature.store.domain.model.BillingCard
import kotlinx.serialization.Serializable

@Serializable
data class CustomerKeyResponseDto(
    val customerKey: String
)

@Serializable
data class BillingIssueRequestDto(
    val authKey: String
)

@Serializable
data class BillingPayRequestDto(
    val orderId: Int  // 앱 내부 주문 ID
)

@Serializable
data class BillingKeyResponseDto(
    val registered: Boolean,
    val cardCompany: String? = null,
    val cardNumberMasked: String? = null,
    val cardType: String? = null
)

fun BillingKeyResponseDto.toDomain(): BillingCard = BillingCard(
    registered = registered,
    cardCompany = cardCompany,
    cardNumberMasked = cardNumberMasked,
    cardType = cardType
)
