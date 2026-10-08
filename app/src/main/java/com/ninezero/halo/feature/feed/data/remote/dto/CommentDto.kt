package com.ninezero.halo.feature.feed.data.remote.dto

import com.ninezero.halo.core.network.PagedDto
import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.UserPreview
import kotlinx.serialization.Serializable

@Serializable
data class CommentRequest(
    val postId: Int,

    val content: String,

    val parentCommentId: Int? = null,

    val mediaDraftIds: List<Int> = emptyList(),

    val clientRequestId: String? = null
)

@Serializable
data class UpdateCommentRequest(
    val content: String
)

@Serializable
data class CommentDto(
    val id: Int,

    val postId: Int,

    val author: UserSummaryDto,

    val content: String,

    val mediaItems: List<MediaItemDto> = emptyList(),

    val parentCommentId: Int? = null,

    val likeCount: Int = 0,

    val replyCount: Int = 0,

    val isLiked: Boolean = false,

    val isBookmarked: Boolean = false,

    val isBlocked: Boolean = false,
    val isBlinded: Boolean = false,
    val isDeleted: Boolean = false,

    val firstReply: CommentDto? = null,

    val hasMoreReplies: Boolean = false,

    val moreReplyCount: Int = 0,

    val replyPreviewUsers: List<UserPreviewDto> = emptyList(),

    val createdAt: String,

    val updatedAt: String
)

/** 답글 미리보기 유저 */
@Serializable
data class UserPreviewDto(
    val id: Int,

    val avatarUrl: String? = null,

    val avatarThumbUrl: String? = null
)

fun CommentDto.toDomain(): Comment {
    val mappedMediaAttachments = mediaItems.sortedBy { it.sortOrder }.map { it.toDomain() }

    return Comment(
        id = id,
        postId = postId,
        author = author.toAuthor(),
        content = content,
        mediaAttachments = mappedMediaAttachments,
        parentCommentId = parentCommentId,
        replyCount = replyCount,
        likeCount = likeCount,
        isLiked = isLiked,
        isBookmarked = isBookmarked,
        isBlocked = isBlocked,
        isBlinded = isBlinded,
        isDeleted = isDeleted,
        firstReply = firstReply?.toDomain(),
        replyPreviewUsers = replyPreviewUsers.map { it.toDomain() },
        createdAt = createdAt.parseTimestamp(),
    )
}

fun UserPreviewDto.toDomain() = UserPreview(
    id = id,
    avatarUrl = avatarUrl,
    avatarThumbUrl = avatarThumbUrl
)

typealias CommentListResponse = PagedDto<CommentDto>
