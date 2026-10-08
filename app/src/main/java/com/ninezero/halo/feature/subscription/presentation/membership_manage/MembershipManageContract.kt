package com.ninezero.halo.feature.subscription.presentation.membership_manage

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier

data class MembershipManageState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val plans: List<SubscriptionPlan> = emptyList(),
    val totalSubscribers: Int = 0,
    val nextPage: Int = 1,  // 다음에 받을 페이지
    val hasMore: Boolean = true,
    // 옵션 시트
    val optionSheetPlan: SubscriptionPlan? = null,
    // 시트 관련
    val showPlanBottomSheet: Boolean = false,
    val editingPlan: SubscriptionPlan? = null,  // null이면 새로 생성, 있으면 수정
    val isSaving: Boolean = false,
    // 삭제 다이얼로그
    val showDeleteDialog: Boolean = false,
    val deletingPlan: SubscriptionPlan? = null,
    val error: String? = null,
    val toastMessage: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface MembershipManageIntent {
    data object RefreshPlans : MembershipManageIntent
    data object LoadMorePlans : MembershipManageIntent
    data object NavigateBack : MembershipManageIntent
    // 옵션 시트
    data class ShowPlanOptions(val plan: SubscriptionPlan) : MembershipManageIntent
    data object DismissPlanOptions : MembershipManageIntent
    // 플랜 생성/수정 시트
    data object ShowCreatePlanSheet : MembershipManageIntent
    data class ShowEditPlanSheet(val plan: SubscriptionPlan) : MembershipManageIntent
    data object DismissPlanSheet : MembershipManageIntent
    data class SavePlan(
        val name: String,
        val tier: SubscriptionPlanTier,
        val description: String,
        val price: String,
        val benefits: List<String>
    ) : MembershipManageIntent
    // 플랜 삭제
    data class ShowDeleteDialog(val plan: SubscriptionPlan) : MembershipManageIntent
    data object DismissDeleteDialog : MembershipManageIntent
    data object ConfirmDeletePlan : MembershipManageIntent
    // 플랜 활성/비활성 토글
    data class TogglePlanActive(val plan: SubscriptionPlan) : MembershipManageIntent
    data object DismissToast : MembershipManageIntent
    data object SnackbarDismissed : MembershipManageIntent
}

sealed interface MembershipManageSideEffect {
    data object NavigateBack : MembershipManageSideEffect
}
