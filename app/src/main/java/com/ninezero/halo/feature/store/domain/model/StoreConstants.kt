package com.ninezero.halo.feature.store.domain.model

object OrderStatus {
    const val PENDING = "PENDING"
    const val CONFIRMED = "CONFIRMED"
    const val DELIVERED = "DELIVERED"
    const val CANCELLED = "CANCELLED"
}

object ShippingStatus {
    const val PREPARING = "PREPARING"
    const val SHIPPED = "SHIPPED"
    const val IN_TRANSIT = "IN_TRANSIT"
    const val OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY"
    const val DELIVERED = "DELIVERED"
}

object PaymentStatus {
    const val PENDING = "PENDING"
    const val COMPLETED = "COMPLETED"
    const val FAILED = "FAILED"
    const val CANCELLED = "CANCELLED"
    const val REFUND_PENDING = "REFUND_PENDING"
    const val REFUNDED = "REFUNDED"
}

object ProductStatus {
    const val ACTIVE = "ACTIVE"
    const val SOLD_OUT = "SOLD_OUT"
    const val DISCONTINUED = "DISCONTINUED"
}
