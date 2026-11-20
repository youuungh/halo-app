package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.store.domain.model.ShippingStatus

/** 배송 단계 */
@Composable
fun ShippingTracker(
    shippingStatus: ShippingStatus,
    modifier: Modifier = Modifier
) {
    val currentIndex = SHIPPING_STEPS.indexOfFirst { it.first == shippingStatus }.coerceAtLeast(0)
    val activeColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val progress = currentIndex.toFloat() / SHIPPING_STEPS.lastIndex

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(contentAlignment = Alignment.CenterStart) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(trackColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(3.dp)
                        .background(activeColor)
                )
            }
            StepRow {
                SHIPPING_STEPS.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (index <= currentIndex) activeColor else trackColor)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        StepRow {
            SHIPPING_STEPS.forEachIndexed { index, (_, label) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == currentIndex) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

/** 첫 칸은 왼쪽 끝, 끝 칸은 오른쪽 끝 */
@Composable
private fun StepRow(content: @Composable () -> Unit) {
    Layout(content = content, modifier = Modifier.fillMaxWidth()) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val width = constraints.maxWidth
        val height = placeables.maxOfOrNull { it.height } ?: 0
        val lastIndex = placeables.lastIndex
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val point = if (lastIndex == 0) 0 else width * index / lastIndex
                val x = (point - placeable.width / 2).coerceIn(0, width - placeable.width)
                placeable.placeRelative(x, (height - placeable.height) / 2)
            }
        }
    }
}

/** 배송 단계와 짧은 이름 */
internal val SHIPPING_STEPS = listOf(
    ShippingStatus.PREPARING to "준비중",
    ShippingStatus.SHIPPED to "발송",
    ShippingStatus.IN_TRANSIT to "배송중",
    ShippingStatus.OUT_FOR_DELIVERY to "배송출발",
    ShippingStatus.DELIVERED to "배송완료"
)

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
