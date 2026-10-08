package com.ninezero.halo.feature.creator.presentation

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.network.ApiException
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.creator.domain.repository.CreatorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class CreatorApplicationViewModel @Inject constructor(
    private val repository: CreatorRepository
) : ViewModel(), OrbitContainerHost<CreatorApplicationState, CreatorApplicationState, CreatorApplicationSideEffect> {

    override val container: OrbitContainer<CreatorApplicationState, CreatorApplicationState, CreatorApplicationSideEffect> =
        orbitContainer(CreatorApplicationState())

    init {
        loadApplication()
    }

    fun onIntent(intent: CreatorApplicationIntent) {
        when (intent) {
            is CreatorApplicationIntent.LoadApplication -> loadApplication()
            is CreatorApplicationIntent.UpdateReason -> updateReason(intent.reason)
            is CreatorApplicationIntent.UpdatePortfolioUrl -> updatePortfolioUrl(intent.url)
            is CreatorApplicationIntent.Submit -> submit()
            is CreatorApplicationIntent.StartReapply -> startReapply()
            is CreatorApplicationIntent.ShowCancelDialog -> showCancelDialog()
            is CreatorApplicationIntent.DismissCancelDialog -> dismissCancelDialog()
            is CreatorApplicationIntent.ConfirmCancel -> confirmCancel()
            is CreatorApplicationIntent.NavigateBack -> navigateBack()
            is CreatorApplicationIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadApplication() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        repository.getMyApplication()
            .onSuccess { application ->
                reduce {
                    state.copy(
                        isLoading = false,
                        application = application
                    )
                }
            }
            .onFailure { e ->
                val isNotFound = e is ApiException && e.code == 404
                reduce {
                    if (isNotFound) {
                        // 신청 내역 없음 → 폼 표시
                        state.copy(isLoading = false, application = null)
                    } else {
                        // 네트워크/서버 에러 → 에러 화면
                        state.copy(isLoading = false, error = "신청 정보를 불러올 수 없습니다")
                    }
                }
            }
    }

    private fun updateReason(reason: String) = blockingIntent {
        reduce { state.copy(reason = reason) }
    }

    private fun updatePortfolioUrl(url: String) = blockingIntent {
        reduce { state.copy(portfolioUrl = url) }
    }

    private fun submit() = intent {
        val trimmedReason = state.reason.trim()
        val trimmedPortfolioUrl = state.portfolioUrl.trim()

        if (trimmedReason.isBlank()) {
            reduce {
                state.copy(snackbarState = SnackbarState("신청 사유를 입력해주세요"))
            }
            return@intent
        }

        if (!trimmedReason.isValidApplicationReason()) {
            reduce {
                state.copy(
                    snackbarState = SnackbarState(
                        "신청 사유는 ${CREATOR_APPLICATION_REASON_MIN_LENGTH}자 이상 ${CREATOR_APPLICATION_REASON_MAX_LENGTH}자 이하로 입력해주세요"
                    )
                )
            }
            return@intent
        }

        if (!trimmedPortfolioUrl.isValidPortfolioUrl()) {
            reduce {
                state.copy(
                    snackbarState = SnackbarState("포트폴리오 URL은 http:// 또는 https://로 시작해야 합니다")
                )
            }
            return@intent
        }

        reduce { state.copy(isSubmitting = true) }

        val portfolioUrl = trimmedPortfolioUrl.ifBlank { null }

        repository.submitApplication(trimmedReason, portfolioUrl)
            .onSuccess { application ->
                reduce {
                    state.copy(
                        isSubmitting = false,
                        application = application,
                        reason = "",
                        portfolioUrl = "",
                        snackbarState = SnackbarState("크리에이터 신청이 완료되었습니다")
                    )
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isSubmitting = false,
                        snackbarState = SnackbarState(e.message ?: "신청에 실패했습니다")
                    )
                }
            }
    }

    private fun startReapply() = intent {
        val application = state.application ?: return@intent
        val remainingDays = application.reapplyDaysLeft()

        if (remainingDays != null) {
            reduce {
                state.copy(
                    snackbarState = SnackbarState("${remainingDays}일 후 다시 신청할 수 있습니다")
                )
            }
            return@intent
        }

        reduce {
            state.copy(
                application = null,
                reason = application.reason,
                portfolioUrl = application.portfolioUrl.orEmpty(),
                error = null
            )
        }
    }

    private fun showCancelDialog() = intent {
        reduce { state.copy(showCancelDialog = true) }
    }

    private fun dismissCancelDialog() = intent {
        reduce { state.copy(showCancelDialog = false) }
    }

    private fun confirmCancel() = intent {
        reduce { state.copy(showCancelDialog = false, isSubmitting = true) }

        repository.cancelApplication()
            .onSuccess {
                reduce {
                    state.copy(
                        isSubmitting = false,
                        application = null,
                        snackbarState = SnackbarState("신청이 취소되었습니다")
                    )
                }
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isSubmitting = false,
                        snackbarState = SnackbarState(e.message ?: "취소에 실패했습니다")
                    )
                }
            }
    }

    private fun navigateBack() = intent {
        postSideEffect(CreatorApplicationSideEffect.NavigateBack)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
