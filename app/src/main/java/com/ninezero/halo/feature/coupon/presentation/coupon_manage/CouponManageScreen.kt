package com.ninezero.halo.feature.coupon.presentation.coupon_manage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
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
import com.ninezero.halo.core.ui.components.layout.PagedListScreenShell
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.coupon.domain.model.CouponStatus
import com.ninezero.halo.feature.coupon.presentation.coupon_manage.components.CouponFormBottomSheet
import com.ninezero.halo.feature.coupon.presentation.coupon_manage.components.CreatorCouponCard
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CouponManageScreen(
    onNavigateBack: () -> Unit,
    viewModel: CouponManageViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CouponManageSideEffect.NavigateBack -> onNavigateBack()
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(CouponManageIntent.SnackbarDismissed) }
    )

    CouponManageScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )
}

@Composable
internal fun CouponManageScreenContent(
    state: CouponManageState,
    onIntent: (CouponManageIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    PagedListScreenShell(
        title = "쿠폰 관리",
        isRefreshing = state.isRefreshing,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        isEmpty = state.coupons.isEmpty(),
        error = state.error,
        emptyTitle = "발행한 쿠폰이 없어요",
        emptyDescription = "새 쿠폰을 만들어보세요",
        onNavigateBack = { onIntent(CouponManageIntent.NavigateBack) },
        onRefresh = { onIntent(CouponManageIntent.RefreshCoupons) },
        onRetry = { onIntent(CouponManageIntent.LoadCoupons) },
        onLoadMore = { onIntent(CouponManageIntent.LoadMoreCoupons) },
        snackbarHostState = snackbarHostState,
        headerActions = { hazeState, backgroundColor ->
            GlassIconButton(
                onClick = { onIntent(CouponManageIntent.ShowCreateCouponSheet) },
                icon = painterResource(R.drawable.ic_plus),
                contentDescription = "새 쿠폰 만들기",
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    ) { listState, headerHeight ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
        ) {
            items(
                items = state.coupons,
                key = { it.id }
            ) { coupon ->
                Column(modifier = Modifier.animateItem()) {
                    CreatorCouponCard(
                        coupon = coupon,
                        onMoreOptions = { onIntent(CouponManageIntent.ShowCouponOptions(coupon)) }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }

            if (state.isLoading && state.coupons.isNotEmpty()) {
                item(key = "loading_more") {
                    ListLoadMoreIndicator()
                }
            }
        }
    }

    CenterToast(
        visible = state.toastMessage != null,
        message = state.toastMessage ?: "",
        onDismiss = { onIntent(CouponManageIntent.DismissToast) }
    )

    state.optionSheetCoupon?.let { coupon ->
        EditDeleteOptionBottomSheet(
            isActive = coupon.status == CouponStatus.ACTIVE,
            onToggleActive = if (coupon.isExpired) null else {
                { onIntent(CouponManageIntent.ToggleCouponActive(coupon)) }
            },
            onEdit = { onIntent(CouponManageIntent.ShowEditCouponSheet(coupon)) },
            onDelete = { onIntent(CouponManageIntent.ShowDeleteDialog(coupon)) },
            onDismiss = { onIntent(CouponManageIntent.DismissCouponOptions) }
        )
    }

    if (state.showCouponBottomSheet) {
        CouponFormBottomSheet(
            isEditing = state.editingCoupon != null,
            formState = state.couponFormState,
            isSaving = state.isSaving,
            onDismiss = { onIntent(CouponManageIntent.DismissCouponSheet) },
            onCodeChange = { onIntent(CouponManageIntent.UpdateCode(it)) },
            onNameChange = { onIntent(CouponManageIntent.UpdateName(it)) },
            onDescriptionChange = { onIntent(CouponManageIntent.UpdateDescription(it)) },
            onTypeChange = { onIntent(CouponManageIntent.UpdateType(it)) },
            onDiscountValueChange = { onIntent(CouponManageIntent.UpdateDiscountValue(it)) },
            onMinOrderAmountChange = { onIntent(CouponManageIntent.UpdateMinOrderAmount(it)) },
            onMaxDiscountAmountChange = { onIntent(CouponManageIntent.UpdateMaxDiscountAmount(it)) },
            onTotalQuantityChange = { onIntent(CouponManageIntent.UpdateTotalQuantity(it)) },
            onMaxUseCountChange = { onIntent(CouponManageIntent.UpdateMaxUseCount(it)) },
            onStartDateChange = { onIntent(CouponManageIntent.UpdateStartDate(it)) },
            onEndDateChange = { onIntent(CouponManageIntent.UpdateEndDate(it)) },
            onSave = { onIntent(CouponManageIntent.SaveCoupon) },
            saveError = state.saveError,
            onSaveErrorDismiss = { onIntent(CouponManageIntent.DismissSaveError) }
        )
    }

    if (state.showDeleteDialog && state.deletingCoupon != null) {
        ConfirmDialog(
            title = "쿠폰을 삭제하시겠어요?",
            confirmText = "삭제",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(CouponManageIntent.ConfirmDelete) },
            onDismiss = { onIntent(CouponManageIntent.DismissDeleteDialog) }
        )
    }
}
