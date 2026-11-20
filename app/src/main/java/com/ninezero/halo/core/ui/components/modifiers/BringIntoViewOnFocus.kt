package com.ninezero.halo.core.ui.components.modifiers

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest

/** 키보드 · 하단 바 위로 띄우는 여유 */
val BringIntoViewGap = 12.dp

/** 포커스된 칸 전체를 키보드 위로, 화면이 줄면 다시 맞춤 */
@Composable
fun Modifier.bringIntoViewOnFocus(isFocused: Boolean, bottomReserve: Dp = 0.dp): Modifier {
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val requester = remember { BringIntoViewRequester() }
    var size by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(isFocused, size, bottomReserve) {
        if (!isFocused || size == IntSize.Zero) return@LaunchedEffect
        val reservePx = with(density) { (bottomReserve + BringIntoViewGap).toPx() }
        snapshotFlow { imeInsets.getBottom(density) }
            .collectLatest { imeBottom ->
                if (imeBottom <= 0) return@collectLatest
                requester.bringIntoView(Rect(0f, 0f, size.width.toFloat(), size.height + reservePx))
            }
    }

    return bringIntoViewRequester(requester).onSizeChanged { size = it }
}
