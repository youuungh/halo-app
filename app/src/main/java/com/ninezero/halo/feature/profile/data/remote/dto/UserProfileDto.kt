package com.ninezero.halo.feature.profile.data.remote.dto

import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.profile.domain.model.UpdateProfileRequest
import com.ninezero.halo.feature.profile.domain.model.UserProfile
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val id: Int,

    val userId: Int,

    val username: String,

    val role: String = "USER",

    val displayName: String,

    val bio: String? = null,

    val avatarUrl: String? = null,

    val avatarThumbUrl: String? = null,

    val location: String? = null,

    val website: String? = null,

    val followerCount: Int = 0,

    val followingCount: Int = 0,

    val postCount: Int = 0,

    val subscriberCount: Int? = null,

    val hasSubscriptionPlans: Boolean = false,

    val isCreator: Boolean = false,

    val isBlocked: Boolean = false,

    val isBlockedByMe: Boolean = false,

    val isBlockedByOther: Boolean = false,

    val isFollowing: Boolean = false,

    val isSubscribed: Boolean = false,

    val notifyNewPost: Boolean = false,

    val notifyNewProduct: Boolean = false,

    val createdAt: String,

    val updatedAt: String
)

fun UserProfileDto.toDomain(): UserProfile {
    return UserProfile(
        userId = userId,
        username = username,
        role = role,
        displayName = displayName,
        bio = bio,
        avatarUrl = avatarUrl,
        avatarThumbUrl = avatarThumbUrl,
        location = location,
        website = website,
        followerCount = followerCount,
        followingCount = followingCount,
        postCount = postCount,
        subscriberCount = subscriberCount,
        hasSubscriptionPlans = hasSubscriptionPlans,
        isCreator = isCreator,
        isBlocked = isBlocked,
        isBlockedByMe = isBlockedByMe,
        isBlockedByOther = isBlockedByOther,
        isFollowing = isFollowing,
        isSubscribed = isSubscribed,
        isNotifyEnabled = notifyNewPost || notifyNewProduct,
        createdAt = createdAt.parseTimestamp(),
        updatedAt = updatedAt.parseTimestamp()
    )
}

@Serializable
data class UpdateProfileRequestDto(
    val displayName: String? = null,

    val bio: String? = null,

    val location: String? = null,

    val website: String? = null
)

fun UpdateProfileRequest.toDto(): UpdateProfileRequestDto {
    return UpdateProfileRequestDto(
        displayName = displayName,
        bio = bio,
        location = location,
        website = website
    )
}
