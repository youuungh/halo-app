package com.ninezero.halo.feature.profile.data.remote.dto

import com.ninezero.halo.core.network.PagedDto
import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.feature.profile.domain.model.FollowedCreator
import kotlinx.serialization.Serializable

@Serializable
data class FollowRequestDto(
    val targetUserId: Int
)

@Serializable
data class FollowResponseDto(
    val followerCount: Int,

    val followingCount: Int,

    val userId: Int? = null
)

/** 커서 목록 + 탭 숫자용 전체 수 */
@Serializable
data class FollowListResponseDto(
    val items: List<UserSummaryDto>,
    val nextCursor: String? = null,
    val hasNext: Boolean = false,
    val totalCount: Int = 0
)

@Serializable
data class FollowedCreatorDto(
    val id: Int,

    val username: String,

    val displayName: String,

    val avatarUrl: String? = null,

    val avatarThumbUrl: String? = null,

    val notifyNewPost: Boolean = false,

    val notifyNewProduct: Boolean = false
)

typealias FollowedCreatorsResponseDto = PagedDto<FollowedCreatorDto>

fun FollowedCreatorDto.toDomain(): FollowedCreator {
    return FollowedCreator(
        id = id,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        avatarThumbUrl = avatarThumbUrl,
        isNotifyEnabled = notifyNewPost || notifyNewProduct
    )
}

@Serializable
data class NotificationSettingResponseDto(
    val notifyNewPost: Boolean? = null,

    val notifyNewProduct: Boolean? = null
)

