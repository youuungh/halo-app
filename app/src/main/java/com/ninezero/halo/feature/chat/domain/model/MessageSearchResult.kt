package com.ninezero.halo.feature.chat.domain.model

data class MessageSearchResult(
    val items: List<Message>,
    val hasNext: Boolean,
    val totalCount: Int
)
