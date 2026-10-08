package com.ninezero.halo.core.network.dto

import com.ninezero.halo.core.model.UserSummary
import kotlinx.serialization.Serializable

@Serializable
data class UserSummaryDto(
    val id: Int,

    val username: String,

    val displayName: String? = null,

    val avatarUrl: String? = null,

    val avatarThumbUrl: String? = null,

    val role: String = "USER",

    val followerCount: Int = 0,

    val followingCount: Int = 0,

    val postCount: Int = 0,

    val isFollowing: Boolean = false,

    val isDeleted: Boolean = false
)

fun UserSummaryDto.toDomain() = UserSummary(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    avatarThumbUrl = avatarThumbUrl,
    role = role,
    followerCount = followerCount,
    followingCount = followingCount,
    postCount = postCount,
    isFollowing = isFollowing
)
