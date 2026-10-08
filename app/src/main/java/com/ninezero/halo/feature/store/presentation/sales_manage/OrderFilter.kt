package com.ninezero.halo.feature.store.presentation.sales_manage

import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus

enum class OrderFilter(val label: String) {
    ALL("전체"),
    PENDING("대기"),  // PENDING/CONFIRMED/PREPARING 포함
    SHIPPED("배송중"),
    DELIVERED("완료"),
    CANCELLED("취소")
}

fun Order.matches(filter: OrderFilter): Boolean = when (filter) {
    OrderFilter.ALL -> true
    OrderFilter.PENDING -> status == OrderStatus.PENDING ||
            (status == OrderStatus.CONFIRMED && shippingStatus == ShippingStatus.PREPARING)
    OrderFilter.SHIPPED -> shippingStatus == ShippingStatus.SHIPPED ||
            shippingStatus == ShippingStatus.IN_TRANSIT ||
            shippingStatus == ShippingStatus.OUT_FOR_DELIVERY
    OrderFilter.DELIVERED -> shippingStatus == ShippingStatus.DELIVERED || status == OrderStatus.DELIVERED
    OrderFilter.CANCELLED -> status == OrderStatus.CANCELLED
}
