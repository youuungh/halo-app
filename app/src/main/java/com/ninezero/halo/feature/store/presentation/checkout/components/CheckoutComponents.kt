package com.ninezero.halo.feature.store.presentation.checkout.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninezero.halo.BuildConfig
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.point.domain.model.PointBalance
import com.ninezero.halo.feature.store.domain.model.BillingCard
import com.ninezero.halo.feature.store.domain.model.OrderLine
import com.ninezero.halo.feature.store.presentation.components.cardLabel
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.LocalIsDarkTheme
import java.text.NumberFormat

@Composable
fun CouponSection(
    availableCount: Int,
    selectedCount: Int,
    totalDiscount: Long,
    numberFormat: NumberFormat,
    onShowCouponSheet: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "할인 쿠폰",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onShowCouponSheet)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (totalDiscount > 0) "${numberFormat.format(totalDiscount)}원 할인 적용됨"
                else if (selectedCount > 0) "${selectedCount}개 쿠폰 선택됨"
                else if (availableCount > 0) "쿠폰을 선택해주세요"
                else "사용 가능한 쿠폰 없음",
                style = MaterialTheme.typography.bodySmall,
                color = if (totalDiscount > 0 || selectedCount > 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = "쿠폰 선택",
                modifier = Modifier.height(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PointSection(
    pointBalance: PointBalance?,
    usedPoints: Int,
    maxUsablePoints: Int,
    validationMessage: String?,
    numberFormat: NumberFormat,
    onUpdateUsedPoints: (String) -> Unit,
    onUseAllPoints: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var localText by remember { mutableStateOf(if (usedPoints == 0) "" else numberFormat.format(usedPoints)) }
    var isFocused by remember { mutableStateOf(false) }

    // 입력 중엔 그대로, 포커스가 빠지면 적용값으로
    LaunchedEffect(usedPoints, isFocused) {
        if (!isFocused) {
            localText = if (usedPoints == 0) "" else numberFormat.format(usedPoints)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "포인트",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "보유 ${numberFormat.format(pointBalance?.totalPoints ?: 0)}P",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "사용",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = localText,
                    // 키보드를 닫지 않고 결제해도 반영되도록 입력마다 적용
                    onValueChange = {
                        localText = it.filter { c -> c.isDigit() }
                        onUpdateUsedPoints(localText)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState -> isFocused = focusState.isFocused },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterEnd) {
                            if (localText.isEmpty() && !isFocused) {
                                Text(
                                    text = "0",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "P",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(
                        enabled = (pointBalance?.totalPoints ?: 0) > 0,
                        onClick = {
                            onUseAllPoints()
                            // 사용제한에 맞춘 값, 보유 전액 아님
                            val capped = minOf(pointBalance?.totalPoints ?: 0, maxUsablePoints)
                            localText = if (capped > 0) numberFormat.format(capped) else ""
                        }
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "전액사용",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if ((pointBalance?.totalPoints ?: 0) > 0)
                        MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = validationMessage
                ?: "최대 ${numberFormat.format(maxUsablePoints)}P까지 사용할 수 있습니다",
            style = MaterialTheme.typography.labelSmall,
            color = if (validationMessage != null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
fun PaymentProviderSection(
    selectedProvider: String,
    savedCard: BillingCard?,
    onShowCardSheet: () -> Unit,
    onSelectProvider: (String) -> Unit
) {
    val isCardSelected = selectedProvider == "TOSS_CARD" || selectedProvider == "TOSS_BILLING"

    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Text(
            text = "결제수단",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // 신용·체크카드
            val cardSubtitle = when (selectedProvider) {
                "TOSS_BILLING" -> if (savedCard != null) cardLabel(savedCard) else "저장카드 결제"
                "TOSS_CARD" -> "카드 일반결제"
                else -> null
            }
            val cardSubtitleIcon: (@Composable () -> Unit)? =
                if (selectedProvider == "TOSS_BILLING") {
                    { CardLogo(savedCard, iconSize = 16.dp) }
                } else null
            PaymentMethodRow(
                label = "신용 · 체크카드",
                subtitle = cardSubtitle,
                isSelected = isCardSelected,
                onClick = onShowCardSheet,
                subtitleIcon = cardSubtitleIcon
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethodCell(
                    providerKey = "TOSS_TOSSPAY",
                    label = "토스페이",
                    isSelected = selectedProvider == "TOSS_TOSSPAY",
                    onClick = { onSelectProvider("TOSS_TOSSPAY") },
                    modifier = Modifier.weight(1f)
                )
                PaymentMethodCell(
                    providerKey = "TOSS_MOBILE",
                    label = "휴대폰 결제",
                    isSelected = selectedProvider == "TOSS_MOBILE",
                    onClick = { onSelectProvider("TOSS_MOBILE") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethodCell(
                    providerKey = "TOSS_TRANSFER",
                    label = "계좌이체",
                    isSelected = selectedProvider == "TOSS_TRANSFER",
                    onClick = { onSelectProvider("TOSS_TRANSFER") },
                    modifier = Modifier.weight(1f)
                )
                if (BuildConfig.DEBUG) {
                    PaymentMethodCell(
                        providerKey = "MOCK",
                        label = "테스트 결제",
                        isSelected = selectedProvider == "MOCK",
                        onClick = { onSelectProvider("MOCK") },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(
    label: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    subtitleIcon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (subtitleIcon != null) {
                        subtitleIcon()
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PaymentMethodCell(
    providerKey: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (providerKey == "TOSS_TOSSPAY") {
            Image(
                painter = painterResource(
                    if (LocalIsDarkTheme.current) R.drawable.tosspay_logo_primary_white
                    else R.drawable.tosspay_logo_primary
                ),
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(18.dp)
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EarnPointsSection(
    orderItems: List<OrderLine>,
    creatorTiers: Map<Int, SubscriptionPlanTier>,
    numberFormat: NumberFormat
) {
    data class EarnDetail(
        val creatorName: String,
        val tier: SubscriptionPlanTier,
        val rate: Double,
        val amount: Long,
        val earnPoints: Long
    )

    val details = remember(orderItems, creatorTiers) {
        orderItems
            .groupBy { it.product.creator.id }
            .map { (creatorId, items) ->
                val creator = items.first().product.creator
                val tier = creatorTiers[creatorId] ?: SubscriptionPlanTier.FREE
                val rate = when (tier) {
                    SubscriptionPlanTier.FREE -> 0.01
                    SubscriptionPlanTier.TIER1 -> 0.05
                    SubscriptionPlanTier.TIER2 -> 0.10
                }
                val amount = items.sumOf { it.subtotal }
                val earnPoints = (amount * rate).toLong()
                EarnDetail(
                    creatorName = creator.displayName ?: creator.username,
                    tier = tier,
                    rate = rate,
                    amount = amount,
                    earnPoints = earnPoints
                )
            }
    }

    val totalEarnPoints = remember(details) { details.sumOf { it.earnPoints } }

    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "적립 예정",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${numberFormat.format(totalEarnPoints)}P",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (details.size > 1 || details.any { it.tier != SubscriptionPlanTier.FREE }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        details.forEach { detail ->
                            val tierLabel = when (detail.tier) {
                                SubscriptionPlanTier.FREE -> "기본 1%"
                                SubscriptionPlanTier.TIER1 -> "멤버십 5%"
                                SubscriptionPlanTier.TIER2 -> "멤버십 10%"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${detail.creatorName} ($tierLabel)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+${numberFormat.format(detail.earnPoints)}P",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSummarySection(
    subtotal: Long,
    shippingFee: Long,
    couponDiscount: Long,
    shippingDiscount: Long,
    totalAmount: Long,
    numberFormat: NumberFormat
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Text(
            text = "주문 요약",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PaymentRow("상품금액", "${numberFormat.format(subtotal)}원")
                PaymentRow("배송비", "${numberFormat.format(shippingFee)}원")
                if (shippingDiscount > 0) {
                    PaymentRow(
                        label = "배송비 할인",
                        value = "-${numberFormat.format(shippingDiscount)}원",
                        valueColor = MaterialTheme.colorScheme.error
                    )
                }
                if (couponDiscount > 0) {
                    PaymentRow(
                        label = "쿠폰 할인",
                        value = "-${numberFormat.format(couponDiscount)}원",
                        valueColor = MaterialTheme.colorScheme.error
                    )
                }
            }
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "총 주문 금액",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${numberFormat.format(totalAmount.coerceAtLeast(0))}원",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PaymentRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                CouponSection(
                    availableCount = 3,
                    selectedCount = 1,
                    totalDiscount = 13_350L,
                    numberFormat = rememberKoreanNumberFormat(),
                    onShowCouponSheet = {}
                )
                CouponSection(
                    availableCount = 0,
                    selectedCount = 0,
                    totalDiscount = 0L,
                    numberFormat = rememberKoreanNumberFormat(),
                    onShowCouponSheet = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PointSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                PointSection(
                    pointBalance = PointBalance(totalPoints = 12_500),
                    usedPoints = 5_000,
                    maxUsablePoints = 12_500,
                    validationMessage = null,
                    numberFormat = rememberKoreanNumberFormat(),
                    onUpdateUsedPoints = {},
                    onUseAllPoints = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PointSectionValidationPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                PointSection(
                    pointBalance = PointBalance(totalPoints = 12_500),
                    usedPoints = 20_000,
                    maxUsablePoints = 12_500,
                    validationMessage = "최대 12,500P까지 사용할 수 있습니다",
                    numberFormat = rememberKoreanNumberFormat(),
                    onUpdateUsedPoints = {},
                    onUseAllPoints = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PaymentProviderSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                PaymentProviderSection(
                    selectedProvider = "CARD",
                    savedCard = BillingCard(
                        registered = true,
                        cardCompany = "신한",
                        cardNumberMasked = "1234-****-****-5678",
                        cardType = "체크"
                    ),
                    onShowCardSheet = {},
                    onSelectProvider = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PaymentProviderSectionNoCardPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                PaymentProviderSection(
                    selectedProvider = "CARD",
                    savedCard = null,
                    onShowCardSheet = {},
                    onSelectProvider = {}
                )
            }
        }
    }
}
