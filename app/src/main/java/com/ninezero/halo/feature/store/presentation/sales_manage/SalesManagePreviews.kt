package com.ninezero.halo.feature.store.presentation.sales_manage

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderCreatorGroup
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus

internal fun previewSalesManageState(): SalesManageState {
    return SalesManageState(
        isLoading = false,
        orders = previewOrders
    )
}

internal fun previewSalesManageExpandedState(): SalesManageState {
    return SalesManageState(
        isLoading = false,
        orders = previewOrders,
        expandedOrderIds = setOf(1, 3)
    )
}

internal fun previewSalesManageEmptyState(): SalesManageState {
    return SalesManageState(
        isLoading = false,
        orders = emptyList()
    )
}

internal fun previewSalesManageLoadingState(): SalesManageState {
    return SalesManageState(isLoading = true)
}

internal fun previewSalesManageErrorState(): SalesManageState {
    return SalesManageState(
        isLoading = false,
        error = "네트워크 연결을 확인해주세요"
    )
}

internal fun previewSalesManageCancelDialogState(): SalesManageState {
    return SalesManageState(
        isLoading = false,
        orders = previewOrders,
        showCancelConfirm = true,
        cancelConfirmOrderId = previewOrders.first().id
    )
}

private val previewOrders = listOf(
    previewOrder(
        id = 1,
        userId = 100,
        orderNumber = "2026030871483781",
        items = listOf(
            previewOrderItem(
                id = 1,
                productId = 1,
                productName = "프리미엄 블루투스 이어폰 Pro Max",
                price = "89000",
                originalPrice = "110000",
                quantity = 1,
                subtotal = "89000"
            ),
            previewOrderItem(
                id = 2,
                productId = 2,
                productName = "기계식 키보드 청축",
                price = "65000",
                quantity = 2,
                subtotal = "130000"
            )
        ),
        totalPrice = "219000",
        status = OrderStatus.PENDING,
        shippingAddress = "서울시 강남구 테헤란로 123",
        shippingPhone = "010-1234-5678",
        shippingName = "홍길동",
        memo = "부재시 문 앞에 놓아주세요",
        shippingStatus = ShippingStatus.PREPARING,
        createdAtOffset = 3_600_000L,
        updatedAtOffset = 3_600_000L
    ),
    previewOrder(
        id = 2,
        userId = 101,
        orderNumber = "2026030771580591",
        items = listOf(
            previewOrderItem(
                id = 3,
                productId = 3,
                productName = "오가닉 코튼 티셔츠",
                price = "39000",
                originalPrice = "49000",
                quantity = 1,
                subtotal = "39000"
            )
        ),
        totalPrice = "39000",
        status = OrderStatus.CONFIRMED,
        shippingAddress = "서울시 마포구 연남로 45",
        shippingPhone = "010-9876-5432",
        shippingName = "김철수",
        shippingFee = "3000",
        shippingStatus = ShippingStatus.PREPARING,
        createdAtOffset = 86_400_000L,
        updatedAtOffset = 86_400_000L
    ),
    previewOrder(
        id = 3,
        userId = 102,
        orderNumber = "2026030571669482",
        items = listOf(
            previewOrderItem(
                id = 4,
                productId = 4,
                productName = "핸드메이드 가죽 지갑",
                price = "85000",
                originalPrice = "95000",
                quantity = 1,
                subtotal = "85000"
            )
        ),
        totalPrice = "85000",
        status = OrderStatus.CONFIRMED,
        shippingAddress = "서울시 서초구 반포대로 200",
        shippingPhone = "010-5555-6666",
        shippingName = "이영희",
        trackingNumber = "1234567890",
        carrier = "CJ대한통운",
        shippingStatus = ShippingStatus.IN_TRANSIT,
        createdAtOffset = 259_200_000L,
        updatedAtOffset = 172_800_000L
    ),
    previewOrder(
        id = 4,
        userId = 103,
        orderNumber = "2026030471766292",
        items = listOf(
            previewOrderItem(
                id = 5,
                productId = 5,
                productName = "미니 세라믹 화병",
                price = "28000",
                originalPrice = "32000",
                quantity = 1,
                subtotal = "28000"
            )
        ),
        totalPrice = "31000",
        status = OrderStatus.CANCELLED,
        shippingAddress = "서울시 송파구 올림픽로 300",
        shippingPhone = "010-2222-3333",
        shippingName = "박민지",
        memo = "배송 전 연락 부탁드려요",
        pointsUsed = "1000",
        couponDiscount = "2000",
        shippingFee = "3000",
        shippingStatus = ShippingStatus.PREPARING,
        paymentStatus = "CANCELLED",
        createdAtOffset = 345_600_000L,
        updatedAtOffset = 259_200_000L
    )
)

private fun previewOrder(
    id: Int,
    userId: Int,
    orderNumber: String,
    items: List<OrderItem>,
    totalPrice: String,
    status: String,
    shippingAddress: String,
    shippingPhone: String,
    shippingName: String,
    shippingStatus: String,
    createdAtOffset: Long,
    updatedAtOffset: Long,
    memo: String? = null,
    pointsUsed: String = "0",
    couponDiscount: String = "0",
    trackingNumber: String? = null,
    carrier: String? = null,
    shippingFee: String = "0",
    paymentStatus: String = "COMPLETED",
    now: Long = System.currentTimeMillis()
): Order {
    val creatorGroup = OrderCreatorGroup(
        creatorId = 1,
        creatorName = "MyStore",
        status = status,
        items = items,
        subtotal = totalPrice,
        shippingFee = shippingFee,
        trackingNumber = trackingNumber,
        carrier = carrier,
        shippingStatus = shippingStatus
    )

    return Order(
        id = id,
        orderNumber = orderNumber,
        items = items,
        totalPrice = totalPrice,
        status = status,
        shippingAddress = shippingAddress,
        shippingPhone = shippingPhone,
        shippingName = shippingName,
        paymentProvider = "MOCK",
        paymentStatus = paymentStatus,
        memo = memo,
        pointsUsed = pointsUsed,
        couponDiscount = couponDiscount,
        trackingNumber = trackingNumber,
        carrier = carrier,
        shippingFee = shippingFee,
        shippingStatus = shippingStatus,
        createdAt = now - createdAtOffset,
        updatedAt = now - updatedAtOffset,
        creatorGroups = listOf(creatorGroup)
    )
}

private fun previewOrderItem(
    id: Int,
    productId: Int,
    productName: String,
    price: String,
    quantity: Int,
    subtotal: String,
    originalPrice: String? = null
): OrderItem {
    return OrderItem(
        id = id,
        productId = productId,
        productName = productName,
        productImageUrl = null,
        price = price,
        originalPrice = originalPrice,
        quantity = quantity,
        subtotal = subtotal,
        creatorId = 1,
        creatorName = "MyStore",
        creatorAvatarUrl = null
    )
}

@HaloLightDarkPreview
@Composable
private fun SalesManageScreenPreview() {
    HaloScreenPreview {
        SalesManageScreenContent(
            state = previewSalesManageState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesManageScreenExpandedPreview() {
    HaloScreenPreview {
        SalesManageScreenContent(
            state = previewSalesManageExpandedState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesManageScreenLoadingPreview() {
    HaloScreenPreview {
        SalesManageScreenContent(
            state = previewSalesManageLoadingState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesManageScreenEmptyPreview() {
    HaloScreenPreview {
        SalesManageScreenContent(
            state = previewSalesManageEmptyState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesManageScreenErrorPreview() {
    HaloScreenPreview {
        SalesManageScreenContent(
            state = previewSalesManageErrorState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesManageScreenCancelDialogPreview() {
    HaloScreenPreview {
        SalesManageScreenContent(
            state = previewSalesManageCancelDialogState()
        )
    }
}
