package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.PaymentStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.ui.theme.HaloTheme

private val StatusGray = Color(0xFF9E9E9E)
private val StatusOrange = Color(0xFFFF9800)
private val StatusGreen = Color(0xFF4CAF50)
private val StatusRed = Color(0xFFF44336)

@Composable
fun OrderStatusBadge(
    status: String,
    shippingStatus: String?,
    paymentStatus: String?,
    modifier: Modifier = Modifier
) {
    val (text, color) = when {
        paymentStatus == PaymentStatus.REFUNDED -> "환불됨" to StatusRed
        paymentStatus == PaymentStatus.PENDING -> "결제 확인 중" to StatusOrange  // 결제 결과 불명, 서버가 확정 전
        status == OrderStatus.PENDING -> "주문 대기" to StatusGray
        status == OrderStatus.CANCELLED -> "취소됨" to StatusRed
        shippingStatus == ShippingStatus.DELIVERED || status == OrderStatus.DELIVERED -> "배송 완료" to StatusGreen
        shippingStatus == ShippingStatus.SHIPPED ||
                shippingStatus == ShippingStatus.IN_TRANSIT ||
                shippingStatus == ShippingStatus.OUT_FOR_DELIVERY -> "배송중" to StatusOrange
        status == OrderStatus.CONFIRMED -> "주문 확인" to StatusGray
        else -> status to StatusGray
    }
    StatusChip(
        text = text,
        color = color,
        corner = 6.dp,
        horizontalPadding = 8.dp,
        verticalPadding = 4.dp,
        modifier = modifier
    )
}

@Composable
fun GroupShippingBadge(
    status: String?,
    shippingStatus: String?,
    modifier: Modifier = Modifier
) {
    val (text, color) = when {
        status == OrderStatus.CANCELLED -> "취소됨" to StatusRed
        shippingStatus == ShippingStatus.PREPARING -> "배송 준비중" to StatusGray
        shippingStatus == ShippingStatus.SHIPPED -> "발송됨" to StatusOrange
        shippingStatus == ShippingStatus.IN_TRANSIT -> "배송중" to StatusOrange
        shippingStatus == ShippingStatus.OUT_FOR_DELIVERY -> "배송 출발" to StatusOrange
        shippingStatus == ShippingStatus.DELIVERED -> "배송 완료" to StatusGreen
        else -> (shippingStatus ?: status).orEmpty() to StatusGray
    }
    StatusChip(
        text = text,
        color = color,
        corner = 4.dp,
        horizontalPadding = 6.dp,
        verticalPadding = 2.dp,
        modifier = modifier
    )
}

@Composable
private fun StatusChip(
    text: String,
    color: Color,
    corner: Dp,
    horizontalPadding: Dp,
    verticalPadding: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderStatusBadgePreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrderStatusBadge(OrderStatus.PENDING, null, null)
                OrderStatusBadge(OrderStatus.PENDING, null, PaymentStatus.PENDING)
                OrderStatusBadge(OrderStatus.CONFIRMED, ShippingStatus.PREPARING, null)
                OrderStatusBadge(OrderStatus.CONFIRMED, ShippingStatus.IN_TRANSIT, null)
                OrderStatusBadge(OrderStatus.DELIVERED, ShippingStatus.DELIVERED, null)
                OrderStatusBadge(OrderStatus.CANCELLED, null, null)
                OrderStatusBadge(OrderStatus.DELIVERED, null, PaymentStatus.REFUNDED)
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun GroupShippingBadgePreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GroupShippingBadge(OrderStatus.CONFIRMED, ShippingStatus.PREPARING)
                GroupShippingBadge(OrderStatus.CONFIRMED, ShippingStatus.SHIPPED)
                GroupShippingBadge(OrderStatus.CONFIRMED, ShippingStatus.IN_TRANSIT)
                GroupShippingBadge(OrderStatus.CONFIRMED, ShippingStatus.OUT_FOR_DELIVERY)
                GroupShippingBadge(OrderStatus.CANCELLED, null)
            }
        }
    }
}
