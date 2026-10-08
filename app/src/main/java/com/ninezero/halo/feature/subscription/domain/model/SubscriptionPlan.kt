package com.ninezero.halo.feature.subscription.domain.model

enum class SubscriptionPlanTier {
    FREE,
    TIER1,
    TIER2
}

data class SubscriptionPlan(
    val id: Int,
    val name: String,
    val tier: SubscriptionPlanTier,
    val description: String,
    val price: String,
    val benefits: List<String>,
    val subscriberCount: Int,
    val isActive: Boolean
)

data class SubscriptionPlanListResult(
    val items: List<SubscriptionPlan>,
    val hasNext: Boolean
)
