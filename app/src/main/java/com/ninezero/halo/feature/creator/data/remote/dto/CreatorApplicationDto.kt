package com.ninezero.halo.feature.creator.data.remote.dto

import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.creator.domain.model.CreatorApplication
import com.ninezero.halo.feature.creator.domain.model.CreatorApplicationStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreatorApplicationRequest(
    val reason: String,
    val portfolioUrl: String? = null
)

@Serializable
data class CreatorApplicationResponse(
    val id: Int,
    val userId: Int,
    val username: String,
    val reason: String,
    val portfolioUrl: String? = null,
    val status: String,
    val rejectionReason: String? = null,
    val reviewedBy: Int? = null,
    val createdAt: String,
    val reviewedAt: String? = null
)

fun CreatorApplicationResponse.toDomain(): CreatorApplication {
    return CreatorApplication(
        reason = reason,
        portfolioUrl = portfolioUrl,
        status = CreatorApplicationStatus.fromString(status),
        rejectionReason = rejectionReason,
        createdAt = createdAt.parseTimestamp(),
        reviewedAt = reviewedAt?.parseTimestamp()?.takeIf { it > 0L }
    )
}
