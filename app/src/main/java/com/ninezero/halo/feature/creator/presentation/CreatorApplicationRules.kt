package com.ninezero.halo.feature.creator.presentation

import com.ninezero.halo.feature.creator.domain.model.CreatorApplication
import com.ninezero.halo.feature.creator.domain.model.CreatorApplicationStatus

const val CREATOR_APPLICATION_REASON_MIN_LENGTH = 10
const val CREATOR_APPLICATION_REASON_MAX_LENGTH = 300
const val CREATOR_REAPPLY_COOLDOWN_DAYS = 7

fun String.isValidApplicationReason(): Boolean {
    val length = trim().length
    return length in CREATOR_APPLICATION_REASON_MIN_LENGTH..CREATOR_APPLICATION_REASON_MAX_LENGTH
}

fun String.isValidPortfolioUrl(): Boolean {
    if (isBlank()) return true

    return startsWith("http://", ignoreCase = true) ||
            startsWith("https://", ignoreCase = true) ||
            startsWith("//")
}

fun CreatorApplication.reapplyDaysLeft(nowMillis: Long = System.currentTimeMillis()): Int? {
    if (status !in listOf(CreatorApplicationStatus.REJECTED, CreatorApplicationStatus.REVOKED)) {
        return null
    }

    val reviewedAtMillis = reviewedAt?.takeIf { it > 0L } ?: return null
    val elapsedDays = ((nowMillis - reviewedAtMillis) / (24L * 60L * 60L * 1000L)).toInt()
    val remainingDays = CREATOR_REAPPLY_COOLDOWN_DAYS - elapsedDays

    return remainingDays.takeIf { it > 0 }
}
