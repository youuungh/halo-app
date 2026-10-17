package com.ninezero.halo.core.ui.components.snackbar

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.ninezero.halo.core.util.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
class SnackbarRelay {
    // 호스트 쪽 scope로 띄워야 요청한 카드가 사라져도 스낵바가 남음
    private class Entry(val hostState: SnackbarHostState, val scope: CoroutineScope)

    private val stack = mutableStateListOf<Entry>()

    val current: SnackbarHostState? get() = stack.lastOrNull()?.hostState

    fun register(hostState: SnackbarHostState, scope: CoroutineScope) {
        stack.removeAll { it.hostState == hostState }
        stack.add(Entry(hostState, scope))
    }

    fun unregister(hostState: SnackbarHostState) {
        stack.removeAll { it.hostState == hostState }
    }

    /** 지금 화면 스낵바로 띄움, 등록된 호스트가 없으면 false */
    fun show(message: String): Boolean {
        val entry = stack.lastOrNull() ?: return false
        entry.hostState.currentSnackbarData?.dismiss()
        entry.scope.launch { entry.hostState.showSnackbar(message) }
        return true
    }
}

/** 화면 VM을 거치지 않는 안내, 등록된 호스트가 없으면 토스트 */
fun SnackbarRelay.showOrToast(context: Context, message: String) {
    if (!show(message)) context.showToast(message)
}

val LocalSnackbarRelay = staticCompositionLocalOf { SnackbarRelay() }
