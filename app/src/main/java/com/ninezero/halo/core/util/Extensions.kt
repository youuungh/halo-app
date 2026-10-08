package com.ninezero.halo.core.util

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import timber.log.Timber
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

// Context 확장
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.launchCustomTab(link: String) {
    try {
        val uri = if (link.startsWith("http://") || link.startsWith("https://")) {
            link.toUri()
        } else {
            "https://$link".toUri()
        }
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(this, uri)
    } catch (e: Exception) {
        Timber.e(e, "커스텀 탭 실행 실패: $link")
    }
}

fun Context.createCameraImageUri(): Uri {
    val cacheDir = File(cacheDir, "camera_photos").apply {
        if (!exists()) mkdirs()
    }
    val imageFile = File(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
}

// String 확장
private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

fun String.isValidEmail(): Boolean = this.matches(EMAIL_REGEX)

/** DTO enum 파싱 */
inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =  // 모르는 값은 기본값
    this?.let { runCatching { enumValueOf<T>(it.uppercase()) }.getOrNull() } ?: default

fun String.stripPasswordWhitespace(): String = filterNot { it.isWhitespace() }

fun String.isValidPassword(): Boolean {  // 8~20자, 영문·숫자·특수문자
    return this.length in Constants.Auth.MIN_PASSWORD_LENGTH..Constants.Auth.MAX_PASSWORD_LENGTH &&
            this.none { it.isWhitespace() } &&
            this.any { it.isDigit() } &&
            this.any { it.isLetter() } &&
            this.any { !it.isLetterOrDigit() }
}

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

fun Long.toRelativeTime(): String {  // 6일까지 상대, 1년 미만 MM/dd, 이후 yy/MM/dd
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < 60_000 -> "방금 전"
        diff < 3600_000 -> "${diff / 60_000}분 전"
        diff < 86400_000 -> "${diff / 3600_000}시간 전"
        diff < 604800_000 -> "${diff / 86400_000}일 전"
        diff < 31536000_000 -> this.toLocalDateTime().format(DateFormatters.MONTH_DAY)
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

// Number 확장
data class FormattedCount(val number: String, val suffix: String?)

fun Int.toFormattedParts(): FormattedCount {
    return when {
        this < 1000 -> FormattedCount(this.toString(), null)
        this < 10000 -> {
            val thousands = this / 1000f
            val number = String.format(Locale.KOREA, "%.1f", thousands)
            FormattedCount(number, "천")
        }
        else -> {
            val tenThousands = this / 10000f
            val number = String.format(Locale.KOREA, "%.1f", tenThousands)
            FormattedCount(number, "만")
        }
    }
}

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

// List 확장

/** 앞부분만 새로 받은 목록을 기존 목록에 합침 */
fun <T> List<T>.mergeFreshHead(freshHead: List<T>, id: (T) -> Any): List<T> {  // 새 앞부분 뒤에 기존 목록의 그 이후 항목을 이어 붙임
    // 경계는 새 앞부분 끝에서 찾은 첫 공통 항목
    // 시간순 목록 전용, 순위가 바뀌는 목록엔 안 씀
    val positions = withIndex().associate { (index, item) -> id(item) to index }
    val boundary = freshHead.asReversed().firstNotNullOfOrNull { positions[id(it)] } ?: -1
    val tail = if (boundary >= 0) drop(boundary + 1) else emptyList()
    return (freshHead + tail).distinctBy(id)
}
