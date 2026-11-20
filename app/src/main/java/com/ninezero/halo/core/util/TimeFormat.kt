package com.ninezero.halo.core.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private object DateFormatters {
    val MONTH_DAY: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd")
    val SHORT_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yy/MM/dd")
    val MESSAGE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA)
    val DATE_SEPARATOR: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREA)
}

fun Long.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

/** ISO 8601 문자열을 epoch millis로 */
fun String.parseTimestamp(): Long {
    return try {
        LocalDateTime.parse(this, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .toInstant(ZoneOffset.UTC).toEpochMilli()
    } catch (e: Exception) {
        0L
    }
}

/** epoch millis를 UTC ISO 문자열로 */
fun Long.toUtcIsoTimestamp(): String =  // 서버는 zone 없는 UTC
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDateTime()
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

fun LocalDateTime.toEpochMillis(): Long =
    this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun Long.toFormattedDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return this.toLocalDateTime().format(formatter)
}

fun Long.toRelativeTime(): String {  // 6일까지 상대, 그 뒤 같은 해면 MM/dd, 다른 해면 yy/MM/dd
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < 60_000 -> "방금 전"
        diff < 3600_000 -> "${diff / 60_000}분 전"
        diff < 86400_000 -> "${diff / 3600_000}시간 전"
        diff < 604800_000 -> "${diff / 86400_000}일 전"
        this.toLocalDateTime().year == now.toLocalDateTime().year ->
            this.toLocalDateTime().format(DateFormatters.MONTH_DAY)
        else -> this.toLocalDateTime().format(DateFormatters.SHORT_DATE)
    }
}

/** 채팅 메시지 시각 표시 */
fun Long.toMessageTime(): String {
    return this.toLocalDateTime().format(DateFormatters.MESSAGE_TIME)
}

/** 채팅 날짜 구분선 표시 */
fun Long.toDateSeparator(): String {
    return this.toLocalDateTime().format(DateFormatters.DATE_SEPARATOR)
}

fun Long.isSameDay(other: Long): Boolean {
    return this.toLocalDateTime().toLocalDate() == other.toLocalDateTime().toLocalDate()
}

fun Long.isSameMinute(other: Long): Boolean {
    val a = this.toLocalDateTime()
    val b = other.toLocalDateTime()
    return a.toLocalDate() == b.toLocalDate() && a.hour == b.hour && a.minute == b.minute
}
