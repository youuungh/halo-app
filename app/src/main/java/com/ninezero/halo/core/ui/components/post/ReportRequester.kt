package com.ninezero.halo.core.ui.components.post

import androidx.compose.runtime.staticCompositionLocalOf
import com.ninezero.halo.core.model.ReportTargetType

val LocalReportRequester =
    staticCompositionLocalOf<(targetType: ReportTargetType, targetId: Int) -> Unit> {
        { _, _ -> }
    }
