package com.ninezero.halo.feature.store.presentation.cart.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassActionButton
import com.ninezero.halo.core.ui.components.counter.AnimatedCounter
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.LocalIsDarkTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import java.text.NumberFormat

@Composable
fun CartBottomBar(
    selectedItemCount: Int,
    selectedTotalPrice: Long,
    selectedTotalShippingFee: Long,
    selectedTotalDiscount: Long,
    selectedOriginalShippingFee: Long,
    hasMembershipFreeShipping: Boolean,
    selectedOrderTotal: Long,
    hasSelectedItems: Boolean,
    numberFormat: NumberFormat,
    hazeState: HazeState,
    backgroundColor: Color,
    onCheckout: () -> Unit,
    onBottomBarHeightChanged: (Dp) -> Unit,
    modifier: Modifier = Modifier,
    onContentHeightChanged: (Dp) -> Unit = {},
) {
    var isBottomExpanded by remember { mutableStateOf(false) }
    val isDark = LocalIsDarkTheme.current
    val density = LocalDensity.current
    val bottomTintColor = if (isDark) {
        Color.White.copy(alpha = 0.2f)
    } else {
        Color.Black.copy(alpha = 0.2f)
    }
    val expandedBoxTintColor = if (isDark) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.Black.copy(alpha = 0.1f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .haloBlur(state = hazeState) {
                style = HaloBlurStyle(
                    backgroundColor = backgroundColor,
                    tint = HaloBlurTint(bottomTintColor),
                    blurRadius = 25.dp,
                    noiseFactor = 0f
                )
                progressive = HazeProgressive.verticalGradient(
                    startIntensity = 0.2f,
                    endIntensity = 1f
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { /* 터치 이벤트 소비 */ }
            }
            .onGloballyPositioned { coordinates ->
                if (!isBottomExpanded) {
                    onBottomBarHeightChanged(with(density) { coordinates.size.height.toDp() })
                }
            }
            // 키보드 위 또는 네비바 위
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
            // 인셋 제외 콘텐츠 높이
            .onGloballyPositioned { coordinates ->
                if (!isBottomExpanded) {
                    onContentHeightChanged(with(density) { coordinates.size.height.toDp() })
                }
            }
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 12.dp)
    ) {
        // 펼친 가격 내역
        AnimatedVisibility(
            visible = isBottomExpanded,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 접기 버튼
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_down),
                        contentDescription = "접기",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { isBottomExpanded = false },
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 가격 상세
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .haloBlur(state = hazeState) {
                            style = HaloBlurStyle(
                                backgroundColor = backgroundColor,
                                tint = HaloBlurTint(expandedBoxTintColor),
                                blurRadius = 40.dp,
                                noiseFactor = 0f
                            )
                        }
                        .padding(12.dp)
                ) {
                    SummaryRow(
                        label = "총 선택상품금액",
                        value = "${numberFormat.format(selectedTotalPrice)}원",
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (hasMembershipFreeShipping && selectedTotalShippingFee == 0L) {
                        // "무료배송" + 취소선
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "총 배송비",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "무료배송",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${numberFormat.format(selectedOriginalShippingFee)}원",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    } else {
                        SummaryRow(
                            label = "총 배송비",
                            value = if (selectedTotalShippingFee == 0L) "무료배송"
                                    else "+${numberFormat.format(selectedTotalShippingFee)}원",
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (selectedTotalDiscount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        SummaryRow(
                            label = "총 할인예상금액",
                            value = "-${numberFormat.format(selectedTotalDiscount)}원",
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            valueColor = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // 요약 행
        val glowColor = if (isDark) Color.White else Color.Black
        val textGlow = Shadow(
            color = glowColor.copy(alpha = 0.5f),
            blurRadius = 8f
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isBottomExpanded = !isBottomExpanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    append("총 ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append("${selectedItemCount}건")
                    }
                    append(" 주문 예상금액")
                },
                style = MaterialTheme.typography.bodyMedium.copy(shadow = textGlow),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            AnimatedCounter(
                count = selectedOrderTotal.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = textGlow
                ),
                color = MaterialTheme.colorScheme.onSurface,
                suffix = "원"
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(
                    if (isBottomExpanded) R.drawable.ic_circle_chevron_down
                    else R.drawable.ic_circle_chevron_up
                ),
                contentDescription = if (isBottomExpanded) "접기" else "펼치기",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 주문 버튼
        GlassActionButton(
            onClick = onCheckout,
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick(),
            enabled = hasSelectedItems
        ) {
            Text(
                text = "주문하기",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CartBottomBarPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                CartBottomBar(
                    selectedItemCount = 3,
                    selectedTotalPrice = 243_000L,
                    selectedTotalShippingFee = 3_000L,
                    selectedTotalDiscount = 20_000L,
                    selectedOriginalShippingFee = 3_000L,
                    hasMembershipFreeShipping = false,
                    selectedOrderTotal = 226_000L,
                    hasSelectedItems = true,
                    numberFormat = rememberKoreanNumberFormat(),
                    hazeState = remember { HazeState() },
                    backgroundColor = MaterialTheme.colorScheme.background,
                    onCheckout = {},
                    onBottomBarHeightChanged = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CartBottomBarEmptyPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                CartBottomBar(
                    selectedItemCount = 0,
                    selectedTotalPrice = 0L,
                    selectedTotalShippingFee = 0L,
                    selectedTotalDiscount = 0L,
                    selectedOriginalShippingFee = 0L,
                    hasMembershipFreeShipping = false,
                    selectedOrderTotal = 0L,
                    hasSelectedItems = false,
                    numberFormat = rememberKoreanNumberFormat(),
                    hazeState = remember { HazeState() },
                    backgroundColor = MaterialTheme.colorScheme.background,
                    onCheckout = {},
                    onBottomBarHeightChanged = {}
                )
                CartBottomBar(
                    selectedItemCount = 2,
                    selectedTotalPrice = 178_000L,
                    selectedTotalShippingFee = 0L,
                    selectedTotalDiscount = 0L,
                    selectedOriginalShippingFee = 3_000L,
                    hasMembershipFreeShipping = true,
                    selectedOrderTotal = 178_000L,
                    hasSelectedItems = true,
                    numberFormat = rememberKoreanNumberFormat(),
                    hazeState = remember { HazeState() },
                    backgroundColor = MaterialTheme.colorScheme.background,
                    onCheckout = {},
                    onBottomBarHeightChanged = {}
                )
            }
        }
    }
}
