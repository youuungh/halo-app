package com.ninezero.halo.core.ui.components.bottomsheet

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScrollModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Velocity

/** 시트 내부 목록의 끝 플링 반동 차단 */
fun Modifier.blockSheetFling(): Modifier = this.then(BlockSheetFlingElement)  // 드래그 잔여분은 시트로 통과

/** 시트 내부 스크롤 전파 전부 차단 */
fun Modifier.blockSheetDrag(): Modifier = this.then(BlockSheetDragElement)

private object BlockSheetFlingElement : ModifierNodeElement<BlockSheetFlingNode>() {
    override fun create() = BlockSheetFlingNode()
    override fun update(node: BlockSheetFlingNode) {}
    override fun hashCode(): Int = "BlockSheetFling".hashCode()
    override fun equals(other: Any?): Boolean = other === this
}

private class BlockSheetFlingNode : DelegatingNode() {
    // 이번 제스처에서 드래그 잔여분이 시트로 넘어갔는지
    private var sheetDragged = false

    init {
        delegate(
            nestedScrollModifierNode(
                connection = object : NestedScrollConnection {
                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        if (source == NestedScrollSource.UserInput && available.y > 0f) {
                            sheetDragged = true
                        }
                        return Offset.Zero
                    }

                    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                        val passThrough = sheetDragged
                        sheetDragged = false
                        return if (passThrough) Velocity.Zero else available
                    }
                },
                dispatcher = null
            )
        )
    }
}

private object BlockSheetDragElement : ModifierNodeElement<BlockSheetDragNode>() {
    override fun create() = BlockSheetDragNode()
    override fun update(node: BlockSheetDragNode) {}
    override fun hashCode(): Int = "BlockSheetDrag".hashCode()
    override fun equals(other: Any?): Boolean = other === this
}

private class BlockSheetDragNode : DelegatingNode() {
    init {
        delegate(
            nestedScrollModifierNode(
                connection = object : NestedScrollConnection {
                    // 드래그 잔여분만 소비해 시트 드래그 차단
                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset = if (source == NestedScrollSource.UserInput) available else Offset.Zero

                    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
                        available
                },
                dispatcher = null
            )
        )
    }
}
