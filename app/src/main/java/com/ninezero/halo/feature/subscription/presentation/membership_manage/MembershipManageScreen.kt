package com.ninezero.halo.feature.subscription.presentation.membership_manage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.EditDeleteOptionBottomSheet
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.PagedListScreenShell
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.subscription.presentation.components.SubscriptionPlanBottomSheet
import com.ninezero.halo.feature.subscription.presentation.membership_manage.components.PlanItem
import com.ninezero.halo.feature.subscription.presentation.membership_manage.components.PlanItemSkeleton
import com.ninezero.halo.feature.subscription.presentation.membership_manage.components.SummarySection
import com.ninezero.halo.feature.subscription.presentation.membership_manage.components.SummarySectionSkeleton
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MembershipManageScreen(
    onNavigateBack: () -> Unit,
    viewModel: MembershipManageViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MembershipManageSideEffect.NavigateBack -> onNavigateBack()
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(MembershipManageIntent.SnackbarDismissed) }
    )

    MembershipManageScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )
}

@Composable
internal fun MembershipManageScreenContent(
    state: MembershipManageState,
    onIntent: (MembershipManageIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val isInitialLoading = state.isLoading && state.plans.isEmpty()

    PagedListScreenShell(
        title = "멤버십 관리",
        isRefreshing = state.isRefreshing,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        isEmpty = state.plans.isEmpty(),
        error = state.error,
        emptyTitle = "아직 멤버십이 없어요",
        emptyDescription = "새 멤버십을 생성해보세요",
        onNavigateBack = { onIntent(MembershipManageIntent.NavigateBack) },
        onRefresh = { onIntent(MembershipManageIntent.RefreshPlans) },
        onRetry = { onIntent(MembershipManageIntent.RefreshPlans) },
        onLoadMore = { onIntent(MembershipManageIntent.LoadMorePlans) },
        snackbarHostState = snackbarHostState,
        headerActions = { hazeState, backgroundColor ->
            GlassIconButton(
                onClick = { onIntent(MembershipManageIntent.ShowCreatePlanSheet) },
                icon = painterResource(R.drawable.ic_plus),
                contentDescription = "새 멤버십 생성",
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    ) { listState, headerHeight ->
        LazyColumn(
            state = if (isInitialLoading) rememberLazyListState() else listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp),
            userScrollEnabled = !isInitialLoading
        ) {
            if (isInitialLoading) {
                // 커스텀 스켈레톤
                item { SummarySectionSkeleton() }
                items(5) { PlanItemSkeleton() }
            } else {
                // 요약 섹션
                item {
                    SummarySection(
                        totalPlans = state.plans.size,
                        totalSubscribers = state.totalSubscribers
                    )
                }

                // 빈 상태
                if (state.plans.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "아직 멤버십 플랜이 없습니다",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(
                        items = state.plans,
                        key = { it.id }
                    ) { plan ->
                        Column(modifier = Modifier.animateItem()) {
                            PlanItem(
                                plan = plan,
                                onMoreOptions = { onIntent(MembershipManageIntent.ShowPlanOptions(plan)) }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // 추가 로딩 스켈레톤
                    if (state.isLoading && state.plans.isNotEmpty()) {
                        items(3) { PlanItemSkeleton() }
                    }
                }
            }
        }
    }

    CenterToast(
        visible = state.toastMessage != null,
        message = state.toastMessage ?: "",
        onDismiss = { onIntent(MembershipManageIntent.DismissToast) }
    )

    state.optionSheetPlan?.let { plan ->
        EditDeleteOptionBottomSheet(
            isActive = plan.isActive,
            onToggleActive = { onIntent(MembershipManageIntent.TogglePlanActive(plan)) },
            onEdit = { onIntent(MembershipManageIntent.ShowEditPlanSheet(plan)) },
            onDelete = { onIntent(MembershipManageIntent.ShowDeleteDialog(plan)) },
            onDismiss = { onIntent(MembershipManageIntent.DismissPlanOptions) }
        )
    }

    // 플랜 생성/수정 BottomSheet
    if (state.showPlanBottomSheet) {
        SubscriptionPlanBottomSheet(
            plan = state.editingPlan,
            isSaving = state.isSaving,
            onDismiss = { onIntent(MembershipManageIntent.DismissPlanSheet) },
            onSave = { name, tier, description, price, benefits ->
                onIntent(MembershipManageIntent.SavePlan(name, tier, description, price, benefits))
            },
            toastMessage = state.toastMessage,
            onToastDismiss = { onIntent(MembershipManageIntent.DismissToast) }
        )
    }

    if (state.showDeleteDialog && state.deletingPlan != null) {
        ConfirmDialog(
            title = "플랜 삭제",
            message = "'${state.deletingPlan.name}' 플랜을 삭제하시겠어요?\n활성 구독자가 있으면 삭제할 수 없습니다.",
            confirmText = "삭제",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(MembershipManageIntent.ConfirmDeletePlan) },
            onDismiss = { onIntent(MembershipManageIntent.DismissDeleteDialog) }
        )
    }
}
