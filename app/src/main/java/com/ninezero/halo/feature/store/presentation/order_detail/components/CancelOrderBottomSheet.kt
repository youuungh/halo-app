package com.ninezero.halo.feature.store.presentation.order_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.input.FormTextField
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.domain.model.OrderStatus
import com.ninezero.halo.feature.store.domain.model.ShippingStatus
import com.ninezero.halo.feature.store.domain.model.creatorGroupsOrFallback
import com.ninezero.halo.feature.store.presentation.order_detail.CancelReason
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.text.NumberFormat

private const val CANCEL_REASON_MAX_LENGTH = 50

@Composable
fun CancelOrderBottomSheet(
    order: Order,
    creatorId: Int?,
    selectedReason: CancelReason,
    reasonEtc: String,
    isCancelling: Boolean,
    numberFormat: NumberFormat,
    onSelectReason: (CancelReason) -> Unit,
    onUpdateReasonEtc: (String) -> Unit,
    onProceed: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) },
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        BottomSheetDragHandle()
        CancelOrderContent(
            order = order,
            creatorId = creatorId,
            selectedReason = selectedReason,
            reasonEtc = reasonEtc,
            isCancelling = isCancelling,
            numberFormat = numberFormat,
            onSelectReason = onSelectReason,
            onUpdateReasonEtc = onUpdateReasonEtc,
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
    reasonEtc: String,
    isCancelling: Boolean,
    numberFormat: NumberFormat,
    onSelectReason: (CancelReason) -> Unit,
    onUpdateReasonEtc: (String) -> Unit,
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

    val subtotal = group?.subtotal?.toLongOrNull() ?: 0L
    val shippingFee = group?.shippingFee?.toLongOrNull() ?: 0L
    // 단일 그룹은 주문 단위, 멀티는 그룹 귀속분
    val pointsUsed: Long
    val couponDiscount: Long
    val refundEstimate: Long
    if (isSingleGroup) {
        pointsUsed = order.pointsUsed.toLongOrNull() ?: 0L
        couponDiscount = order.couponDiscount.toLongOrNull() ?: 0L
        refundEstimate = order.totalPrice.toLongOrNull() ?: 0L
    } else {
        couponDiscount = group?.couponDiscount?.toLongOrNull() ?: 0L
        // 포인트는 순액 비율 배분
        val groupNet = (subtotal + shippingFee - couponDiscount).coerceAtLeast(0L)
        val activeNet = groups
            .filter { it.status != OrderStatus.CANCELLED }
            .sumOf { g ->
                ((g.subtotal.toLongOrNull() ?: 0L) + (g.shippingFee.toLongOrNull() ?: 0L) -
                    (g.couponDiscount.toLongOrNull() ?: 0L)).coerceAtLeast(0L)
            }
        // 서버 HALF_UP과 맞춰 반올림
        pointsUsed = if (activeNet > 0L)
            ((order.pointsUsed.toLongOrNull() ?: 0L) * groupNet + activeNet / 2) / activeNet
        else 0L
        refundEstimate = (groupNet - pointsUsed).coerceAtLeast(0L)
    }

    val canProceed = selectedReason != CancelReason.ETC || reasonEtc.isNotBlank()

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
            Text(
                text = "취소 사유",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column {
                    CancelReason.entries.forEachIndexed { index, reason ->
                        ReasonOptionItem(
                            text = reason.label,
                            selected = reason == selectedReason,
                            onClick = { onSelectReason(reason) }
                        )
                        if (index < CancelReason.entries.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = selectedReason == CancelReason.ETC,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    FormTextField(
                        value = reasonEtc,
                        onValueChange = onUpdateReasonEtc,
                        placeholder = "취소 사유를 입력해주세요",
                        maxLength = CANCEL_REASON_MAX_LENGTH,
                        showCounter = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "결제 정보를 확인해주세요",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            RefundRow("상품금액", "${numberFormat.format(subtotal)}원")
            RefundRow("배송비", if (shippingFee > 0) "${numberFormat.format(shippingFee)}원" else "무료")
            if (pointsUsed > 0) {
                RefundRow("포인트 사용", "-${numberFormat.format(pointsUsed)}원")
            }
            if (couponDiscount > 0) {
                RefundRow("쿠폰할인", "-${numberFormat.format(couponDiscount)}원")
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "환불 예상금액",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${numberFormat.format(refundEstimate)}원",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            RefundRow("환불 수단", order.paymentProvider?.let { getPaymentProviderText(it) } ?: "-")
        }

        // 헤더
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = surface,
                        tint = HaloBlurTint(surface),
                        noiseFactor = 0f
                    )
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1f,
                        endIntensity = 0f
                    )
                }
                .onGloballyPositioned { headerHeight = with(density) { it.size.height.toDp() } }
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text(
                text = "주문 취소",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center)
            )
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                GlassIconButton(
                    onClick = onDismiss,
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    hazeState = hazeState,
                    backgroundColor = surface,
                    size = 36.dp,
                    iconSize = 20.dp
                )
            }
        }

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
                    .haloBlur(state = hazeState) {
                        style = HaloBlurStyle(
                            backgroundColor = surface,
                            tint = HaloBlurTint(surface),
                            noiseFactor = 0f
                        )
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0f,
                            endIntensity = 1f
                        )
                    }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars.exclude(WindowInsets.ime))
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 8.dp)
            ) {
                Button(
                    onClick = onProceed,
                    enabled = canProceed && !isCancelling,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .bounceClick(enabled = canProceed && !isCancelling),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = "주문 취소",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RefundRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
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
            color = MaterialTheme.colorScheme.onSurface
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
            price = "89000",
            quantity = 1,
            subtotal = "89000"
        )
    ),
    totalPrice = "92000",
    status = OrderStatus.PENDING,
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
                reasonEtc = "",
                isCancelling = false,
                numberFormat = rememberKoreanNumberFormat(),
                onSelectReason = {},
                onUpdateReasonEtc = {},
                onProceed = {},
                onDismiss = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CancelOrderContentEtcPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CancelOrderContent(
                order = previewOrder(),
                creatorId = null,
                selectedReason = CancelReason.ETC,
                reasonEtc = "배송이 너무 늦어서",
                isCancelling = false,
                numberFormat = rememberKoreanNumberFormat(),
                onSelectReason = {},
                onUpdateReasonEtc = {},
                onProceed = {},
                onDismiss = {}
            )
        }
    }
}
