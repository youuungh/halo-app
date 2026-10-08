package com.ninezero.halo.core.ui.components.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf

@Stable
class SnackbarRelay {
    private val stack = mutableStateListOf<SnackbarHostState>()

    val current: SnackbarHostState? get() = stack.lastOrNull()

    fun register(hostState: SnackbarHostState) {
        stack.remove(hostState)
        stack.add(hostState)
    }

    fun unregister(hostState: SnackbarHostState) {
        stack.remove(hostState)
    }
}

val LocalSnackbarRelay = staticCompositionLocalOf { SnackbarRelay() }
