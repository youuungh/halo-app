package com.ninezero.halo.feature.store.presentation.order_list

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderCreatorGroup
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import java.util.concurrent.TimeUnit

private val previewOrders = listOf(
    Order(
        id = 101, orderNumber = "2026082710000137",
        items = listOf(
            OrderItem(1, 1, "프리미엄 블루투스 이어폰", null, "79000", originalPrice = null, quantity = 1, subtotal = "79000"),
            OrderItem(2, 2, "무선 충전기", null, "35000", originalPrice = null, quantity = 2, subtotal = "75000")
        ),
        totalPrice = "154000", status = OrderStatus.DELIVERED,
        shippingAddress = "서울시", shippingPhone = "010-1234-5678", shippingName = "홍길동",
        paymentProvider = null, paymentStatus = null, memo = null,
        pointsUsed = "0", couponDiscount = "0", trackingNumber = null, carrier = null,
        shippingStatus = "DELIVERED",
        actualDeliveryDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10),
        createdAt = System.currentTimeMillis() - 86400000 * 3,
        updatedAt = System.currentTimeMillis() - 86400000 * 3
    ),
    Order(
        id = 102, orderNumber = "2026082710000274",
        items = listOf(OrderItem(3, 3, "스마트 워치", null, "250000", originalPrice = null, quantity = 1, subtotal = "250000")),
        totalPrice = "250000", status = OrderStatus.DELIVERED,
        shippingAddress = "서울시", shippingPhone = "010-1234-5678", shippingName = "홍길동",
        paymentProvider = null, paymentStatus = null, memo = null,
        pointsUsed = "0", couponDiscount = "0", trackingNumber = null, carrier = null,
        shippingStatus = "DELIVERED",
        actualDeliveryDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(29),
        createdAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30),
        updatedAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(29)
    ),
    Order(
        id = 103, orderNumber = "2026082710000411",
        items = listOf(OrderItem(4, 4, "올인원 충전 케이블", null, "12000", originalPrice = null, quantity = 1, subtotal = "12000")),
        totalPrice = "12000", status = OrderStatus.PENDING,
        shippingAddress = "서울시", shippingPhone = "010-1234-5678", shippingName = "홍길동",
        paymentProvider = null, paymentStatus = null, memo = null,
        pointsUsed = "0", couponDiscount = "0", trackingNumber = null, carrier = null,
        shippingStatus = "PREPARING",
        createdAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
        updatedAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
    ),
    Order(
        id = 104, orderNumber = "2026082710000548",
        items = listOf(OrderItem(5, 5, "미니 가습기", null, "39000", originalPrice = "45000", quantity = 1, subtotal = "39000")),
        totalPrice = "42000", status = OrderStatus.CANCELLED,
        shippingAddress = "서울시", shippingPhone = "010-1234-5678", shippingName = "홍길동",
        paymentProvider = null, paymentStatus = "CANCELLED", memo = "문 앞에 놓아주세요",
        pointsUsed = "1000", couponDiscount = "2000", trackingNumber = null, carrier = null,
        shippingStatus = ShippingStatus.PREPARING,
        createdAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
        updatedAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
    )
)

@HaloLightDarkPreview
@Composable
private fun OrderListScreenPreview() {
    OrderListPreviewContent(
        state = OrderListState(isLoading = false, orders = previewOrders, hasMore = false)
    )
}

@HaloLightDarkPreview
@Composable
private fun OrderListExpandedPreview() {
    OrderListPreviewContent(
        state = OrderListState(
            isLoading = false,
            orders = previewOrders,
            hasMore = false,
            expandedOrderIds = setOf(1)
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun OrderListMultiCreatorPreview() {
    val multiCreatorOrder = Order(
        id = 105, orderNumber = "2026082710000685",
        items = listOf(
            OrderItem(10, 10, "프리미엄 블루투스 이어폰", null, "89000",
                originalPrice = "110000", quantity = 1, subtotal = "89000",
                creatorId = 100, creatorName = "TechStore", creatorAvatarUrl = null),
            OrderItem(11, 11, "기계식 키보드 청축", null, "65000",
                originalPrice = null, quantity = 1, subtotal = "65000",
                creatorId = 100, creatorName = "TechStore", creatorAvatarUrl = null),
            OrderItem(12, 12, "오가닉 코튼 티셔츠", null, "39000",
                originalPrice = "49000", quantity = 1, subtotal = "39000",
                creatorId = 200, creatorName = "FashionLab", creatorAvatarUrl = null)
        ),
        totalPrice = "193000", status = OrderStatus.CONFIRMED,
        shippingAddress = "서울시 강남구 테헤란로 123",
        shippingPhone = "010-1234-5678", shippingName = "홍길동",
        paymentProvider = null, paymentStatus = null, memo = null,
        pointsUsed = "0", couponDiscount = "0",
        trackingNumber = null, carrier = null,
        shippingStatus = ShippingStatus.PREPARING,
        createdAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
        updatedAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
        creatorGroups = listOf(
            OrderCreatorGroup(
                creatorId = 100, creatorName = "TechStore", creatorAvatarUrl = null,
                status = OrderStatus.CONFIRMED,
                items = listOf(
                    OrderItem(10, 10, "프리미엄 블루투스 이어폰", null, "89000",
                        originalPrice = "110000", quantity = 1, subtotal = "89000",
                        creatorId = 100, creatorName = "TechStore", creatorAvatarUrl = null),
                    OrderItem(11, 11, "기계식 키보드 청축", null, "65000",
                        originalPrice = null, quantity = 1, subtotal = "65000",
                        creatorId = 100, creatorName = "TechStore", creatorAvatarUrl = null)
                ),
                subtotal = "154000", shippingFee = "0",
                trackingNumber = "1234567890", carrier = "CJ대한통운",
                shippingStatus = ShippingStatus.IN_TRANSIT
            ),
            OrderCreatorGroup(
                creatorId = 200, creatorName = "FashionLab", creatorAvatarUrl = null,
                status = OrderStatus.CANCELLED,
                items = listOf(
                    OrderItem(12, 12, "오가닉 코튼 티셔츠", null, "39000",
                        originalPrice = "49000", quantity = 1, subtotal = "39000",
                        creatorId = 200, creatorName = "FashionLab", creatorAvatarUrl = null)
                ),
                subtotal = "39000", shippingFee = "3000",
                shippingStatus = ShippingStatus.PREPARING
            )
        )
    )
    OrderListPreviewContent(
        state = OrderListState(
            isLoading = false,
            orders = listOf(multiCreatorOrder),
            hasMore = false,
            expandedOrderIds = setOf(10)
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun OrderListLoadingPreview() {
    OrderListPreviewContent(state = OrderListState(isLoading = true))
}

@HaloLightDarkPreview
@Composable
private fun OrderListEmptyPreview() {
    OrderListPreviewContent(
        state = OrderListState(isLoading = false, orders = emptyList(), hasMore = false)
    )
}

@HaloLightDarkPreview
@Composable
private fun OrderListErrorPreview() {
    OrderListPreviewContent(
        state = OrderListState(
            isLoading = false,
            error = "네트워크 연결을 확인해주세요"
        )
    )
}

@Composable
private fun OrderListPreviewContent(
    state: OrderListState
) {
    HaloScreenPreview {
        OrderListScreenContent(
            state = state
        )
    }
}
