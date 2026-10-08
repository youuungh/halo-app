package com.ninezero.halo.feature.subscription.presentation.membership_manage

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import java.time.LocalDateTime

internal fun previewMembershipManageState(): MembershipManageState {
    return MembershipManageState(isLoading = false, plans = previewMembershipPlans, totalSubscribers = 185)
}

internal fun previewMembershipManageDeleteDialogState(): MembershipManageState {
    return MembershipManageState(
        isLoading = false,
        plans = previewMembershipPlans,
        showDeleteDialog = true,
        deletingPlan = previewMembershipPlans.first()
    )
}

internal fun previewMembershipManageLoadingState(): MembershipManageState {
    return MembershipManageState(isLoading = true)
}

internal fun previewMembershipManageEmptyState(): MembershipManageState {
    return MembershipManageState(isLoading = false, plans = emptyList(), totalSubscribers = 0)
}

private val previewMembershipPlans = listOf(
    previewMembershipPlan(
        id = 1,
        name = "Basic",
        tier = SubscriptionPlanTier.TIER1,
        description = "기본 혜택을 누려보세요",
        price = "4,900",
        benefits = listOf("전용 콘텐츠 열람", "커뮤니티 참여"),
        subscriberCount = 128
    ),
    previewMembershipPlan(
        id = 2,
        name = "Premium",
        tier = SubscriptionPlanTier.TIER2,
        description = "프리미엄 혜택을 누려보세요. 모든 기본 혜택 포함.",
        price = "9,900",
        benefits = listOf("전용 콘텐츠 열람", "커뮤니티 참여", "1:1 메시지", "할인 혜택"),
        subscriberCount = 45
    )
)

private fun previewMembershipPlan(
    id: Int,
    name: String,
    tier: SubscriptionPlanTier,
    description: String,
    price: String,
    benefits: List<String>,
    subscriberCount: Int,
    now: LocalDateTime = LocalDateTime.now()
): SubscriptionPlan {
    return SubscriptionPlan(
        id = id,
        name = name,
        tier = tier,
        description = description,
        price = price,
        benefits = benefits,
        subscriberCount = subscriberCount,
        isActive = true
    )
}

@HaloLightDarkPreview
@Composable
private fun MembershipManageScreenPreview() {
    HaloScreenPreview {
        MembershipManageScreenContent(
            state = previewMembershipManageState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MembershipManageScreenDeleteDialogPreview() {
    HaloScreenPreview {
        MembershipManageScreenContent(
            state = previewMembershipManageDeleteDialogState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MembershipManageScreenLoadingPreview() {
    HaloScreenPreview {
        MembershipManageScreenContent(
            state = previewMembershipManageLoadingState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MembershipManageScreenEmptyPreview() {
    HaloScreenPreview {
        MembershipManageScreenContent(
            state = previewMembershipManageEmptyState(),
            onIntent = {}
        )
    }
}
