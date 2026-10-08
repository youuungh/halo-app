package com.ninezero.halo.feature.store.presentation.sales_manage.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.feature.store.domain.model.creatorGroupsOrFallback
import com.ninezero.halo.feature.store.presentation.components.OrderStatusBadge
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun SalesOrderCard(
    order: Order,
    isExpanded: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onRegisterShipping: () -> Unit,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier,
    isStatusUpdating: Boolean = false,
) {
    val numberFormat = rememberKoreanNumberFormat()
    val sellerGroup = remember(order) { order.creatorGroupsOrFallback().firstOrNull() }
    val sellerShippingFee = remember(order, sellerGroup) {
        sellerGroup?.shippingFee?.toLongOrNull()
            ?: order.shippingFee.toLongOrNull()
            ?: 0L
    }
    val sellerSubtotal = remember(order, sellerGroup) {
        sellerGroup?.subtotal?.toLongOrNull()
            ?: order.items.sumOf { it.subtotal.toLongOrNull() ?: 0L }
    }
    val sellerOrderAmount = remember(sellerSubtotal, sellerShippingFee) {
        sellerSubtotal + sellerShippingFee
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // 상태 뱃지 + 날짜/주문번호
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            OrderStatusBadge(order.status, order.shippingStatus, order.paymentStatus)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = order.createdAt.toFormattedDate("yyyy.MM.dd HH:mm"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = order.orderNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 주문 상품 목록
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.productImageUrl != null) {
                        AsyncImage(
                            model = item.productImageUrl,
                            contentDescription = item.productName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.productName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${numberFormat.format(item.price.toLongOrNull() ?: 0)}원 × ${item.quantity}개",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 수령인 + 총액
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onToggleExpand() }
                    .padding(vertical = 2.dp, horizontal = 2.dp)
            ) {
                Text(
                    text = order.shippingName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    painter = painterResource(
                        if (isExpanded) R.drawable.ic_chevron_up
                        else R.drawable.ic_chevron_down
                    ),
                    contentDescription = if (isExpanded) "접기" else "펼치기",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${numberFormat.format(sellerOrderAmount)}원",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 수령인 상세 정보
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RecipientInfoRow("연락처", order.shippingPhone)
                RecipientInfoRow("주소", order.shippingAddress)
                if (!order.memo.isNullOrBlank()) {
                    RecipientInfoRow("메모", order.memo)
                }
            }
        }

        // 액션 버튼
        when {
            order.status == OrderStatus.PENDING -> {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onConfirm,
                        enabled = !isStatusUpdating,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .bounceClick()
                    ) {
                        Text(
                            text = "주문 확인",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        enabled = !isStatusUpdating,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .bounceClick()
                    ) {
                        Text(
                            text = "판매 취소",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            order.status == OrderStatus.CONFIRMED && order.shippingStatus == ShippingStatus.PREPARING -> {
                if (order.trackingNumber == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRegisterShipping,
                            enabled = !isStatusUpdating,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .bounceClick()
                        ) {
                            Text(
                                text = "송장번호 입력",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = onCancel,
                            enabled = !isStatusUpdating,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .bounceClick()
                        ) {
                            Text(
                                text = "판매 취소",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 송장 정보 표시
        if (order.trackingNumber != null && order.carrier != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.carrier,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = order.trackingNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun RecipientInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


private fun previewOrder(
    id: Int,
    status: String,
    shippingStatus: String,
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
            price = "89000",
            quantity = 1,
            subtotal = "89000",
            creatorId = 1,
            creatorName = "크리에이터 샵"
        )
    ),
    totalPrice = "92000",
    status = status,
    shippingAddress = "서울시 강남구 테헤란로 123",
    shippingPhone = "010-1234-5678",
    shippingName = "홍길동",
    paymentProvider = "TOSS",
    paymentStatus = "COMPLETED",
    memo = null,
    pointsUsed = "0",
    couponDiscount = "0",
    trackingNumber = trackingNumber,
    carrier = if (trackingNumber != null) "CJ대한통운" else null,
    shippingFee = "3000",
    shippingStatus = shippingStatus,
    createdAt = System.currentTimeMillis() - 86_400_000L,
    updatedAt = System.currentTimeMillis()
)

@HaloLightDarkPreview
@Composable
private fun SalesOrderCardPendingPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(12.dp)) {
                SalesOrderCard(
                    order = previewOrder(1, OrderStatus.PENDING, ShippingStatus.PREPARING),
                    isExpanded = false,
                    onConfirm = {},
                    onCancel = {},
                    onRegisterShipping = {},
                    onToggleExpand = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesOrderCardAwaitingShippingPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(12.dp)) {
                SalesOrderCard(
                    order = previewOrder(2, OrderStatus.CONFIRMED, ShippingStatus.PREPARING),
                    isExpanded = true,
                    onConfirm = {},
                    onCancel = {},
                    onRegisterShipping = {},
                    onToggleExpand = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesOrderCardShippingPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(12.dp)) {
                SalesOrderCard(
                    order = previewOrder(
                        id = 3,
                        status = OrderStatus.CONFIRMED,
                        shippingStatus = ShippingStatus.IN_TRANSIT,
                        trackingNumber = "1234567890"
                    ),
                    isExpanded = true,
                    onConfirm = {},
                    onCancel = {},
                    onRegisterShipping = {},
                    onToggleExpand = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesOrderCardUpdatingPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(12.dp)) {
                SalesOrderCard(
                    order = previewOrder(4, OrderStatus.PENDING, ShippingStatus.PREPARING),
                    isExpanded = false,
                    onConfirm = {},
                    onCancel = {},
                    onRegisterShipping = {},
                    onToggleExpand = {},
                    isStatusUpdating = true
                )
            }
        }
    }
}
