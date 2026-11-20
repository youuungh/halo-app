package com.ninezero.halo.feature.store.presentation.checkout.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.core.ui.components.button.PrimaryButton
import com.ninezero.halo.core.ui.components.button.PrimaryButtonStyle
import com.ninezero.halo.core.ui.components.input.CouponCodeInputField
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.components.toggle.SelectionRadio
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.feature.store.domain.model.calcCouponBenefit
import com.ninezero.halo.feature.store.domain.model.isCouponUsable
import com.ninezero.halo.feature.store.presentation.checkout.CouponCreatorInfo
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.text.NumberFormat

// 하단 버튼 48 + 위아래 여백
private val BottomButtonArea = 84.dp

@Composable
fun CouponSelectBottomSheet(
    coupons: List<Coupon>,
    orderAmountByCreatorId: Map<Int, Long>,
    couponInfoByCreator: Map<Int, CouponCreatorInfo>,
    draftSelectedCoupons: Map<Int, Coupon>,
    couponCode: String,
    isClaiming: Boolean,
    isLoading: Boolean,
    error: String?,
    onToggleCoupon: (Coupon) -> Unit,
    onUpdateCouponCode: (String) -> Unit,
    onClaimCoupon: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    FloatingBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        fullHeight = true
    ) {
        CouponSelectContent(
            coupons = coupons,
            orderAmountByCreatorId = orderAmountByCreatorId,
            couponInfoByCreator = couponInfoByCreator,
            draftSelectedCoupons = draftSelectedCoupons,
            couponCode = couponCode,
            isClaiming = isClaiming,
            isLoading = isLoading,
            error = error,
            onToggleCoupon = onToggleCoupon,
            onUpdateCouponCode = onUpdateCouponCode,
            onClaimCoupon = onClaimCoupon,
            onApply = onApply,
            onDismiss = onDismiss,
            onRetry = onRetry,
            // 쿠폰 수·로딩과 상관없이 높이 고정, 목록만 스크롤
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Composable
private fun CouponSelectContent(
    coupons: List<Coupon>,
    orderAmountByCreatorId: Map<Int, Long>,
    couponInfoByCreator: Map<Int, CouponCreatorInfo>,
    draftSelectedCoupons: Map<Int, Coupon>,
    couponCode: String,
    isClaiming: Boolean,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    error: String? = null,
    numberFormat: NumberFormat = rememberKoreanNumberFormat(),
    onToggleCoupon: (Coupon) -> Unit = {},
    onUpdateCouponCode: (String) -> Unit = {},
    onClaimCoupon: () -> Unit = {},
    onApply: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    // 이번 주문 크리에이터의 쿠폰만, 주문 순서대로
    val couponsByCreator = remember(coupons, couponInfoByCreator) {
        val grouped = coupons.groupBy { it.creatorId }
        couponInfoByCreator.keys.mapNotNull { creatorId -> grouped[creatorId]?.let { creatorId to it } }
    }
    val totalDiscount = remember(draftSelectedCoupons, orderAmountByCreatorId) {
        draftSelectedCoupons.entries.sumOf { (creatorId, coupon) ->
            calcCouponBenefit(coupon, orderAmountByCreatorId[creatorId] ?: 0L)
        }
    }
    val isEmpty = !isLoading && error == null && couponsByCreator.isEmpty()
    val surface = MaterialTheme.colorScheme.surface
    val hazeState = remember { HazeState() }
    val listState = rememberLazyListState()
    // 목록이 상단 밑으로 들어갈 때만 경계선
    val dividerAlpha by animateFloatAsState(
        targetValue = if (listState.canScrollBackward) 1f else 0f,
        label = "couponHeaderDivider"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "할인쿠폰",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)
        )

        CouponCodeInputField(
            value = couponCode,
            onValueChange = onUpdateCouponCode,
            onClaimClick = onClaimCoupon,
            isClaiming = isClaiming,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 6.dp)
        ) {
            // 목록은 버튼 아래로 흐르고 버튼 뒤는 점점 흐려짐
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
            ) {
                when {
                    isLoading -> {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            repeat(4) {
                                CouponSelectItemSkeleton(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // 불러오기 실패는 "쿠폰 없음"과 구분
                    error != null -> {
                        CouponSheetMessage(
                            title = "쿠폰을 불러오지 못했습니다",
                            subtitle = error,
                            actionLabel = "다시 불러오기",
                            onAction = onRetry
                        )
                    }

                    isEmpty -> {
                        CouponSheetMessage(
                            title = "사용할 수 있는 쿠폰이 없어요",
                            subtitle = "쿠폰 코드가 있다면 위에 입력해주세요"
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = BottomButtonArea),
                            overscrollEffect = null
                        ) {
                            items(couponsByCreator, key = { (creatorId, _) -> creatorId }) { (creatorId, creatorCoupons) ->
                                CreatorCouponGroup(
                                    creatorInfo = couponInfoByCreator.getValue(creatorId),
                                    coupons = creatorCoupons,
                                    orderAmount = orderAmountByCreatorId[creatorId] ?: 0L,
                                    selectedCoupon = draftSelectedCoupons[creatorId],
                                    numberFormat = numberFormat,
                                    onToggleCoupon = onToggleCoupon
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer { alpha = dividerAlpha }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .haloBlur(hazeState, HaloBlurs.fadeUp(surface, end = 1f))
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
            ) {
                if (isEmpty || error != null) {
                    PrimaryButton(
                        text = "닫기",
                        onClick = onDismiss,
                        style = PrimaryButtonStyle.Tonal
                    )
                } else {
                    PrimaryButton(
                        text = if (totalDiscount > 0L) "${numberFormat.format(totalDiscount)}원 할인 적용" else "쿠폰 없이 확인",
                        onClick = onApply,
                        enabled = !isLoading
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatorCouponGroup(
    creatorInfo: CouponCreatorInfo,
    coupons: List<Coupon>,
    orderAmount: Long,
    selectedCoupon: Coupon?,
    numberFormat: NumberFormat,
    onToggleCoupon: (Coupon) -> Unit
) {
    // 쓸 수 있는 쿠폰 중 할인이 가장 큰 것
    val bestCouponId = remember(coupons, orderAmount, creatorInfo) {
        coupons
            .filter { couponBlockReason(it, orderAmount, creatorInfo, numberFormat) == null }
            .maxByOrNull { calcCouponBenefit(it, orderAmount) }
            ?.takeIf { calcCouponBenefit(it, orderAmount) > 0L }
            ?.id
    }
    val shape = MaterialTheme.shapes.large

    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = creatorInfo.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .cardBorder(shape)
                .clip(shape)
        ) {
            val hasUsableCoupon = coupons.any { couponBlockReason(it, orderAmount, creatorInfo, numberFormat) == null }
            coupons.forEachIndexed { index, coupon ->
                if (index > 0) CouponRowDivider()
                val blockReason = couponBlockReason(coupon, orderAmount, creatorInfo, numberFormat)
                CouponOptionRow(
                    coupon = coupon,
                    benefit = calcCouponBenefit(coupon, orderAmount),
                    blockReason = blockReason,
                    isSelected = selectedCoupon?.id == coupon.id,
                    isBest = coupon.id == bestCouponId,
                    numberFormat = numberFormat,
                    onClick = { onToggleCoupon(coupon) }
                )
            }
            if (hasUsableCoupon) {
                CouponRowDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCoupon?.let(onToggleCoupon) }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionRadio(selected = selectedCoupon == null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "쿠폰 적용 안 함",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CouponOptionRow(
    coupon: Coupon,
    benefit: Long,
    blockReason: String?,
    isSelected: Boolean,
    isBest: Boolean,
    numberFormat: NumberFormat,
    onClick: () -> Unit
) {
    val isEnabled = blockReason == null
    val conditions = listOfNotNull(
        coupon.minOrderAmount?.takeIf { it > 0L }
            ?.let { "${numberFormat.format(it)}원 이상" },
        coupon.maxDiscountAmount?.takeIf { coupon.type == CouponType.PERCENTAGE }
            ?.let { "최대 ${numberFormat.format(it)}원" }
    ).joinToString(" · ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick)
            .alpha(if (isEnabled) 1f else 0.5f)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        SelectionRadio(selected = isSelected, enabled = isEnabled)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = coupon.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            if (blockReason != null) {
                Text(
                    text = blockReason,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                if (conditions.isNotEmpty()) {
                    Text(
                        text = conditions,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${coupon.expiresAt.toFormattedDate("yyyy.MM.dd")}까지",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (coupon.daysRemaining in 0..Constants.Commerce.COUPON_EXPIRING_SOON_DAYS) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (coupon.daysRemaining == 0) "D-day" else "D-${coupon.daysRemaining}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    MaterialTheme.shapes.extraSmall
                                )
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
        if (isEnabled) {
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (coupon.type == CouponType.FREE_SHIPPING) {
                        "무료배송"
                    } else {
                        "${numberFormat.format(benefit)}원 할인"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (isBest) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "최대 할인",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.shapes.extraSmall
                            )
                            .padding(horizontal = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CouponRowDivider() {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 14.dp)
    )
}

@Composable
private fun CouponSheetMessage(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp, bottom = BottomButtonArea),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/** 쓸 수 없는 이유, 서버 조건과 같음 */
private fun couponBlockReason(
    coupon: Coupon,
    orderAmount: Long,
    creatorInfo: CouponCreatorInfo,
    numberFormat: NumberFormat
): String? {
    if (!isCouponUsable(coupon, orderAmount)) {
        val minAmount = coupon.minOrderAmount ?: 0L
        return "${numberFormat.format(minAmount - orderAmount)}원 더 담으면 사용 가능"
    }
    if (coupon.type == CouponType.FREE_SHIPPING && creatorInfo.freeShipBlockReason != null) {
        return creatorInfo.freeShipBlockReason
    }
    return null
}

private val previewCoupons = listOf(
    Coupon(
        id = 1, creatorId = 1, code = "WINTER10", name = "겨울잠 준비 10% 할인",
        type = CouponType.PERCENTAGE, discountValue = 10L, minOrderAmount = 30000L,
        maxDiscountAmount = 3000L, status = UserCouponStatus.AVAILABLE,
        expiresAt = System.currentTimeMillis() + 3 * 86_400_000L, daysRemaining = 3
    ),
    Coupon(
        id = 2, creatorId = 1, code = "ONYX1000", name = "오닉스 네 살 1,000원 할인",
        type = CouponType.FIXED_AMOUNT, discountValue = 1000L, minOrderAmount = 10000L,
        maxDiscountAmount = null, status = UserCouponStatus.AVAILABLE,
        expiresAt = System.currentTimeMillis() + 40 * 86_400_000L, daysRemaining = 40
    ),
    Coupon(
        id = 3, creatorId = 2, code = "STAR2000", name = "별빛 겨울 2,000원 할인",
        type = CouponType.FIXED_AMOUNT, discountValue = 2000L, minOrderAmount = 30000L,
        maxDiscountAmount = null, status = UserCouponStatus.AVAILABLE,
        expiresAt = System.currentTimeMillis() + 15 * 86_400_000L, daysRemaining = 15
    )
)

private val previewCreators = mapOf(
    1 to CouponCreatorInfo(name = "오닉스", freeShipBlockReason = "멤버십 무료배송 적용 중"),
    2 to CouponCreatorInfo(name = "아마리")
)

@HaloLightDarkPreview
@Composable
private fun CouponSelectPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CouponSelectContent(
                coupons = previewCoupons,
                orderAmountByCreatorId = mapOf(1 to 45_000L, 2 to 18_000L),
                couponInfoByCreator = previewCreators,
                draftSelectedCoupons = mapOf(1 to previewCoupons[0]),
                couponCode = "",
                isClaiming = false,
                modifier = Modifier.height(640.dp)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponSelectEmptyPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CouponSelectContent(
                coupons = emptyList(),
                orderAmountByCreatorId = emptyMap(),
                couponInfoByCreator = previewCreators,
                draftSelectedCoupons = emptyMap(),
                couponCode = "",
                isClaiming = false,
                modifier = Modifier.height(640.dp)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponSelectErrorPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CouponSelectContent(
                coupons = emptyList(),
                orderAmountByCreatorId = emptyMap(),
                couponInfoByCreator = previewCreators,
                draftSelectedCoupons = emptyMap(),
                couponCode = "",
                isClaiming = false,
                error = "쿠폰을 불러올 수 없습니다",
                modifier = Modifier.height(640.dp)
            )
        }
    }
}
