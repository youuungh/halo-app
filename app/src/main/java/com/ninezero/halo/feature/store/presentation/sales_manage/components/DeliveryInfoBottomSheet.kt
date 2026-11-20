package com.ninezero.halo.feature.store.presentation.sales_manage.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.copyTextToClipboard
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.GroupShape
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.PaymentStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

private val CopyButtonSize = 36.dp

/** 배송 정보 묶음 시트 */
@Composable
fun DeliveryInfoBottomSheet(
    order: Order,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    FloatingBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        DeliveryInfoContent(order = order, onDismiss = onDismiss)
    }
}

@Composable
private fun DeliveryInfoContent(
    order: Order,
    onDismiss: () -> Unit
) {
    val hazeState = remember { HazeState() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            GlassIconButton(
                onClick = onDismiss,
                icon = painterResource(R.drawable.ic_close),
                contentDescription = "닫기",
                hazeState = hazeState,
                backgroundColor = MaterialTheme.colorScheme.surface,
                size = 36.dp,
                iconSize = 20.dp,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(
                text = "배송 정보",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 이름 위 · 값 아래 줄을 테두리 한 묶음으로
        val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        Column(
            modifier = Modifier
                .hazeSource(hazeState)
                .fillMaxWidth()
                .clip(GroupShape)
                .border(0.5.dp, lineColor, GroupShape)
        ) {
            val rows = listOfNotNull(
                Triple("받는 분", order.shippingName, null),
                Triple("연락처", order.shippingPhone, "연락처를 복사했습니다"),
                Triple("주소", order.shippingAddress, "주소를 복사했습니다"),
                order.memo?.takeIf { it.isNotBlank() }?.let { Triple("배송 메모", it, null) },
                Triple("주문번호", order.orderNumber, "주문번호를 복사했습니다")
            )
            rows.forEachIndexed { index, (label, value, copyMessage) ->
                if (index > 0) HorizontalDivider(thickness = 0.5.dp, color = lineColor)
                DeliveryInfoRow(label = label, value = value, copyMessage = copyMessage)
            }
        }
    }
}

@Composable
private fun DeliveryInfoRow(
    label: String,
    value: String,
    copyMessage: String?
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 6.dp, top = 11.dp, bottom = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (copyMessage != null) {
            // 바탕 원 없이 누르면 리플만
            Box(
                modifier = Modifier
                    .size(CopyButtonSize)
                    .clip(CircleShape)
                    .clickable { context.copyTextToClipboard(value, copyMessage) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_copy),
                    contentDescription = "$label 복사",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun DeliveryInfoContentPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            DeliveryInfoContent(
                order = Order(
                    id = 1,
                    orderNumber = "2026111460778811",
                    items = emptyList(),
                    totalPrice = 92000L,
                    status = OrderStatus.CONFIRMED,
                    shippingAddress = "서울시 강남구 테헤란로 123 12층 (06236)",
                    shippingPhone = "010-1234-5678",
                    shippingName = "홍길동",
                    paymentProvider = "TOSS",
                    paymentStatus = PaymentStatus.COMPLETED,
                    memo = "문 앞에 놓아 주세요",
                    pointsUsed = 0L,
                    couponDiscount = 0L,
                    trackingNumber = null,
                    carrier = null,
                    shippingFee = 3000L,
                    shippingStatus = ShippingStatus.PREPARING,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ),
                onDismiss = {}
            )
        }
    }
}
