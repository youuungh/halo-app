package com.ninezero.halo.feature.address.presentation.address_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.address.domain.repository.AddressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class AddressDetailViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), OrbitContainerHost<AddressDetailState, AddressDetailState, AddressDetailSideEffect> {
    private val addressId: Int? = savedStateHandle.get<Int>("addressId")?.takeIf { it > 0 }
    private val zipCode: String = savedStateHandle.get<String>("zipCode") ?: ""
    private val address: String = savedStateHandle.get<String>("address") ?: ""

    override val container: OrbitContainer<AddressDetailState, AddressDetailState, AddressDetailSideEffect> =
        orbitContainer(
            AddressDetailState(
                isEditMode = addressId != null,
                addressId = addressId,
                zipCode = zipCode,
                address = address,
                // 편집 모드는 서버에서 값을 받아 채우므로 로딩으로 시작
                isLoading = addressId != null
            )
        )

    init {
        if (addressId != null) {
            loadAddress(addressId)
        }
    }

    fun onIntent(intent: AddressDetailIntent) {
        when (intent) {
            is AddressDetailIntent.UpdateRecipientName -> updateRecipientName(intent.name)
            is AddressDetailIntent.UpdateRecipientPhone -> updateRecipientPhone(intent.phone)
            is AddressDetailIntent.UpdateAddressDetail -> updateAddressDetail(intent.detail)
            is AddressDetailIntent.UpdateMemo -> updateMemo(intent.memo)
            is AddressDetailIntent.Save -> save()
            is AddressDetailIntent.ShowPostcodeSheet -> showPostcodeSheet()
            is AddressDetailIntent.DismissPostcodeSheet -> dismissPostcodeSheet()
            is AddressDetailIntent.ShowMemoSheet -> showMemoSheet()
            is AddressDetailIntent.DismissMemoSheet -> dismissMemoSheet()
            is AddressDetailIntent.UpdateAddress -> updateAddress(intent.zipCode, intent.address)
            is AddressDetailIntent.NavigateBack -> navigateBack()
            is AddressDetailIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadAddress(id: Int) = intent {
        addressRepository.getAddress(id)
            .onSuccess { addr ->
                reduce {
                    state.copy(
                        isLoading = false,
                        zipCode = addr.zipCode,
                        address = addr.address,
                        addressDetail = addr.addressDetail ?: "",
                        recipientName = addr.recipientName,
                        // 서버는 하이픈 포함, 입력창엔 숫자만
                        recipientPhone = addr.recipientPhone.filter { it.isDigit() },
                        memo = addr.memo ?: "",
                        isDefault = addr.isDefault
                    )
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isLoading = false,
                        snackbarState = SnackbarState(e.message ?: "배송지 정보를 불러올 수 없습니다")
                    )
                }
            }
    }

    private fun updateRecipientName(name: String) = blockingIntent {
        reduce { state.copy(recipientName = name, recipientNameError = null) }
    }

    private fun updateRecipientPhone(phone: String) = blockingIntent {
        val digits = phone.filter { it.isDigit() }
        reduce { state.copy(recipientPhone = digits, recipientPhoneError = null) }
    }

    private fun updateAddressDetail(detail: String) = blockingIntent {
        reduce { state.copy(addressDetail = detail) }
    }

    private fun updateMemo(memo: String) = blockingIntent {
        reduce { state.copy(memo = memo) }
    }

    private fun save() = intent {
        var hasError = false
        var nameError: String? = null
        var phoneError: String? = null

        if (state.recipientName.isBlank()) {
            nameError = "수취인을 입력해주세요"
            hasError = true
        }
        if (state.recipientPhone.isBlank()) {
            phoneError = "연락처를 입력해주세요"
            hasError = true
        } else if (!state.recipientPhone.isValidPhoneLength()) {
            phoneError = "올바른 연락처를 입력해주세요"
            hasError = true
        }

        if (hasError) {
            reduce {
                state.copy(
                    recipientNameError = nameError,
                    recipientPhoneError = phoneError
                )
            }
            return@intent
        }

        reduce { state.copy(isSaving = true) }

        // 서버 형식에 맞춰 하이픈 변환
        val formattedPhone = formatPhoneWithHyphen(state.recipientPhone)
        val currentAddressId = state.addressId
        val result = if (state.isEditMode && currentAddressId != null) {
            addressRepository.updateAddress(
                id = currentAddressId,
                recipientName = state.recipientName,
                recipientPhone = formattedPhone,
                zipCode = state.zipCode,
                address = state.address,
                addressDetail = state.addressDetail.ifBlank { null },
                memo = state.memo.ifBlank { null },
                // 미전송 시 기본 배송지 해제됨
                isDefault = state.isDefault
            )
        } else {
            addressRepository.createAddress(
                recipientName = state.recipientName,
                recipientPhone = formattedPhone,
                zipCode = state.zipCode,
                address = state.address,
                addressDetail = state.addressDetail.ifBlank { null },
                memo = state.memo.ifBlank { null }
            )
        }

        result
            .onSuccess {
                reduce { state.copy(isSaving = false) }
                postSideEffect(AddressDetailSideEffect.SaveSuccess)
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isSaving = false,
                        snackbarState = SnackbarState(
                            e.message
                                ?: if (state.isEditMode) "배송지 수정에 실패했습니다" else "배송지 등록에 실패했습니다"
                        )
                    )
                }
            }
    }

    private fun showPostcodeSheet() = intent {
        reduce { state.copy(showPostcodeSheet = true) }
    }

    private fun dismissPostcodeSheet() = intent {
        reduce { state.copy(showPostcodeSheet = false) }
    }

    private fun showMemoSheet() = intent {
        reduce { state.copy(showMemoSheet = true) }
    }

    private fun dismissMemoSheet() = intent {
        reduce { state.copy(showMemoSheet = false) }
    }

    private fun updateAddress(zipCode: String, address: String) = intent {
        reduce { state.copy(zipCode = zipCode, address = address, showPostcodeSheet = false) }
    }

    private fun navigateBack() = intent {
        postSideEffect(AddressDetailSideEffect.NavigateBack)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}

private fun String.isValidPhoneLength(): Boolean {  // 서버 전화번호 형식과 같은 기준
    val d = filter { it.isDigit() }
    return if (d.startsWith("02")) d.length in 9..10 else d.length in 10..11
}

private fun formatPhoneWithHyphen(digits: String): String {  // 02는 2자리 지역번호
    val d = digits.filter { it.isDigit() }
    return if (d.startsWith("02")) {
        when (d.length) {
            in 0..2 -> d
            in 3..5 -> "${d.take(2)}-${d.drop(2)}"
            in 6..9 -> "${d.take(2)}-${d.substring(2, d.length - 4)}-${d.takeLast(4)}"
            else -> "${d.take(2)}-${d.substring(2, 6)}-${d.takeLast(4)}"
        }
    } else {
        when (d.length) {
            in 0..3 -> d
            in 4..7 -> "${d.take(3)}-${d.drop(3)}"
            in 8..10 -> "${d.take(3)}-${d.substring(3, d.length - 4)}-${d.takeLast(4)}"
            else -> "${d.take(3)}-${d.substring(3, 7)}-${d.takeLast(4)}"
        }
    }
}
