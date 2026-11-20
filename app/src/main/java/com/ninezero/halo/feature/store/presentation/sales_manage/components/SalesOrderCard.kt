package com.ninezero.halo.feature.store.presentation.sales_manage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.CardShape
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.PaymentStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.feature.store.presentation.components.OrderGroupAction
import com.ninezero.halo.feature.store.presentation.components.OrderGroupActionBar
import com.ninezero.halo.feature.store.presentation.components.OrderStatusBadge

private val ProductImageSize = 60.dp

/** 멤버십 관리 카드 틀 + 글자 버튼 바 */
@Composable
fun SalesOrderCard(
    order: Order,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onRegisterShipping: () -> Unit,
    onEditShipping: () -> Unit,
    onShowDeliveryInfo: () -> Unit,
    onTrackShipping: () -> Unit,
    modifier: Modifier = Modifier,
    isStatusUpdating: Boolean = false,
    isCancelling: Boolean = false,
) {
    val numberFormat = rememberKoreanNumberFormat()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .cardBorder(CardShape)
            .clip(CardShape)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp)) {
            // 상태 · 날짜
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrderStatusBadge(order.status, order.shippingStatus, order.paymentStatus)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = order.createdAt.toFormattedDate("yyyy.MM.dd HH:mm"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 대표 상품, '배송 정보 ›'를 사진 밑단에
            val firstItem = order.items.firstOrNull()
            val hasImage = firstItem?.productImageUrl != null
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasImage) {
                    AsyncImage(
                        model = firstItem.productImageUrl,
                        contentDescription = firstItem.productName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(ProductImageSize)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .then(if (hasImage) Modifier.height(ProductImageSize) else Modifier),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (firstItem != null) {
                        // 이름 · 값은 위에 붙이고 '배송 정보 ›'만 아래 끝으로
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = firstItem.productName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (order.items.size > 1) {
                                    Text(
                                        text = " 외 ${order.items.size - 1}건",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                            Text(
                                text = "${numberFormat.format(firstItem.price)}원 × ${firstItem.quantity}개",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onShowDeliveryInfo
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "배송 정보",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 할 일이 없으면 바도 없음
        OrderGroupActionBar(
            salesActions(
                order = order,
                isStatusUpdating = isStatusUpdating,
                isCancelling = isCancelling,
                onConfirm = onConfirm,
                onCancel = onCancel,
                onRegisterShipping = onRegisterShipping,
                onEditShipping = onEditShipping,
                onTrackShipping = onTrackShipping
            )
        )
    }
}

/** 판매 취소는 왼쪽 빨강, 할 일은 오른쪽 파랑 */
@Composable
private fun salesActions(
    order: Order,
    isStatusUpdating: Boolean,
    isCancelling: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onRegisterShipping: () -> Unit,
    onEditShipping: () -> Unit,
    onTrackShipping: () -> Unit
): List<OrderGroupAction> {
    val colors = MaterialTheme.colorScheme
    val enabled = !isStatusUpdating
    val cancel = OrderGroupAction(
        label = "판매 취소",
        color = colors.error,
        isLoading = isStatusUpdating && isCancelling,
        enabled = enabled,
        onClick = onCancel
    )
    // 결제가 확정되지 않았거나 환불된 주문은 처리할 일이 없음
    val isNotPaid = order.paymentStatus == PaymentStatus.PENDING || order.paymentStatus == PaymentStatus.REFUNDED

    return when {
        isNotPaid || order.status == OrderStatus.CANCELLED -> emptyList()

        order.status == OrderStatus.PENDING -> listOf(
            cancel,
            OrderGroupAction(
                label = "주문 확인",
                color = colors.primary,
                isLoading = isStatusUpdating && !isCancelling,
                enabled = enabled,
                onClick = onConfirm
            )
        )

        order.status == OrderStatus.CONFIRMED &&
            order.shippingStatus == ShippingStatus.PREPARING &&
            order.trackingNumber == null -> listOf(
            cancel,
            OrderGroupAction("송장번호 입력", colors.primary, enabled = enabled, onClick = onRegisterShipping)
        )

        order.trackingNumber != null -> listOfNotNull(
            // SHIPPED까지만 수정
            OrderGroupAction("송장 수정", colors.onSurface, enabled = enabled, onClick = onEditShipping)
                .takeIf { order.shippingStatus == ShippingStatus.SHIPPED },
            OrderGroupAction("배송 조회", colors.primary, onClick = onTrackShipping)
        )

        else -> emptyList()
    }
}

private fun previewOrder(
    id: Int,
    status: OrderStatus,
    shippingStatus: ShippingStatus,
    trackingNumber: String? = null
) = Order(
    id = id,
    orderNumber = "2026082710000$id",
    items = listOf(
        OrderItem(
            id = id * 10,
            productId = id * 10,
            productName = "프리미엄 무선 이어폰 Pro Max",
            productImageUrl = null,
            price = 89000L,
            quantity = 1,
            subtotal = 89000L,
            creatorId = 1,
            creatorName = "크리에이터 샵"
        )
    ),
    totalPrice = 92000L,
    status = status,
    shippingAddress = "서울시 강남구 테헤란로 123",
    shippingPhone = "010-1234-5678",
    shippingName = "홍길동",
    paymentProvider = "TOSS",
    paymentStatus = PaymentStatus.COMPLETED,
    memo = null,
    pointsUsed = 0L,
    couponDiscount = 0L,
    trackingNumber = trackingNumber,
    carrier = if (trackingNumber != null) "CJ대한통운" else null,
    shippingFee = 3000L,
    shippingStatus = shippingStatus,
    createdAt = System.currentTimeMillis() - 86_400_000L,
    updatedAt = System.currentTimeMillis()
)

@Composable
private fun PreviewCard(order: Order, isStatusUpdating: Boolean = false) {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                SalesOrderCard(
                    order = order,
                    onConfirm = {},
                    onCancel = {},
                    onRegisterShipping = {},
                    onEditShipping = {},
                    onShowDeliveryInfo = {},
                    onTrackShipping = {},
                    isStatusUpdating = isStatusUpdating
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesOrderCardPendingPreview() {
    PreviewCard(previewOrder(1, OrderStatus.PENDING, ShippingStatus.PREPARING))
}

@HaloLightDarkPreview
@Composable
private fun SalesOrderCardAwaitingShippingPreview() {
    PreviewCard(previewOrder(2, OrderStatus.CONFIRMED, ShippingStatus.PREPARING))
}

@HaloLightDarkPreview
@Composable
private fun SalesOrderCardShippingPreview() {
    PreviewCard(previewOrder(3, OrderStatus.CONFIRMED, ShippingStatus.SHIPPED, trackingNumber = "601234567890"))
}

@HaloLightDarkPreview
@Composable
private fun SalesOrderCardUpdatingPreview() {
    PreviewCard(previewOrder(4, OrderStatus.PENDING, ShippingStatus.PREPARING), isStatusUpdating = true)
}
