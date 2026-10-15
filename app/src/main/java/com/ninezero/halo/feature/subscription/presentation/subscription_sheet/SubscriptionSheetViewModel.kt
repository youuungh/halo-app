package com.ninezero.halo.feature.subscription.presentation.subscription_sheet

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.network.ApiException
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.feed.domain.event.SubscriptionEventBus
import com.ninezero.halo.feature.store.domain.model.billingAuthFailMessage
import com.ninezero.halo.feature.store.domain.model.isBillingAuthCanceled
import com.ninezero.halo.feature.store.domain.repository.BillingRepository
import com.ninezero.halo.feature.subscription.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class SubscriptionSheetViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val billingRepository: BillingRepository,
    private val subscriptionEventBus: SubscriptionEventBus
) : ViewModel(), OrbitContainerHost<SubscriptionSheetState, SubscriptionSheetState, SubscriptionSheetSideEffect> {

    override val container: OrbitContainer<SubscriptionSheetState, SubscriptionSheetState, SubscriptionSheetSideEffect> =
        orbitContainer(SubscriptionSheetState())

    fun onIntent(intent: SubscriptionSheetIntent) {
        when (intent) {
            is SubscriptionSheetIntent.Open -> open(intent.creatorId, intent.creatorName)
            is SubscriptionSheetIntent.Subscribe -> subscribe(intent.planId)
            is SubscriptionSheetIntent.ConfirmPayment -> confirmPayment()
            is SubscriptionSheetIntent.Retry -> loadPlans()
            is SubscriptionSheetIntent.Dismiss -> dismiss()
            is SubscriptionSheetIntent.BillingAuthReturned -> onBillingAuthReturned(intent.authKey)
            is SubscriptionSheetIntent.BillingAuthDismissed -> onBillingAuthDismissed()
            is SubscriptionSheetIntent.BillingAuthFailed -> onBillingAuthFailed(intent.code, intent.message)
        }
    }

    private fun open(creatorId: Int, creatorName: String) = intent {
        reduce {
            state.copy(
                isVisible = true,
                creatorId = creatorId,
                creatorName = creatorName,
                plans = emptyList(),
                currentPlanId = null,
                error = null,
                confirmingPlanId = null,
                confirmCard = null,
                upgradeDiff = null
            )
        }
        loadPlans()
    }

    private fun loadPlans() = intent {
        val creatorId = state.creatorId ?: return@intent
        reduce { state.copy(isLoading = true, error = null) }

        // 업그레이드 판정용 현재 플랜 함께 조회, 상태 실패는 무시
        val (plansResult, statusResult, tierResult) = coroutineScope {
            val plans = async { subscriptionRepository.getCreatorPlans(creatorId, page = 1, limit = 20) }
            val status = async { subscriptionRepository.getSubscriptionStatus(creatorId) }
            val tier = async { subscriptionRepository.getCreatorTier(creatorId) }
            Triple(plans.await(), status.await(), tier.await())
        }
        if (state.creatorId != creatorId) return@intent
        plansResult
            .onSuccess { result ->
                reduce {
                    state.copy(
                        isLoading = false,
                        plans = result.items,
                        currentPlanId = statusResult.getOrNull()?.planId,
                        currentTier = tierResult.getOrNull()
                    )
                }
            }
            .onFailure { e -> reduce { state.copy(isLoading = false, error = e.message ?: "플랜을 불러오지 못했습니다") } }
    }

    private fun subscribe(planId: Int) = intent {  // 결제수단 없으면 카드 등록 WebView
        reduce { state.copy(subscribingPlanId = planId) }

        val card = billingRepository.getMyBillingKey().getOrElse { e ->
            reduce { state.copy(subscribingPlanId = null) }
            postSideEffect(SubscriptionSheetSideEffect.ShowToast(e.message ?: "결제수단을 확인하지 못했습니다"))
            return@intent
        }

        if (card.registered) {
            // 저장카드 있음 → 바로 청구하지 않고 결제 확인 단계로
            reduce {
                state.copy(
                    subscribingPlanId = null,
                    confirmingPlanId = planId,
                    confirmCard = card,
                    upgradeDiff = state.upgradeDiffTo(planId)
                )
            }
            return@intent
        }

        // 결제수단 없으면 카드 등록 WebView
        val customerKey = billingRepository.getCustomerKey().getOrElse { e ->
            reduce { state.copy(subscribingPlanId = null) }
            postSideEffect(SubscriptionSheetSideEffect.ShowToast(e.message ?: "결제 준비에 실패했습니다"))
            return@intent
        }
        reduce {
            state.copy(
                subscribingPlanId = null,
                pendingPlanId = planId,
                billingWebViewUrl = Constants.billingAuthUrl(customerKey),
                showBillingWebView = true
            )
        }
    }

    private fun onBillingAuthReturned(authKey: String) = intent {  // 빌링키 발급 후 결제 확인 단계
        val planId = state.pendingPlanId
        reduce { state.copy(showBillingWebView = false, subscribingPlanId = planId) }
        if (planId == null) {
            reduce { state.copy(subscribingPlanId = null) }
            return@intent
        }

        billingRepository.issueBillingKey(authKey)
            .onSuccess { card ->
                // 카드 등록 후 결제 확인 단계
                reduce {
                    state.copy(
                        subscribingPlanId = null,
                        pendingPlanId = null,
                        confirmingPlanId = planId,
                        confirmCard = card,
                        upgradeDiff = state.upgradeDiffTo(planId)
                    )
                }
            }
            .onFailure { e ->
                reduce { state.copy(subscribingPlanId = null, pendingPlanId = null) }
                postSideEffect(SubscriptionSheetSideEffect.ShowToast(e.message ?: "카드 등록에 실패했습니다"))
            }
    }

    private fun SubscriptionSheetState.upgradeDiffTo(planId: Int): Long? {  // 상위 티어가 아니면 null
        if (currentPlanId == null) return null
        val currentPrice = plans.find { it.id == currentPlanId }?.price ?: currentTier?.planPrice ?: return null
        val target = plans.find { it.id == planId } ?: return null
        return ((target.price.toBigDecimalOrNull()?.toLong() ?: 0L) -
            (currentPrice.toBigDecimalOrNull()?.toLong() ?: 0L)).takeIf { it > 0L }
    }

    private fun onBillingAuthDismissed() = intent {
        reduce { state.copy(showBillingWebView = false, subscribingPlanId = null, pendingPlanId = null) }
    }

    private fun onBillingAuthFailed(code: String?, message: String?) = intent {
        reduce { state.copy(showBillingWebView = false, subscribingPlanId = null, pendingPlanId = null) }
        if (isBillingAuthCanceled(code)) return@intent
        postSideEffect(SubscriptionSheetSideEffect.ShowToast(billingAuthFailMessage(message)))
    }

    private fun confirmPayment() = intent {  // 실제 청구
        // 연타 가드, 이중 청구 방지
        if (state.subscribingPlanId != null) return@intent
        val planId = state.confirmingPlanId ?: return@intent
        reduce { state.copy(subscribingPlanId = planId) }
        if (state.upgradeDiff != null) {
            performUpgrade(planId)
        } else {
            performSubscribe(planId)
        }
    }

    private fun performUpgrade(planId: Int) = intent {
        subscriptionRepository.upgradeSubscription(planId)
            .onSuccess { subscription ->
                reduce {
                    state.copy(
                        subscribingPlanId = null,
                        isVisible = false,
                        currentPlanId = planId,
                        confirmingPlanId = null,
                        confirmCard = null,
                        upgradeDiff = null
                    )
                }
                subscriptionEventBus.emitSubscribed(subscription.creatorId)
                postSideEffect(SubscriptionSheetSideEffect.ShowToast("업그레이드가 완료되었습니다"))
            }
            .onFailure { e ->
                if (e.isPaymentResultUnknown()) {
                    closeForUnknownResult()
                } else {
                    reduce { state.copy(subscribingPlanId = null) }
                    postSideEffect(SubscriptionSheetSideEffect.ShowToast(e.message ?: "업그레이드에 실패했습니다"))
                }
            }
    }

    private fun performSubscribe(planId: Int) = intent {
        subscriptionRepository.subscribe(planId, autoRenew = true)
            .onSuccess { subscription ->
                reduce {
                    state.copy(
                        subscribingPlanId = null,
                        isVisible = false,
                        currentPlanId = planId,
                        pendingPlanId = null,
                        confirmingPlanId = null,
                        confirmCard = null,
                        upgradeDiff = null
                    )
                }
                // 구독한 크리에이터의 잠금 글들이 언락되도록 broadcast
                subscriptionEventBus.emitSubscribed(subscription.creatorId)
                postSideEffect(SubscriptionSheetSideEffect.ShowToast("구독이 완료되었습니다"))
            }
            .onFailure { e ->
                if (e.isPaymentResultUnknown()) {
                    closeForUnknownResult()
                } else {
                    reduce { state.copy(subscribingPlanId = null, pendingPlanId = null) }
                    postSideEffect(SubscriptionSheetSideEffect.ShowToast(e.message ?: "구독에 실패했습니다"))
                }
            }
    }

    // 결과 불명이라 재결제를 막고 닫음, 확정은 서버
    private fun closeForUnknownResult() = intent {
        reduce {
            state.copy(
                subscribingPlanId = null,
                pendingPlanId = null,
                isVisible = false,
                confirmingPlanId = null,
                confirmCard = null,
                upgradeDiff = null
            )
        }
        postSideEffect(SubscriptionSheetSideEffect.ShowToast("결제 결과를 확인하고 있습니다. 잠시 후 내 구독에서 확인해주세요"))
    }

    // 연결 끊김·타임아웃·5xx는 결제 결과 불명
    private fun Throwable.isPaymentResultUnknown(): Boolean {
        val code = (this as? ApiException)?.code ?: return true
        return code == 0 || code >= 500
    }

    private fun dismiss() = intent {
        reduce { state.copy(isVisible = false, confirmingPlanId = null, confirmCard = null, upgradeDiff = null) }
    }

}
