package com.ninezero.halo.feature.subscription.presentation.my_subscriptions

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.subscription.domain.model.Subscription
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionStatus

data class MySubscriptionsState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val subscriptions: List<Subscription> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
    // 필터
    val selectedStatus: SubscriptionStatus? = null,  // null = 전체
    // 자동 갱신 변경
    val isUpdatingAutoRenew: Boolean = false,
    // 자동 갱신 해제 확인 다이얼로그
    val autoRenewOffTarget: Subscription? = null,
    val error: String? = null,
    val toastMessage: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface MySubscriptionsIntent {
    data object RefreshSubscriptions : MySubscriptionsIntent
    data object LoadMoreSubscriptions : MySubscriptionsIntent
    data object NavigateBack : MySubscriptionsIntent
    // 필터
    data class FilterByStatus(val status: SubscriptionStatus?) : MySubscriptionsIntent
    // 자동 갱신 변경
    data class ToggleAutoRenew(val subscriptionId: Int, val autoRenew: Boolean) : MySubscriptionsIntent
    data class RequestAutoRenewOff(val subscription: Subscription) : MySubscriptionsIntent
    data object ConfirmAutoRenewOff : MySubscriptionsIntent
    data object DismissAutoRenewOffDialog : MySubscriptionsIntent
    // 크리에이터 프로필로 이동
    data class NavigateToCreatorProfile(val creatorId: Int) : MySubscriptionsIntent
    data object DismissToast : MySubscriptionsIntent
    data object SnackbarDismissed : MySubscriptionsIntent
}

sealed interface MySubscriptionsSideEffect {
    data object NavigateBack : MySubscriptionsSideEffect
    data class NavigateToCreatorProfile(val creatorId: Int) : MySubscriptionsSideEffect
}
