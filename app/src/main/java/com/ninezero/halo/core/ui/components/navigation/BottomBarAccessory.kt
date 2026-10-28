package com.ninezero.halo.core.ui.components.navigation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/** 하단 바 옆 원형 버튼 */
data class BottomBarAccessory(
    @param:DrawableRes val iconRes: Int,
    val contentDescription: String,
    val onClick: () -> Unit
)

/** 탭 화면이 하단 바 옆 버튼을 띄우고 거두는 통로 */
@Stable
class BottomBarAccessoryState {
    var accessory: BottomBarAccessory? by mutableStateOf(null)
        private set

    fun show(accessory: BottomBarAccessory) {
        this.accessory = accessory
    }

    fun hide() {
        accessory = null
    }
}

val LocalBottomBarAccessory = staticCompositionLocalOf<BottomBarAccessoryState?> { null }
