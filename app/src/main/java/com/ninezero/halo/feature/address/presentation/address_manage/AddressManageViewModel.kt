package com.ninezero.halo.feature.address.presentation.address_manage

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.address.domain.repository.AddressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class AddressManageViewModel @Inject constructor(
    private val addressRepository: AddressRepository
) : ViewModel(), OrbitContainerHost<AddressManageState, AddressManageState, AddressManageSideEffect> {

    override val container: OrbitContainer<AddressManageState, AddressManageState, AddressManageSideEffect> =
        orbitContainer(AddressManageState())

    init {
        loadAddresses()
    }

    fun onIntent(intent: AddressManageIntent) {
        when (intent) {
            is AddressManageIntent.LoadAddresses -> loadAddresses()
            is AddressManageIntent.RefreshAddresses -> refreshAddresses()
            is AddressManageIntent.NavigateBack -> navigateBack()
            is AddressManageIntent.ShowPostcodeSheet -> showPostcodeSheet()
            is AddressManageIntent.DismissPostcodeSheet -> dismissPostcodeSheet()
            is AddressManageIntent.AddressSelected -> onAddressSelected(intent.zipCode, intent.address)
            is AddressManageIntent.NavigateToEdit -> navigateToEdit(intent.addressId)
            is AddressManageIntent.ShowDeleteDialog -> showDeleteDialog(intent.addressId)
            is AddressManageIntent.DismissDeleteDialog -> dismissDeleteDialog()
            is AddressManageIntent.ConfirmDelete -> confirmDelete()
            is AddressManageIntent.SetDefault -> setDefault(intent.addressId)
            is AddressManageIntent.DismissToast -> dismissToast()
            is AddressManageIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun onAddressSelected(zipCode: String, address: String) = intent {
        reduce { state.copy(showPostcodeSheet = false) }
        postSideEffect(AddressManageSideEffect.NavigateToCreate(zipCode, address))
    }

    private fun loadAddresses() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        addressRepository.getAddresses()
            .onSuccess { addresses ->
                reduce { state.copy(isLoading = false, addresses = addresses) }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "배송지를 불러오는데 실패했습니다"
                    )
                }
            }
    }

    private fun refreshAddresses() = intent {
        reduce { state.copy(isRefreshing = true) }
        addressRepository.getAddresses()
            .onSuccess { addresses ->
                reduce {
                    state.copy(isRefreshing = false, addresses = addresses, error = null)
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        snackbarState = SnackbarState(e.message ?: "새로고침에 실패했습니다")
                    )
                }
            }
    }

    private fun navigateBack() = intent {
        postSideEffect(AddressManageSideEffect.NavigateBack)
    }

    private fun showPostcodeSheet() = intent {
        reduce { state.copy(showPostcodeSheet = true) }
    }

    private fun dismissPostcodeSheet() = intent {
        reduce { state.copy(showPostcodeSheet = false) }
    }

    private fun navigateToEdit(addressId: Int) = intent {
        postSideEffect(AddressManageSideEffect.NavigateToEdit(addressId))
    }

    private fun showDeleteDialog(addressId: Int) = intent {
        if (state.addresses.size <= 1) {
            reduce {
                state.copy(
                    snackbarState = SnackbarState("배송지는 하나 이상 등록되어 있어야 합니다")
                )
            }
            return@intent
        }
        reduce { state.copy(showDeleteDialog = true, deleteTargetId = addressId) }
    }

    private fun dismissDeleteDialog() = intent {
        reduce { state.copy(showDeleteDialog = false, deleteTargetId = null) }
    }

    private fun confirmDelete() = intent {
        val targetId = state.deleteTargetId ?: return@intent
        if (state.addresses.size <= 1) {
            reduce {
                state.copy(
                    showDeleteDialog = false,
                    deleteTargetId = null,
                    snackbarState = SnackbarState("배송지는 하나 이상 등록되어 있어야 합니다")
                )
            }
            return@intent
        }

        reduce { state.copy(showDeleteDialog = false) }
        addressRepository.deleteAddress(targetId)
            .onSuccess {
                reduce {
                    state.copy(
                        deleteTargetId = null,
                        addresses = state.addresses.filter { it.id != targetId },
                        snackbarState = SnackbarState("배송지가 삭제되었습니다")
                    )
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        deleteTargetId = null,
                        snackbarState = SnackbarState(e.message ?: "배송지 삭제에 실패했습니다")
                    )
                }
            }
    }

    private fun setDefault(addressId: Int) = intent {
        val target = state.addresses.find { it.id == addressId } ?: return@intent
        val newDefault = !target.isDefault

        addressRepository.updateAddress(
            id = addressId,
            recipientName = target.recipientName,
            recipientPhone = target.recipientPhone,
            zipCode = target.zipCode,
            address = target.address,
            addressDetail = target.addressDetail,
            memo = target.memo,
            isDefault = newDefault
        ).onSuccess {
            reduce {
                state.copy(
                    addresses = state.addresses.map { addr ->
                        when {
                            addr.id == addressId -> addr.copy(isDefault = newDefault)
                            newDefault && addr.isDefault -> addr.copy(isDefault = false)
                            else -> addr
                        }
                    },
                    toastMessage = if (newDefault) "기본 배송지로 설정되었습니다" else "기본 배송지가 해제되었습니다"
                )
            }
        }.onFailure { e ->
            reduce {
                state.copy(
                    snackbarState = SnackbarState(e.message ?: "기본 배송지 설정에 실패했습니다")
                )
            }
        }
    }

    private fun dismissToast() = intent {
        reduce { state.copy(toastMessage = null) }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
