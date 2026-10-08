package com.ninezero.halo.core.ui.components.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalBottomBarPadding = compositionLocalOf { 16.dp }  // 떠 있는 하단 바가 차지하는 높이

object BottomBarDefaults {
    /** 알약 높이 */
    val PillHeight: Dp = 56.dp

    /** 알약 상하 여백 */
    val VerticalMargin: Dp = 8.dp

    /** 알약 좌우 여백 */
    val HorizontalMargin: Dp = 16.dp

    /** 콘텐츠와 알약 사이 여유 */
    val ContentGap: Dp = 16.dp

    /** 내비바 인셋 뺀 알약 높이 */
    val PillFootprint: Dp = VerticalMargin + PillHeight + ContentGap  // navigationBarsPadding 이중 계산 방지용

    /** 알약을 피할 하단 여백 */
    @Composable
    fun reservedContentPadding(): Dp {
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        return navBottom + PillFootprint
    }
}
