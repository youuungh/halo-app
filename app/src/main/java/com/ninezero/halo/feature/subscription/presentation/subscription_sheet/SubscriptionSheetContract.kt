package com.ninezero.halo.feature.subscription.presentation.subscription_sheet

import com.ninezero.halo.feature.store.domain.model.BillingCard
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo

data class SubscriptionSheetState(
    val isVisible: Boolean = false,
    val creatorId: Int? = null,
    val creatorName: String = "",
    val plans: List<SubscriptionPlan> = emptyList(),
    val currentPlanId: Int? = null,
    val currentTier: UserTierInfo? = null,  // 비활성 현재 플랜은 목록에 없어 따로 받음
    val isLoading: Boolean = false,
    val error: String? = null,
    val subscribingPlanId: Int? = null,
    // 결제 확인 단계
    val confirmingPlanId: Int? = null,
    val confirmCard: BillingCard? = null,
    val upgradeDiff: Long? = null,  // 업그레이드면 차액, null이면 일반
    // 빌링 카드 등록
    val pendingPlanId: Int? = null,
    val showBillingWebView: Boolean = false,
    val billingWebViewUrl: String? = null
)

sealed interface SubscriptionSheetIntent {
    data class Open(val creatorId: Int, val creatorName: String) : SubscriptionSheetIntent
    data class Subscribe(val planId: Int) : SubscriptionSheetIntent
    data object ConfirmPayment : SubscriptionSheetIntent
    data object Retry : SubscriptionSheetIntent
    data object Dismiss : SubscriptionSheetIntent
    // 빌링 등록 결과
    data class BillingAuthReturned(val authKey: String) : SubscriptionSheetIntent
    data object BillingAuthDismissed : SubscriptionSheetIntent
}

sealed interface SubscriptionSheetSideEffect {
    data class ShowToast(val message: String) : SubscriptionSheetSideEffect
}
