package com.ninezero.halo.feature.address.presentation.address_detail

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

data class AddressDetailState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val addressId: Int? = null,
    val zipCode: String = "",
    val address: String = "",
    val addressDetail: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val memo: String = "",
    val isDefault: Boolean = false,
    val isSaving: Boolean = false,
    // 검증
    val recipientNameError: String? = null,
    val recipientPhoneError: String? = null,
    // 주소 변경 시트
    val showPostcodeSheet: Boolean = false,
    // 배송 메모 선택 시트
    val showMemoSheet: Boolean = false,
    val snackbarState: SnackbarState? = null
)

sealed interface AddressDetailIntent {
    data class UpdateRecipientName(val name: String) : AddressDetailIntent
    data class UpdateRecipientPhone(val phone: String) : AddressDetailIntent
    data class UpdateAddressDetail(val detail: String) : AddressDetailIntent
    data class UpdateMemo(val memo: String) : AddressDetailIntent
    data object Save : AddressDetailIntent
    data object ShowPostcodeSheet : AddressDetailIntent
    data object DismissPostcodeSheet : AddressDetailIntent
    data object ShowMemoSheet : AddressDetailIntent
    data object DismissMemoSheet : AddressDetailIntent
    data class UpdateAddress(val zipCode: String, val address: String) : AddressDetailIntent
    data object NavigateBack : AddressDetailIntent
    data object SnackbarDismissed : AddressDetailIntent
}

sealed interface AddressDetailSideEffect {
    data object NavigateBack : AddressDetailSideEffect
    data object SaveSuccess : AddressDetailSideEffect
}
