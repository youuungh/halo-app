package com.ninezero.halo.feature.chat.domain.model

enum class ChatMessageType {
    TEXT, IMAGE, VIDEO, FILE, POST_LINK, PRODUCT_LINK;

    companion object {
        fun from(value: String): ChatMessageType =
            entries.firstOrNull { it.name == value } ?: TEXT
    }
}
