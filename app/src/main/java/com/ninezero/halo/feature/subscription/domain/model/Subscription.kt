package com.ninezero.halo.feature.subscription.domain.model

import com.ninezero.halo.core.model.UserSummary

enum class SubscriptionStatus {
    ACTIVE,
    CANCELLED,
    EXPIRED
}

data class Subscription(
    val id: Int,
    val creatorId: Int,
    val creatorUsername: String,
    val creatorAvatarUrl: String? = null,
    val creatorRole: String? = null,
    val planName: String,
    val planPrice: String,
    val status: SubscriptionStatus,
    val expiresAt: Long,
    val autoRenew: Boolean,
    val daysRemaining: Int
)

data class SubscriptionListResult(
    val items: List<Subscription>,
    val hasNext: Boolean
)

data class SubscriptionStatusInfo(
    val isSubscribed: Boolean,
    val planId: Int?
)

data class SubscriberListResult(
    val items: List<UserSummary>,
    val totalCount: Int,
    val hasNext: Boolean
)

data class UserTierInfo(
    val tier: SubscriptionPlanTier,
    val isSubscribed: Boolean,
    // 현재 구독 플랜, 비활성화된 플랜도 포함
    val planName: String? = null,
    val planPrice: String? = null
)
