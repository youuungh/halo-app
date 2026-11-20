package com.ninezero.halo.feature.store.presentation.order_list.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.PaymentStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.feature.store.domain.model.creatorGroupsOrFallback
import com.ninezero.halo.feature.store.presentation.components.CartCircleButton
import com.ninezero.halo.feature.store.presentation.components.GroupShippingBadge
import com.ninezero.halo.feature.store.presentation.components.OrderDetailItemRow
import com.ninezero.halo.feature.store.presentation.components.OrderStatusBadge
import java.util.Calendar
import java.util.concurrent.TimeUnit

// 여러 건 주문 카드 아래 사진 줄에 보이는 최대 장수
private const val STRIP_MAX_IMAGES = 4

@Composable
fun OrderCard(
    order: Order,
    isExpanded: Boolean,
    onOrderClick: () -> Unit,
    onToggleExpand: () -> Unit,
    onWriteReview: (OrderItem) -> Unit,
    onAddToCart: (OrderItem) -> Unit,
    onTrackShipping: (creatorId: Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = rememberKoreanNumberFormat()
    val creatorGroups = remember(order) { order.creatorGroupsOrFallback() }

    // 그룹 상태를 대표 상태로 집계
    val actualShippingStatus = remember(creatorGroups) {
        when {
            creatorGroups.isNotEmpty() && creatorGroups.all { it.shippingStatus == ShippingStatus.DELIVERED } ->
                ShippingStatus.DELIVERED
            creatorGroups.any { it.shippingStatus in InTransitStatuses } -> ShippingStatus.IN_TRANSIT
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 상태 배지 + 날짜
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OrderStatusBadge(
                status = order.status,
                shippingStatus = actualShippingStatus,
                paymentStatus = order.paymentStatus
            )
            Text(
                text = orderDateLabel(order.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (itemCount <= 1) {
            if (firstItem == null) return@Column
            OrderDetailItemRow(
                item = firstItem,
                numberFormat = numberFormat,
                showLineTotal = true,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOrderClick
                ),
                // 장바구니 원, 못 사는 상품은 화살표만
                trailing = {
                    if (firstItem.isProductActive) CartCircleButton(onClick = { onAddToCart(firstItem) }) else RowChevron()
                }
            )

            // 버튼 줄은 할 일이 있을 때만
            val group = creatorGroups.firstOrNull()
            val primaryAction: OrderPillData? = when {
                canWriteReview && !firstItem.hasReview && firstItem.isProductActive -> OrderPillData(
                    label = "리뷰 쓰기",
                    badge = reviewDeadlineLabel,
                    onClick = { onWriteReview(firstItem) }
                )
                group?.shippingStatus in InTransitStatuses -> OrderPillData(
                    label = "배송조회",
                    onClick = { onTrackShipping(group?.creatorId) }
                )
                else -> null
            }
            if (primaryAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OrderPill(primaryAction, Modifier.fillMaxWidth())
            }
        } else {
            // 여러 건: 이름 · 건수 · 금액, 아래 사진 줄
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOrderClick
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${firstItem?.productName.orEmpty()} 외 ${itemCount - 1}건",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row {
                        Text(
                            text = "${itemCount}건 · ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${numberFormat.format(order.totalPrice)}원",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                RowChevron()
            }

            AnimatedVisibility(
                visible = !isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ImageStrip(items = order.items, modifier = Modifier.padding(top = 10.dp))
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    creatorGroups.forEach { group ->
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 12.dp, bottom = 10.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = group.creatorName ?: "스토어",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                VerifiedBadge(role = group.creatorRole, size = 14.dp)
                            }
                            GroupShippingBadge(status = group.status, shippingStatus = group.shippingStatus)
                        }
                        group.items.forEach { item ->
                            OrderDetailItemRow(
                                item = item,
                                numberFormat = numberFormat,
                                showLineTotal = true,
                                imageSize = 44.dp,
                                contentPadding = PaddingValues(vertical = 6.dp),
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onOrderClick
                                )
                            )
                        }
                    }
                }
            }

            // 모두 보기 · 접기 알약
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "접기" else "${itemCount}건 모두 보기",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    painter = painterResource(
                        if (isExpanded) R.drawable.ic_chevron_up_small else R.drawable.ic_chevron_down_small
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun reviewDeadlineLabel(deadlineMillis: Long): String {
    val now = System.currentTimeMillis()
    val daysPassed = TimeUnit.MILLISECONDS.toDays(now - deadlineMillis).toInt()
    val daysLeft = Constants.Commerce.REVIEW_WRITE_DEADLINE_DAYS - daysPassed

    return when {
        daysLeft < 0 -> "만료"
        daysLeft == 0 -> "D-Day"
        else -> "D-$daysLeft"
    }
}

private val InTransitStatuses = setOf(ShippingStatus.SHIPPED, ShippingStatus.IN_TRANSIT, ShippingStatus.OUT_FOR_DELIVERY)

/** 올해는 '10.15', 이전 해는 '2025.10.15' */
private fun orderDateLabel(createdAt: Long): String {
    val year = Calendar.getInstance().apply { timeInMillis = createdAt }.get(Calendar.YEAR)
    val thisYear = Calendar.getInstance().get(Calendar.YEAR)
    return createdAt.toFormattedDate(if (year == thisYear) "MM.dd" else "yyyy.MM.dd")
}

@Immutable
private data class OrderPillData(
    val label: String,
    val badge: String? = null,
    val onClick: () -> Unit
)

/** 리뷰 쓰기 · 배송조회 전용 */
@Composable
private fun OrderPill(pill: OrderPillData, modifier: Modifier = Modifier) {
    val contentColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    Row(
        modifier = modifier
            .bounceClick()
            .height(36.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = pill.onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = pill.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
        if (pill.badge != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = pill.badge,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(contentColor.copy(alpha = 0.12f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

/** 여러 건 주문의 사진 줄, 넘치면 '+N' */
@Composable
private fun ImageStrip(items: List<OrderItem>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items.take(STRIP_MAX_IMAGES).forEach { item ->
            AsyncImage(
                model = item.productImageUrl,
                contentDescription = item.productName,
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_image_placeholder),
                error = painterResource(R.drawable.ic_image_placeholder),
                fallback = painterResource(R.drawable.ic_image_placeholder)
            )
        }
        if (items.size > STRIP_MAX_IMAGES) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${items.size - STRIP_MAX_IMAGES}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RowChevron() {
    Icon(
        painter = painterResource(R.drawable.ic_chevron_right),
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    )
}


private fun previewOrder(
    id: Int,
    status: OrderStatus,
    shippingStatus: ShippingStatus,
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
            price = 89000L,
            quantity = 1,
            subtotal = 89000L
        )
    },
    totalPrice = 89000L * itemCount,
    status = status,
    shippingAddress = "서울시 강남구 테헤란로 123",
    shippingPhone = "010-1234-5678",
    shippingName = "홍길동",
    paymentProvider = "TOSS",
    paymentStatus = PaymentStatus.COMPLETED,
    memo = null,
    pointsUsed = 0L,
    couponDiscount = 0L,
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
                    onAddToCart = {},
                    onTrackShipping = {}
                )
                OrderCard(
                    order = previewOrder(2, OrderStatus.CONFIRMED, ShippingStatus.IN_TRANSIT, itemCount = 3),
                    isExpanded = false,
                    onOrderClick = {},
                    onToggleExpand = {},
                    onWriteReview = {},
                    onAddToCart = {},
                    onTrackShipping = {}
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
                    onAddToCart = {},
                    onTrackShipping = {}
                )
            }
        }
    }
}
