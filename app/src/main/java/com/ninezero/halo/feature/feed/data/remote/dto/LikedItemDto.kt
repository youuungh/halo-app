package com.ninezero.halo.feature.feed.data.remote.dto

import com.ninezero.halo.core.network.CursorDto
import com.ninezero.halo.feature.feed.domain.model.LikeTargetType
import com.ninezero.halo.feature.feed.domain.model.LikedItem
import kotlinx.serialization.Serializable

@Serializable
enum class LikeTargetTypeDto {
    POST,
    COMMENT
}

@Serializable
data class LikedItemDto(
    val targetType: LikeTargetTypeDto,
    val targetId: Int,
    val createdAt: String,
    val post: PostDto? = null,
    val comment: CommentDto? = null
)

typealias LikedListResponse = CursorDto<LikedItemDto>

fun LikedItemDto.toDomain(): LikedItem {
    return LikedItem(
        targetType = when (targetType) {
            LikeTargetTypeDto.POST -> LikeTargetType.POST
            LikeTargetTypeDto.COMMENT -> LikeTargetType.COMMENT
        },
        targetId = targetId,
        post = post?.toDomain(),
        comment = comment?.toDomain()
    )
}
