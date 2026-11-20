package com.ninezero.halo.core.ui.components.navigation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/** 하단 바 옆 원형 버튼 */
data class BottomBarSideButton(
    @param:DrawableRes val iconRes: Int,
    val contentDescription: String,
    val onClick: () -> Unit
)

/** 탭 화면이 하단 바 옆 버튼을 띄우고 거두는 통로 */
@Stable
class BottomBarSideButtonState {
    var sideButton: BottomBarSideButton? by mutableStateOf(null)
        private set

    fun show(sideButton: BottomBarSideButton) {
        this.sideButton = sideButton
    }

    fun hide() {
        sideButton = null
    }
}

/** 하단 바 옆 버튼 상태, 없으면 null */
val LocalBottomBarSideButton = staticCompositionLocalOf<BottomBarSideButtonState?> { null }
