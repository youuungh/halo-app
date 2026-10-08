package com.ninezero.halo.feature.feed.domain.model

data class MediaItem(
    val id: Int,
    val type: MediaType,
    val url: String,
    val thumbnailUrl: String?,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Long? = null
)

enum class MediaType {
    IMAGE,
    VIDEO,
}
