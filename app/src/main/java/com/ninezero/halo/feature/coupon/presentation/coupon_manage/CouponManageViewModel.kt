package com.ninezero.halo.feature.coupon.presentation.coupon_manage

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.toEpochMillis
import com.ninezero.halo.core.util.toLocalDateTime
import com.ninezero.halo.feature.coupon.domain.model.CouponStatus
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.CreateCouponRequest
import com.ninezero.halo.feature.coupon.domain.model.CreatorCoupon
import com.ninezero.halo.feature.coupon.domain.model.UpdateCouponRequest
import com.ninezero.halo.feature.coupon.domain.repository.CouponRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CouponManageViewModel @Inject constructor(
    private val couponRepository: CouponRepository
) : ViewModel(), OrbitContainerHost<CouponManageState, CouponManageState, CouponManageSideEffect> {

    override val container: OrbitContainer<CouponManageState, CouponManageState, CouponManageSideEffect> =
        orbitContainer(CouponManageState())

    init {
        loadCoupons()
    }

    fun onIntent(intent: CouponManageIntent) {
        when (intent) {
            is CouponManageIntent.LoadCoupons -> loadCoupons()
            is CouponManageIntent.RefreshCoupons -> refreshCoupons()
            is CouponManageIntent.LoadMoreCoupons -> loadMoreCoupons()
            is CouponManageIntent.NavigateBack -> navigateBack()
            is CouponManageIntent.ShowCouponOptions -> showCouponOptions(intent.coupon)
            is CouponManageIntent.DismissCouponOptions -> dismissCouponOptions()
            is CouponManageIntent.ShowCreateCouponSheet -> showCreateCouponSheet()
            is CouponManageIntent.ShowEditCouponSheet -> showEditCouponSheet(intent.coupon)
            is CouponManageIntent.DismissCouponSheet -> dismissCouponSheet()
            is CouponManageIntent.UpdateCode -> updateCode(intent.code)
            is CouponManageIntent.UpdateName -> updateName(intent.name)
            is CouponManageIntent.UpdateDescription -> updateDescription(intent.description)
            is CouponManageIntent.UpdateType -> updateType(intent.type)
            is CouponManageIntent.UpdateDiscountValue -> updateDiscountValue(intent.value)
            is CouponManageIntent.UpdateMinOrderAmount -> updateMinOrderAmount(intent.value)
            is CouponManageIntent.UpdateMaxDiscountAmount -> updateMaxDiscountAmount(intent.value)
            is CouponManageIntent.UpdateTotalQuantity -> updateTotalQuantity(intent.value)
            is CouponManageIntent.UpdateMaxUseCount -> updateMaxUseCount(intent.value)
            is CouponManageIntent.UpdateStartDate -> updateStartDate(intent.date)
            is CouponManageIntent.UpdateEndDate -> updateEndDate(intent.date)
            is CouponManageIntent.SaveCoupon -> saveCoupon()
            is CouponManageIntent.ToggleCouponActive -> toggleCouponActive(intent.coupon)
            is CouponManageIntent.ShowDeleteDialog -> showDeleteDialog(intent.coupon)
            is CouponManageIntent.DismissDeleteDialog -> dismissDeleteDialog()
            is CouponManageIntent.ConfirmDelete -> confirmDelete()
            is CouponManageIntent.DismissToast -> dismissToast()
            is CouponManageIntent.DismissSaveError -> dismissSaveError()
            is CouponManageIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadCoupons() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        couponRepository.getCreatedCoupons(page = 1)
            .onSuccess { result ->
                reduce {
                    state.copy(
                        isLoading = false,
                        coupons = result.coupons,
                        page = result.page,
                        hasMore = result.hasMore
                    )
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "쿠폰을 불러오는데 실패했습니다"
                    )
                }
            }
    }

    private fun refreshCoupons() = intent {
        reduce { state.copy(isRefreshing = true) }
        couponRepository.getCreatedCoupons(page = 1)
            .onSuccess { result ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        coupons = result.coupons,
                        page = result.page,
                        hasMore = result.hasMore,
                        error = null
                    )
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

    private fun loadMoreCoupons() = intent {
        if (state.isLoading || !state.hasMore) return@intent

        reduce { state.copy(isLoading = true) }

        val nextPage = state.page + 1
        couponRepository.getCreatedCoupons(page = nextPage)
            .onSuccess { result ->
                reduce {
                    state.copy(
                        isLoading = false,
                        coupons = (state.coupons + result.coupons).distinctBy { it.id },
                        page = result.page,
                        hasMore = result.hasMore
                    )
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isLoading = false,
                        snackbarState = SnackbarState(e.message ?: "더 불러오는데 실패했습니다")
                    )
                }
            }
    }

    private fun navigateBack() = intent {
        postSideEffect(CouponManageSideEffect.NavigateBack)
    }

    private fun showCreateCouponSheet() = intent {
        reduce {
            state.copy(
                showCouponBottomSheet = true,
                editingCoupon = null,
                couponFormState = CouponFormState(),
                saveError = null
            )
        }
    }

    private fun showCouponOptions(coupon: CreatorCoupon) = intent {
        reduce { state.copy(optionSheetCoupon = coupon) }
    }

    private fun dismissCouponOptions() = intent {
        reduce { state.copy(optionSheetCoupon = null) }
    }

    private fun showEditCouponSheet(coupon: CreatorCoupon) = intent {
        reduce {
            state.copy(
                showCouponBottomSheet = true,
                editingCoupon = coupon,
                couponFormState = CouponFormState(
                    code = coupon.code,
                    name = coupon.name,
                    description = coupon.description ?: "",
                    type = coupon.type,
                    discountTarget = coupon.discountTarget,
                    discountValue = coupon.discountValue,
                    minOrderAmount = coupon.minOrderAmount,
                    maxDiscountAmount = coupon.maxDiscountAmount ?: "",
                    totalQuantity = coupon.totalQuantity.toString(),
                    maxUseCount = coupon.maxUseCount.toString(),
                    startDate = coupon.startDate.toLocalDateTime(),
                    endDate = coupon.endDate.toLocalDateTime()
                ),
                saveError = null
            )
        }
    }

    private fun dismissCouponSheet() = intent {
        reduce {
            state.copy(
                showCouponBottomSheet = false,
                editingCoupon = null,
                couponFormState = CouponFormState(),
                saveError = null
            )
        }
    }

    private fun updateCode(code: String) = blockingIntent {
        reduce { state.copy(couponFormState = state.couponFormState.copy(code = code)) }
    }

    private fun updateName(name: String) = blockingIntent {
        reduce { state.copy(couponFormState = state.couponFormState.copy(name = name)) }
    }

    private fun updateDescription(description: String) = blockingIntent {
        reduce { state.copy(couponFormState = state.couponFormState.copy(description = description)) }
    }

    private fun updateType(type: CouponType) = intent {
        reduce { state.copy(couponFormState = state.couponFormState.copy(type = type)) }
    }

    private fun updateDiscountValue(value: String) = blockingIntent {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            reduce { state.copy(couponFormState = state.couponFormState.copy(discountValue = value)) }
        }
    }

    private fun updateMinOrderAmount(value: String) = blockingIntent {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            reduce { state.copy(couponFormState = state.couponFormState.copy(minOrderAmount = value)) }
        }
    }

    private fun updateMaxDiscountAmount(value: String) = blockingIntent {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            reduce { state.copy(couponFormState = state.couponFormState.copy(maxDiscountAmount = value)) }
        }
    }

    private fun updateTotalQuantity(value: String) = blockingIntent {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            reduce { state.copy(couponFormState = state.couponFormState.copy(totalQuantity = value)) }
        }
    }

    private fun updateMaxUseCount(value: String) = blockingIntent {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            reduce { state.copy(couponFormState = state.couponFormState.copy(maxUseCount = value)) }
        }
    }

    private fun updateStartDate(date: LocalDateTime) = intent {
        reduce { state.copy(couponFormState = state.couponFormState.copy(startDate = date)) }
    }

    private fun updateEndDate(date: LocalDateTime) = intent {
        reduce { state.copy(couponFormState = state.couponFormState.copy(endDate = date)) }
    }

    private fun saveCoupon() = intent {
        val form = state.couponFormState
        if (!form.isValid) {
            reduce { state.copy(saveError = "필수 항목을 입력해주세요") }
            return@intent
        }

        reduce { state.copy(isSaving = true, saveError = null) }

        val editingCoupon = state.editingCoupon
        if (editingCoupon != null) {
            val request = UpdateCouponRequest(
                name = form.name,
                // 빈 칸은 ""로 보내 서버에서 지움
                description = form.description.trim(),
                minOrderAmount = form.minOrderAmount.trim(),
                maxDiscountAmount = form.maxDiscountAmount.trim(),
                totalQuantity = form.totalQuantity.toIntOrNull(),
                maxUseCount = form.maxUseCount.toIntOrNull(),
                status = null,
                startDate = form.startDate.toEpochMillis(),
                endDate = form.endDate.toEpochMillis()
            )
            couponRepository.updateCoupon(editingCoupon.id, request)
                .onSuccess { updatedCoupon ->
                    reduce {
                        state.copy(
                            isSaving = false,
                            showCouponBottomSheet = false,
                            editingCoupon = null,
                            couponFormState = CouponFormState(),
                            coupons = state.coupons.map { if (it.id == updatedCoupon.id) updatedCoupon else it },
                            toastMessage = "쿠폰이 수정되었습니다"
                        )
                    }
                }
                .onFailure { e ->
                    reduce {
                        state.copy(
                            isSaving = false,
                            saveError = e.message ?: "쿠폰 수정에 실패했습니다"
                        )
                    }
                }
        } else {
            val request = CreateCouponRequest(
                code = form.code.ifBlank { null },
                name = form.name,
                description = form.description.ifBlank { null },
                type = form.type,
                discountTarget = form.discountTarget,
                discountValue = form.discountValue,
                minOrderAmount = form.minOrderAmount.ifBlank { null },
                maxDiscountAmount = form.maxDiscountAmount.ifBlank { null },
                totalQuantity = form.totalQuantity.toIntOrNull() ?: 100,
                maxUseCount = form.maxUseCount.toIntOrNull() ?: 1,
                targetIds = null,
                startDate = form.startDate.toEpochMillis(),
                endDate = form.endDate.toEpochMillis()
            )
            couponRepository.createCoupon(request)
                .onSuccess { newCoupon ->
                    reduce {
                        state.copy(
                            isSaving = false,
                            showCouponBottomSheet = false,
                            couponFormState = CouponFormState(),
                            coupons = listOf(newCoupon) + state.coupons,
                            toastMessage = "쿠폰이 생성되었습니다"
                        )
                    }
                }
                .onFailure { e ->
                    reduce {
                        state.copy(
                            isSaving = false,
                            saveError = e.message ?: "쿠폰 생성에 실패했습니다"
                        )
                    }
                }
        }
    }

    private fun toggleCouponActive(coupon: CreatorCoupon) = intent {
        val newStatus = if (coupon.status == CouponStatus.ACTIVE) CouponStatus.DISABLED else CouponStatus.ACTIVE
        val request = UpdateCouponRequest(
            name = null,
            description = null,
            minOrderAmount = null,
            maxDiscountAmount = null,
            totalQuantity = null,
            maxUseCount = null,
            status = newStatus,
            startDate = null,
            endDate = null
        )
        couponRepository.updateCoupon(coupon.id, request)
            .onSuccess { updatedCoupon ->
                reduce {
                    state.copy(
                        coupons = state.coupons.map { if (it.id == updatedCoupon.id) updatedCoupon else it },
                        toastMessage = if (updatedCoupon.isActive) "쿠폰이 활성화되었습니다" else "쿠폰이 비활성화되었습니다"
                    )
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(snackbarState = SnackbarState(e.message ?: "쿠폰 상태 변경에 실패했습니다"))
                }
            }
    }

    private fun showDeleteDialog(coupon: CreatorCoupon) = intent {
        reduce { state.copy(showDeleteDialog = true, deletingCoupon = coupon) }
    }

    private fun dismissDeleteDialog() = intent {
        reduce { state.copy(showDeleteDialog = false, deletingCoupon = null) }
    }

    private fun confirmDelete() = intent {
        val coupon = state.deletingCoupon ?: return@intent

        // 요청 전에 닫음, 연타로 삭제 요청이 두 번 가지 않게
        reduce { state.copy(showDeleteDialog = false, deletingCoupon = null) }
        couponRepository.deleteCoupon(coupon.id)
            .onSuccess {
                reduce {
                    state.copy(
                        coupons = state.coupons.filter { it.id != coupon.id },
                        toastMessage = "쿠폰이 삭제되었습니다"
                    )
                }
            }
            .onFailure { e ->
                reduce { state.copy(snackbarState = SnackbarState(e.message ?: "쿠폰 삭제에 실패했습니다")) }
            }
    }

    private fun dismissToast() = intent {
        reduce { state.copy(toastMessage = null) }
    }

    private fun dismissSaveError() = intent {
        reduce { state.copy(saveError = null) }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
