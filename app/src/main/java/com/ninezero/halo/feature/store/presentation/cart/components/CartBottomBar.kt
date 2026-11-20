package com.ninezero.halo.feature.store.presentation.cart.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.PrimaryButton
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.text.AnimatedCounter
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.DarkGlassTint
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.core.ui.theme.MembershipColors
import com.ninezero.halo.feature.store.presentation.components.MembershipBreakdown
import com.ninezero.halo.feature.store.presentation.components.MembershipLine
import com.ninezero.halo.feature.store.presentation.components.PriceBreakdownRow
import com.ninezero.halo.feature.store.presentation.components.PriceSubLine
import com.ninezero.halo.feature.store.presentation.components.PriceSubLines
import dev.chrisbanes.haze.HazeState
import java.text.NumberFormat

/** 하단 바 가격 내역 */
@Immutable
data class CartBarSummary(
    val itemCount: Int = 0,
    // 할인 전 정가 합계
    val productAmount: Long = 0L,
    val productDiscount: Long = 0L,
    // 멤버십 무료배송 빼기 전 배송비
    val shippingFee: Long = 0L,
    val orderTotal: Long = 0L,
    val membership: MembershipBreakdown = MembershipBreakdown()
)

private val BarShape = RoundedCornerShape(32.dp)

// 그림자 없이 가장자리 선만
private val LightBarOutline = Shadow(radius = 0.dp, spread = 0.5.dp, color = Color.Black.copy(alpha = 0.1f))
private val DarkBarOutline = Shadow(radius = 0.dp, spread = 0.5.dp, color = Color.White.copy(alpha = 0.12f))
// 위 가장자리 안쪽 빛
private val LightBarHighlight = Shadow(radius = 1.dp, color = Color.White.copy(alpha = 0.9f), offset = DpOffset(0.dp, 1.dp))
private val DarkBarHighlight = Shadow(radius = 1.dp, color = Color.White.copy(alpha = 0.12f), offset = DpOffset(0.dp, 1.dp))
private val LightBarTint = Color.White.copy(alpha = 0.62f)
private val DarkBarTint = DarkGlassTint.copy(alpha = 0.62f)

@Composable
fun CartBottomBar(
    summary: CartBarSummary,
    numberFormat: NumberFormat,
    hazeState: HazeState,
    backgroundColor: Color,
    onCheckout: () -> Unit,
    onBottomBarHeightChanged: (Dp) -> Unit,
    // 펼침 · 접힘 중에도 매 프레임 알림
    onContentHeightChange: (Dp) -> Unit,
    modifier: Modifier = Modifier,
    onContentHeightChanged: (Dp) -> Unit = {},
) {
    var isExpanded by remember { mutableStateOf(false) }
    val hasSelectedItems = summary.itemCount > 0
    val density = LocalDensity.current
    val isDark = LocalIsDarkTheme.current
    val chevronRotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "chevron")
    // 선택이 모두 풀리면 접힌 상태로 돌아감
    LaunchedEffect(hasSelectedItems) {
        if (!hasSelectedItems) isExpanded = false
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            // 카드 뒤로 목록이 점점 흐려지며 사라짐
            .haloBlur(hazeState, HaloBlurs.fadeUp(backgroundColor))
            .onGloballyPositioned { coordinates ->
                if (!isExpanded) {
                    onBottomBarHeightChanged(with(density) { coordinates.size.height.toDp() })
                }
            }
            // 키보드 위 또는 네비바 위
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
            // 카드 위 흐려지는 자리, 스낵바 간격엔 안 넣음
            .padding(top = 28.dp)
            // 인셋·흐려지는 자리 제외 높이
            .onGloballyPositioned { coordinates ->
                val height = with(density) { coordinates.size.height.toDp() }
                onContentHeightChange(height)
                if (!isExpanded) {
                    onContentHeightChanged(height)
                }
            }
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .dropShadow(BarShape, if (isDark) DarkBarOutline else LightBarOutline)
                .clip(BarShape)
                // 서리 낀 블러, 흰 면을 깔아 글자가 읽히게
                .haloBlur(
                    hazeState,
                    HaloBlurStyle(
                        backgroundColor = backgroundColor,
                        tint = if (isDark) DarkBarTint else LightBarTint,
                        blurRadius = 24.dp,
                        noiseFactor = 0f
                    )
                )
                .innerShadow(BarShape, if (isDark) DarkBarHighlight else LightBarHighlight)
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 12.dp)
        ) {
            // 펼친 가격 내역
            AnimatedVisibility(
                visible = isExpanded && hasSelectedItems,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                PriceBreakdown(
                    summary = summary,
                    numberFormat = numberFormat,
                    onCollapse = { isExpanded = false }
                )
            }

            // 요약 행
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = hasSelectedItems,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isExpanded = !isExpanded }
                    .padding(start = 6.dp, end = 6.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("총 ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append("${summary.itemCount}건")
                        }
                        append(" 주문 예상금액")
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (hasSelectedItems) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_up),
                        contentDescription = if (isExpanded) "접기" else "펼치기",
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer { rotationZ = chevronRotation },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "0원",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 주문 버튼
            PrimaryButton(
                onClick = onCheckout,
                enabled = hasSelectedItems,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (hasSelectedItems) {
                    AnimatedCounter(
                        count = summary.orderTotal.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        suffix = "원 주문하기",
                        shiftDigits = false
                    )
                } else {
                    Text(
                        text = "주문하기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceBreakdown(
    summary: CartBarSummary,
    numberFormat: NumberFormat,
    onCollapse: () -> Unit
) {
    val membershipColor = MembershipColors.label
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Column(modifier = Modifier.fillMaxWidth()) {
        // 접기 손잡이
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCollapse
                )
                .padding(bottom = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
            )
        }

        Column(modifier = Modifier.padding(horizontal = 6.dp)) {
            PriceBreakdownRow("상품 금액", "${numberFormat.format(summary.productAmount)}원")
            if (summary.productDiscount > 0L) {
                PriceBreakdownRow(
                    label = "상품 할인",
                    value = "-${numberFormat.format(summary.productDiscount)}원",
                    valueColor = MaterialTheme.colorScheme.error
                )
            }
            PriceBreakdownRow("배송비", "${numberFormat.format(summary.shippingFee)}원")
            if (summary.membership.shippingLines.isNotEmpty()) {
                PriceSubLines {
                    summary.membership.shippingLines.forEach { line ->
                        PriceSubLine(
                            label = "${line.creatorName} 멤버십 무료배송",
                            value = "-${numberFormat.format(line.amount)}원",
                            color = membershipColor,
                            showSparkle = true
                        )
                    }
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = dividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "결제 예정 금액",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${numberFormat.format(summary.orderTotal)}원",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            PriceBreakdownRow(
                label = "구매 시 적립",
                value = "${numberFormat.format(summary.membership.totalEarnPoints)}P",
                small = true
            )
            // 멤버십 적립이 섞일 때만 나눠 보여 줌
            if (summary.membership.earnLines.isNotEmpty()) {
                PriceSubLines {
                    if (summary.membership.baseEarnPoints > 0L) {
                        PriceSubLine(
                            label = "기본 적립 1%",
                            value = "${numberFormat.format(summary.membership.baseEarnPoints)}P",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    summary.membership.earnLines.forEach { line ->
                        PriceSubLine(
                            label = "${line.creatorName} 멤버십 적립 ${line.ratePercent}%",
                            value = "${numberFormat.format(line.amount)}P",
                            color = membershipColor,
                            showSparkle = true
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = dividerColor,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
        )
    }
}

private val PreviewSummary = CartBarSummary(
    itemCount = 5,
    productAmount = 89_000L,
    productDiscount = 6_000L,
    shippingFee = 9_000L,
    orderTotal = 89_000L,
    membership = MembershipBreakdown(
        shippingLines = listOf(MembershipLine("오닉스", 3_000L)),
        baseEarnPoints = 320L,
        earnLines = listOf(MembershipLine("오닉스", 2_250L, 5))
    )
)

@HaloLightDarkPreview
@Composable
private fun CartBottomBarPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                CartBottomBar(
                    summary = PreviewSummary,
                    numberFormat = rememberKoreanNumberFormat(),
                    hazeState = remember { HazeState() },
                    backgroundColor = MaterialTheme.colorScheme.background,
                    onCheckout = {},
                    onBottomBarHeightChanged = {},
                    onContentHeightChange = {}
                )
                CartBottomBar(
                    summary = CartBarSummary(),
                    numberFormat = rememberKoreanNumberFormat(),
                    hazeState = remember { HazeState() },
                    backgroundColor = MaterialTheme.colorScheme.background,
                    onCheckout = {},
                    onBottomBarHeightChanged = {},
                    onContentHeightChange = {}
                )
            }
        }
    }
}
