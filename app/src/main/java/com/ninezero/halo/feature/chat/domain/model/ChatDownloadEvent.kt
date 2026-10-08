package com.ninezero.halo.feature.chat.domain.model

sealed interface ChatDownloadEvent {
    val fileUrl: String

    data class Progress(
        override val fileUrl: String,
        val progress: Float
    ) : ChatDownloadEvent

    data class Success(
        override val fileUrl: String,
        val fileName: String
    ) : ChatDownloadEvent

    data class Failure(
        override val fileUrl: String
    ) : ChatDownloadEvent
}
