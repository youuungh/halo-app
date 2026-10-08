package com.ninezero.halo.feature.profile.data.remote.dto

import com.ninezero.halo.core.network.CursorDto
import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.feed.data.remote.dto.MediaItemDto
import com.ninezero.halo.feature.feed.data.remote.dto.toDomain
import com.ninezero.halo.feature.profile.domain.model.CommentThreadData
import com.ninezero.halo.feature.profile.domain.model.PostSummaryData
import com.ninezero.halo.feature.profile.domain.model.ThreadCommentData
import com.ninezero.halo.feature.profile.domain.model.UserSummaryData
import kotlinx.serialization.Serializable

typealias UserCommentThreadsResponseDto = CursorDto<CommentThreadDto>

@Serializable
data class CommentThreadDto(
    val comments: List<ThreadCommentDto>,

    val post: PostSummaryForCommentDto
)

@Serializable
data class ThreadCommentDto(
    val id: Int,

    val content: String,

    val mediaItems: List<MediaItemDto> = emptyList(),

    val author: UserSummaryDto,

    val replyTo: UserSummaryDto? = null,

    val createdAt: String,

    val likeCount: Int,

    val replyCount: Int,

    val isLiked: Boolean,

    val isBookmarked: Boolean = false,

    val isProfileUser: Boolean,

    val isBlocked: Boolean = false,

    val isDeleted: Boolean = false,

    val isBlinded: Boolean = false
)

@Serializable
data class PostSummaryForCommentDto(
    val id: Int,

    val contextType: String,

    val excerpt: String,

    val mediaItems: List<MediaItemDto> = emptyList(),

    val author: UserSummaryDto,

    val creator: UserSummaryDto? = null,

    val likeCount: Int = 0,

    val commentCount: Int = 0,

    val isLiked: Boolean = false,

    val isBookmarked: Boolean = false,

    val isDeleted: Boolean = false,

    val isHidden: Boolean = false,

    val requiredTier: String = "FREE",

    val isSecret: Boolean = false,

    val canAccess: Boolean = true,

    val createdAt: String
)

fun UserSummaryDto.toUserSummaryData(): UserSummaryData {
    return UserSummaryData(
        id = id,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        avatarThumbUrl = avatarThumbUrl,
        role = role,
        isFollowing = isFollowing,
        isDeleted = isDeleted
    )
}

fun ThreadCommentDto.toThreadCommentData(): ThreadCommentData {
    val mappedMediaAttachments = mediaItems.sortedBy { it.sortOrder }.map { it.toDomain() }

    return ThreadCommentData(
        id = id,
        content = content,
        mediaAttachments = mappedMediaAttachments,
        author = author.toUserSummaryData(),
        replyTo = replyTo?.toUserSummaryData(),
        createdAt = createdAt.parseTimestamp(),
        likeCount = likeCount,
        replyCount = replyCount,
        isLiked = isLiked,
        isBookmarked = isBookmarked,
        isProfileUser = isProfileUser,
        isBlocked = isBlocked,
        isDeleted = isDeleted,
        isBlinded = isBlinded
    )
}

fun PostSummaryForCommentDto.toPostSummaryData(): PostSummaryData {
    val mappedMediaAttachments = mediaItems.sortedBy { it.sortOrder }.map { it.toDomain() }

    return PostSummaryData(
        id = id,
        excerpt = excerpt,
        mediaAttachments = mappedMediaAttachments,
        author = author.toUserSummaryData(),
        createdAt = createdAt.parseTimestamp(),
        likeCount = likeCount,
        commentCount = commentCount,
        isLiked = isLiked,
        isBookmarked = isBookmarked,
        isDeleted = isDeleted,
        isHidden = isHidden,
        requiredTier = requiredTier,
        isSecret = isSecret,
        canAccess = canAccess
    )
}

fun CommentThreadDto.toCommentThreadData(): CommentThreadData {
    return CommentThreadData(
        comments = comments.map { it.toThreadCommentData() },
        post = post.toPostSummaryData()
    )
}
