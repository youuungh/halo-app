package com.ninezero.halo.feature.store.presentation.order_detail.components

internal fun getPaymentProviderText(provider: String): String = when (provider) {
    "MOCK" -> "테스트 결제"
    "TOSS_PAYMENTS" -> "토스페이"
    "TOSS_BILLING" -> "저장카드 결제"
    "NONE" -> "포인트·쿠폰"
    else -> provider
}
