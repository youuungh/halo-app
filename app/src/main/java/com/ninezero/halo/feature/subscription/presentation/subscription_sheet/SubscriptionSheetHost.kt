package com.ninezero.halo.feature.subscription.presentation.subscription_sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.bottomsheet.WebViewAuthBottomSheet
import com.ninezero.halo.core.ui.components.post.LocalSubscribeRequester
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.showToast
import com.ninezero.halo.feature.subscription.presentation.components.MembershipBottomSheet
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/** 전역 구독 시트 호스트 */
@Composable
fun SubscriptionSheetHost(
    onNavigateToMySubscriptions: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SubscriptionSheetViewModel = hiltViewModel()
    val state by viewModel.collectAsState()
    val billingHost = remember { Constants.BASE_URL.toUri().host ?: "10.0.2.2" }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is SubscriptionSheetSideEffect.ShowToast -> context.showToast(effect.message)
        }
    }

    CompositionLocalProvider(
        LocalSubscribeRequester provides { creatorId, creatorName ->
            viewModel.onIntent(SubscriptionSheetIntent.Open(creatorId, creatorName))
        }
    ) {
        content()
    }

    if (state.isVisible) {
        MembershipBottomSheet(
            creatorName = state.creatorName,
            plans = state.plans,
            currentPlanId = state.currentPlanId,
            currentTier = state.currentTier,
            isLoading = state.isLoading,
            error = state.error,
            subscribingPlanId = state.subscribingPlanId,
            confirmingPlanId = state.confirmingPlanId,
            confirmCard = state.confirmCard,
            upgradeDiff = state.upgradeDiff,
            onSubscribe = { planId -> viewModel.onIntent(SubscriptionSheetIntent.Subscribe(planId)) },
            onPay = { viewModel.onIntent(SubscriptionSheetIntent.ConfirmPayment) },
            onRetry = { viewModel.onIntent(SubscriptionSheetIntent.Retry) },
            onManageSubscription = {
                viewModel.onIntent(SubscriptionSheetIntent.Dismiss)
                onNavigateToMySubscriptions()
            },
            onDismiss = { viewModel.onIntent(SubscriptionSheetIntent.Dismiss) }
        )
    }

    // 결제수단 미등록 시 카드 등록 WebView
    val billingUrl = state.billingWebViewUrl
    if (state.showBillingWebView && billingUrl != null) {
        WebViewAuthBottomSheet(
            url = billingUrl,
            title = "카드 등록",
            allowedHostSuffixes = listOf(billingHost, "tosspayments.com"),
            returnScheme = "haloapp",
            onReturn = { uri ->
                val authKey = uri.getQueryParameter("authKey")
                if (authKey != null) {
                    viewModel.onIntent(SubscriptionSheetIntent.BillingAuthReturned(authKey))
                } else {
                    viewModel.onIntent(SubscriptionSheetIntent.BillingAuthDismissed)
                }
            },
            onDismiss = { viewModel.onIntent(SubscriptionSheetIntent.BillingAuthDismissed) }
        )
    }
}
