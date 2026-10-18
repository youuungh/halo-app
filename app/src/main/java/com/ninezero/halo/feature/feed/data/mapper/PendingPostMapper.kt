package com.ninezero.halo.feature.feed.data.mapper

import com.ninezero.halo.core.database.entity.PendingPostEntity
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import kotlinx.serialization.json.Json

fun PendingPostEntity.toPost(): Post {
    val mediaUriList: List<String> = mediaUris?.let {
        try {
            Json.decodeFromString<List<String>>(it)
        } catch (e: Exception) {
            emptyList()
        }
    } ?: emptyList()

    // 원본 크기 파싱
    val dimensionList: List<List<Int?>> = mediaDimensions?.let {
        try {
            Json.decodeFromString<List<List<Int?>>>(it)
        } catch (e: Exception) {
            emptyList()
        }
    } ?: emptyList()

    val mediaAttachments = mediaUriList.mapIndexed { index, uri ->
        MediaItem(
            id = -index - 1,
            type = if (isVideoMediaUrl(uri)) MediaType.VIDEO else MediaType.IMAGE,
            url = uri,
            thumbnailUrl = null,
            width = dimensionList.getOrNull(index)?.getOrNull(0),
            height = dimensionList.getOrNull(index)?.getOrNull(1)
        )
    }

    return Post(
        id = -id.toInt(),  // 음수 ID로 서버 포스트와 구분
        content = content,
        author = Author(
            id = authorId,
            username = authorUsername,
            displayName = authorDisplayName,
            avatarUrl = authorAvatarUrl
        ),
        mediaAttachments = mediaAttachments,
        likeCount = 0,
        commentCount = 0,
        viewCount = 0,
        isLiked = false,
        isBookmarked = false,
        isSecret = isSecret,
        createdAt = createdAt,
        contextType = if (creatorId != null) PostContextType.COMMUNITY else PostContextType.GENERAL,
        pendingId = id
    )
}

internal fun isVideoMediaUrl(url: String): Boolean {
    val lower = url.lowercase()
    return lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".avi") ||
            lower.endsWith(".webm") || lower.contains("video")
}
