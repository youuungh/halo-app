package com.ninezero.halo.feature.profile.data.remote.dto

import com.ninezero.halo.core.network.CursorDto
import com.ninezero.halo.feature.feed.data.remote.dto.PostDto
import kotlinx.serialization.Serializable

@Serializable
data class CommunityResponseDto(
    val pinnedPosts: List<PostDto> = emptyList(),
    val posts: CursorDto<PostDto>
)

@Serializable
data class CommunityPostRequest(
    val content: String,
    val isSecret: Boolean = false,
    val mediaDraftIds: List<Int> = emptyList(),
    val clientRequestId: String? = null
)
