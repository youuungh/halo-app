package com.ninezero.halo.core.ui.components.picker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetDrag
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs

private val ITEM_HEIGHT = 40.dp

/** 휠 피커 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemCount: Int = 5
) {
    val halfVisible = visibleItemCount / 2
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // 외부에서 selectedIndex가 바뀌면 해당 위치로 스크롤
    LaunchedEffect(selectedIndex) {
        if (listState.firstVisibleItemIndex != selectedIndex) {
            listState.scrollToItem(selectedIndex)
        }
    }

    val currentItems = rememberUpdatedState(items)
    val currentSelectedIndex = rememberUpdatedState(selectedIndex)
    val currentOnSelectedChanged = rememberUpdatedState(onSelectedChanged)
    LaunchedEffect(listState) {
        snapshotFlow {
            // 패딩 아이템이 있어 첫 항목이 중앙
            listState.firstVisibleItemIndex
        }
            .distinctUntilChanged()
            .collect { index ->
                if (index in currentItems.value.indices && index != currentSelectedIndex.value) {
                    currentOnSelectedChanged.value(index)
                }
            }
    }

    Box(
        modifier = modifier.height(ITEM_HEIGHT * visibleItemCount),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.matchParentSize().blockSheetDrag(),
            overscrollEffect = null
        ) {
            // 첫 실제 아이템이 중앙에 올 수 있도록 하는 상단 패딩 아이템
            items(halfVisible) {
                Box(modifier = Modifier.height(ITEM_HEIGHT).fillMaxWidth())
            }

            items(items.size) { index ->
                val layoutInfo = listState.layoutInfo
                val viewportCenter = layoutInfo.viewportStartOffset +
                        (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                // LazyColumn 인덱스에는 패딩 아이템 오프셋이 포함됨
                val lazyIndex = index + halfVisible
                val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == lazyIndex }
                val distanceFromCenter = if (itemInfo != null) {
                    abs((itemInfo.offset + itemInfo.size / 2) - viewportCenter).toFloat()
                } else {
                    Float.MAX_VALUE
                }
                val maxDistance = ITEM_HEIGHT.value * halfVisible * 2
                val fraction = (distanceFromCenter / maxDistance).coerceIn(0f, 1f)
                val itemAlpha = 1f - fraction * 0.7f
                val fontSize = lerp(18.sp, 14.sp, fraction)

                Box(
                    modifier = Modifier
                        .height(ITEM_HEIGHT)
                        .fillMaxWidth()
                        .alpha(itemAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        fontSize = fontSize,
                        fontWeight = if (fraction < 0.2f) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 마지막 항목을 중앙에 올리는 하단 패딩
            items(halfVisible) {
                Box(modifier = Modifier.height(ITEM_HEIGHT).fillMaxWidth())
            }
        }

        // 선택 인디케이터: 상단 divider
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset(y = -(ITEM_HEIGHT / 2)),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // 선택 인디케이터: 하단 divider
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset(y = ITEM_HEIGHT / 2),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
