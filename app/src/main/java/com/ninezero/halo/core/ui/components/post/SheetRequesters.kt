package com.ninezero.halo.core.ui.components.post

import androidx.compose.runtime.staticCompositionLocalOf
import com.ninezero.halo.core.model.ReportTargetType

/** 신고 시트 열기, ReportHost가 받음 */
val LocalReportRequester =
    staticCompositionLocalOf<(targetType: ReportTargetType, targetId: Int) -> Unit> {
        { _, _ -> }
    }

/** 구독 시트 열기, SubscriptionSheetHost가 받음 */
val LocalSubscribeRequester =
    staticCompositionLocalOf<(creatorId: Int, creatorName: String) -> Unit> {
        { _, _ -> }
    }
