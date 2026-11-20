package com.ninezero.halo.feature.store.presentation.order_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.SheetHeader
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.input.FormTextField
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.components.toggle.SelectionRadio
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.GroupShape
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.PaymentStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.feature.store.domain.model.creatorGroupsOrFallback
import com.ninezero.halo.feature.store.presentation.order_detail.CancelReason
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.text.NumberFormat

private const val CANCEL_REASON_MAX_LENGTH = 50

@Composable
fun CancelOrderBottomSheet(
    order: Order,
    creatorId: Int?,
    selectedReason: CancelReason,
    otherReason: String,
    isCancelling: Boolean,
    numberFormat: NumberFormat,
    onSelectReason: (CancelReason) -> Unit,
    onUpdateOtherReason: (String) -> Unit,
    onProceed: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    FloatingBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
        showDragHandle = false,  // 헤더 안에 있음
        dismissEnabled = !isCancelling
    ) {
        CancelOrderContent(
            order = order,
            creatorId = creatorId,
            selectedReason = selectedReason,
            otherReason = otherReason,
            isCancelling = isCancelling,
            numberFormat = numberFormat,
            onSelectReason = onSelectReason,
            onUpdateOtherReason = onUpdateOtherReason,
            onProceed = onProceed,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun CancelOrderContent(
    order: Order,
    creatorId: Int?,
    selectedReason: CancelReason,
    otherReason: String,
    isCancelling: Boolean,
    numberFormat: NumberFormat,
    onSelectReason: (CancelReason) -> Unit,
    onUpdateOtherReason: (String) -> Unit,
    onProceed: () -> Unit,
    onDismiss: () -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(0.dp) }
    var bottomBarHeight by remember { mutableStateOf(0.dp) }

    val groups = remember(order) { order.creatorGroupsOrFallback() }
    val group = remember(groups, creatorId) { groups.firstOrNull { it.creatorId == creatorId } }
    val isSingleGroup = groups.size <= 1

    val subtotal = group?.subtotal ?: 0L
    val shippingFee = group?.shippingFee ?: 0L
    // 단일 그룹은 주문 단위, 멀티는 그룹 귀속분
    val pointsUsed: Long
    val couponDiscount: Long
    val refundEstimate: Long
    if (isSingleGroup) {
        pointsUsed = order.pointsUsed
        couponDiscount = order.couponDiscount
        refundEstimate = order.totalPrice
    } else {
        couponDiscount = group?.couponDiscount ?: 0L
        // 포인트는 순액 비율 배분
        val groupNet = (subtotal + shippingFee - couponDiscount).coerceAtLeast(0L)
        val activeNet = groups
            .filter { it.status != OrderStatus.CANCELLED }
            .sumOf { g ->
                (g.subtotal + g.shippingFee - g.couponDiscount).coerceAtLeast(0L)
            }
        // 서버 HALF_UP과 맞춰 반올림
        pointsUsed = if (activeNet > 0L)
            ((order.pointsUsed) * groupNet + activeNet / 2) / activeNet
        else 0L
        refundEstimate = (groupNet - pointsUsed).coerceAtLeast(0L)
    }

    val canProceed = selectedReason != CancelReason.OTHER || otherReason.isNotBlank()

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .hazeSource(hazeState)
                .blockSheetFling()
                .verticalScroll(rememberScrollState(), overscrollEffect = null)
                .padding(top = headerHeight, bottom = bottomBarHeight)
                .padding(horizontal = 16.dp)
        ) {
            // 무엇을 취소하는지
            val items = group?.items ?: order.items
            items.firstOrNull()?.let { first ->
                CancelTargetRow(
                    imageUrl = first.productImageUrl,
                    creatorName = group?.creatorName,
                    itemCount = items.size,
                    productName = if (items.size > 1) "${first.productName} 외 ${items.size - 1}건" else first.productName,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            CancelSectionLabel("취소 사유")
            val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(GroupShape)
                    .border(0.5.dp, lineColor, GroupShape)
            ) {
                CancelReason.entries.forEachIndexed { index, reason ->
                    if (index > 0) HorizontalDivider(thickness = 0.5.dp, color = lineColor)
                    ReasonOptionItem(
                        text = reason.label,
                        selected = reason == selectedReason,
                        onClick = { onSelectReason(reason) }
                    )
                }
            }
            AnimatedVisibility(
                visible = selectedReason == CancelReason.OTHER,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    FormTextField(
                        value = otherReason,
                        onValueChange = onUpdateOtherReason,
                        placeholder = "취소 사유를 입력해주세요",
                        maxLength = CANCEL_REASON_MAX_LENGTH,
                        showCounter = true,
                        modifier = Modifier.fillMaxWidth(),
                        bringIntoViewReserve = bottomBarHeight  // 밑변 · 글자 수까지 하단 바 위로
                    )
                }
            }

            // 환불 정보: 내역 → 예상 금액 → 수단
            CancelSectionLabel("환불 정보")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, lineColor, GroupShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                RefundRow("상품금액", "${numberFormat.format(subtotal)}원")
                RefundRow("배송비", if (shippingFee > 0) "${numberFormat.format(shippingFee)}원" else "무료")
                if (pointsUsed > 0) {
                    RefundRow("포인트 사용", "-${numberFormat.format(pointsUsed)}원", isMinus = true)
                }
                if (couponDiscount > 0) {
                    RefundRow("쿠폰할인", "-${numberFormat.format(couponDiscount)}원", isMinus = true)
                }
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = lineColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "환불 예상 금액",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${numberFormat.format(refundEstimate)}원",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                RefundRow("환불 수단", order.paymentProvider?.let { getPaymentProviderText(it) } ?: "-")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        SheetHeader(
            title = "주문 취소",
            onClose = onDismiss,
            hazeState = hazeState,
            onHeightChange = { headerHeight = it },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 하단 바
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onGloballyPositioned { bottomBarHeight = with(density) { it.size.height.toDp() } }
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .haloBlur(hazeState, HaloBlurs.fadeUp(surface, end = 1f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars.exclude(WindowInsets.ime))
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 16.dp)
            ) {
                Button(
                    onClick = onProceed,
                    enabled = canProceed && !isCancelling,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .bounceClick(enabled = canProceed && !isCancelling),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = "주문 취소하기",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CancelSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
    )
}

/** 취소할 상품 한 줄: 사진 · 크리에이터 · 상품 수 · 이름 */
@Composable
private fun CancelTargetRow(
    imageUrl: String?,
    creatorName: String?,
    itemCount: Int,
    productName: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(MaterialTheme.shapes.small),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_image_placeholder),
            error = painterResource(R.drawable.ic_image_placeholder),
            fallback = painterResource(R.drawable.ic_image_placeholder)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = listOfNotNull(creatorName, "상품 ${itemCount}개").joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = productName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReasonOptionItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectionRadio(selected = selected)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RefundRow(label: String, value: String, isMinus: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isMinus) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun previewOrder() = Order(
    id = 1,
    orderNumber = "2026082710000137",
    items = listOf(
        OrderItem(
            id = 10,
            productId = 10,
            productName = "프리미엄 무선 이어폰 Pro Max",
            productImageUrl = null,
            price = 89000L,
            quantity = 1,
            subtotal = 89000L
        )
    ),
    totalPrice = 92000L,
    status = OrderStatus.PENDING,
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
    shippingStatus = ShippingStatus.PREPARING,
    createdAt = System.currentTimeMillis() - 86_400_000L,
    updatedAt = System.currentTimeMillis()
)

@HaloLightDarkPreview
@Composable
private fun CancelOrderContentPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CancelOrderContent(
                order = previewOrder(),
                creatorId = null,
                selectedReason = CancelReason.CHANGED_MIND,
                otherReason = "",
                isCancelling = false,
                numberFormat = rememberKoreanNumberFormat(),
                onSelectReason = {},
                onUpdateOtherReason = {},
                onProceed = {},
                onDismiss = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CancelOrderContentOtherPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CancelOrderContent(
                order = previewOrder(),
                creatorId = null,
                selectedReason = CancelReason.OTHER,
                otherReason = "배송이 너무 늦어서",
                isCancelling = false,
                numberFormat = rememberKoreanNumberFormat(),
                onSelectReason = {},
                onUpdateOtherReason = {},
                onProceed = {},
                onDismiss = {}
            )
        }
    }
}
