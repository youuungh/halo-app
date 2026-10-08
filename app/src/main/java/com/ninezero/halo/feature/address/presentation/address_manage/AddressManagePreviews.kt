package com.ninezero.halo.feature.address.presentation.address_manage

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.address.domain.model.Address

internal fun previewAddressManageState(): AddressManageState {
    return AddressManageState(isLoading = false, addresses = previewAddresses)
}

internal fun previewAddressManageDeleteDialogState(): AddressManageState {
    return AddressManageState(
        isLoading = false,
        addresses = previewAddresses,
        showDeleteDialog = true
    )
}

internal fun previewAddressManageEmptyState(): AddressManageState {
    return AddressManageState(isLoading = false, addresses = emptyList())
}

internal fun previewAddressManageLoadingState(): AddressManageState {
    return AddressManageState(isLoading = true)
}

internal fun previewAddressManageErrorState(): AddressManageState {
    return AddressManageState(
        isLoading = false,
        addresses = emptyList(),
        error = "배송지를 불러올 수 없습니다"
    )
}

private val previewAddresses = listOf(
    Address(
        id = 1,
        recipientName = "홍길동",
        recipientPhone = "010-1234-5678",
        zipCode = "06123",
        address = "서울특별시 강남구 테헤란로 123",
        addressDetail = "101동 202호",
        memo = "부재시 문앞에 놓아주세요",
        isDefault = true
    ),
    Address(
        id = 2,
        recipientName = "김철수",
        recipientPhone = "010-9876-5432",
        zipCode = "04123",
        address = "서울특별시 마포구 월드컵북로 396",
        addressDetail = "상암동 누리꿈스퀘어",
        memo = null
    )
)

@HaloLightDarkPreview
@Composable
private fun AddressManageScreenPreview() {
    HaloScreenPreview {
        AddressManageScreenContent(
            state = previewAddressManageState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun AddressManageScreenDeleteDialogPreview() {
    HaloScreenPreview {
        AddressManageScreenContent(
            state = previewAddressManageDeleteDialogState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun AddressManageScreenLoadingPreview() {
    HaloScreenPreview {
        AddressManageScreenContent(
            state = previewAddressManageLoadingState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun AddressManageScreenEmptyPreview() {
    HaloScreenPreview {
        AddressManageScreenContent(
            state = previewAddressManageEmptyState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun AddressManageScreenErrorPreview() {
    HaloScreenPreview {
        AddressManageScreenContent(
            state = previewAddressManageErrorState(),
            onIntent = {}
        )
    }
}
