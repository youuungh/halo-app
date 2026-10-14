package com.ninezero.halo.feature.store.presentation.checkout.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.button.SheetActionButton
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.feature.store.domain.model.calcSingleCouponDiscount
import com.ninezero.halo.feature.store.domain.model.isCouponUsable
import com.ninezero.halo.feature.store.presentation.checkout.CouponCreatorInfo
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.LocalIsDarkTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import java.text.NumberFormat

@Composable
fun CouponSelectBottomSheet(
    coupons: List<Coupon>,
    orderCreatorIds: Set<Int>,
    orderAmountByCreatorId: Map<Int, Long>,
    couponInfoByCreator: Map<Int, CouponCreatorInfo>,
    draftSelectedCoupons: Map<Int, Coupon>,
    canApply: Boolean,
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
    val numberFormat = rememberKoreanNumberFormat()
    val sheetProperties = remember { ModalBottomSheetProperties(shouldDismissOnBackPress = false) }  // 시트 기본 뒤로가기는 예측형 비활성화

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets(0) },
        properties = sheetProperties,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        BackHandler(onBack = onDismiss)
        CouponSelectContent(
            coupons = coupons,
            orderCreatorIds = orderCreatorIds,
            orderAmountByCreatorId = orderAmountByCreatorId,
            couponInfoByCreator = couponInfoByCreator,
            draftSelectedCoupons = draftSelectedCoupons,
            canApply = canApply,
            couponCode = couponCode,
            isClaiming = isClaiming,
            isLoading = isLoading,
            error = error,
            numberFormat = numberFormat,
            onToggleCoupon = onToggleCoupon,
            onUpdateCouponCode = onUpdateCouponCode,
            onClaimCoupon = onClaimCoupon,
            onApply = onApply,
            onDismiss = onDismiss,
            onRetry = onRetry
        )
    }
}

@Composable
private fun CouponSelectContent(
    coupons: List<Coupon>,
    orderCreatorIds: Set<Int>,
    couponInfoByCreator: Map<Int, CouponCreatorInfo>,
    draftSelectedCoupons: Map<Int, Coupon>,
    couponCode: String,
    isClaiming: Boolean,
    orderAmountByCreatorId: Map<Int, Long> = emptyMap(),
    canApply: Boolean = draftSelectedCoupons.isNotEmpty(),
    isLoading: Boolean = false,
    error: String? = null,
    numberFormat: NumberFormat = rememberKoreanNumberFormat(),
    onToggleCoupon: (Coupon) -> Unit = {},
    onUpdateCouponCode: (String) -> Unit = {},
    onClaimCoupon: () -> Unit = {},
    onApply: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onRetry: () -> Unit = {},
    initialExpanded: Boolean = false
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(0.dp) }
    var isExpanded by remember { mutableStateOf(initialExpanded) }

    val selectedCount = draftSelectedCoupons.size
    val totalDiscount = remember(draftSelectedCoupons, orderAmountByCreatorId) {
        calcPreviewDiscount(draftSelectedCoupons, orderAmountByCreatorId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 콘텐츠
        when {
            // 로딩 상태
            isLoading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .hazeSource(hazeState)
                        .blockSheetFling(),
                    contentPadding = PaddingValues(
                        top = headerHeight + 8.dp,
                        bottom = 140.dp
                    ),
                    overscrollEffect = null
                ) {
                    items(5) {
                        CouponSelectItemSkeleton(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            // 에러 상태
            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                        .padding(top = headerHeight + 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onRetry,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }
            }
            // 빈 상태
            coupons.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                        .padding(top = headerHeight + 8.dp)
                ) {
                    CouponCodeInput(
                        value = couponCode,
                        onValueChange = onUpdateCouponCode,
                        onClaimClick = onClaimCoupon,
                        isClaiming = isClaiming,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "사용 가능한 쿠폰이 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // 쿠폰 목록
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .hazeSource(hazeState)
                        .blockSheetFling(),
                    contentPadding = PaddingValues(
                        top = headerHeight + 8.dp,
                        bottom = 140.dp
                    ),
                    overscrollEffect = null
                ) {
                    item(key = "coupon_code_input") {
                        CouponCodeInput(
                            value = couponCode,
                            onValueChange = onUpdateCouponCode,
                            onClaimClick = onClaimCoupon,
                            isClaiming = isClaiming,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(coupons, key = { it.id }) { coupon ->
                        val isApplicableToCurrentOrder = coupon.creatorId in orderCreatorIds
                        val creatorInfo = couponInfoByCreator[coupon.creatorId]
                        val freeShipBlockReason = if (coupon.type == CouponType.FREE_SHIPPING) {
                            creatorInfo?.freeShipBlockReason
                        } else null
                        val isSelected = draftSelectedCoupons[coupon.creatorId]?.id == coupon.id
                        val creatorLabel = creatorInfo?.name
                            ?: coupon.creatorName
                            ?: "크리에이터 #${coupon.creatorId}"
                        val creatorOrderAmount = orderAmountByCreatorId[coupon.creatorId] ?: 0L
                        val disabledReason = when {
                            !isApplicableToCurrentOrder -> "특정 크리에이터 상품에만 사용 가능"
                            // 서버 거부 조건은 선택 차단
                            !isCouponUsable(coupon, creatorOrderAmount) ->
                                "최소 주문 금액 미달 (${numberFormat.format(coupon.minOrderAmount?.toBigDecimal()?.toInt() ?: 0)}원 이상)"
                            freeShipBlockReason != null -> "무료배송 쿠폰 사용 불가: $freeShipBlockReason"
                            else -> null
                        }
                        CouponSelectItem(
                            coupon = coupon,
                            creatorLabel = creatorLabel,
                            isEnabled = disabledReason == null,
                            disabledReason = disabledReason,
                            isSelected = isSelected,
                            numberFormat = numberFormat,
                            onClick = { onToggleCoupon(coupon) }
                        )
                    }
                }
            }
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .hazeEffect(state = hazeState) {
                    style = HazeStyle(
                        backgroundColor = backgroundColor,
                        tint = HazeTint(backgroundColor),
                        noiseFactor = 0f
                    )
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1f,
                        endIntensity = 0f
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .onGloballyPositioned { coordinates ->
                    headerHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    onClick = onDismiss,
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    size = 36.dp,
                    iconSize = 20.dp
                )
                Text(
                    text = "할인쿠폰",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 하단 바
        val isDark = LocalIsDarkTheme.current
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = if (isExpanded) 20.dp else 0.dp,
                        topEnd = if (isExpanded) 20.dp else 0.dp
                    )
                )
                .hazeEffect(state = hazeState) {
                    style = HazeStyle(
                        backgroundColor = backgroundColor,
                        tint = HazeTint(bottomTintColor),
                        blurRadius = 25.dp,
                        noiseFactor = 0f
                    )
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = if (isExpanded) 0.2f else 0f,
                        endIntensity = 1f
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 8.dp)
        ) {

            AnimatedVisibility(
                visible = isExpanded && selectedCount > 0,
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
                                ) { isExpanded = false },
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 가격 상세
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .hazeEffect(state = hazeState) {
                                style = HazeStyle(
                                    backgroundColor = backgroundColor,
                                    tint = HazeTint(expandedBoxTintColor),
                                    blurRadius = 40.dp,
                                    noiseFactor = 0f
                                )
                            }
                            .padding(12.dp)
                    ) {
                        draftSelectedCoupons.entries.forEachIndexed { index, (creatorId, coupon) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = coupon.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                //실제 할인액
                                Text(
                                    text = "-${numberFormat.format(previewDiscountFor(coupon, orderAmountByCreatorId[creatorId] ?: 0L))}원",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (index < draftSelectedCoupons.size - 1) {
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // 요약 행
            if (selectedCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "총 ${numberFormat.format(totalDiscount)}원",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${selectedCount}개 사용)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        painter = painterResource(
                            if (isExpanded) R.drawable.ic_circle_chevron_down
                            else R.drawable.ic_circle_chevron_up
                        ),
                        contentDescription = if (isExpanded) "접기" else "펼치기",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 적용 버튼
            SheetActionButton(
                text = "사용하기",
                onClick = onApply,
                enabled = canApply
            )
        }
    }
}

@Composable
private fun CouponSelectItem(
    coupon: Coupon,
    creatorLabel: String,
    isEnabled: Boolean,
    disabledReason: String?,
    isSelected: Boolean,
    numberFormat: NumberFormat,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(IntrinsicSize.Min)
            .border(
                width = 0.5.dp,
                color = if (!isEnabled) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (!isEnabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable(enabled = isEnabled, onClick = onClick)
            .alpha(if (isEnabled) 1f else 0.62f)
    ) {
        // 좌측: 쿠폰 정보
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            // 할인 금액
            Row {
                Text(
                    text = coupon.discountText,
                    modifier = Modifier.alignByBaseline(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                coupon.maxDiscountAmount?.let { maxAmount ->
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(최대 ${numberFormat.format(maxAmount.toBigDecimal().toInt())}원)",
                        modifier = Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 쿠폰명, 무료배송은 숨김
            if (coupon.type != CouponType.FREE_SHIPPING) {
                Text(
                    text = coupon.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            CreatorExclusiveBadge(text = "$creatorLabel 상품 전용")
            if (disabledReason != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = disabledReason,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 유효기간
            Text(
                text = "${coupon.expiresAt.toFormattedDate("yyyy.MM.dd")}까지",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 최소 주문 금액
            coupon.minOrderAmount?.let { minAmount ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${numberFormat.format(minAmount.toBigDecimal().toInt())}원 이상 주문 시",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 우측: CheckCircle
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    if (isSelected) R.drawable.ic_circle_check
                    else R.drawable.ic_circle
                ),
                contentDescription = if (isSelected) "선택됨" else "선택안됨",
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
private fun CreatorExclusiveBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CouponCodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    onClaimClick: () -> Unit,
    isClaiming: Boolean,
    modifier: Modifier = Modifier
) {
    val isClaimEnabled = value.isNotBlank() && !isClaiming

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.uppercase()) },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            enabled = !isClaiming,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (isClaimEnabled) onClaimClick() }
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = "쿠폰 코드 입력",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isClaimEnabled) MaterialTheme.colorScheme.inverseSurface
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable(enabled = isClaimEnabled, onClick = onClaimClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isClaiming) {
                LoadingIndicator(
                    size = LoadingIndicatorSize.Small,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            } else {
                Text(
                    text = "등록",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isClaimEnabled) MaterialTheme.colorScheme.inverseOnSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))
    }
}

private fun previewDiscountFor(coupon: Coupon, groupAmount: Long): Long =
    if (coupon.type == CouponType.FREE_SHIPPING) {
        if (isCouponUsable(coupon, groupAmount)) Constants.Commerce.DEFAULT_SHIPPING_FEE else 0L
    } else {
        calcSingleCouponDiscount(coupon, groupAmount)
    }

private fun calcPreviewDiscount(
    selectedCoupons: Map<Int, Coupon>,
    orderAmountByCreatorId: Map<Int, Long>
): Long {
    var totalDiscount = 0L

    for ((creatorId, coupon) in selectedCoupons) {
        val groupAmount = orderAmountByCreatorId[creatorId] ?: continue
        totalDiscount += previewDiscountFor(coupon, groupAmount)
    }

    return totalDiscount
}

private val previewCoupons = listOf(
    Coupon(
        id = 1, creatorId = 1, code = "WELCOME10", name = "신규 가입 할인 쿠폰",
        type = CouponType.PERCENTAGE, discountValue = "10", minOrderAmount = "10000",
        maxDiscountAmount = "5000", status = UserCouponStatus.AVAILABLE,
        expiresAt = System.currentTimeMillis() + 30 * 86_400_000L, daysRemaining = 30
    ),
    Coupon(
        id = 2, creatorId = 2, code = "SAVE5000", name = "5천원 할인 쿠폰",
        type = CouponType.FIXED_AMOUNT, discountValue = "5000", minOrderAmount = "30000",
        maxDiscountAmount = null, status = UserCouponStatus.AVAILABLE,
        expiresAt = System.currentTimeMillis() + 3 * 86_400_000L, daysRemaining = 2
    ),
    Coupon(
        id = 3, creatorId = 3, code = "FREESHIP", name = "무료배송 쿠폰",
        type = CouponType.FREE_SHIPPING, discountValue = "0", minOrderAmount = null,
        maxDiscountAmount = null, status = UserCouponStatus.AVAILABLE,
        expiresAt = System.currentTimeMillis() + 15 * 86_400_000L, daysRemaining = 15
    )
)

@HaloLightDarkPreview
@Composable
private fun CouponSelectNonePreview() {
    HaloTheme {
        CouponSelectContent(
            coupons = previewCoupons,
            orderCreatorIds = setOf(1, 2),
            orderAmountByCreatorId = mapOf(1 to 50_000L, 2 to 20_000L),
            couponInfoByCreator = mapOf(
                1 to CouponCreatorInfo(name = "크리에이터A"),
                2 to CouponCreatorInfo(name = "크리에이터B"),
                3 to CouponCreatorInfo(
                    name = "크리에이터C",
                    freeShipBlockReason = "멤버십 무료배송 적용 중"
                )
            ),
            draftSelectedCoupons = emptyMap(),
            couponCode = "",
            isClaiming = false
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponSelectOnePreview() {
    HaloTheme {
        CouponSelectContent(
            coupons = previewCoupons,
            orderCreatorIds = setOf(1, 2),
            orderAmountByCreatorId = mapOf(1 to 50_000L, 2 to 50_000L),
            couponInfoByCreator = mapOf(
                1 to CouponCreatorInfo(name = "크리에이터A"),
                2 to CouponCreatorInfo(name = "크리에이터B"),
                3 to CouponCreatorInfo(name = "크리에이터C")
            ),
            draftSelectedCoupons = mapOf(1 to previewCoupons[0]),
            couponCode = "",
            isClaiming = false
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponSelectTwoExpandedPreview() {
    HaloTheme {
        CouponSelectContent(
            coupons = previewCoupons,
            orderCreatorIds = setOf(1, 2),
            orderAmountByCreatorId = mapOf(1 to 50_000L, 2 to 50_000L),
            couponInfoByCreator = mapOf(
                1 to CouponCreatorInfo(name = "크리에이터A"),
                2 to CouponCreatorInfo(name = "크리에이터B"),
                3 to CouponCreatorInfo(name = "크리에이터C")
            ),
            draftSelectedCoupons = mapOf(
                1 to previewCoupons[0],
                2 to previewCoupons[1]
            ),
            couponCode = "",
            isClaiming = false,
            initialExpanded = true
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponSelectLoadingPreview() {
    HaloTheme {
        CouponSelectContent(
            coupons = emptyList(),
            orderCreatorIds = emptySet(),
            couponInfoByCreator = emptyMap(),
            draftSelectedCoupons = emptyMap(),
            couponCode = "",
            isClaiming = false,
            isLoading = true
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponSelectEmptyPreview() {
    HaloTheme {
        CouponSelectContent(
            coupons = emptyList(),
            orderCreatorIds = emptySet(),
            couponInfoByCreator = emptyMap(),
            draftSelectedCoupons = emptyMap(),
            couponCode = "",
            isClaiming = false
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponSelectErrorPreview() {
    HaloTheme {
        CouponSelectContent(
            coupons = emptyList(),
            orderCreatorIds = emptySet(),
            couponInfoByCreator = emptyMap(),
            draftSelectedCoupons = emptyMap(),
            couponCode = "",
            isClaiming = false,
            error = "쿠폰을 불러올 수 없습니다"
        )
    }
}
