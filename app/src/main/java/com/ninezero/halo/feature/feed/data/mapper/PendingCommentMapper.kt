package com.ninezero.halo.feature.feed.data.mapper

import com.ninezero.halo.core.database.entity.PendingCommentEntity
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import kotlinx.serialization.json.Json

fun PendingCommentEntity.toComment(): Comment {
    // 로컬 URI도 AsyncImage에서 표시 가능
    val parsedMediaAttachments = mediaUris?.let {
        try {
            Json.decodeFromString<List<String>>(it)
        } catch (e: Exception) {
            emptyList()
        }
    } ?: emptyList()

    // 원본 크기 파싱
    val parsedDimensions = mediaDimensions?.let {
        try {
            Json.decodeFromString<List<String?>>(it)
        } catch (e: Exception) {
            null
        }
    }

    val mappedMediaAttachments = parsedMediaAttachments.mapIndexed { index, url ->
        val dims = parsedDimensions?.getOrNull(index)?.split("x")
        MediaItem(
            id = -index - 1,
            type = if (isVideoUrl(url)) MediaType.VIDEO else MediaType.IMAGE,
            url = url,
            thumbnailUrl = null,
            width = dims?.getOrNull(0)?.toIntOrNull(),
            height = dims?.getOrNull(1)?.toIntOrNull()
        )
    }

    return Comment(
        id = -id.toInt(),  // 음수 ID로 서버 댓글과 구분
        postId = postId,
        author = Author(
            id = authorId,
            username = authorUsername,
            displayName = authorDisplayName,
            avatarUrl = authorAvatarUrl
        ),
        content = content,
        mediaAttachments = mappedMediaAttachments,
        parentCommentId = parentCommentId,
        replyCount = 0,
        likeCount = 0,
        isLiked = false,
        isBlocked = false,
        createdAt = createdAt,
        pendingId = id
    )
}

private fun isVideoUrl(url: String): Boolean {
    val lower = url.lowercase()
    return lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".avi") ||
            lower.endsWith(".webm") || lower.contains("video")
}
