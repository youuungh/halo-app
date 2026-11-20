package com.ninezero.halo.feature.subscription.presentation.membership_manage

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
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
import com.ninezero.halo.core.ui.components.list.PagedListScreenShell
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.subscription.presentation.components.SubscriptionPlanBottomSheet
import com.ninezero.halo.feature.subscription.presentation.membership_manage.components.PlanItem
import com.ninezero.halo.feature.subscription.presentation.membership_manage.components.SummarySection
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
    PagedListScreenShell(
        title = "멤버십 관리",
        isRefreshing = state.isRefreshing,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        isEmpty = state.plans.isEmpty(),
        error = state.error,
        errorTitle = "멤버십 목록을 불러오지 못했습니다",
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
        // 첫 로딩 · 빈 목록은 셸이 보여 줌
        if (state.plans.isEmpty()) return@PagedListScreenShell
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
        ) {
            // 요약 섹션
            item {
                SummarySection(
                    totalPlans = state.plans.size,
                    totalSubscribers = state.totalSubscribers
                )
            }

            items(
                items = state.plans,
                key = { it.id }
            ) { plan ->
                PlanItem(
                    plan = plan,
                    onMoreOptions = { onIntent(MembershipManageIntent.ShowPlanOptions(plan)) },
                    modifier = Modifier
                        // 위치 이동 애니메이션은 끔, 따로 출렁임 방지
                        .animateItem(placementSpec = null)
                        .padding(bottom = 10.dp)
                )
            }

            if (state.isLoading) {
                item(key = "loading_more") {
                    ListLoadMoreIndicator()
                }
            }
        }
    }

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
            saveError = state.saveError,
            onSaveErrorDismiss = { onIntent(MembershipManageIntent.DismissSaveError) }
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
