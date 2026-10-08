package com.ninezero.halo.feature.profile.domain.model

data class FollowedCreator(
    val id: Int,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val avatarThumbUrl: String?,
    val isNotifyEnabled: Boolean
)

data class FollowedCreatorsResult(
    val items: List<FollowedCreator>,
    val hasNext: Boolean
)
