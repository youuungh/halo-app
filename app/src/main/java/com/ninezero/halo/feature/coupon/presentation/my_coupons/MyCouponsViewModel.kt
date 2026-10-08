package com.ninezero.halo.feature.coupon.presentation.my_coupons

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.feature.coupon.domain.repository.CouponRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class MyCouponsViewModel @Inject constructor(
    private val couponRepository: CouponRepository
) : ViewModel(), OrbitContainerHost<MyCouponsState, MyCouponsState, MyCouponsSideEffect> {

    override val container: OrbitContainer<MyCouponsState, MyCouponsState, MyCouponsSideEffect> =
        orbitContainer(MyCouponsState())

    init {
        loadCoupons()
    }

    fun onIntent(intent: MyCouponsIntent) {
        when (intent) {
            is MyCouponsIntent.LoadCoupons -> loadCoupons()
            is MyCouponsIntent.RefreshCoupons -> refreshCoupons()
            is MyCouponsIntent.LoadMoreCoupons -> loadMoreCoupons()
            is MyCouponsIntent.NavigateBack -> navigateBack()
            is MyCouponsIntent.FilterByStatus -> filterByStatus(intent.status)
            is MyCouponsIntent.UpdateCouponCode -> updateCouponCode(intent.code)
            is MyCouponsIntent.ClaimCoupon -> claimCoupon()
            is MyCouponsIntent.DismissToast -> dismissToast()
            is MyCouponsIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadCoupons() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        val status = state.selectedStatus

        couponRepository.getClaimedCoupons(
            status = status,
            page = 1,
            limit = 20
        ).onSuccess { result ->
            if (state.selectedStatus != status) return@intent
            reduce {
                state.copy(
                    isLoading = false,
                    coupons = result.coupons,
                    page = result.page,
                    hasMore = result.hasMore
                )
            }
        }.onFailure { error ->
            if (state.selectedStatus != status) return@intent
            reduce {
                state.copy(
                    isLoading = false,
                    error = error.message ?: "쿠폰을 불러올 수 없습니다"
                )
            }
        }
    }

    private fun refreshCoupons() = intent {
        reduce { state.copy(isRefreshing = true) }

        val status = state.selectedStatus
        couponRepository.getClaimedCoupons(
            status = status,
            page = 1,
            limit = 20
        ).onSuccess { result ->
            if (state.selectedStatus != status) {
                reduce { state.copy(isRefreshing = false) }
                return@intent
            }
            reduce {
                state.copy(
                    isRefreshing = false,
                    coupons = result.coupons,
                    page = result.page,
                    hasMore = result.hasMore,
                    error = null
                )
            }
        }.onFailure { error ->
            reduce { state.copy(isRefreshing = false, snackbarState = SnackbarState(error.message ?: "새로고침 실패")) }
        }
    }

    private fun loadMoreCoupons() = intent {
        if (!state.hasMore || state.isLoading) return@intent

        reduce { state.copy(isLoading = true) }

        val nextPage = state.page + 1
        val status = state.selectedStatus

        couponRepository.getClaimedCoupons(
            status = status,
            page = nextPage,
            limit = 20
        ).onSuccess { result ->
            if (state.selectedStatus != status) return@intent
            reduce {
                state.copy(
                    isLoading = false,
                    coupons = (state.coupons + result.coupons).distinctBy { it.id },
                    page = result.page,
                    hasMore = result.hasMore
                )
            }
        }.onFailure { error ->
            reduce { state.copy(isLoading = false, snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")) }
        }
    }

    private fun filterByStatus(status: UserCouponStatus?) = intent {
        if (state.selectedStatus == status) return@intent

        reduce {
            state.copy(
                selectedStatus = status,
                isLoading = true,
                coupons = emptyList(),
                page = 1,
                hasMore = true
            )
        }

        couponRepository.getClaimedCoupons(
            status = status,
            page = 1,
            limit = 20
        ).onSuccess { result ->
            if (state.selectedStatus != status) return@intent
            reduce {
                state.copy(
                    isLoading = false,
                    coupons = result.coupons,
                    page = result.page,
                    hasMore = result.hasMore
                )
            }
        }.onFailure { error ->
            if (state.selectedStatus != status) return@intent
            reduce { state.copy(isLoading = false, snackbarState = SnackbarState(error.message ?: "필터 적용 실패")) }
        }
    }

    private fun updateCouponCode(code: String) = blockingIntent {
        reduce { state.copy(couponCode = code) }
    }

    private fun claimCoupon() = intent {
        val code = state.couponCode.trim()
        if (code.isBlank()) return@intent

        reduce { state.copy(isClaiming = true) }

        couponRepository.claimCoupon(code)
            .onSuccess { coupon ->
                reduce {
                    state.copy(
                        isClaiming = false,
                        couponCode = "",
                        // 새 쿠폰은 사용 가능 상태라 전체·사용 가능 필터에만 추가
                        coupons = if (state.selectedStatus == null || state.selectedStatus == UserCouponStatus.AVAILABLE) {
                            listOf(coupon) + state.coupons
                        } else {
                            state.coupons
                        },
                        toastMessage = "쿠폰이 등록되었습니다"
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isClaiming = false,
                        snackbarState = SnackbarState(error.message ?: "쿠폰 등록 실패")
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

    private fun navigateBack() = intent {
        postSideEffect(MyCouponsSideEffect.NavigateBack)
    }
}
