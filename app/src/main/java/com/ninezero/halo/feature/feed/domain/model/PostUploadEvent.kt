package com.ninezero.halo.feature.feed.domain.model

sealed interface PostUploadEvent {
    data class Success(
        val creatorId: Int? = null
    ) : PostUploadEvent

    data class Failure(
        val creatorId: Int? = null
    ) : PostUploadEvent
}
