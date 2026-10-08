package com.ninezero.halo.feature.store.domain.model

data class CreateOrderRequest(
    val shippingAddress: String,
    val shippingPhone: String,
    val shippingName: String,
    val paymentProvider: String,
    val memo: String? = null,
    val pointsToUse: String? = null,
    val couponCodes: List<String>? = null,
    val cartItemIds: List<Int>? = null
)
