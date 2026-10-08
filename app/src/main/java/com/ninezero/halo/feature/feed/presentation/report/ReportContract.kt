package com.ninezero.halo.feature.feed.presentation.report

import com.ninezero.halo.core.model.ReportTargetType

data class ReportState(
    val isSubmitting: Boolean = false
)

sealed interface ReportIntent {
    data class Submit(val targetType: ReportTargetType, val targetId: Int) : ReportIntent
}

sealed interface ReportSideEffect {
    data class ShowMessage(val message: String) : ReportSideEffect
}
