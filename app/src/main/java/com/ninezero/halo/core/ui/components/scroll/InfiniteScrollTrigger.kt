package com.ninezero.halo.core.ui.components.scroll

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

/** 무한 스크롤 트리거 */
@Composable
fun InfiniteScrollTrigger(
    listState: LazyListState,
    isLoading: Boolean,
    hasMore: Boolean,
    threshold: Int = 3,
    onLoadMore: () -> Unit
) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val nearEnd by remember(listState, threshold) {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - threshold
        }
    }
    val totalCount by remember(listState) { derivedStateOf { listState.layoutInfo.totalItemsCount } }

    // 실패 뒤 끝을 벗어났다 돌아와야 재시도
    var firedAtCount by remember(listState) { mutableStateOf<Int?>(null) }
    LaunchedEffect(nearEnd) {
        if (!nearEnd) firedAtCount = null
    }
    LaunchedEffect(nearEnd, isLoading, hasMore, totalCount) {
        if (nearEnd && !isLoading && hasMore && firedAtCount != totalCount) {
            firedAtCount = totalCount
            currentOnLoadMore()
        }
    }
}
