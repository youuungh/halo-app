package com.ninezero.halo.feature.store.presentation.order_complete

import com.ninezero.halo.feature.store.domain.model.Order

data class OrderCompleteState(
    val order: Order? = null  // 못 받으면 요약 카드 없이 완료 문구만
)
