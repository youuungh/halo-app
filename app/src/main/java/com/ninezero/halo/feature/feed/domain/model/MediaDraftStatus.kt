package com.ninezero.halo.feature.feed.domain.model

/** 초안 가공 상태 */
enum class MediaDraftStatus {
    READY,
    PROCESSING,
    FAILED;

    companion object {
        fun from(value: String?): MediaDraftStatus =
            entries.firstOrNull { it.name == value } ?: FAILED  // 모르는 값은 기다리지 않음
    }
}
