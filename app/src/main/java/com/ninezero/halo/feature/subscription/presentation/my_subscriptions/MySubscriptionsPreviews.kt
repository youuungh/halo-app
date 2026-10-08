package com.ninezero.halo.feature.subscription.presentation.my_subscriptions

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.subscription.domain.model.Subscription
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionStatus

internal fun previewMySubscriptionsState(): MySubscriptionsState {
    return MySubscriptionsState(
        isLoading = false,
        subscriptions = previewSubscriptions
    )
}

internal fun previewMySubscriptionsLoadingState(): MySubscriptionsState {
    return MySubscriptionsState(isLoading = true)
}

internal fun previewMySubscriptionsEmptyState(): MySubscriptionsState {
    return MySubscriptionsState(isLoading = false)
}

private val previewSubscriptions = listOf(
    previewSubscription(
        id = 1,
        creatorId = 2,
        creatorUsername = "크리에이터A",
        planId = 1,
        planName = "Premium",
        planPrice = "9,900",
        status = SubscriptionStatus.ACTIVE,
        startedDaysAgo = 20,
        expiresDaysFromNow = 10,
        autoRenew = true,
        daysRemaining = 10
    ),
    previewSubscription(
        id = 2,
        creatorId = 3,
        creatorUsername = "크리에이터B",
        planId = 2,
        planName = "Basic",
        planPrice = "4,900",
        status = SubscriptionStatus.ACTIVE,
        startedDaysAgo = 5,
        expiresDaysFromNow = 25,
        autoRenew = false,
        daysRemaining = 25
    ),
    previewSubscription(
        id = 3,
        creatorId = 4,
        creatorUsername = "크리에이터C",
        planId = 3,
        planName = "VIP",
        planPrice = "19,900",
        status = SubscriptionStatus.CANCELLED,
        startedDaysAgo = 30,
        expiresDaysFromNow = 5,
        cancelledDaysAgo = 2,
        autoRenew = false,
        daysRemaining = 5
    ),
    previewSubscription(
        id = 4,
        creatorId = 5,
        creatorUsername = "크리에이터D",
        planId = 4,
        planName = "Standard",
        planPrice = "7,900",
        status = SubscriptionStatus.ACTIVE,
        startedDaysAgo = 15,
        expiresDaysFromNow = 15,
        autoRenew = true,
        daysRemaining = 15
    ),
    previewSubscription(
        id = 5,
        creatorId = 6,
        creatorUsername = "크리에이터E",
        planId = 5,
        planName = "Supporter",
        planPrice = "2,900",
        status = SubscriptionStatus.EXPIRED,
        startedDaysAgo = 60,
        expiresDaysFromNow = -30,
        autoRenew = false,
        daysRemaining = 0
    )
)

private fun previewSubscription(
    id: Int,
    creatorId: Int,
    creatorUsername: String,
    planId: Int,
    planName: String,
    planPrice: String,
    status: SubscriptionStatus,
    startedDaysAgo: Long,
    expiresDaysFromNow: Long,
    autoRenew: Boolean,
    daysRemaining: Int,
    cancelledDaysAgo: Long? = null,
    now: Long = System.currentTimeMillis()
): Subscription {
    return Subscription(
        id = id,
        creatorId = creatorId,
        creatorUsername = creatorUsername,
        creatorRole = "CREATOR",
        planName = planName,
        planPrice = planPrice,
        status = status,
        expiresAt = now + expiresDaysFromNow * 86_400_000L,
        autoRenew = autoRenew,
        daysRemaining = daysRemaining
    )
}

@HaloLightDarkPreview
@Composable
private fun MySubscriptionsScreenPreview() {
    HaloScreenPreview {
        MySubscriptionsScreenContent(
            state = previewMySubscriptionsState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MySubscriptionsScreenLoadingPreview() {
    HaloScreenPreview {
        MySubscriptionsScreenContent(
            state = previewMySubscriptionsLoadingState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MySubscriptionsScreenEmptyPreview() {
    HaloScreenPreview {
        MySubscriptionsScreenContent(
            state = previewMySubscriptionsEmptyState(),
            onIntent = {}
        )
    }
}


