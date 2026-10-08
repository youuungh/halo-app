package com.ninezero.halo.feature.feed.domain.model

/** 서버가 가공해 둔 미디어 초안 */
data class MediaDraft(
    val id: Int,
    val status: MediaDraftStatus
)
