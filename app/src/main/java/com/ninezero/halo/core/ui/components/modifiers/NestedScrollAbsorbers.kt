package com.ninezero.halo.core.ui.components.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity

/** 가로 스크롤 잔여분의 부모 전파 차단 */
private val HorizontalScrollLeakBlocker = object : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = Offset(available.x, 0f)

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
        Velocity(available.x, 0f)
}

/** 가로 스크롤 잔여분 차단 modifier */
fun Modifier.blockHorizontalScrollLeak(): Modifier =
    this.nestedScroll(HorizontalScrollLeakBlocker)
