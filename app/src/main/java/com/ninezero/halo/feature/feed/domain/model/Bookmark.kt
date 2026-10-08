package com.ninezero.halo.feature.feed.domain.model

enum class BookmarkTargetType {
    POST,
    COMMENT
}

data class BookmarkItem(
    val targetType: BookmarkTargetType,
    val targetId: Int,
    val post: Post? = null,
    val comment: Comment? = null
)
