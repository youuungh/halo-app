package com.ninezero.halo.feature.profile.presentation.my_profile.components

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/** CollapsibleHeaderLayout 외부 state */
@Stable
class CollapsibleHeaderState(initialScrollOffset: Float = 0f) {
    internal var headerHeight by mutableFloatStateOf(0f)
    internal var scrollOffset by mutableFloatStateOf(initialScrollOffset)  // 0 펼침, -headerHeight 접힘

    val isFullyExpanded: Boolean
        get() = scrollOffset >= -0.5f

    // 헤더를 부드럽게 펼침
    suspend fun expand() {
        animate(scrollOffset, 0f) { value, _ ->
            scrollOffset = value
        }
    }

    companion object {
        // scrollOffset만 저장·복원
        val Saver: Saver<CollapsibleHeaderState, Float> = Saver(
            save = { it.scrollOffset },
            restore = { CollapsibleHeaderState(initialScrollOffset = it) }
        )
    }
}

@Composable
fun rememberCollapsibleHeaderState(): CollapsibleHeaderState =
    rememberSaveable(saver = CollapsibleHeaderState.Saver) { CollapsibleHeaderState() }
