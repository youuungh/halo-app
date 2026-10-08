package com.ninezero.halo.feature.feed.data.remote.dto

import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.feature.feed.domain.model.Author

fun UserSummaryDto.toAuthor() = Author(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    avatarThumbUrl = avatarThumbUrl,
    role = role,
    isFollowing = isFollowing,
    isDeleted = isDeleted
)
