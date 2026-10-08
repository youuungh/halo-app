package com.ninezero.halo.feature.profile.data.remote.dto

import com.ninezero.halo.feature.feed.data.remote.dto.PostDto
import com.ninezero.halo.feature.feed.data.remote.dto.PostListResponse
import com.ninezero.halo.feature.feed.data.remote.dto.toDomain
import com.ninezero.halo.feature.profile.domain.model.CreatorProfileSection
import com.ninezero.halo.feature.profile.domain.model.CreatorStoreSection
import com.ninezero.halo.feature.profile.domain.model.PostTagSection
import com.ninezero.halo.feature.profile.domain.model.ProductTagSection
import com.ninezero.halo.feature.profile.domain.model.SectionTag
import com.ninezero.halo.feature.store.data.remote.dto.CreatorStoreSectionsResponseDto
import com.ninezero.halo.feature.store.data.remote.dto.ProductSectionTagDto
import com.ninezero.halo.feature.store.data.remote.dto.toDomain
import kotlinx.serialization.Serializable

@Serializable
data class CreatorProfileSectionDto(
    val pinnedPosts: List<PostDto> = emptyList(),

    val tagSections: List<PostTagSectionDto> = emptyList(),

    val allPosts: PostListResponse
)

@Serializable
data class PostTagSectionDto(
    val tag: PostTagDto,

    val posts: List<PostDto>,

    val hasMore: Boolean
)

@Serializable
data class PostTagDto(
    val id: Int,

    val name: String,

    val targetType: String? = null,

    val isSectionEnabled: Boolean = false,

    val postCount: Int? = null,

    val productCount: Int? = null,

    val createdAt: String? = null
)

fun PostTagDto.toDomain(): SectionTag {
    return SectionTag(
        id = id,
        name = name
    )
}

fun ProductSectionTagDto.toSectionTag(): SectionTag {
    return SectionTag(
        id = id,
        name = name
    )
}

fun PostTagSectionDto.toDomain(): PostTagSection {
    return PostTagSection(
        tag = tag.toDomain(),
        posts = posts.map { it.toDomain() },
        hasMore = hasMore
    )
}

fun CreatorProfileSectionDto.toDomain(): CreatorProfileSection {
    return CreatorProfileSection(
        pinnedPosts = pinnedPosts.map { it.toDomain() },
        postSections = tagSections.map { it.toDomain() }
    )
}

fun CreatorStoreSectionsResponseDto.toCreatorStoreSection(): CreatorStoreSection {
    return CreatorStoreSection(
        tagSections = tagSections.map { section ->
            ProductTagSection(
                tag = section.tag.toSectionTag(),
                products = section.items.map { it.toDomain() },
                hasMore = section.hasMore
            )
        },
        allProducts = allProducts.items.map { it.toDomain() },
        hasNext = allProducts.hasNext
    )
}
