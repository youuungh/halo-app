package com.ninezero.halo.feature.notification.domain.model

object NotificationType {
    // Social 알림
    const val FOLLOW = "FOLLOW"
    const val LIKE_POST = "LIKE_POST"
    const val LIKE_COMMENT = "LIKE_COMMENT"
    const val COMMENT = "COMMENT"
    const val REPLY = "REPLY"
    const val MENTION = "MENTION"
    const val NEW_POST = "NEW_POST"

    // Commerce 알림 - 주문
    const val ORDER_CREATED = "ORDER_CREATED"
    const val ORDER_RECEIVED = "ORDER_RECEIVED"
    const val ORDER_CONFIRMED = "ORDER_CONFIRMED"
    const val ORDER_CANCELLED = "ORDER_CANCELLED"

    // Commerce 알림 - 배송
    const val SHIPPING_STARTED = "SHIPPING_STARTED"
    const val SHIPPING_IN_TRANSIT = "SHIPPING_IN_TRANSIT"
    const val SHIPPING_OUT_FOR_DELIVERY = "SHIPPING_OUT_FOR_DELIVERY"
    const val SHIPPING_DELIVERED = "SHIPPING_DELIVERED"

    // Commerce 알림 - 기타
    const val PRODUCT_REVIEWED = "PRODUCT_REVIEWED"
    const val NEW_PRODUCT = "NEW_PRODUCT"

    // Chat 알림
    const val CHAT_MESSAGE = "CHAT_MESSAGE"

    // Point 알림
    const val POINT_EARNED = "POINT_EARNED"
    const val POINT_EXPIRED = "POINT_EXPIRED"

    // Creator 알림
    const val CREATOR_APPROVED = "CREATOR_APPROVED"
    const val CREATOR_REJECTED = "CREATOR_REJECTED"
    const val CREATOR_DEMOTED = "CREATOR_DEMOTED"

    // 시스템 알림
    const val SYSTEM_ANNOUNCEMENT = "SYSTEM_ANNOUNCEMENT"
}
