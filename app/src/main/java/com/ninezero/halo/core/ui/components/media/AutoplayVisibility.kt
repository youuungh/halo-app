package com.ninezero.halo.core.ui.components.media

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs

private const val START_FRACTION = 0.5f  // 새 영상 자동재생을 "시작"할 최소 가시 비율
private const val KEEP_FRACTION = 0.15f  // 재생 중 영상을 유지할 최소 가시 비율
private const val AUTOPLAY_DEBOUNCE_MS = 150L
private const val START_MAX_VELOCITY_ITEMS_PER_SEC = 2.0f

/** 아이템의 화면 노출 비율 */
private fun LazyListItemInfo.visibleFraction(layoutInfo: LazyListLayoutInfo): Float {
    if (size <= 0) return 0f
    val visible = (minOf(offset + size, layoutInfo.viewportEndOffset) -
        maxOf(offset, layoutInfo.viewportStartOffset)).coerceAtLeast(0)
    return visible.toFloat() / size
}

/** 자동재생 대상 선택 */
private fun <K> selectAutoplayTarget(  // 시작은 중앙 가까운 것, 유지는 이전 대상 우선
    layoutInfo: LazyListLayoutInfo,
    prev: K?,
    allowNewStart: Boolean,
    identityOf: (LazyListItemInfo) -> K?
): K? {
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
    var startId: K? = null
    var startDist = Int.MAX_VALUE
    var prevKeepable = false
    for (item in layoutInfo.visibleItemsInfo) {
        val id = identityOf(item) ?: continue
        val fraction = item.visibleFraction(layoutInfo)
        if (id == prev && fraction >= KEEP_FRACTION) prevKeepable = true
        if (fraction >= START_FRACTION) {
            val dist = abs((item.offset + item.size / 2) - viewportCenter)
            if (dist < startDist) {
                startDist = dist
                startId = id
            }
        }
    }
    // 빠른 스크롤 중 새 시작 보류, 직전 대상 유지
    val isNewStart = startId != null && startId != prev
    val resolved = if (isNewStart && !allowNewStart) null else startId
    return resolved ?: prev?.takeIf { prevKeepable }
}

val LocalAutoplayEnabled = compositionLocalOf { true }

/** 자동재생 대상 상태 */
@Composable
fun <K : Any> rememberAutoplayTarget(  // 빠른 플링에서는 새 시작 보류
    listState: LazyListState,
    identityOf: (LazyListItemInfo) -> K?
): State<K?> {
    val target = remember { mutableStateOf<K?>(null) }
    // identityOf는 최신 것을 쓰도록 추적
    val identityState = rememberUpdatedState(identityOf)
    // 숨겨진 탭에서는 정지
    val enabled = LocalAutoplayEnabled.current

    // 스크롤 속도, 플링 중 새 시작 차단용
    // 정착 판정은 isScrollInProgress
    val scrollVelocity = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(listState) {
        var lastPos = Float.NaN
        var lastTimeNanos = 0L
        snapshotFlow {
            val firstSize = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 1
            listState.firstVisibleItemIndex +
                listState.firstVisibleItemScrollOffset.toFloat() / firstSize.coerceAtLeast(1)
        }.collect { pos ->
            val now = System.nanoTime()
            if (!lastPos.isNaN() && lastTimeNanos != 0L) {
                val dt = (now - lastTimeNanos) / 1_000_000_000f
                if (dt > 0f) scrollVelocity.floatValue = abs(pos - lastPos) / dt
            }
            lastPos = pos
            lastTimeNanos = now
        }
    }

    LaunchedEffect(listState, enabled) {
        if (!enabled) {
            // 탭이 숨겨지면 즉시 정지
            target.value = null
            return@LaunchedEffect
        }
        snapshotFlow {
            // 멈춰 있으면 새 시작 허용, 스크롤 중엔 느릴 때만
            val allowNewStart = !listState.isScrollInProgress ||
                scrollVelocity.floatValue <= START_MAX_VELOCITY_ITEMS_PER_SEC
            selectAutoplayTarget(listState.layoutInfo, target.value, allowNewStart, identityState.value)
        }.collectLatest { value ->
            // collectLatest가 직전 delay 취소
            delay(AUTOPLAY_DEBOUNCE_MS)
            target.value = value
        }
    }
    return target
}

/** 자동재생 포스트 id 상태 */
@Composable
fun rememberSettledVideoPostId(
    listState: LazyListState,
    videoPostIds: Set<Int>,
    keyToId: (Any?) -> Int?
): State<Int?> = rememberAutoplayTarget(listState) { item ->
    keyToId(item.key)?.takeIf { it in videoPostIds }
}
