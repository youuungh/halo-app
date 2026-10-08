package com.ninezero.halo.feature.subscription.domain.usecase

import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.subscription.domain.repository.SubscriptionRepository
import javax.inject.Inject

/** 활성 플랜이 있는 내 멤버십 등급 */
class GetMyPlanTiersUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(): Result<Set<SubscriptionPlanTier>> =
        subscriptionRepository.getMyPlans(page = 1, limit = 20)  // 등급마다 활성 플랜은 하나
            .map { result -> result.items.filter { it.isActive }.map { it.tier }.toSet() }
}
