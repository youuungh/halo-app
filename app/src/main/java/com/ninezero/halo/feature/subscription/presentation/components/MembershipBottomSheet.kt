package com.ninezero.halo.feature.subscription.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.SheetHeader
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.button.PrimaryButton
import com.ninezero.halo.core.ui.components.list.ListErrorState
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.media.Avatar
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.components.selector.SegmentedTabRow
import com.ninezero.halo.core.ui.components.selector.segmentedTabTransition
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.MembershipColors
import com.ninezero.halo.feature.store.domain.model.BillingCard
import com.ninezero.halo.feature.store.presentation.components.cardLabel
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun MembershipBottomSheet(
    creatorName: String,
    creatorAvatarUrl: String?,
    creatorRole: String?,
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

    FloatingBottomSheet(
        onDismissRequest = {
            if (subscribingPlanId == null) onDismiss()
        },
        sheetState = sheetState,
        showDragHandle = false,  // 헤더 안에 있음
        dismissEnabled = subscribingPlanId == null
    ) {
        MembershipBottomSheetContent(
            creatorName = creatorName,
            creatorAvatarUrl = creatorAvatarUrl,
            creatorRole = creatorRole,
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
    creatorAvatarUrl: String?,
    creatorRole: String?,
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

    Box(modifier = Modifier.fillMaxWidth()) {
        val confirmPlan = confirmingPlanId?.let { id -> plans.find { it.id == id } }
        if (confirmPlan != null) {
            PaymentConfirmContent(
                plan = confirmPlan,
                card = confirmCard,
                isProcessing = subscribingPlanId == confirmingPlanId,
                upgradeDiff = upgradeDiff,
                topPadding = headerHeight + 16.dp,
                bottomPadding = 16.dp,
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
                bottom = 16.dp
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
                            ListErrorState(
                                title = "멤버십 정보를 불러오지 못했습니다",
                                reason = error,
                                onRetry = onRetry
                            )
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
                                text = "등록된 멤버십 플랜이 없어요",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> item {
                    MembershipPlansContent(
                        creatorName = creatorName,
                        creatorAvatarUrl = creatorAvatarUrl,
                        creatorRole = creatorRole,
                        plans = plans,
                        currentPlanId = currentPlanId,
                        currentTier = currentTier,
                        subscribingPlanId = subscribingPlanId,
                        onSubscribe = onSubscribe,
                        onManageSubscription = onManageSubscription
                    )
                }
            }
        }

        SheetHeader(
            title = "$creatorName 멤버십",
            onClose = onDismiss,
            hazeState = hazeState,
            onHeightChange = { headerHeight = it }
        )

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

/** 크리에이터 머리 + 플랜 탭 + 고른 플랜 칸 + 결제 버튼 */
@Composable
private fun MembershipPlansContent(
    creatorName: String,
    creatorAvatarUrl: String?,
    creatorRole: String?,
    plans: List<SubscriptionPlan>,
    currentPlanId: Int?,
    currentTier: UserTierInfo?,
    subscribingPlanId: Int?,
    onSubscribe: (planId: Int) -> Unit,
    onManageSubscription: () -> Unit
) {
    val numberFormat = rememberKoreanNumberFormat()
    val isSubscribed = currentPlanId != null
    val currentPlan = plans.find { it.id == currentPlanId }
    // 비활성 현재 플랜은 목록에 없어 받은 정보로
    val currentName = currentPlan?.name ?: currentTier?.planName
    val currentPrice = currentPlan?.price ?: currentTier?.planPrice
    // 구독 중이면 지금 플랜과 상위 플랜만
    val tabPlans = if (isSubscribed && currentPrice != null) {
        plans.filter { it.id == currentPlanId || it.price > currentPrice }
    } else {
        plans
    }
    // 처음엔 업그레이드할 플랜, 없으면 이용 중 플랜
    var selectedId by remember(tabPlans) {
        mutableStateOf(tabPlans.firstOrNull { it.id != currentPlanId }?.id ?: tabPlans.firstOrNull()?.id)
    }
    val selectedIndex = tabPlans.indexOfFirst { it.id == selectedId }
    val selected = tabPlans.getOrNull(selectedIndex)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CreatorHeader(
            creatorName = creatorName,
            avatarUrl = creatorAvatarUrl,
            role = creatorRole,
            currentPlanName = if (isSubscribed) currentName ?: "" else null,
            onManageSubscription = onManageSubscription
        )

        if (tabPlans.size > 1) {
            SegmentedTabRow(
                tabs = tabPlans.map { it.name },
                selectedIndex = selectedIndex,
                onTabSelected = { selectedId = tabPlans[it].id }
            )
        }

        if (selected != null) {
            // 칸은 그대로, 안의 내용만 바뀜
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .cardBorder(MaterialTheme.shapes.large)
            ) {
                AnimatedContent(
                    targetState = selectedIndex,
                    transitionSpec = { segmentedTabTransition() },
                    label = "plan_tab"
                ) { index ->
                    tabPlans.getOrNull(index)?.let { PlanDetailContent(plan = it) }
                }
            }

            val isCurrent = selected.id == currentPlanId
            val upgradeDiff = if (isSubscribed && !isCurrent && currentPrice != null) {
                (selected.price - currentPrice).takeIf { it > 0L }
            } else {
                null
            }
            var lastUpgradeDiff by remember { mutableLongStateOf(0L) }
            var lastUpgradePrice by remember { mutableLongStateOf(0L) }
            if (upgradeDiff != null) {
                lastUpgradeDiff = upgradeDiff
                lastUpgradePrice = selected.price
            }
            AnimatedVisibility(
                visible = upgradeDiff != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                // 사라지는 동안에도 마지막 값을 그림
                val diff = upgradeDiff ?: lastUpgradeDiff
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "지금 결제 (차액)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "₩${numberFormat.format(diff)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "다음 결제일부터 월 ₩${numberFormat.format(lastUpgradePrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            PrimaryButton(
                text = when {
                    isCurrent -> "이용 중인 플랜입니다"
                    isSubscribed -> "${selected.name} 플랜으로 업그레이드"
                    else -> "${selected.name} 구독하기"
                },
                onClick = { onSubscribe(selected.id) },
                isLoading = subscribingPlanId == selected.id,
                enabled = !isCurrent
            )
        }
    }
}

/** 사진 · 이름, 구독 중이면 이용 중 알약 */
@Composable
private fun CreatorHeader(
    creatorName: String,
    avatarUrl: String?,
    role: String?,
    currentPlanName: String?,
    onManageSubscription: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(
            model = avatarUrl,
            size = 40.dp
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = creatorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                VerifiedBadge(role = role)
            }
            if (currentPlanName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (currentPlanName.isBlank()) "이용 중" else "$currentPlanName 이용 중",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MembershipColors.label,
                    modifier = Modifier
                        .border(0.5.dp, MembershipColors.label, CircleShape)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }

        if (currentPlanName != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onManageSubscription() }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "구독 관리",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/** 고른 플랜의 가격 · 설명 · 혜택 */
@Composable
private fun PlanDetailContent(plan: SubscriptionPlan) {
    var isExpanded by remember(plan.id) { mutableStateOf(false) }
    val hasMoreBenefits = plan.benefits.size > 3
    val numberFormat = rememberKoreanNumberFormat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .animateContentSize()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "₩${numberFormat.format(plan.price)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/월",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                )
            }
            // 잠금 문구의 등급과 이어지게
            TierBadge(tier = plan.tier)
        }

        if (plan.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
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
            shape = MaterialTheme.shapes.large,
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
                    TierBadge(tier = plan.tier)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₩${numberFormat.format(plan.price)}/월",
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
                .clip(MaterialTheme.shapes.medium)
                .cardBorder(MaterialTheme.shapes.medium)
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

        PrimaryButton(
            text = "결제하기",
            onClick = onPay,
            isLoading = isProcessing
        )
    }
}

@Composable
private fun TierBadge(tier: SubscriptionPlanTier) {
    val text = tier.label

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val previewPlans = listOf(
    SubscriptionPlan(
        id = 1,
        name = "Basic",
        tier = SubscriptionPlanTier.TIER1,
        description = "기본 혜택을 누려보세요",
        price = 4900L,
        benefits = listOf("전용 콘텐츠 열람", "커뮤니티 참여"),
        subscriberCount = 128,
        isActive = true
    ),
    SubscriptionPlan(
        id = 2,
        name = "Premium",
        tier = SubscriptionPlanTier.TIER2,
        description = "프리미엄 혜택을 누려보세요. 모든 기본 혜택 포함.",
        price = 9900L,
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
                creatorAvatarUrl = null,
                creatorRole = "CREATOR",
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
                creatorAvatarUrl = null,
                creatorRole = "CREATOR",
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
                creatorAvatarUrl = null,
                creatorRole = "CREATOR",
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
                creatorAvatarUrl = null,
                creatorRole = "CREATOR",
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
                creatorAvatarUrl = null,
                creatorRole = "CREATOR",
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
                creatorAvatarUrl = null,
                creatorRole = "CREATOR",
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
                creatorAvatarUrl = null,
                creatorRole = "CREATOR",
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
                creatorAvatarUrl = null,
                creatorRole = "CREATOR",
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
