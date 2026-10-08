package com.ninezero.halo.feature.address.presentation.address_detail

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview

internal fun previewAddressDetailCreateState(): AddressDetailState {
    return AddressDetailState(
        zipCode = "06123",
        address = "서울특별시 강남구 테헤란로 123 (역삼동)"
    )
}

internal fun previewAddressDetailErrorState(): AddressDetailState {
    return AddressDetailState(
        zipCode = "06123",
        address = "서울특별시 강남구 테헤란로 123 (역삼동)",
        addressDetail = "",
        recipientName = "",
        recipientPhone = "0101234",
        recipientNameError = "받는 이를 입력해주세요",
        recipientPhoneError = "올바른 연락처를 입력해주세요"
    )
}

internal fun previewAddressDetailEditState(): AddressDetailState {
    return AddressDetailState(
        isEditMode = true,
        addressId = 1,
        zipCode = "06123",
        address = "서울특별시 강남구 테헤란로 123 (역삼동)",
        addressDetail = "101동 202호",
        recipientName = "홍길동",
        recipientPhone = "010-1234-5678",
        memo = "부재시 문앞에 놓아주세요"
    )
}

@HaloLightDarkPreview
@Composable
private fun AddressDetailScreenCreatePreview() {
    HaloScreenPreview {
        AddressDetailScreenContent(
            state = previewAddressDetailCreateState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun AddressDetailScreenErrorPreview() {
    HaloScreenPreview {
        AddressDetailScreenContent(
            state = previewAddressDetailErrorState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun AddressDetailScreenEditPreview() {
    HaloScreenPreview {
        AddressDetailScreenContent(
            state = previewAddressDetailEditState(),
            onIntent = {}
        )
    }
}
