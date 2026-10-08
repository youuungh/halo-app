package com.ninezero.halo.feature.store.presentation.order_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun OrderSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun OrderSectionDivider() {
    HorizontalDivider(
        thickness = 6.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 200.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ShippingTracker(
    shippingStatus: String,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        ShippingStatus.PREPARING to "준비중",
        ShippingStatus.SHIPPED to "발송",
        ShippingStatus.IN_TRANSIT to "배송중",
        ShippingStatus.OUT_FOR_DELIVERY to "배송출발",
        ShippingStatus.DELIVERED to "배송완료"
    )

    val currentIndex = steps.indexOfFirst { it.first == shippingStatus }.let {
        if (it < 0) 0 else it
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        steps.forEachIndexed { index, (_, label) ->
            val isActive = index <= currentIndex
            val circleColor = if (isActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outlineVariant

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(circleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            // 연결선
            if (index < steps.lastIndex) {
                val lineColor = if (index < currentIndex)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outlineVariant

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                        .padding(top = 11.dp)  // 원 중앙 높이
                        .height(1.5.dp)
                        .background(lineColor)
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderDetailComponentsPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                OrderSectionHeader(title = "주문 정보")
                InfoRow(label = "주문번호", value = "2026082710000137")
                InfoRow(label = "받는 사람", value = "홍길동")
                InfoRow(label = "주소", value = "서울시 강남구 테헤란로 123 456동 789호")
                OrderSectionDivider()
                OrderSectionHeader(title = "결제 정보")
                InfoRow(label = "결제 수단", value = "토스페이")
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ShippingTrackerPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                ShippingTracker(shippingStatus = ShippingStatus.PREPARING)
                ShippingTracker(shippingStatus = ShippingStatus.IN_TRANSIT)
                ShippingTracker(shippingStatus = ShippingStatus.DELIVERED)
            }
        }
    }
}
