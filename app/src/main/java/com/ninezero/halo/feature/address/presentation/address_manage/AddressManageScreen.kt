package com.ninezero.halo.feature.address.presentation.address_manage

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.PagedListScreenShell
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.address.presentation.address_manage.components.AddressItem
import com.ninezero.halo.feature.address.presentation.components.DaumPostcodeBottomSheet
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun AddressManageScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreate: (zipCode: String, address: String) -> Unit,
    onNavigateToEdit: (addressId: Int) -> Unit,
    viewModel: AddressManageViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var hasInitialResumeHandled by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasInitialResumeHandled) {
            viewModel.onIntent(AddressManageIntent.RefreshAddresses)
        } else {
            hasInitialResumeHandled = true
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AddressManageSideEffect.NavigateBack -> onNavigateBack()
            is AddressManageSideEffect.NavigateToCreate -> onNavigateToCreate(sideEffect.zipCode, sideEffect.address)
            is AddressManageSideEffect.NavigateToEdit -> onNavigateToEdit(sideEffect.addressId)
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(AddressManageIntent.SnackbarDismissed) }
    )

    AddressManageScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onAddressSelected = { zipCode, address ->
            viewModel.onIntent(AddressManageIntent.AddressSelected(zipCode, address))
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
internal fun AddressManageScreenContent(
    state: AddressManageState,
    onIntent: (AddressManageIntent) -> Unit,
    onAddressSelected: (String, String) -> Unit = { _, _ -> },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    PagedListScreenShell(
        title = "주소 관리",
        isRefreshing = state.isRefreshing,
        isLoading = state.isLoading,
        hasMore = false,
        isEmpty = state.addresses.isEmpty(),
        error = state.error,
        emptyTitle = "등록된 배송지가 없어요",
        emptyDescription = "배송지를 추가해보세요",
        onNavigateBack = { onIntent(AddressManageIntent.NavigateBack) },
        onRefresh = { onIntent(AddressManageIntent.RefreshAddresses) },
        onRetry = { onIntent(AddressManageIntent.LoadAddresses) },
        onLoadMore = {},
        snackbarHostState = snackbarHostState,
        headerActions = { hazeState, backgroundColor ->
            GlassIconButton(
                onClick = { onIntent(AddressManageIntent.ShowPostcodeSheet) },
                icon = painterResource(R.drawable.ic_plus),
                contentDescription = "배송지 추가",
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    ) { _, headerHeight ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
        ) {
            items(
                items = state.addresses,
                key = { it.id }
            ) { address ->
                Column(modifier = Modifier.animateItem()) {
                    AddressItem(
                        address = address,
                        onEdit = { onIntent(AddressManageIntent.NavigateToEdit(address.id)) },
                        onDelete = { onIntent(AddressManageIntent.ShowDeleteDialog(address.id)) },
                        onSetDefault = { onIntent(AddressManageIntent.SetDefault(address.id)) }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    CenterToast(
        visible = state.toastMessage != null,
        message = state.toastMessage ?: "",
        onDismiss = { onIntent(AddressManageIntent.DismissToast) }
    )

    if (state.showPostcodeSheet) {
        DaumPostcodeBottomSheet(
            onDismiss = { onIntent(AddressManageIntent.DismissPostcodeSheet) },
            onAddressSelected = onAddressSelected
        )
    }

    if (state.showDeleteDialog) {
        ConfirmDialog(
            title = "배송지를 삭제하시겠어요?",
            confirmText = "삭제",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(AddressManageIntent.ConfirmDelete) },
            onDismiss = { onIntent(AddressManageIntent.DismissDeleteDialog) }
        )
    }
}
