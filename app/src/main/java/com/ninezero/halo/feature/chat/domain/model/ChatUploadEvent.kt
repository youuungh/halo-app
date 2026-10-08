package com.ninezero.halo.feature.chat.domain.model


sealed interface ChatUploadEvent {
    val pendingMessageId: Long
    val roomId: Int

    data class Failure(
        override val pendingMessageId: Long,
        override val roomId: Int,
        val errorMessage: String
    ) : ChatUploadEvent
}
