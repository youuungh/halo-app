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

/** 좋아요 토글 결과 */
data class LikeResult(
    val isLiked: Boolean,
    val likeCount: Int
)
