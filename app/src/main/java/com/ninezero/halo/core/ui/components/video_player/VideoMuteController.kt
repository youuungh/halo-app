package com.ninezero.halo.core.ui.components.video_player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 전역 음소거 상태 */
object VideoMuteController {  // 기본 음소거
    var muted by mutableStateOf(true)
        private set

    fun toggle() {
        muted = !muted
    }

    fun updateMuted(value: Boolean) {
        muted = value
    }
}
