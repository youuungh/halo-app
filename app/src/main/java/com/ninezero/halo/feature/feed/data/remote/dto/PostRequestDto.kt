package com.ninezero.halo.feature.feed.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostRequest(
    val content: String,

    val postType: String = "TEXT",

    val productId: Int? = null,

    val requiredTier: String = "FREE",

    val tagIds: List<Int>? = null,

    val sectionTagId: Int? = null,

    val mediaDraftIds: List<Int> = emptyList(),

    val clientRequestId: String? = null
)

@Serializable
data class UpdatePostRequest(
    val content: String,

    val tagIds: List<Int>? = null,

    val sectionTagId: Int? = null
)

@Serializable
data class PinPostResponse(
    val isPinned: Boolean
)
