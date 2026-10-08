package com.ninezero.halo.feature.explore.domain.model

data class Creator(
    val id: Int,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val avatarThumbUrl: String? = null,
    val role: String,
    val isFollowing: Boolean
)
