package com.ninezero.halo.feature.subscription.data.repository

import com.ninezero.halo.core.network.dto.toDomain
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallNoData
import com.ninezero.halo.feature.subscription.data.remote.api.SubscriptionApi
import com.ninezero.halo.feature.subscription.data.remote.dto.CreateSubscriptionPlanRequestDto
import com.ninezero.halo.feature.subscription.data.remote.dto.SubscribeRequestDto
import com.ninezero.halo.feature.subscription.data.remote.dto.UpdateSubscriptionPlanRequestDto
import com.ninezero.halo.feature.subscription.data.remote.dto.toDomain
import com.ninezero.halo.feature.subscription.domain.model.SubscriberListResult
import com.ninezero.halo.feature.subscription.domain.model.Subscription
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionListResult
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanListResult
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionStatus
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionStatusInfo
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo
import com.ninezero.halo.feature.subscription.domain.repository.SubscriptionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val api: SubscriptionApi
) : SubscriptionRepository {

    // 구독 플랜, Creator
    override suspend fun getMyPlans(page: Int, limit: Int): Result<SubscriptionPlanListResult> {
        return safeApiCall {
            api.getMyPlans(page, limit)
        }.map { it.toDomain() }
    }

    override suspend fun createPlan(
        name: String,
        tier: SubscriptionPlanTier,
        description: String,
        price: String,
        benefits: List<String>
    ): Result<SubscriptionPlan> {
        return safeApiCall {
            api.createPlan(
                CreateSubscriptionPlanRequestDto(
                    name = name,
                    tier = tier.name,
                    description = description,
                    price = price,
                    benefits = benefits
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun updatePlan(
        planId: Int,
        name: String?,
        description: String?,
        price: String?,
        benefits: List<String>?,
        isActive: Boolean?
    ): Result<SubscriptionPlan> {
        return safeApiCall {
            api.updatePlan(
                planId,
                UpdateSubscriptionPlanRequestDto(
                    name = name,
                    description = description,
                    price = price,
                    benefits = benefits,
                    isActive = isActive
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun deletePlan(planId: Int): Result<Unit> {
        return safeApiCallNoData {
            api.deletePlan(planId)
        }
    }

    override suspend fun getMySubscribers(page: Int, limit: Int, search: String?): Result<SubscriberListResult> {
        return safeApiCall {
            api.getMySubscribers(page, limit, search)
        }.map { response ->
            SubscriberListResult(
                items = response.items.map { it.toDomain() },
                totalCount = response.totalCount,
                hasNext = response.hasNext
            )
        }
    }

    // 구독 플랜, 공개
    override suspend fun getCreatorPlans(creatorId: Int, page: Int, limit: Int): Result<SubscriptionPlanListResult> {
        return safeApiCall {
            api.getCreatorPlans(creatorId, page, limit)
        }.map { it.toDomain() }
    }

    // 구독
    override suspend fun subscribe(planId: Int, autoRenew: Boolean): Result<Subscription> {
        return safeApiCall {
            api.subscribe(SubscribeRequestDto(planId, autoRenew))
        }.map { it.toDomain() }
    }

    override suspend fun upgradeSubscription(planId: Int): Result<Subscription> {
        return safeApiCall {
            api.upgradeSubscription(SubscribeRequestDto(planId, autoRenew = true))
        }.map { it.toDomain() }
    }

    override suspend fun getMySubscriptions(
        status: SubscriptionStatus?,
        page: Int,
        limit: Int
    ): Result<SubscriptionListResult> {
        return safeApiCall {
            api.getMySubscriptions(status?.name, page, limit)
        }.map { it.toDomain() }
    }

    override suspend fun getSubscriptionStatus(creatorId: Int): Result<SubscriptionStatusInfo> {
        return safeApiCall {
            api.getSubscriptionStatus(creatorId)
        }.map { it.toDomain() }
    }

    override suspend fun getCreatorTier(creatorId: Int): Result<UserTierInfo> {
        return safeApiCall {
            api.getCreatorTier(creatorId)
        }.map { it.toDomain() }
    }

    override suspend fun cancelSubscription(subscriptionId: Int): Result<Unit> {
        return safeApiCallNoData {
            api.cancelSubscription(subscriptionId)
        }
    }

    override suspend fun updateAutoRenew(subscriptionId: Int, autoRenew: Boolean): Result<Unit> {
        return safeApiCallNoData {
            api.updateAutoRenew(subscriptionId, autoRenew)
        }
    }
}
