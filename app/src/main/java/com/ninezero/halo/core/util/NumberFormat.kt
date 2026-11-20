package com.ninezero.halo.core.util

import java.util.Locale

data class FormattedCount(val number: String, val suffix: String?)

fun Int.toFormattedParts(): FormattedCount {
    if (this < 1000) return FormattedCount(this.toString(), null)
    // 소수 첫째 자리 반올림, 9,950 이상은 만으로
    val tenths = (this + 50) / 100
    if (tenths < 100) return FormattedCount(tenths.toOneDecimal(), "천")
    return FormattedCount(((this + 500) / 1000).toOneDecimal(), "만")
}

/** 소수 한 자리로, 끝이 0이면 정수만 */
private fun Int.toOneDecimal(): String =
    if (this % 10 == 0) "${this / 10}" else "${this / 10}.${this % 10}"

fun Int.toFormattedFollower(): String {
    return when {
        this < 10000 -> String.format(Locale.KOREA, "%,d명", this)
        else -> {
            val tenThousands = this / 10000f
            String.format(Locale.KOREA, "%.1f만명", tenThousands)
        }
    }
}

fun Int.toFormattedPrice(): String {
    return String.format(Locale.KOREA, "%,d원", this)
}

fun Long.toFormattedPrice(): String {
    return String.format(Locale.KOREA, "%,d원", this)
}

fun Long.toCompactWon(): String {  // 만·억 단위
    return when {
        this < 10_000L -> String.format(Locale.KOREA, "%,d", this)
        this < 100_000_000L -> String.format(Locale.KOREA, "%,d만", this / 10_000L)
        else -> String.format(Locale.KOREA, "%.1f억", this / 100_000_000.0)
    }
}

fun Int.toFormattedViewCount(): String {
    return when {
        this < 1000 -> String.format(Locale.KOREA, "%,d", this)
        this < 10000 -> String.format(Locale.KOREA, "%.1f천", this / 1000f)
        else -> String.format(Locale.KOREA, "%.1f만", this / 10000f)
    }
}

fun Int.toFormattedCount(): String {
    return when {
        this < 10000 -> String.format(Locale.KOREA, "%,d", this)
        else -> String.format(Locale.KOREA, "%.1f만", this / 10000f)
    }
}

fun Long.toReadableFileSize(): String {
    return when {
        this < 1024 -> "0 KB"
        this < 1024 * 1024 -> "${this / 1024} KB"
        this < 1024 * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", this / (1024.0 * 1024.0))
        else -> String.format(Locale.getDefault(), "%.1f GB", this / (1024.0 * 1024.0 * 1024.0))
    }
}

fun Long.toVideoDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
