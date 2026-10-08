
package com.ninezero.halo.core.ui.components.modifiers

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** 시스템 바가 숨겨져도 유지되는 statusBars 패딩 */
@Composable
fun Modifier.statusBarsPaddingIgnoringVisibility(): Modifier =
    windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility)

/** 시스템 바가 숨겨져도 유지되는 navigationBars 패딩 */
@Composable
fun Modifier.navigationBarsPaddingIgnoringVisibility(): Modifier =
    windowInsetsPadding(WindowInsets.navigationBarsIgnoringVisibility)
