package com.ninezero.halo.feature.subscription.domain.model

enum class SubscriptionPlanTier(val label: String) {
    FREE("무료"),
    TIER1("Tier 1"),
    TIER2("Tier 2")
}

data class SubscriptionPlan(
    val id: Int,
    val name: String,
    val tier: SubscriptionPlanTier,
    val description: String,
    val price: Long,
    val benefits: List<String>,
    val subscriberCount: Int,
    val isActive: Boolean
)

data class SubscriptionPlanListResult(
    val items: List<SubscriptionPlan>,
    val hasNext: Boolean
)
