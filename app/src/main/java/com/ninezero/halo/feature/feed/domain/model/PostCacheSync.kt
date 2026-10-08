package com.ninezero.halo.feature.feed.domain.model

/** 캐시 포스트로 교체 */
fun List<Post>.syncWithCache(cached: Map<Int, Post>, previousCachedIds: Set<Int>): List<Post> =
    mapNotNull { post -> cached[post.id] ?: if (post.id in previousCachedIds) null else post }

fun Post.syncWithCache(cached: Map<Int, Post>, previousCachedIds: Set<Int>): Post? =
    cached[id] ?: if (id in previousCachedIds) null else this

class CachedPostsTracker {
    private var knownCachedPostIds: Set<Int> = emptySet()

    fun track(cachedPosts: List<Post>): CachedPostsSnapshot {
        val cached = cachedPosts.associateBy { it.id }
        val previousCachedIds = knownCachedPostIds
        knownCachedPostIds = cached.keys
        return CachedPostsSnapshot(cached, previousCachedIds)
    }
}

data class CachedPostsSnapshot(
    val cached: Map<Int, Post>,
    val previousCachedIds: Set<Int>
)

class CachedCommentsTracker {
    private var knownCachedCommentIds: Set<Int> = emptySet()

    fun track(currentCachedIds: Set<Int>): Set<Int> {
        val previousCachedIds = knownCachedCommentIds
        knownCachedCommentIds = currentCachedIds
        return previousCachedIds
    }
}
