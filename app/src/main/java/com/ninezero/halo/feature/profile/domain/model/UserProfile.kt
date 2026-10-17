package com.ninezero.halo.feature.profile.domain.model

data class UserProfile(
    val userId: Int,
    val role: String,
    val displayName: String,
    val bio: String?,
    val avatarUrl: String?,
    val avatarThumbUrl: String? = null,
    val location: String?,
    val website: String?,
    val followerCount: Int,
    val followingCount: Int,
    val postCount: Int,
    val subscriberCount: Int?,
    val hasSubscriptionPlans: Boolean,
    val isCreator: Boolean,
    val isBlocked: Boolean,  // 양방향
    val isBlockedByMe: Boolean = false,  // 차단 해제 버튼 분기
    val isBlockedByOther: Boolean = false,  // 안내 문구 분기
    val createdAt: Long,
    val updatedAt: Long,

    // 앱 전용 필드
    val username: String = "",
    val isFollowing: Boolean = false,
    val isSubscribed: Boolean = false,
    val isNotifyEnabled: Boolean = false,
    val isOwnProfile: Boolean = false,
    val coverImageUrl: String? = null
)

data class UpdateProfileRequest(
    val displayName: String?,
    val bio: String?,
    val location: String?,
    val website: String?
)
