package com.ninezero.halo.feature.tag.data.remote.dto

import com.ninezero.halo.feature.tag.domain.model.Tag
import kotlinx.serialization.Serializable

@Serializable
data class TagDto(
    val id: Int,

    val name: String,

    val targetType: String,

    val isSectionEnabled: Boolean = false,

    val postCount: Int? = null,

    val productCount: Int? = null,

    val createdAt: String? = null
)

@Serializable
data class TagRequest(
    val name: String,

    val targetType: String,

    val isSectionEnabled: Boolean = false
)

@Serializable
data class UpdateTagRequest(
    val name: String? = null,

    val isSectionEnabled: Boolean? = null
)

fun TagDto.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        isSectionEnabled = isSectionEnabled,
        itemCount = postCount ?: productCount ?: 0
    )
}

@Serializable
data class TagListResponseDto(
    val tags: List<TagDto>
)
