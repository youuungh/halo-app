package com.ninezero.halo.feature.feed.presentation.report

import androidx.lifecycle.ViewModel
import com.ninezero.halo.core.model.ReportTargetType
import com.ninezero.halo.feature.feed.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel(), OrbitContainerHost<ReportState, ReportState, ReportSideEffect> {

    override val container: OrbitContainer<ReportState, ReportState, ReportSideEffect> =
        orbitContainer(ReportState())

    fun onIntent(intent: ReportIntent) {
        when (intent) {
            is ReportIntent.Submit -> submit(intent.targetType, intent.targetId)
        }
    }

    private fun submit(targetType: ReportTargetType, targetId: Int) = intent {
        if (state.isSubmitting) return@intent
        reduce { state.copy(isSubmitting = true) }

        reportRepository.report(targetType, targetId)
            .onSuccess {
                postSideEffect(ReportSideEffect.ShowMessage("신고 처리되었습니다"))
            }
            .onFailure { error ->
                postSideEffect(ReportSideEffect.ShowMessage(error.message ?: "신고에 실패했습니다"))
            }

        reduce { state.copy(isSubmitting = false) }
    }
}
