package com.ninezero.halo.feature.feed.domain.model

data class Post(
    val id: Int,
    val content: String,
    val author: Author,
    val mediaAttachments: List<MediaItem> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val viewCount: Int = 0,
    val shareCount: Int = 0,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val isPinned: Boolean = false,
    val isBlocked: Boolean = false,
    val requiredTier: String = "FREE",   // FREE, TIER1, TIER2
    val isSecret: Boolean = false,       // 비밀글, 크리에이터만 열람
    val canAccess: Boolean = true,
    val isBlinded: Boolean = false,      // 신고 누적으로 가려짐, 본인에게만
    val contextType: PostContextType = PostContextType.GENERAL,
    val createdAt: Long,
    val pendingId: Long? = null
) {
    val isUploading: Boolean get() = pendingId != null
    val isRestricted: Boolean get() = !canAccess  // 블러 처리 조건
}

fun List<Post>.nextVideoUrlsAfter(currentPostId: Int?, count: Int): List<String> {  // 프리로드용, id 없으면 처음부터
    val curIdx = indexOfFirst { it.id == currentPostId }
    val start = if (curIdx >= 0) curIdx + 1 else 0
    return asSequence().drop(start)
        .mapNotNull { post -> post.mediaAttachments.firstOrNull { it.type == MediaType.VIDEO }?.url }
        .take(count)
        .toList()
}

/** 다음 포스트 첫 이미지 url 목록 */
fun List<Post>.nextImageUrlsAfter(currentPostId: Int?, count: Int): List<String> {  // 프리페치용, id 없으면 처음부터
    val curIdx = indexOfFirst { it.id == currentPostId }
    val start = if (curIdx >= 0) curIdx + 1 else 0
    return asSequence().drop(start).take(count)
        .mapNotNull { post ->
            post.mediaAttachments.firstOrNull { it.type == MediaType.IMAGE }?.let { it.thumbnailUrl ?: it.url }
        }
        .toList()
}
