package com.ninezero.halo.core.ui.components.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

data class CountdownRemaining(
    val totalMillis: Long,
    val days: Long,
    val hours: Int,
    val minutes: Int,
    val seconds: Int
) {
    val isFinished: Boolean get() = totalMillis <= 0L
}

private fun countdownRemainingOf(targetMillis: Long, nowMillis: Long): CountdownRemaining {
    val remaining = (targetMillis - nowMillis).coerceAtLeast(0L)
    val totalSeconds = remaining / 1000L
    val days = totalSeconds / 86_400L
    val hours = ((totalSeconds % 86_400L) / 3_600L).toInt()
    val minutes = ((totalSeconds % 3_600L) / 60L).toInt()
    val seconds = (totalSeconds % 60L).toInt()
    return CountdownRemaining(remaining, days, hours, minutes, seconds)
}

/** 매초 갱신 카운트다운 State */
@Composable
fun rememberCountdown(targetMillis: Long): State<CountdownRemaining> {  // value는 leaf 컴포저블에서만 읽을 것
    val state = remember(targetMillis) {
        mutableStateOf(countdownRemainingOf(targetMillis, System.currentTimeMillis()))
    }
    LaunchedEffect(targetMillis) {
        while (true) {
            val now = System.currentTimeMillis()
            val remaining = countdownRemainingOf(targetMillis, now)
            state.value = remaining
            if (remaining.isFinished) break
            // 다음 초 경계까지 정렬 대기
            delay(1000L - (now % 1000L))
        }
    }
    return state
}

/** N일 HH:MM:SS 또는 HH:MM:SS 포맷 */
fun CountdownRemaining.format(): String =
    if (days > 0) {
        "%d일 %02d:%02d:%02d".format(days, hours, minutes, seconds)
    } else {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
