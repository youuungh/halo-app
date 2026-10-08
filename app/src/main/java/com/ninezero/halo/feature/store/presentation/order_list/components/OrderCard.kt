package com.ninezero.halo.feature.store.presentation.order_list.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.feature.store.domain.model.creatorGroupsOrFallback
import com.ninezero.halo.feature.store.presentation.components.GroupShippingBadge
import com.ninezero.halo.feature.store.presentation.components.OrderStatusBadge
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun OrderCard(
    order: Order,
    isExpanded: Boolean,
    onOrderClick: () -> Unit,
    onToggleExpand: () -> Unit,
    onWriteReview: (OrderItem) -> Unit,
    onAddToCart: (OrderItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = rememberKoreanNumberFormat()
    val creatorGroups = remember(order) { order.creatorGroupsOrFallback() }
    val primaryGroup = creatorGroups.firstOrNull()

    // 그룹 상태를 대표 상태로 집계
    val effectiveShippingStatus = remember(creatorGroups) {
        when {
            creatorGroups.isNotEmpty() && creatorGroups.all { it.shippingStatus == ShippingStatus.DELIVERED } ->
                ShippingStatus.DELIVERED
            creatorGroups.any {
                it.shippingStatus == ShippingStatus.SHIPPED ||
                        it.shippingStatus == ShippingStatus.IN_TRANSIT ||
                        it.shippingStatus == ShippingStatus.OUT_FOR_DELIVERY
            } -> ShippingStatus.IN_TRANSIT
            else -> null
        }
    }

    val isDelivered = order.status == OrderStatus.DELIVERED
    val firstItem = order.items.firstOrNull()
    val itemCount = order.items.size
    val reviewDeadlineBaseMillis = remember(isDelivered, order.actualDeliveryDate, order.updatedAt) {
        if (!isDelivered) {
            null
        } else {
            order.actualDeliveryDate ?: order.updatedAt
        }
    }
    val reviewDeadlineLabel = remember(reviewDeadlineBaseMillis) {
        reviewDeadlineBaseMillis?.let { reviewDeadlineLabel(it) }
    }
    val canWriteReview = isDelivered && reviewDeadlineLabel != null && reviewDeadlineLabel != "만료"
    val forceExpandedForSingleItem = itemCount == 1
    val showCollapsedSummary = !isExpanded && !forceExpandedForSingleItem
    val showExpandedItems = isExpanded || forceExpandedForSingleItem

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            // 펼침/접힘 높이 보간
            .animateContentSize()
    ) {
        // 상태 배지 + 날짜
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (itemCount > 1) {
                Text(
                    text = "총 ${itemCount}건",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                OrderStatusBadge(
                    status = order.status,
                    shippingStatus = effectiveShippingStatus,
                    paymentStatus = order.paymentStatus
                )
            }

            Text(
                text = order.createdAt.toFormattedDate("yyyy.MM.dd HH:mm") + " 주문",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 접힌 상태: 대표 이미지 + 상품명 요약
        if (showCollapsedSummary) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOrderClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 대표 이미지
                Box {
                    AsyncImage(
                        model = firstItem?.productImageUrl,
                        contentDescription = firstItem?.productName,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_img_placeholder),
                        error = painterResource(R.drawable.ic_img_placeholder),
                        fallback = painterResource(R.drawable.ic_img_placeholder)
                    )
                    if (itemCount > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .clip(RoundedCornerShape(topStart = 6.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+${itemCount - 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (primaryGroup?.creatorName != null) {
                        Text(
                            text = if (creatorGroups.size > 1) {
                                "${primaryGroup.creatorName} 외 ${creatorGroups.size - 1}곳"
                            } else {
                                primaryGroup.creatorName
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = firstItem?.productName ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = if (itemCount > 1) Modifier.weight(1f, fill = false) else Modifier
                        )
                        if (itemCount > 1) {
                            Text(
                                text = " 포함 ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "총 ${itemCount}건",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (itemCount > 1) {
                            "총 ${numberFormat.format(order.totalPrice.toLongOrNull() ?: 0)}원"
                        } else {
                            "${numberFormat.format(order.totalPrice.toLongOrNull() ?: 0)}원"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        // 펼친 상태 아이템
        if (showExpandedItems) {
            Column {
                creatorGroups.forEachIndexed { groupIndex, group ->
                    if (groupIndex > 0) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    if (creatorGroups.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = group.creatorName ?: "스토어",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                VerifiedBadge(role = group.creatorRole, size = 14.dp)
                            }
                            GroupShippingBadge(status = group.status, shippingStatus = group.shippingStatus)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        group.items.forEach { item ->
                            OrderExpandedItem(
                                item = item,
                                canWriteReview = canWriteReview && !item.hasReview,
                                reviewDeadlineLabel = reviewDeadlineLabel,
                                numberFormat = numberFormat,
                                onClick = onOrderClick,
                                onWriteReview = { onWriteReview(item) },
                                onAddToCart = { onAddToCart(item) }
                            )
                        }
                    }
                }
            }
        }

        // 펼쳐보기/접기 버튼
        if (itemCount > 1) {
            Spacer(modifier = Modifier.height(14.dp))
            val buttonContentColor = MaterialTheme.colorScheme.onSurface
            OutlinedButton(
                onClick = onToggleExpand,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isExpanded) {
                    Text(
                        text = "접기",
                        style = MaterialTheme.typography.labelMedium,
                        color = buttonContentColor
                    )
                } else {
                    Text(
                        text = "총 ${itemCount}건",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = buttonContentColor
                    )
                    Text(
                        text = " 주문 펼쳐보기",
                        style = MaterialTheme.typography.labelMedium,
                        color = buttonContentColor
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(
                        if (isExpanded) R.drawable.ic_chevron_up_small else R.drawable.ic_chevron_down_small
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = buttonContentColor
                )
            }
        }
    }
}


private fun previewOrder(
    id: Int,
    status: String,
    shippingStatus: String,
    itemCount: Int = 1,
    deliveredDaysAgo: Long? = null
) = Order(
    id = id,
    orderNumber = "2026082710000$id",
    items = List(itemCount) { i ->
        OrderItem(
            id = id * 10 + i,
            productId = id * 10 + i,
            productName = if (i == 0) "프리미엄 무선 이어폰 Pro Max" else "추가 상품 ${i + 1}",
            productImageUrl = null,
            price = "89000",
            quantity = 1,
            subtotal = "89000"
        )
    },
    totalPrice = (89000 * itemCount).toString(),
    status = status,
    shippingAddress = "서울시 강남구 테헤란로 123",
    shippingPhone = "010-1234-5678",
    shippingName = "홍길동",
    paymentProvider = "TOSS",
    paymentStatus = "COMPLETED",
    memo = null,
    pointsUsed = "0",
    couponDiscount = "0",
    trackingNumber = null,
    carrier = null,
    shippingStatus = shippingStatus,
    actualDeliveryDate = deliveredDaysAgo?.let { System.currentTimeMillis() - it * 86_400_000L },
    createdAt = System.currentTimeMillis() - 3 * 86_400_000L,
    updatedAt = System.currentTimeMillis()
)

@HaloLightDarkPreview
@Composable
private fun OrderCardPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                OrderCard(
                    order = previewOrder(1, OrderStatus.PENDING, ShippingStatus.PREPARING),
                    isExpanded = false,
                    onOrderClick = {},
                    onToggleExpand = {},
                    onWriteReview = {},
                    onAddToCart = {}
                )
                OrderCard(
                    order = previewOrder(2, OrderStatus.CONFIRMED, ShippingStatus.IN_TRANSIT, itemCount = 3),
                    isExpanded = false,
                    onOrderClick = {},
                    onToggleExpand = {},
                    onWriteReview = {},
                    onAddToCart = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun OrderCardDeliveredExpandedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                OrderCard(
                    order = previewOrder(
                        id = 3,
                        status = OrderStatus.DELIVERED,
                        shippingStatus = ShippingStatus.DELIVERED,
                        itemCount = 2,
                        deliveredDaysAgo = 5
                    ),
                    isExpanded = true,
                    onOrderClick = {},
                    onToggleExpand = {},
                    onWriteReview = {},
                    onAddToCart = {}
                )
            }
        }
    }
}
