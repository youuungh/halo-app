package com.ninezero.halo.feature.profile.domain.model

import com.ninezero.halo.core.model.UserSummary

data class FollowListResult(
    val items: List<UserSummary>,
    val totalCount: Int,
    val nextCursor: String?
)
