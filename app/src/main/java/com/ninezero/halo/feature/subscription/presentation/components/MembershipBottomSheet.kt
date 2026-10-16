package com.ninezero.halo.feature.subscription.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.store.domain.model.BillingCard
import com.ninezero.halo.feature.store.presentation.components.cardLabel
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo
import com.ninezero.halo.ui.theme.HaloGradient
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun MembershipBottomSheet(
    creatorName: String,
    plans: List<SubscriptionPlan>,
    onSubscribe: (planId: Int) -> Unit,
    onDismiss: () -> Unit,
    currentPlanId: Int? = null,
    currentTier: UserTierInfo? = null,
    isLoading: Boolean = false,
    error: String? = null,
    subscribingPlanId: Int? = null,
    confirmingPlanId: Int? = null,
    confirmCard: BillingCard? = null,
    upgradeDiff: Long? = null,
    onPay: () -> Unit = {},
    onRetry: () -> Unit = {},
    onManageSubscription: () -> Unit = {}
) {
    // confirmValueChange는 SheetState key
    // 람다는 고정, 최신 값은 rememberUpdatedState로
    val currentProcessing = rememberUpdatedState(subscribingPlanId != null)
    val confirmValueChange = remember {
        { nextValue: SheetValue ->
            !(currentProcessing.value && nextValue == SheetValue.Hidden)
        }
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = confirmValueChange
    )

    ModalBottomSheet(
        onDismissRequest = {
            if (subscribingPlanId == null) onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) }  // 시트 inset 끄고 직접 처리
    ) {
        MembershipBottomSheetContent(
            creatorName = creatorName,
            plans = plans,
            currentPlanId = currentPlanId,
            currentTier = currentTier,
            isLoading = isLoading,
            error = error,
            subscribingPlanId = subscribingPlanId,
            confirmingPlanId = confirmingPlanId,
            confirmCard = confirmCard,
            upgradeDiff = upgradeDiff,
            onSubscribe = onSubscribe,
            onPay = onPay,
            onRetry = onRetry,
            onManageSubscription = onManageSubscription,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun MembershipBottomSheetContent(
    creatorName: String,
    plans: List<SubscriptionPlan>,
    onSubscribe: (planId: Int) -> Unit,
    onDismiss: () -> Unit,
    currentPlanId: Int? = null,
    currentTier: UserTierInfo? = null,
    isLoading: Boolean = false,
    error: String? = null,
    subscribingPlanId: Int? = null,
    confirmingPlanId: Int? = null,
    confirmCard: BillingCard? = null,
    upgradeDiff: Long? = null,
    onPay: () -> Unit = {},
    onRetry: () -> Unit = {},
    onManageSubscription: () -> Unit = {}
) {
    val density = LocalDensity.current
    val hazeState = remember { HazeState() }
    val backgroundColor = MaterialTheme.colorScheme.surface
    var headerHeight by remember { mutableStateOf(0.dp) }
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier.fillMaxWidth()) {
        val confirmPlan = confirmingPlanId?.let { id -> plans.find { it.id == id } }
        if (confirmPlan != null) {
            PaymentConfirmContent(
                plan = confirmPlan,
                card = confirmCard,
                isProcessing = subscribingPlanId == confirmingPlanId,
                upgradeDiff = upgradeDiff,
                topPadding = headerHeight + 16.dp,
                bottomPadding = 8.dp + navBottom,
                onPay = onPay
            )
        } else LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .hazeSource(hazeState)
                .blockSheetFling(),
            contentPadding = PaddingValues(
                top = headerHeight + 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp + navBottom
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            overscrollEffect = null
        ) {
            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator(size = LoadingIndicatorSize.Large)
                        }
                    }
                }

                error != null -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
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
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.bounceClick()
                                ) {
                                    Text("재시도")
                                }
                            }
                        }
                    }
                }

                plans.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "등록된 멤버십 플랜이 없습니다",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    // 구독 중 플랜은 상단, 목록에 없으면 받은 정보로
                    if (currentPlanId != null) {
                        val currentPlan = plans.find { it.id == currentPlanId }
                        val fallbackName = currentTier?.planName
                        val fallbackPrice = currentTier?.planPrice
                        when {
                            currentPlan != null -> item {
                                CurrentSubscriptionCard(
                                    name = currentPlan.name,
                                    tier = currentPlan.tier,
                                    price = currentPlan.price,
                                    onManageClick = onManageSubscription
                                )
                            }
                            currentTier != null && fallbackName != null && fallbackPrice != null -> item {
                                CurrentSubscriptionCard(
                                    name = fallbackName,
                                    tier = currentTier.tier,
                                    price = fallbackPrice,
                                    onManageClick = onManageSubscription
                                )
                            }
                        }
                    }

                    // 구독 중이면 상위 플랜만 표시
                    val currentPrice = currentPlanId?.let { id ->
                        (plans.find { it.id == id }?.price ?: currentTier?.planPrice)?.toBigDecimalOrNull()?.toLong()
                    }
                    val visiblePlans = if (currentPrice != null) {
                        plans.filter {
                            (it.price.toBigDecimalOrNull()?.toLong() ?: 0L) > currentPrice
                        }
                    } else {
                        plans.filter { it.id != currentPlanId }
                    }
                    items(
                        items = visiblePlans,
                        key = { it.id }
                    ) { plan ->
                        PlanCard(
                            plan = plan,
                            isSubscribing = subscribingPlanId == plan.id,
                            isUpgrade = currentPlanId != null,
                            onSubscribe = { onSubscribe(plan.id) }
                        )
                    }
                }
            }
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = backgroundColor,
                        tint = HaloBlurTint(backgroundColor),
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
            BottomSheetDragHandle()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    onClick = onDismiss,
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    hazeState = hazeState,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    size = 36.dp,
                    iconSize = 20.dp
                )

                Text(
                    text = "$creatorName 멤버십",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(36.dp))
            }
        }

        if (subscribingPlanId != null) {
            // matchParentSize라 크기에 영향 없이 덮기만
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
            )
        }
    }
}

@Composable
private fun CurrentSubscriptionCard(
    name: String,
    tier: SubscriptionPlanTier,
    price: String,
    onManageClick: () -> Unit
) {
    val numberFormat = rememberKoreanNumberFormat()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(brush = HaloGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_badge_filled),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "현재 구독 중",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                TierBadge(tier = tier, isOnGradient = true)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "₩${
                    price.toBigDecimalOrNull()?.let { numberFormat.format(it) } ?: price
                }/월",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onManageClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("구독 관리")
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isSubscribing: Boolean,
    isUpgrade: Boolean = false,
    onSubscribe: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val hasMoreBenefits = plan.benefits.size > 3
    val numberFormat = rememberKoreanNumberFormat()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TierBadge(tier = plan.tier, isOnGradient = false)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "₩${
                    plan.price.toBigDecimalOrNull()?.let { numberFormat.format(it) } ?: plan.price
                }/월",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (plan.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (plan.benefits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val benefitsToShow = if (isExpanded) plan.benefits else plan.benefits.take(3)
                    benefitsToShow.forEach { benefit ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (hasMoreBenefits) {
                        Text(
                            text = if (isExpanded) "접기" else "혜택 더보기",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { isExpanded = !isExpanded }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { if (!isSubscribing) onSubscribe() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isUpgrade) "업그레이드" else "구독하기",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    if (isSubscribing) {
                        LoadingIndicator(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            size = LoadingIndicatorSize.Small,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentConfirmContent(
    plan: SubscriptionPlan,
    card: BillingCard?,
    isProcessing: Boolean,
    upgradeDiff: Long?,
    topPadding: Dp,
    bottomPadding: Dp,
    onPay: () -> Unit
) {
    val numberFormat = rememberKoreanNumberFormat()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .blockSheetFling()
            .verticalScroll(rememberScrollState(), overscrollEffect = null)
            .padding(top = topPadding, start = 20.dp, end = 20.dp, bottom = bottomPadding)
    ) {
        // 선택 플랜 요약
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TierBadge(tier = plan.tier, isOnGradient = false)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₩${
                        plan.price.toBigDecimalOrNull()
                            ?.let { numberFormat.format(it) } ?: plan.price
                    }/월",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (upgradeDiff != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "지금 결제 (차액)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₩${numberFormat.format(upgradeDiff)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "결제수단",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_credit_card),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = cardLabel(card),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (!isProcessing) onPay() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .bounceClick(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "결제하기",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )
                if (isProcessing) {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        size = LoadingIndicatorSize.Small,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun TierBadge(
    tier: SubscriptionPlanTier,
    isOnGradient: Boolean = false
) {
    val text = when (tier) {
        SubscriptionPlanTier.FREE -> "무료"
        SubscriptionPlanTier.TIER1 -> "Tier 1"
        SubscriptionPlanTier.TIER2 -> "Tier 2"
    }

    val (backgroundColor, textColor) = if (isOnGradient) {
        Color.White.copy(alpha = 0.2f) to Color.White
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f) to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

private val previewPlans = listOf(
    SubscriptionPlan(
        id = 1,
        name = "Basic",
        tier = SubscriptionPlanTier.TIER1,
        description = "기본 혜택을 누려보세요",
        price = "4,900",
        benefits = listOf("전용 콘텐츠 열람", "커뮤니티 참여"),
        subscriberCount = 128,
        isActive = true
    ),
    SubscriptionPlan(
        id = 2,
        name = "Premium",
        tier = SubscriptionPlanTier.TIER2,
        description = "프리미엄 혜택을 누려보세요. 모든 기본 혜택 포함.",
        price = "9,900",
        benefits = listOf("전용 콘텐츠 열람", "커뮤니티 참여", "1:1 메시지", "할인 혜택", "굿즈 할인"),
        subscriberCount = 45,
        isActive = true
    )
)

private val previewCard = BillingCard(
    registered = true,
    cardCompany = "61",
    cardNumberMasked = "1234-56**-****-7890",
    cardType = "신용"
)

@HaloLightDarkPreview
@Composable
private fun MembershipBottomSheetPreview() {
    HaloTheme {
        Surface {
            MembershipBottomSheetContent(
                creatorName = "크리에이터",
                plans = previewPlans,
                currentPlanId = null,
                isLoading = false,
                subscribingPlanId = null,
                onSubscribe = {},
                onManageSubscription = {},
                onDismiss = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MembershipBottomSheetWithSubscriptionPreview() {
    HaloTheme {
        Surface {
            MembershipBottomSheetContent(
                creatorName = "크리에이터",
                plans = previewPlans,
                currentPlanId = 1,
                isLoading = false,
                subscribingPlanId = null,
                onSubscribe = {},
                onManageSubscription = {},
                onDismiss = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MembershipBottomSheetConfirmPreview() {
    HaloTheme {
        Surface {
            MembershipBottomSheetContent(
                creatorName = "크리에이터",
                plans = previewPlans,
                isLoading = false,
                subscribingPlanId = null,
                confirmingPlanId = 2,
                confirmCard = previewCard,
                onSubscribe = {},
                onPay = {},
                onManageSubscription = {},
                onDismiss = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MembershipBottomSheetConfirmUpgradePreview() {
    HaloTheme {
        Surface {
            MembershipBottomSheetContent(
                creatorName = "크리에이터",
                plans = previewPlans,
                currentPlanId = 1,
                isLoading = false,
                subscribingPlanId = null,
                confirmingPlanId = 2,
                confirmCard = previewCard,
                upgradeDiff = 5000L,
                onSubscribe = {},
                onPay = {},
                onManageSubscription = {},
                onDismiss = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MembershipBottomSheetConfirmProcessingPreview() {
    HaloTheme {
        Surface {
            MembershipBottomSheetContent(
                creatorName = "크리에이터",
                plans = previewPlans,
                isLoading = false,
                subscribingPlanId = 2,
                confirmingPlanId = 2,
                confirmCard = null,
                onSubscribe = {},
                onPay = {},
                onManageSubscription = {},
                onDismiss = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MembershipBottomSheetLoadingPreview() {
    HaloTheme {
        Surface {
            MembershipBottomSheetContent(
                creatorName = "크리에이터",
                plans = emptyList(),
                isLoading = true,
                subscribingPlanId = null,
                onSubscribe = {},
                onManageSubscription = {},
                onDismiss = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MembershipBottomSheetEmptyPreview() {
    HaloTheme {
        Surface {
            MembershipBottomSheetContent(
                creatorName = "크리에이터",
                plans = emptyList(),
                isLoading = false,
                subscribingPlanId = null,
                onSubscribe = {},
                onManageSubscription = {},
                onDismiss = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MembershipBottomSheetErrorPreview() {
    HaloTheme {
        Surface {
            MembershipBottomSheetContent(
                creatorName = "크리에이터",
                plans = emptyList(),
                isLoading = false,
                error = "플랜을 불러올 수 없습니다",
                subscribingPlanId = null,
                onSubscribe = {},
                onRetry = {},
                onManageSubscription = {},
                onDismiss = {}
            )
        }
    }
}
