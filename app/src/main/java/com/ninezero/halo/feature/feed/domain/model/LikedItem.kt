package com.ninezero.halo.feature.feed.domain.model

enum class LikeTargetType {
    POST,
    COMMENT
}

data class LikedItem(
    val targetType: LikeTargetType,
    val targetId: Int,
    val post: Post? = null,
    val comment: Comment? = null
)

data class LikeResult(
    val isLiked: Boolean,
    val likeCount: Int
)
