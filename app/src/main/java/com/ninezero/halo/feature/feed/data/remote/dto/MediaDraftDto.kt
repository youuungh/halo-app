package com.ninezero.halo.feature.feed.data.remote.dto

import com.ninezero.halo.feature.feed.domain.model.MediaDraft
import com.ninezero.halo.feature.feed.domain.model.MediaDraftStatus
import kotlinx.serialization.Serializable

@Serializable
data class MediaDraftDto(
    val id: Int,

    val type: String = "IMAGE",

    val url: String,

    val width: Int? = null,

    val height: Int? = null,

    val durationMs: Long? = null,

    val thumbnailUrl: String? = null,

    val previewUrl: String? = null,

    val status: String,  // READY·PROCESSING·FAILED

    val expiresAt: String
)

@Serializable
data class VideoDraftRequest(
    val uploadId: Int
)

fun MediaDraftDto.toDomain(): MediaDraft = MediaDraft(
    id = id,
    status = MediaDraftStatus.from(status)
)
