package com.ninezero.halo.feature.chat.domain.model

enum class MessageStatus {
    SENT,
    SENDING,
    FAILED;

    companion object {
        fun from(value: String?): MessageStatus =
            entries.firstOrNull { it.name == value } ?: SENT
    }
}
