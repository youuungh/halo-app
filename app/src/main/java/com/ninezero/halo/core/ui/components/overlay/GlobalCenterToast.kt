package com.ninezero.halo.core.ui.components.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GlobalCenterToast {
    var message by mutableStateOf<String?>(null)
        private set

    fun show(message: String) {
        this.message = message
    }

    fun dismiss() {
        message = null
    }
}

/** 전역 중앙 토스트 호스트 */
@Composable
fun GlobalCenterToastHost() {
    CenterToast(
        visible = GlobalCenterToast.message != null,
        message = GlobalCenterToast.message ?: "",
        onDismiss = GlobalCenterToast::dismiss
    )
}
