package com.ninezero.halo.feature.feed.domain.model

data class Author(
    val id: Int,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val avatarThumbUrl: String? = null,
    val role: String = "USER",
    val isFollowing: Boolean = false,
    val isDeleted: Boolean = false  // 탈퇴한 사용자, 남은 글·댓글의 작성자
)
