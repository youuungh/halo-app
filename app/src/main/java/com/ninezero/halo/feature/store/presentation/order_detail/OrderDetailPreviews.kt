package com.ninezero.halo.feature.store.presentation.order_detail

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderCreatorGroup
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatusResponse
import java.util.concurrent.TimeUnit

private val previewOrderConfirmed = Order(
    id = 101, orderNumber = "2026082710000137",
    items = listOf(
        OrderItem(1, 1, "프리미엄 블루투스 이어폰 Pro Max", null, "89000",
            originalPrice = "110000", quantity = 1, subtotal = "89000",
            creatorId = 10, creatorName = "TechStore", creatorAvatarUrl = null),
        OrderItem(2, 2, "기계식 키보드 청축", null, "65000",
            originalPrice = "78000", quantity = 2, subtotal = "130000",
            creatorId = 10, creatorName = "TechStore", creatorAvatarUrl = null)
    ),
    totalPrice = "216000", status = OrderStatus.CONFIRMED,
    shippingAddress = "서울시 강남구 테헤란로 123", shippingPhone = "010-1234-5678",
    shippingName = "홍길동", paymentProvider = "MOCK", paymentStatus = "COMPLETED",
    memo = "부재시 문앞에 놓아주세요", pointsUsed = "1000", couponDiscount = "5000",
    trackingNumber = null, carrier = null, shippingFee = "0",
    shippingStatus = ShippingStatus.PREPARING,
    createdAt = System.currentTimeMillis() - 86400000,
    updatedAt = System.currentTimeMillis() - 86400000
)

private val previewOrderDelivered = Order(
    id = 102, orderNumber = "2026082710000274",
    items = listOf(
        OrderItem(3, 3, "오가닉 코튼 티셔츠", null, "39000",
            originalPrice = "49000", quantity = 1, subtotal = "39000",
            creatorId = 20, creatorName = "FashionLab", creatorAvatarUrl = null),
        OrderItem(4, 4, "린넨 와이드 팬츠", null, "59000",
            originalPrice = null, quantity = 1, subtotal = "59000",
            creatorId = 20, creatorName = "FashionLab", creatorAvatarUrl = null),
        OrderItem(5, 5, "핸드메이드 가죽 지갑", null, "85000",
            originalPrice = "95000", quantity = 1, subtotal = "85000",
            creatorId = 30, creatorName = "LeatherCraft", creatorAvatarUrl = null)
    ),
    totalPrice = "183000", status = OrderStatus.DELIVERED,
    shippingAddress = "서울시 마포구 연남로 45", shippingPhone = "010-9876-5432",
    shippingName = "김철수", paymentProvider = "MOCK", paymentStatus = "COMPLETED",
    memo = null, pointsUsed = "0", couponDiscount = "0",
    trackingNumber = "1234567890", carrier = "CJ대한통운", shippingFee = "0",
    shippingStatus = ShippingStatus.DELIVERED,
    actualDeliveryDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5),
    createdAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10),
    updatedAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5)
)

private val previewOrderShipping = Order(
    id = 103, orderNumber = "2026082710000411",
    items = listOf(
        OrderItem(6, 6, "아로마 디퓨저 세트", null, "45000",
            originalPrice = null, quantity = 1, subtotal = "45000",
            creatorId = 40, creatorName = "HomeDecor", creatorAvatarUrl = null)
    ),
    totalPrice = "48000", status = OrderStatus.CONFIRMED,
    shippingAddress = "서울시 서초구 반포대로 200", shippingPhone = "010-5555-6666",
    shippingName = "이영희", paymentProvider = "MOCK", paymentStatus = "COMPLETED",
    memo = "경비실에 맡겨주세요", pointsUsed = "0", couponDiscount = "0",
    trackingNumber = "9876543210", carrier = "한진택배", shippingFee = "3000",
    shippingStatus = ShippingStatus.IN_TRANSIT,
    createdAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3),
    updatedAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
)

private val previewOrderCancelled = Order(
    id = 104, orderNumber = "2026082710000548",
    items = listOf(
        OrderItem(7, 7, "미니 세라믹 화병", null, "28000",
            originalPrice = "32000", quantity = 1, subtotal = "28000",
            creatorId = 50, creatorName = "LivingMood", creatorAvatarUrl = null)
    ),
    totalPrice = "31000", status = OrderStatus.CANCELLED,
    shippingAddress = "서울시 송파구 올림픽로 300", shippingPhone = "010-2222-3333",
    shippingName = "박민지", paymentProvider = "MOCK", paymentStatus = "CANCELLED",
    memo = "배송 전 연락 부탁드려요", pointsUsed = "1000", couponDiscount = "2000",
    trackingNumber = null, carrier = null, shippingFee = "3000",
    shippingStatus = ShippingStatus.PREPARING,
    createdAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(6),
    updatedAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5)
)

private val previewRefundTechItem = OrderItem(
    10, 10, "프리미엄 블루투스 이어폰 Pro Max", null, "89000",
    originalPrice = "110000", quantity = 1, subtotal = "89000",
    creatorId = 10, creatorName = "TechStore", creatorAvatarUrl = null
)
private val previewRefundFashionItem = OrderItem(
    11, 11, "오가닉 코튼 티셔츠", null, "39000",
    originalPrice = "49000", quantity = 1, subtotal = "39000",
    creatorId = 20, creatorName = "FashionLab", creatorAvatarUrl = null
)

// 멀티 판매자 부분 환불 프리뷰
private val previewOrderPartialRefund = Order(
    id = 105, orderNumber = "2026082710000685",
    items = listOf(previewRefundTechItem, previewRefundFashionItem),
    totalPrice = "89000", status = OrderStatus.CONFIRMED,
    shippingAddress = "서울시 강남구 테헤란로 123", shippingPhone = "010-1234-5678",
    shippingName = "홍길동", paymentProvider = "TOSS_PAYMENTS", paymentStatus = "COMPLETED",
    memo = null, pointsUsed = "0", couponDiscount = "0",
    trackingNumber = null, carrier = null, shippingFee = "0",
    shippingStatus = ShippingStatus.PREPARING,
    createdAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
    updatedAt = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2),
    creatorGroups = listOf(
        OrderCreatorGroup(
            creatorId = 10, creatorName = "TechStore", creatorAvatarUrl = null,
            status = OrderStatus.CONFIRMED,
            items = listOf(previewRefundTechItem),
            subtotal = "89000", shippingFee = "0",
            shippingStatus = ShippingStatus.PREPARING
        ),
        OrderCreatorGroup(
            creatorId = 20, creatorName = "FashionLab", creatorAvatarUrl = null,
            status = OrderStatus.CANCELLED,
            items = listOf(previewRefundFashionItem),
            subtotal = "39000", shippingFee = "0",
            shippingStatus = ShippingStatus.PREPARING,
            refundAmount = "39000", refundReason = "단순 변심",
            refundedAt = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)
        )
    )
)

@HaloLightDarkPreview
@Composable
private fun OrderDetailConfirmedPreview() {
    HaloScreenPreview {
        OrderDetailScreenContent(
            state = OrderDetailState(
                isLoading = false,
                order = previewOrderConfirmed,
                shippingStatus = ShippingStatusResponse(
                    shippingStatus = ShippingStatus.PREPARING,
                    trackingNumber = null, carrier = null,
                )
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderDetailDeliveredPreview() {
    HaloScreenPreview {
        OrderDetailScreenContent(
            state = OrderDetailState(
                isLoading = false,
                order = previewOrderDelivered,
                shippingStatus = ShippingStatusResponse(
                    shippingStatus = ShippingStatus.DELIVERED,
                    trackingNumber = "1234567890", carrier = "CJ대한통운",
                )
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderDetailShippingPreview() {
    HaloScreenPreview {
        OrderDetailScreenContent(
            state = OrderDetailState(
                isLoading = false,
                order = previewOrderShipping,
                shippingStatus = ShippingStatusResponse(
                    shippingStatus = ShippingStatus.IN_TRANSIT,
                    trackingNumber = "9876543210", carrier = "한진택배",
                )
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderDetailCancelledPreview() {
    HaloScreenPreview {
        OrderDetailScreenContent(
            state = OrderDetailState(
                isLoading = false,
                order = previewOrderCancelled
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderDetailPartialRefundPreview() {
    HaloScreenPreview {
        OrderDetailScreenContent(
            state = OrderDetailState(
                isLoading = false,
                order = previewOrderPartialRefund,
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderDetailCancelPreview() {
    HaloScreenPreview {
        OrderDetailScreenContent(
            state = OrderDetailState(
                isLoading = false,
                order = previewOrderConfirmed,
                showCancelConfirm = true
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderDetailCancellingPreview() {
    HaloScreenPreview {
        OrderDetailScreenContent(
            state = OrderDetailState(
                isLoading = false,
                order = previewOrderConfirmed,
                isCancelling = true,
                cancelConfirmCreatorId = 10
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderDetailLoadingPreview() {
    HaloScreenPreview {
        OrderDetailScreenContent(
            state = OrderDetailState(isLoading = true)
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderDetailErrorPreview() {
    HaloScreenPreview {
        OrderDetailScreenContent(
            state = OrderDetailState(
                isLoading = false,
                error = "네트워크 연결을 확인해주세요"
            )
        )
    }
}
