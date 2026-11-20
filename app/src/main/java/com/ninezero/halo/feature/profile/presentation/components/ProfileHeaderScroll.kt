package com.ninezero.halo.feature.profile.presentation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import com.ninezero.halo.core.ui.components.layout.GestureStartFlag

/** 헤더와 목록 스크롤 나눔, 한 번에 같이 안 넘어감 */
class ProfileHeaderScroll(
    private val headerState: CollapsibleHeaderState,
    private val maxCollapse: () -> Float,
    private val isListAtTop: () -> Boolean
) : NestedScrollConnection {
    val gestureStart = GestureStartFlag()
    private var startOffset = Float.NaN
    private var listWasAtTop = false

    /** 헤더·탭을 직접 끌 때 */
    fun consume(delta: Float): Float {
        if (headerState.headerHeight == 0f) return 0f
        if (delta > 0f && !isListAtTop()) return 0f
        val newOffset = (headerState.scrollOffset + delta).coerceIn(-maxCollapse(), 0f)
        val consumed = newOffset - headerState.scrollOffset
        headerState.scrollOffset = newOffset
        return consumed
    }

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (headerState.headerHeight == 0f) return Offset.Zero
        if (gestureStart.takeNew()) {
            startOffset = headerState.scrollOffset
            listWasAtTop = isListAtTop()
        }
        val delta = available.y

        // 목록 손짓은 맨 위에서 멈춤, 헤더는 다음 손짓에
        if (delta > 0f && !listWasAtTop) return Offset.Zero

        val consumed = consume(delta)

        // 접히기 전 손짓은 접힘에서 멈춤
        val collapsedOffset = -maxCollapse()
        val staysCollapsed = delta < 0f && startOffset > collapsedOffset && headerState.scrollOffset <= collapsedOffset
        return Offset(0f, if (staysCollapsed) delta else consumed)
    }
}
