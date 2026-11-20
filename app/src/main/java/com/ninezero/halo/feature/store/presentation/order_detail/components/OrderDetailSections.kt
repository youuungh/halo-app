package com.ninezero.halo.feature.store.presentation.order_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.feature.store.domain.model.OrderCreatorGroup
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.feature.store.presentation.components.OrderDetailItemRow
import com.ninezero.halo.feature.store.presentation.components.PriceBreakdownRow
import com.ninezero.halo.feature.store.presentation.components.PriceSubLine
import com.ninezero.halo.feature.store.presentation.components.PriceSubLines
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import java.text.NumberFormat

/** 한 그룹에 리뷰 쓸 상품이 여럿일 때 고르는 시트 */
@Composable
fun ReviewPickerSheet(
    items: List<OrderItem>,
    numberFormat: NumberFormat,
    onPick: (OrderItem) -> Unit,
    onDismiss: () -> Unit
) {
    FloatingBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = "리뷰 쓸 상품",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        items.forEach { item ->
            OrderDetailItemRow(
                item = item,
                numberFormat = numberFormat,
                showPrice = false,
                modifier = Modifier.clickable { onPick(item) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** 배송 정보: 이름 · 전화 한 줄, 주소, 메모 */
@Composable
fun OrderShippingInfo(name: String, phone: String, address: String, memo: String?) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = phone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = address,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!memo.isNullOrBlank()) {
            Text(
                text = memo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 결제 정보 금액 값 */
@Immutable
data class OrderPaymentAmounts(
    val productAmount: Long,
    val productDiscount: Long,
    val couponDiscount: Long,
    val pointsUsed: Long,
    val totalPrice: Long,
    val paymentProviderText: String?
)

/** 결제 정보, 배송비 · 적립은 누르면 내역 */
@Composable
fun OrderPaymentSection(
    amounts: OrderPaymentAmounts,
    groups: List<OrderCreatorGroup>,
    showEarn: Boolean,
    numberFormat: NumberFormat
) {
    val discountColor = MaterialTheme.colorScheme.error
    var isShippingExpanded by rememberSaveable { mutableStateOf(false) }
    var isEarnExpanded by rememberSaveable { mutableStateOf(false) }
    val shippingTotal = groups.sumOf { it.shippingFee }
    val earnTotal = groups.sumOf { it.earnPoints }
    val canExpand = groups.size > 1

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        PriceBreakdownRow("상품 금액", "${numberFormat.format(amounts.productAmount)}원")
        if (amounts.productDiscount > 0L) {
            PriceBreakdownRow("상품 할인", "-${numberFormat.format(amounts.productDiscount)}원", discountColor)
        }
        if (amounts.couponDiscount > 0L) {
            PriceBreakdownRow("쿠폰 할인", "-${numberFormat.format(amounts.couponDiscount)}원", discountColor)
        }
        if (amounts.pointsUsed > 0L) {
            PriceBreakdownRow("포인트 사용", "-${numberFormat.format(amounts.pointsUsed)}원", discountColor)
        }
        ExpandableAmountRow(
            label = "배송비",
            value = if (shippingTotal > 0L) "${numberFormat.format(shippingTotal)}원" else "무료",
            style = MaterialTheme.typography.bodyMedium,
            isExpanded = isShippingExpanded,
            expandable = canExpand,
            onToggle = { isShippingExpanded = !isShippingExpanded }
        )
        ExpandedLines(visible = canExpand && isShippingExpanded) {
            groups.forEach { group ->
                val fee = group.shippingFee
                PriceSubLine(
                    label = group.creatorName ?: "스토어",
                    value = if (fee > 0L) "${numberFormat.format(fee)}원" else "무료",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 10.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "최종 결제 금액",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${numberFormat.format(amounts.totalPrice)}원",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (showEarn) {
            ExpandableAmountRow(
                label = "적립 예정",
                value = "${numberFormat.format(earnTotal)}P",
                style = MaterialTheme.typography.bodySmall,
                isExpanded = isEarnExpanded,
                expandable = groups.isNotEmpty(),
                onToggle = { isEarnExpanded = !isEarnExpanded }
            )
            ExpandedLines(visible = isEarnExpanded) {
                groups.forEach { group ->
                    // 요율은 서버값
                    val tierPrefix = if (group.earnTier == SubscriptionPlanTier.FREE) "기본" else "멤버십"
                    PriceSubLine(
                        label = "${group.creatorName ?: "스토어"} · $tierPrefix ${(group.earnRate * 100).toInt()}%",
                        value = "${numberFormat.format(group.earnPoints)}P",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (amounts.paymentProviderText != null) {
            PriceBreakdownRow("결제 방식", amounts.paymentProviderText, small = true)
        }
    }
}

@Composable
private fun ExpandableAmountRow(
    label: String,
    value: String,
    style: TextStyle,
    isExpanded: Boolean,
    expandable: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (expandable) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle
                    )
                } else {
                    Modifier
                }
            )
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = style, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, style = style, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (expandable) {
                Icon(
                    painter = painterResource(if (isExpanded) R.drawable.ic_chevron_up_small else R.drawable.ic_chevron_down_small),
                    contentDescription = if (isExpanded) "접기" else "펼치기",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExpandedLines(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        PriceSubLines(content)
    }
}
