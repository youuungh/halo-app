package com.ninezero.halo.feature.store.data.remote.dto

import com.ninezero.halo.core.network.CursorDto
import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderCreatorGroup
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.ShippingProgress
import com.ninezero.halo.feature.store.domain.model.ShippingStatusResponse
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: Int,
    val userId: Int,
    val orderNumber: String,
    val items: List<OrderItemDto>,
    val creatorGroups: List<OrderCreatorGroupDto> = emptyList(),
    val totalPrice: String,
    val status: String,
    val shippingAddress: String,
    val shippingPhone: String,
    val shippingName: String,
    val paymentProvider: String? = null,
    val paymentStatus: String? = null,
    val memo: String? = null,
    val pointsUsed: String = "0",
    val couponDiscount: String = "0",
    val couponCode: String? = null,
    val totalEarnPoints: String = "0",
    val shippingFee: String = "0",
    val trackingNumber: String? = null,
    val carrier: String? = null,
    val shippingStatus: String,
    val estimatedDeliveryDate: String? = null,
    val actualDeliveryDate: String? = null,
    val refundAmount: String? = null,
    val refundReason: String? = null,
    val refundedAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class OrderCreatorGroupDto(
    val creatorId: Int? = null,
    val creatorName: String? = null,
    val creatorAvatarUrl: String? = null,
    val creatorRole: String? = null,
    val status: String,
    val items: List<OrderItemDto>,
    val subtotal: String,
    val earnTier: String = "FREE",
    val earnRate: String = "0",
    val earnPoints: String = "0",
    val isSubscribed: Boolean = false,
    val shippingFee: String = "0",
    val couponDiscount: String = "0",
    val trackingNumber: String? = null,
    val carrier: String? = null,
    val shippingStatus: String,
    val estimatedDeliveryDate: String? = null,
    val actualDeliveryDate: String? = null,
    val refundAmount: String? = null,
    val refundReason: String? = null,
    val refundedAt: String? = null
)

typealias OrderListResponseDto = CursorDto<OrderDto>

@Serializable
data class OrderItemDto(
    val id: Int,
    val productId: Int,
    val productName: String,
    val productImageUrl: String? = null,
    val quantity: Int,
    val price: String,
    val originalPrice: String? = null,
    val subtotal: String,
    val creatorId: Int? = null,
    val creatorName: String? = null,
    val creatorAvatarUrl: String? = null,
    val hasReview: Boolean = false,
    val reviewId: Int? = null,
    val isProductActive: Boolean = true,  // 판매 가능 여부
    val requiredTier: String = "FREE",
    val canAccess: Boolean = true  // 구매자 현재 구독 기준
)

@Serializable
data class CreateOrderRequestDto(
    val shippingAddress: String,
    val shippingPhone: String,
    val shippingName: String,
    val paymentProvider: String,
    val memo: String? = null,
    val pointsToUse: String? = null,
    val couponCodes: List<String>? = null,
    val cartItemIds: List<Int>? = null
)

@Serializable
data class CreateDirectOrderRequestDto(
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

@Serializable
data class UpdateOrderStatusRequestDto(
    val status: String
)

@Serializable
data class UpdateShippingRequestDto(
    val trackingNumber: String,
    val carrier: String
)

@Serializable
data class ShippingStatusResponseDto(
    val orderId: Int,
    val creatorId: Int? = null,
    val trackingNumber: String? = null,
    val carrier: String? = null,
    val shippingStatus: String,
    val estimatedDeliveryDate: String? = null,
    val actualDeliveryDate: String? = null,
    val courierName: String? = null,
    val deliveryStatusText: String? = null,
    val lastProgressAt: String? = null,
    val queriedAt: String? = null,
    val progresses: List<ShippingProgressDto> = emptyList()
)

@Serializable
data class ShippingProgressDto(
    val dateTime: String? = null,
    val location: String? = null,
    val status: String? = null,
    val statusCode: String? = null,
    val description: String? = null
)

fun OrderDto.toDomain(): Order = Order(
    id = id,
    orderNumber = orderNumber,
    items = items.map { it.toDomain() },
    creatorGroups = creatorGroups.map { it.toDomain() },
    totalPrice = totalPrice,
    status = status,
    shippingAddress = shippingAddress,
    shippingPhone = shippingPhone,
    shippingName = shippingName,
    paymentProvider = paymentProvider,
    paymentStatus = paymentStatus,
    memo = memo,
    pointsUsed = pointsUsed,
    couponDiscount = couponDiscount,
    trackingNumber = trackingNumber,
    carrier = carrier,
    shippingFee = shippingFee,
    shippingStatus = shippingStatus,
    actualDeliveryDate = actualDeliveryDate.parseTimestampOrNull(),
    createdAt = createdAt.parseTimestamp(),
    updatedAt = updatedAt.parseTimestamp()
)

fun OrderCreatorGroupDto.toDomain(): OrderCreatorGroup = OrderCreatorGroup(
    creatorId = creatorId,
    creatorName = creatorName,
    creatorAvatarUrl = creatorAvatarUrl,
    creatorRole = creatorRole,
    status = status,
    items = items.map { it.toDomain() },
    subtotal = subtotal,
    earnTier = runCatching { SubscriptionPlanTier.valueOf(earnTier) }.getOrDefault(SubscriptionPlanTier.FREE),
    earnRate = earnRate.toDoubleOrNull() ?: 0.0,
    earnPoints = earnPoints.toLongOrNull() ?: 0L,
    isSubscribed = isSubscribed,
    shippingFee = shippingFee,
    couponDiscount = couponDiscount,
    trackingNumber = trackingNumber,
    carrier = carrier,
    shippingStatus = shippingStatus,
    refundAmount = refundAmount,
    refundReason = refundReason,
    refundedAt = refundedAt.parseTimestampOrNull()
)

fun OrderItemDto.toDomain(): OrderItem = OrderItem(
    id = id,
    productId = productId,
    productName = productName,
    productImageUrl = productImageUrl,
    price = price,
    originalPrice = originalPrice,
    quantity = quantity,
    subtotal = subtotal,
    creatorId = creatorId,
    creatorName = creatorName,
    creatorAvatarUrl = creatorAvatarUrl,
    hasReview = hasReview,
    isProductActive = isProductActive,
    requiredTier = requiredTier,
    canAccess = canAccess
)

fun ShippingStatusResponseDto.toDomain(): ShippingStatusResponse =
    ShippingStatusResponse(
        shippingStatus = shippingStatus,
        trackingNumber = trackingNumber,
        carrier = carrier,
        courierName = courierName,
        deliveryStatusText = deliveryStatusText,
        lastProgressAt = lastProgressAt,
        queriedAt = queriedAt,
        progresses = progresses.map { it.toDomain() }
    )

fun ShippingProgressDto.toDomain(): ShippingProgress = ShippingProgress(
    dateTime = dateTime,
    location = location,
    status = status,
    statusCode = statusCode,
    description = description
)

private fun String?.parseTimestampOrNull(): Long? {
    return this?.takeIf { it.isNotBlank() }?.parseTimestamp()?.takeIf { it > 0L }
}
