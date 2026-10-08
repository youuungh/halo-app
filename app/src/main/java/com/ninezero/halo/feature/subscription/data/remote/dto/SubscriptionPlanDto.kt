package com.ninezero.halo.feature.subscription.data.remote.dto

import com.ninezero.halo.core.network.PagedDto
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanListResult
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionPlanDto(
    val id: Int,
    val creatorId: Int,
    val creatorUsername: String,
    val name: String,
    val tier: String,
    val description: String,
    val price: String,
    val benefits: List<String>,
    val subscriberCount: Int,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)

typealias SubscriptionPlanListResponseDto = PagedDto<SubscriptionPlanDto>

@Serializable
data class CreateSubscriptionPlanRequestDto(
    val name: String,
    val tier: String,
    val description: String,
    val price: String,
    val benefits: List<String>
)

@Serializable
data class UpdateSubscriptionPlanRequestDto(
    val name: String? = null,
    val description: String? = null,
    val price: String? = null,
    val benefits: List<String>? = null,
    val isActive: Boolean? = null
)

fun SubscriptionPlanDto.toDomain(): SubscriptionPlan {
    return SubscriptionPlan(
        id = id,
        name = name,
        tier = try {
            SubscriptionPlanTier.valueOf(tier)
        } catch (e: Exception) {
            SubscriptionPlanTier.TIER1
        },
        description = description,
        price = price,
        benefits = benefits,
        subscriberCount = subscriberCount,
        isActive = isActive
    )
}

fun SubscriptionPlanListResponseDto.toDomain(): SubscriptionPlanListResult {
    return SubscriptionPlanListResult(
        items = items.map { it.toDomain() },
        hasNext = hasNext
    )
}
