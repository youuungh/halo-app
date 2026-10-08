package com.ninezero.halo.feature.address.presentation.address_manage

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.address.domain.model.Address

data class AddressManageState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val addresses: List<Address> = emptyList(),
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val deleteTargetId: Int? = null,
    // 주소 변경 시트
    val showPostcodeSheet: Boolean = false,
    val toastMessage: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface AddressManageIntent {
    data object LoadAddresses : AddressManageIntent
    data object RefreshAddresses : AddressManageIntent
    data object NavigateBack : AddressManageIntent
    data object ShowPostcodeSheet : AddressManageIntent
    data object DismissPostcodeSheet : AddressManageIntent
    data class AddressSelected(val zipCode: String, val address: String) : AddressManageIntent
    data class NavigateToEdit(val addressId: Int) : AddressManageIntent
    data class ShowDeleteDialog(val addressId: Int) : AddressManageIntent
    data object DismissDeleteDialog : AddressManageIntent
    data object ConfirmDelete : AddressManageIntent
    data class SetDefault(val addressId: Int) : AddressManageIntent
    data object DismissToast : AddressManageIntent
    data object SnackbarDismissed : AddressManageIntent
}

sealed interface AddressManageSideEffect {
    data object NavigateBack : AddressManageSideEffect
    data class NavigateToCreate(val zipCode: String, val address: String) : AddressManageSideEffect
    data class NavigateToEdit(val addressId: Int) : AddressManageSideEffect
}
