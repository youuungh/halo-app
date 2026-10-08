package com.ninezero.halo.core.model

data class UserSummary(
    val id: Int,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val avatarThumbUrl: String? = null,
    val role: String = "USER",
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val isFollowing: Boolean = false
)
