package com.ninezero.halo.feature.subscription.presentation.membership_manage

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlan
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.subscription.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

private const val PAGE_SIZE = 20

@HiltViewModel
class MembershipManageViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel(), OrbitContainerHost<MembershipManageState, MembershipManageState, MembershipManageSideEffect> {

    override val container: OrbitContainer<MembershipManageState, MembershipManageState, MembershipManageSideEffect> =
        orbitContainer(MembershipManageState())

    init {
        loadPlans()
    }

    fun onIntent(intent: MembershipManageIntent) {
        when (intent) {
            is MembershipManageIntent.RefreshPlans -> refreshPlans()
            is MembershipManageIntent.LoadMorePlans -> loadMorePlans()
            is MembershipManageIntent.NavigateBack -> navigateBack()
            is MembershipManageIntent.ShowPlanOptions -> showPlanOptions(intent.plan)
            is MembershipManageIntent.DismissPlanOptions -> dismissPlanOptions()
            is MembershipManageIntent.ShowCreatePlanSheet -> showCreatePlanSheet()
            is MembershipManageIntent.ShowEditPlanSheet -> showEditPlanSheet(intent.plan)
            is MembershipManageIntent.DismissPlanSheet -> dismissPlanSheet()
            is MembershipManageIntent.SavePlan -> savePlan(
                intent.name,
                intent.tier,
                intent.description,
                intent.price,
                intent.benefits
            )

            is MembershipManageIntent.ShowDeleteDialog -> showDeleteDialog(intent.plan)
            is MembershipManageIntent.DismissDeleteDialog -> dismissDeleteDialog()
            is MembershipManageIntent.ConfirmDeletePlan -> confirmDeletePlan()
            is MembershipManageIntent.TogglePlanActive -> togglePlanActive(intent.plan)
            is MembershipManageIntent.DismissToast -> dismissToast()
            is MembershipManageIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadPlans() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        subscriptionRepository.getMyPlans(1, PAGE_SIZE).onSuccess { result ->
            val totalSubscribers = result.items.sumOf { it.subscriberCount }
            reduce {
                state.copy(
                    isLoading = false,
                    plans = result.items,
                    totalSubscribers = totalSubscribers,
                    nextPage = 2,
                    hasMore = result.hasNext,
                    error = null)
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isLoading = false,
                    error = error.message ?: "플랜을 불러올 수 없습니다"
                )
            }
        }
    }

    private fun refreshPlans() = intent {
        reduce { state.copy(isRefreshing = true) }

        subscriptionRepository.getMyPlans(1, PAGE_SIZE).onSuccess { result ->
            val totalSubscribers = result.items.sumOf { it.subscriberCount }
            reduce {
                state.copy(
                    isRefreshing = false,
                    plans = result.items,
                    totalSubscribers = totalSubscribers,
                    nextPage = 2,
                    hasMore = result.hasNext,
                    error = null)
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isRefreshing = false,
                    toastMessage = error.message ?: "새로고침 실패"
                )
            }
        }
    }

    private fun loadMorePlans() = intent {
        if (state.isLoading || !state.hasMore) return@intent

        reduce { state.copy(isLoading = true) }

        subscriptionRepository.getMyPlans(state.nextPage, PAGE_SIZE).onSuccess { result ->
            val totalSubscribers = (state.plans + result.items).sumOf { it.subscriberCount }
            reduce {
                state.copy(
                    isLoading = false,
                    plans = (state.plans + result.items).distinctBy { it.id },
                    totalSubscribers = totalSubscribers,
                    nextPage = state.nextPage + 1,
                    hasMore = result.hasNext
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isLoading = false,
                    snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")
                )
            }
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(MembershipManageSideEffect.NavigateBack)
    }

    private fun showPlanOptions(plan: SubscriptionPlan) = intent {
        reduce { state.copy(optionSheetPlan = plan) }
    }

    private fun dismissPlanOptions() = intent {
        reduce { state.copy(optionSheetPlan = null) }
    }

    private fun showCreatePlanSheet() = intent {
        reduce { state.copy(showPlanBottomSheet = true, editingPlan = null) }
    }

    private fun showEditPlanSheet(plan: SubscriptionPlan) = intent {
        reduce { state.copy(showPlanBottomSheet = true, editingPlan = plan) }
    }

    private fun dismissPlanSheet() = intent {
        reduce { state.copy(showPlanBottomSheet = false, editingPlan = null) }
    }

    private fun savePlan(
        name: String,
        tier: SubscriptionPlanTier,
        description: String,
        price: String,
        benefits: List<String>
    ) = intent {
        reduce { state.copy(isSaving = true) }

        val editingPlan = state.editingPlan

        val result = if (editingPlan != null) {
            subscriptionRepository.updatePlan(
                planId = editingPlan.id,
                name = name,
                description = description,
                price = price,
                benefits = benefits,
                isActive = null
            )
        } else {
            subscriptionRepository.createPlan(
                name = name,
                tier = tier,
                description = description,
                price = price,
                benefits = benefits
            )
        }

        result.onSuccess { savedPlan ->
            val updatedPlans = if (editingPlan != null) {
                state.plans.map { if (it.id == savedPlan.id) savedPlan else it }
            } else {
                listOf(savedPlan) + state.plans
            }
            val totalSubscribers = updatedPlans.sumOf { it.subscriberCount }

            reduce {
                state.copy(
                    isSaving = false,
                    showPlanBottomSheet = false,
                    editingPlan = null,
                    plans = updatedPlans,
                    totalSubscribers = totalSubscribers,
                    toastMessage = if (editingPlan != null) "플랜이 수정되었습니다" else "플랜이 생성되었습니다"
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isSaving = false,
                    toastMessage = error.message ?: "저장에 실패했습니다"
                )
            }
        }
    }

    private fun showDeleteDialog(plan: SubscriptionPlan) = intent {
        reduce { state.copy(showDeleteDialog = true, deletingPlan = plan) }
    }

    private fun dismissDeleteDialog() = intent {
        reduce { state.copy(showDeleteDialog = false, deletingPlan = null) }
    }

    private fun confirmDeletePlan() = intent {
        val plan = state.deletingPlan ?: return@intent

        // 요청 전에 닫음, 연타로 삭제 요청이 두 번 가지 않게
        reduce { state.copy(showDeleteDialog = false, deletingPlan = null) }
        subscriptionRepository.deletePlan(plan.id).onSuccess {
            val updatedPlans = state.plans.filter { it.id != plan.id }
            val totalSubscribers = updatedPlans.sumOf { it.subscriberCount }

            reduce {
                state.copy(
                    plans = updatedPlans,
                    totalSubscribers = totalSubscribers,
                    toastMessage = "플랜이 삭제되었습니다"
                )
            }
        }.onFailure { error ->
            reduce { state.copy(toastMessage = error.message ?: "삭제에 실패했습니다") }
        }
    }

    private fun togglePlanActive(plan: SubscriptionPlan) = intent {
        val newActive = !plan.isActive
        subscriptionRepository.updatePlan(
            planId = plan.id,
            name = plan.name,
            description = plan.description,
            price = plan.price,
            benefits = plan.benefits,
            isActive = newActive
        ).onSuccess { updatedPlan ->
            val updatedPlans = state.plans.map { if (it.id == updatedPlan.id) updatedPlan else it }
            val totalSubscribers = updatedPlans.sumOf { it.subscriberCount }
            reduce {
                state.copy(
                    plans = updatedPlans,
                    totalSubscribers = totalSubscribers,
                    toastMessage = if (newActive) "플랜이 활성화되었습니다" else "플랜이 비활성화되었습니다"
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(toastMessage = error.message ?: "상태 변경에 실패했습니다")
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
