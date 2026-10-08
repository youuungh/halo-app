package com.ninezero.halo.feature.feed.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LikeResponse(
    val isLiked: Boolean,

    val likeCount: Int,

    val message: String? = null
)
