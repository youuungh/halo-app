package com.ninezero.halo.feature.subscription.data.remote.dto

import com.ninezero.halo.core.network.PagedDto
import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.subscription.domain.model.Subscription
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionListResult
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionStatus
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionStatusInfo
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionDto(
    val id: Int,
    val userId: Int,
    val creatorId: Int,
    val creatorUsername: String,
    val creatorAvatarUrl: String? = null,
    val creatorRole: String? = null,
    val planId: Int,
    val planName: String,
    val planPrice: String,
    val status: String,
    val startedAt: String,
    val expiresAt: String,
    val cancelledAt: String? = null,
    val autoRenew: Boolean,
    val daysRemaining: Int,
    val createdAt: String
)

typealias SubscriptionPaginatedDto = PagedDto<SubscriptionDto>

typealias SubscriberListResponseDto = PagedDto<UserSummaryDto>

@Serializable
data class SubscriptionListResponseDto(
    val subscriptions: SubscriptionPaginatedDto,
    val activeCount: Int
)

@Serializable
data class SubscribeRequestDto(
    val planId: Int,
    val autoRenew: Boolean = true
)

@Serializable
data class SubscriptionStatusResponseDto(
    val isSubscribed: Boolean,
    val subscriptionId: Int? = null,
    val planId: Int? = null,
    val planName: String? = null,
    val expiresAt: String? = null,
    val daysRemaining: Int? = null,
    val autoRenew: Boolean = false
)

@Serializable
data class UserTierResponseDto(
    val tier: String,
    val isSubscribed: Boolean,
    val subscriptionId: Int? = null,
    val planName: String? = null,
    val planPrice: String? = null,
    val expiresAt: String? = null,
    val daysRemaining: Int? = null
)

fun SubscriptionDto.toDomain(): Subscription {
    return Subscription(
        id = id,
        creatorId = creatorId,
        creatorUsername = creatorUsername,
        creatorAvatarUrl = creatorAvatarUrl,
        creatorRole = creatorRole,
        planName = planName,
        planPrice = planPrice,
        status = try {
            SubscriptionStatus.valueOf(status)
        } catch (e: Exception) {
            SubscriptionStatus.ACTIVE
        },
        expiresAt = expiresAt.parseTimestamp(),
        autoRenew = autoRenew,
        daysRemaining = daysRemaining
    )
}

fun SubscriptionListResponseDto.toDomain(): SubscriptionListResult {
    return SubscriptionListResult(
        items = subscriptions.items.map { it.toDomain() },
        hasNext = subscriptions.hasNext
    )
}

fun SubscriptionStatusResponseDto.toDomain(): SubscriptionStatusInfo {
    return SubscriptionStatusInfo(
        isSubscribed = isSubscribed,
        planId = planId
    )
}

fun UserTierResponseDto.toDomain(): UserTierInfo {
    return UserTierInfo(
        tier = try {
            SubscriptionPlanTier.valueOf(tier)
        } catch (e: Exception) {
            SubscriptionPlanTier.FREE
        },
        isSubscribed = isSubscribed,
        planName = planName,
        planPrice = planPrice
    )
}
