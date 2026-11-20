package com.ninezero.halo.feature.store.domain.model

/** 서버 enum과 같은 값 */
enum class OrderStatus { PENDING, CONFIRMED, DELIVERED, CANCELLED }

enum class ShippingStatus { PREPARING, SHIPPED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED }

enum class PaymentStatus { PENDING, COMPLETED, FAILED, CANCELLED, REFUND_PENDING, REFUNDED }

enum class ProductStatus { ACTIVE, SOLD_OUT, DISCONTINUED }
