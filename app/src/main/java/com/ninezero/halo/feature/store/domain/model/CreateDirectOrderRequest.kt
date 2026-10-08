package com.ninezero.halo.feature.store.domain.model

/** 바로구매 주문 요청 */
data class CreateDirectOrderRequest(  // 장바구니를 거치지 않음
    val productId: Int,
    val quantity: Int,
    val shippingAddress: String,
    val shippingPhone: String,
    val shippingName: String,
    val paymentProvider: String,
    val memo: String? = null,
    val pointsToUse: String? = null,
    val couponCodes: List<String>? = null
)
