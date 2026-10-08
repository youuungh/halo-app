package com.ninezero.halo.feature.feed.data.remote.dto

import com.ninezero.halo.core.network.CursorDto
import com.ninezero.halo.core.network.PagedDto
import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.feed.domain.model.BookmarkItem
import com.ninezero.halo.feature.feed.domain.model.BookmarkTargetType
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.NewMediaItem
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class MediaItemDto(
    val id: Int = 0,

    val type: String = "IMAGE",

    val url: String,

    val width: Int? = null,

    val height: Int? = null,

    val durationMs: Long? = null,

    val thumbnailUrl: String? = null,

    val previewUrl: String? = null,

    val sortOrder: Int = 0
)

/** 새 미디어 순서 항목 */
@Serializable
data class MediaItemRefDto(  // 파일 파트 번호와 직접 업로드 ID 중 하나
    val fileIndex: Int? = null,
    val uploadId: Int? = null
)

@Serializable
data class VideoUploadPrepareRequest(
    val mimeType: String,
    val size: Long
)

/** multipart items 파트 */
fun List<NewMediaItem>.toItemsPart(): RequestBody = Json.encodeToString(
    map { item ->
        when (item) {
            is NewMediaItem.FilePart -> MediaItemRefDto(fileIndex = item.index)
            is NewMediaItem.DirectUpload -> MediaItemRefDto(uploadId = item.uploadId)
        }
    }
).toRequestBody("text/plain".toMediaType())

@Serializable
data class ProductSummaryDto(
    val id: Int,

    val name: String,

    val price: String,

    val imageUrl: String? = null,

    val isAvailable: Boolean = true
)

@Serializable
data class PostDto(
    val id: Int,

    val author: UserSummaryDto,

    val content: String,

    val postType: String = "TEXT",  // TEXT, IMAGE, VIDEO, PRODUCT

    val contextType: String = "GENERAL",  // GENERAL, CREATOR_FEED, COMMUNITY

    val status: String = "ACTIVE",  // ACTIVE, HIDDEN

    val mediaItems: List<MediaItemDto> = emptyList(),

    val tags: List<String> = emptyList(),

    val likeCount: Int = 0,

    val commentCount: Int = 0,

    val viewCount: Int = 0,

    val shareCount: Int = 0,

    val isLiked: Boolean = false,

    val isBookmarked: Boolean = false,

    val isPinned: Boolean = false,

    val productInfo: ProductSummaryDto? = null,

    val requiredTier: String = "FREE",

    val isSecret: Boolean = false,

    val canAccess: Boolean = true,
    val isBlinded: Boolean = false,

    val createdAt: String,

    val updatedAt: String
)

typealias PostListResponse = PagedDto<PostDto>

@Serializable
data class BookmarkResponse(
    val isBookmarked: Boolean,
)

@Serializable
enum class BookmarkTargetTypeDto {
    POST,
    COMMENT
}

@Serializable
data class BookmarkItemDto(
    val targetType: BookmarkTargetTypeDto,

    val targetId: Int,

    val createdAt: String,

    val post: PostDto? = null,

    val comment: CommentDto? = null
)

typealias BookmarkListResponse = CursorDto<BookmarkItemDto>

@Serializable
data class BlockResponse(
    val isBlocked: Boolean
)

@Serializable
data class BlockRelationResponse(
    val isBlockedByMe: Boolean = false,
    val isBlockedByOther: Boolean = false,
    val isBlocked: Boolean = false
)

typealias BlockedUserListResponse = CursorDto<UserSummaryDto>

@Serializable
data class HiddenPostResponse(
    val isHidden: Boolean
)

fun MediaItemDto.toDomain(): MediaItem {
    return MediaItem(
        id = id,
        type = when (type.uppercase()) {
            "IMAGE" -> MediaType.IMAGE
            "VIDEO" -> MediaType.VIDEO
            else -> MediaType.IMAGE
        },
        url = url,
        thumbnailUrl = thumbnailUrl,
        width = width,
        height = height,
        durationMs = durationMs
    )
}

fun PostDto.toDomain(): Post {
    val mappedMediaItems = mediaItems.sortedBy { it.sortOrder }.map { it.toDomain() }

    return Post(
        id = id,
        content = content,
        author = author.toAuthor(),
        mediaAttachments = mappedMediaItems,
        likeCount = likeCount,
        commentCount = commentCount,
        viewCount = viewCount,
        shareCount = shareCount,
        isLiked = isLiked,
        isBookmarked = isBookmarked,
        isPinned = isPinned,
        requiredTier = requiredTier,
        isSecret = isSecret,
        canAccess = canAccess,
        isBlinded = isBlinded,
        contextType = when (contextType.uppercase()) {
            "CREATOR_FEED" -> PostContextType.CREATOR_FEED
            "COMMUNITY" -> PostContextType.COMMUNITY
            else -> PostContextType.GENERAL
        },
        createdAt = createdAt.parseTimestamp(),
    )
}

fun BookmarkItemDto.toDomain(): BookmarkItem {
    return BookmarkItem(
        targetType = when (targetType) {
            BookmarkTargetTypeDto.POST -> BookmarkTargetType.POST
            BookmarkTargetTypeDto.COMMENT -> BookmarkTargetType.COMMENT
        },
        targetId = targetId,
        post = post?.toDomain(),
        comment = comment?.toDomain()
    )
}

@Serializable
data class FeedResponse(
    val posts: List<PostDto>,

    val totalCount: Int = 0,

    val page: Int = 1,

    val totalPages: Int = 1,

    val hasNext: Boolean = false,

    val hasPrevious: Boolean = false,

    val feedType: String = "EXPLORE",  // EXPLORE, HASHTAG, TRENDING
)
