package com.ninezero.halo.feature.store.domain.model

data class PaymentPrepareInfo(
    val orderId: String,
    val orderName: String,
    val amount: Long
)
