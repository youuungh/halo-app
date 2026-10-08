package com.ninezero.halo.feature.subscription.domain.repository

import com.ninezero.halo.feature.subscription.domain.model.SubscriberListResult
import com.ninezero.halo.feature.subscription.domain.model.Subscription
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionListResult
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanListResult
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionStatus
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionStatusInfo
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo

interface SubscriptionRepository {
    // 구독 플랜, Creator

    /** 내 플랜 목록 조회 */
    suspend fun getMyPlans(page: Int, limit: Int): Result<SubscriptionPlanListResult>

    /** 플랜 생성 */
    suspend fun createPlan(
        name: String,
        tier: SubscriptionPlanTier,
        description: String,
        price: String,
        benefits: List<String>
    ): Result<SubscriptionPlan>

    /** 플랜 수정 */
    suspend fun updatePlan(
        planId: Int,
        name: String?,
        description: String?,
        price: String?,
        benefits: List<String>?,
        isActive: Boolean?
    ): Result<SubscriptionPlan>

    /** 플랜 삭제 */
    suspend fun deletePlan(planId: Int): Result<Unit>

    /** 내 구독자 목록 조회 */
    suspend fun getMySubscribers(page: Int, limit: Int, search: String?): Result<SubscriberListResult>

    // 구독 플랜, 공개
    /** 크리에이터 플랜 목록 조회 */
    suspend fun getCreatorPlans(creatorId: Int, page: Int, limit: Int): Result<SubscriptionPlanListResult>

    // 구독

    /** 구독 */
    suspend fun subscribe(planId: Int, autoRenew: Boolean): Result<Subscription>
    /** 구독 업그레이드 */
    suspend fun upgradeSubscription(planId: Int): Result<Subscription>

    /** 내 구독 목록 조회 */
    suspend fun getMySubscriptions(status: SubscriptionStatus?, page: Int, limit: Int): Result<SubscriptionListResult>

    /** 크리에이터 구독 상태 확인 */
    suspend fun getSubscriptionStatus(creatorId: Int): Result<SubscriptionStatusInfo>

    /** 내 구독 티어 확인 */
    suspend fun getCreatorTier(creatorId: Int): Result<UserTierInfo>

    /** 구독 취소 */
    suspend fun cancelSubscription(subscriptionId: Int): Result<Unit>

    /** 자동갱신 설정 변경 */
    suspend fun updateAutoRenew(subscriptionId: Int, autoRenew: Boolean): Result<Unit>
}
