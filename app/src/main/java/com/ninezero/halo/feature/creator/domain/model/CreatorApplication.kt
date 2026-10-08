package com.ninezero.halo.feature.creator.domain.model

data class CreatorApplication(
    val reason: String,
    val portfolioUrl: String?,
    val status: CreatorApplicationStatus,
    val rejectionReason: String?,
    val createdAt: Long,
    val reviewedAt: Long?
)

enum class CreatorApplicationStatus {
    PENDING, APPROVED, REJECTED, REVOKED;

    companion object {
        fun fromString(value: String): CreatorApplicationStatus {
            return entries.firstOrNull { it.name == value } ?: PENDING
        }
    }
}
