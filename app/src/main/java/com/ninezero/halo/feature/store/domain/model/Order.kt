package com.ninezero.halo.feature.store.domain.model

import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier

data class Order(
    val id: Int,
    val orderNumber: String,
    val items: List<OrderItem>,
    val totalPrice: String,
    val status: String,
    val shippingAddress: String,
    val shippingPhone: String,
    val shippingName: String,
    val paymentProvider: String?,
    val paymentStatus: String?,
    val memo: String?,
    val pointsUsed: String,
    val couponDiscount: String,
    val trackingNumber: String?,
    val carrier: String?,
    val shippingFee: String = "0",
    val shippingStatus: String,
    val actualDeliveryDate: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val creatorGroups: List<OrderCreatorGroup> = emptyList()
)

data class OrderCreatorGroup(
    val creatorId: Int?,
    val creatorName: String? = null,
    val creatorAvatarUrl: String? = null,
    val creatorRole: String? = null,
    val status: String,
    val items: List<OrderItem>,
    val subtotal: String,
    // 적립은 서버 계산값
    val earnTier: SubscriptionPlanTier = SubscriptionPlanTier.FREE,
    val earnRate: Double = 0.0,
    val earnPoints: Long = 0,
    val isSubscribed: Boolean = false,
    val shippingFee: String = "0",
    val couponDiscount: String = "0",
    val trackingNumber: String? = null,
    val carrier: String? = null,
    val shippingStatus: String,
    val refundAmount: String? = null,
    val refundReason: String? = null,
    val refundedAt: Long? = null
)

data class OrderItem(
    val id: Int,
    val productId: Int,
    val productName: String,
    val productImageUrl: String?,
    val price: String,
    val originalPrice: String? = null,
    val quantity: Int,
    val subtotal: String,
    val creatorId: Int? = null,
    val creatorName: String? = null,
    val creatorAvatarUrl: String? = null,
    val hasReview: Boolean = false,
    val isProductActive: Boolean = true
)

// 배송 추적 시각 3개는 외부 API
data class ShippingStatusResponse(
    val shippingStatus: String,
    val trackingNumber: String?,
    val carrier: String?,
    val courierName: String? = null,
    val deliveryStatusText: String? = null,
    val lastProgressAt: String? = null,
    val queriedAt: String? = null,
    val progresses: List<ShippingProgress> = emptyList()
)

fun Order.creatorGroupsOrFallback(): List<OrderCreatorGroup> {
    if (creatorGroups.isNotEmpty()) return creatorGroups

    val groupedItems = items.groupBy { it.creatorId }
    val singleGroup = groupedItems.size <= 1

    return groupedItems.map { (creatorId, grouped) ->
        val subtotal = grouped.sumOf { it.subtotal.toLongOrNull() ?: 0L }.toString()
        OrderCreatorGroup(
            creatorId = creatorId,
            creatorName = grouped.firstOrNull()?.creatorName,
            creatorAvatarUrl = grouped.firstOrNull()?.creatorAvatarUrl,
            status = if (singleGroup) status else "PENDING",
            items = grouped,
            subtotal = subtotal,
            shippingFee = if (singleGroup) shippingFee else "0",
            trackingNumber = if (singleGroup) trackingNumber else null,
            carrier = if (singleGroup) carrier else null,
            shippingStatus = if (singleGroup) shippingStatus else "PREPARING",
        )
    }
}

data class ShippingProgress(
    val dateTime: String? = null,
    val location: String? = null,
    val status: String? = null,
    val statusCode: String? = null,
    val description: String? = null
)
