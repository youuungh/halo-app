package com.ninezero.halo.feature.profile.domain.model

import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.syncWithCache
import com.ninezero.halo.feature.store.domain.model.Product

data class SectionTag(
    val id: Int,
    val name: String
)

data class PostTagSection(
    val tag: SectionTag,
    val posts: List<Post>,
    val hasMore: Boolean
)

fun List<PostTagSection>.syncWithCache(
    cached: Map<Int, Post>,
    previousCachedIds: Set<Int>
): List<PostTagSection> = mapNotNull { section ->
    val synced = section.posts.syncWithCache(cached, previousCachedIds)
    when {
        synced.isEmpty() -> null
        synced == section.posts -> section
        else -> section.copy(posts = synced)
    }
}

data class ProductTagSection(
    val tag: SectionTag,
    val products: List<Product>,
    val hasMore: Boolean
)

/** 스토어 탭 섹션 */
data class CreatorStoreSection(
    val tagSections: List<ProductTagSection> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    val hasNext: Boolean = false
)

/** 홈 탭 섹션 */
data class CreatorProfileSection(
    val pinnedPosts: List<Post> = emptyList(),
    val postSections: List<PostTagSection> = emptyList(),
)

enum class SectionType {
    PINNED_POST,
    POST_TAG,
    PRODUCT_TAG
}
