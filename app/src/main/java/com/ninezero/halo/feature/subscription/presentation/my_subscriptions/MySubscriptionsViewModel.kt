package com.ninezero.halo.feature.subscription.presentation.my_subscriptions

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.subscription.domain.model.Subscription
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionStatus
import com.ninezero.halo.feature.subscription.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class MySubscriptionsViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel(), OrbitContainerHost<MySubscriptionsState, MySubscriptionsState, MySubscriptionsSideEffect> {

    override val container: OrbitContainer<MySubscriptionsState, MySubscriptionsState, MySubscriptionsSideEffect> =
        orbitContainer(MySubscriptionsState())

    init {
        loadSubscriptions()
    }

    fun onIntent(intent: MySubscriptionsIntent) {
        when (intent) {
            is MySubscriptionsIntent.RefreshSubscriptions -> refreshSubscriptions()
            is MySubscriptionsIntent.LoadMoreSubscriptions -> loadMoreSubscriptions()
            is MySubscriptionsIntent.NavigateBack -> navigateBack()
            is MySubscriptionsIntent.FilterByStatus -> filterByStatus(intent.status)
            is MySubscriptionsIntent.ToggleAutoRenew -> toggleAutoRenew(intent.subscriptionId, intent.autoRenew)
            is MySubscriptionsIntent.RequestAutoRenewOff -> requestAutoRenewOff(intent.subscription)
            is MySubscriptionsIntent.ConfirmAutoRenewOff -> confirmAutoRenewOff()
            is MySubscriptionsIntent.DismissAutoRenewOffDialog -> dismissAutoRenewOffDialog()
            is MySubscriptionsIntent.NavigateToCreatorProfile -> navigateToCreatorProfile(intent.creatorId)
            is MySubscriptionsIntent.DismissToast -> dismissToast()
            is MySubscriptionsIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadSubscriptions() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        val status = state.selectedStatus
        subscriptionRepository.getMySubscriptions(
            status = status,
            page = 1,
            limit = 20
        )
            .onSuccess { result ->
                if (state.selectedStatus != status) return@intent
                reduce {
                    state.copy(
                        subscriptions = result.items,
                        isLoading = false,
                        page = 1,
                        hasMore = result.hasNext,
                        error = null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "구독 목록을 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun refreshSubscriptions() = intent {
        reduce { state.copy(isRefreshing = true) }

        val status = state.selectedStatus
        subscriptionRepository.getMySubscriptions(
            status = status,
            page = 1,
            limit = 20
        )
            .onSuccess { result ->
                if (state.selectedStatus != status) {
                    reduce { state.copy(isRefreshing = false) }
                    return@intent
                }
                reduce {
                    state.copy(
                        subscriptions = result.items,
                        isRefreshing = false,
                        page = 1,
                        hasMore = result.hasNext,
                        error = null
                    )
                }
            }
            .onFailure { error ->
                reduce { state.copy(isRefreshing = false, snackbarState = SnackbarState(error.message ?: "새로고침 실패")) }
            }
    }

    private fun loadMoreSubscriptions() = intent {
        if (state.isLoading || !state.hasMore) return@intent

        val nextPage = state.page + 1
        reduce { state.copy(isLoading = true) }

        val status = state.selectedStatus
        subscriptionRepository.getMySubscriptions(
            status = status,
            page = nextPage,
            limit = 20
        )
            .onSuccess { result ->
                if (state.selectedStatus != status) return@intent
                reduce {
                    state.copy(
                        subscriptions = (state.subscriptions + result.items).distinctBy { it.id },
                        isLoading = false,
                        page = nextPage,
                        hasMore = result.hasNext
                    )
                }
            }
            .onFailure { error ->
                reduce { state.copy(isLoading = false, snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")) }
            }
    }

    private fun navigateBack() = intent {
        postSideEffect(MySubscriptionsSideEffect.NavigateBack)
    }

    private fun filterByStatus(status: SubscriptionStatus?) = intent {
        if (state.selectedStatus == status) return@intent
        reduce {
            state.copy(
                selectedStatus = status,
                subscriptions = emptyList(),
                isLoading = true,
                page = 1,
                hasMore = true
            )
        }
        loadSubscriptions()
    }

    private fun requestAutoRenewOff(subscription: Subscription) = intent {
        reduce { state.copy(autoRenewOffTarget = subscription) }
    }

    private fun dismissAutoRenewOffDialog() = intent {
        reduce { state.copy(autoRenewOffTarget = null) }
    }

    private fun confirmAutoRenewOff() = intent {
        val target = state.autoRenewOffTarget ?: return@intent

        reduce { state.copy(autoRenewOffTarget = null, isUpdatingAutoRenew = true) }

        // 낙관적 갱신
        reduce {
            state.copy(
                subscriptions = state.subscriptions.map { s ->
                    if (s.id == target.id) {
                        s.copy(autoRenew = false)
                    } else s
                }
            )
        }

        subscriptionRepository.cancelSubscription(target.id)
            .onSuccess {
                reduce {
                    state.copy(
                        isUpdatingAutoRenew = false,
                        toastMessage = "자동 갱신이 비활성화되었습니다"
                    )
                }
            }
            .onFailure { error ->
                // 롤백
                reduce {
                    state.copy(
                        isUpdatingAutoRenew = false,
                        subscriptions = state.subscriptions.map { s ->
                            if (s.id == target.id) {
                                s.copy(autoRenew = true)
                            } else s
                        },
                        toastMessage = error.message ?: "자동 갱신 변경에 실패했습니다"
                    )
                }
            }
    }

    private fun toggleAutoRenew(subscriptionId: Int, autoRenew: Boolean) = intent {
        reduce { state.copy(isUpdatingAutoRenew = true) }

        // 낙관적 갱신
        reduce {
            state.copy(
                subscriptions = state.subscriptions.map { s ->
                    if (s.id == subscriptionId) {
                        s.copy(autoRenew = autoRenew)
                    } else s
                }
            )
        }

        subscriptionRepository.updateAutoRenew(subscriptionId, autoRenew)
            .onSuccess {
                reduce {
                    state.copy(
                        isUpdatingAutoRenew = false,
                        toastMessage = if (autoRenew) "자동 갱신이 활성화되었습니다" else "자동 갱신이 비활성화되었습니다"
                    )
                }
            }
            .onFailure { error ->
                // 롤백
                reduce {
                    state.copy(
                        isUpdatingAutoRenew = false,
                        subscriptions = state.subscriptions.map { s ->
                            if (s.id == subscriptionId) {
                                s.copy(autoRenew = !autoRenew)
                            } else s
                        },
                        toastMessage = error.message ?: "자동 갱신 변경에 실패했습니다"
                    )
                }
            }
    }

    private fun navigateToCreatorProfile(creatorId: Int) = intent {
        postSideEffect(MySubscriptionsSideEffect.NavigateToCreatorProfile(creatorId))
    }

    private fun dismissToast() = intent {
        reduce { state.copy(toastMessage = null) }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
