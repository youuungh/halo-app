package com.ninezero.halo.core.util

/** 금액 문자열을 원 단위 Long으로, "12000.00"도 받음 */
fun String?.toAmountLong(): Long = toAmountLongOrNull() ?: 0L

fun String?.toAmountLongOrNull(): Long? = this?.trim()?.toBigDecimalOrNull()?.toLong()

/** 문자열을 enum으로 */
inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =  // 모르는 값은 기본값
    this?.let { runCatching { enumValueOf<T>(it.uppercase()) }.getOrNull() } ?: default
